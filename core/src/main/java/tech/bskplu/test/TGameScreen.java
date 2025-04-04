package tech.bskplu.test;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import tech.bskplu.test.ai.GameTiledGraph;
import tech.bskplu.test.characters.*;
import tech.bskplu.test.manager.*;


/**
 * @ClassName: TGameScreen
 * @Description: 主游戏场景，负责整体流程管理
 * @Author BsKPLu
 * @Date 2025/4/3
 * @Version 1.1
 */
public class TGameScreen implements Screen {
    private final Game game;
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private WorldManager worldManager;
    private Box2DDebugRenderer debugRenderer;
    public static final float PIXELS_PER_METER = 32f;
    private float timeStep = 1 / 60f;
    private int velocityIterations = 6;
    private int positionIterations = 2;
    private static final float GAME_WIDTH_PIXELS = 800f;
    private static final float GAME_HEIGHT_PIXELS = 600f;

    private Player player;
    private Enemy enemy;
    private AIManager aiManager;
    private UIManager uiManager;

    private Texture groundTexture;
    private Music music;

    /**
     * 构造函数：初始化游戏场景
     * @param game 游戏主类
     */
    public TGameScreen(Game game) {
        this.game = game;
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, GAME_WIDTH_PIXELS, GAME_HEIGHT_PIXELS);

        worldManager = new WorldManager();
        debugRenderer = new Box2DDebugRenderer();

        groundTexture = new Texture(Gdx.files.internal("ground.png"));

        player = new Player(worldManager.getWorld());
        aiManager = new AIManager(new GameTiledGraph(GAME_WIDTH_PIXELS / PIXELS_PER_METER, GAME_HEIGHT_PIXELS / PIXELS_PER_METER, 0.5f, worldManager.getGroundBodies()));
        enemy = new Enemy(worldManager.getWorld(), aiManager.getTiledGraph(), aiManager.getPathFinder());

        music = Gdx.audio.newMusic(Gdx.files.internal("LanTingXu.mp3"));

        uiManager = new UIManager();
    }

    @Override
    public void show() {
        music.setVolume(0.1f);
        music.setLooping(true);
        music.play();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        player.handleInput();
        enemy.updateEnemyAI(delta, player.getPlayerBody());

        worldManager.getWorld().step(timeStep, velocityIterations, positionIterations);
        player.setPlayerStateTime(player.getPlayerStateTime() + delta);
        enemy.setEnemyStateTime(enemy.getEnemyStateTime() + delta);

        camera.position.set(player.getPlayerBody().getPosition().x * PIXELS_PER_METER, player.getPlayerBody().getPosition().y * PIXELS_PER_METER, 0);
        camera.update();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // 绘制地面
        for (Body groundBody : worldManager.getGroundBodies()) {
            float x = groundBody.getPosition().x * PIXELS_PER_METER;
            float y = groundBody.getPosition().y * PIXELS_PER_METER;
            PolygonShape shape = (PolygonShape) groundBody.getFixtureList().get(0).getShape();
            Vector2 vertex0 = new Vector2();
            Vector2 vertex1 = new Vector2();
            shape.getVertex(0, vertex0);
            shape.getVertex(1, vertex1);
            float width = vertex0.x * 2 * PIXELS_PER_METER;
            float height = vertex1.y * 2 * PIXELS_PER_METER;
            batch.draw(groundTexture, x - width / 2, y - height / 2, width, height);
        }

        // 绘制玩家
        TextureRegion playerFrame = player.getCurrentFrame(player.getPlayerStateTime());
        if (playerFrame != null) {
            float playerX = player.getPlayerBody().getPosition().x * PIXELS_PER_METER - playerFrame.getRegionWidth() / 2f;
            float playerY = player.getPlayerBody().getPosition().y * PIXELS_PER_METER - playerFrame.getRegionHeight() / 2f;
            batch.draw(playerFrame, playerX, playerY);
        }

        // 绘制敌人
        TextureRegion enemyFrame = enemy.getEnemyCurrentFrame(enemy.getEnemyStateTime());
        if (enemyFrame != null && enemy.getEnemyBody() != null) {
            float enemyX = enemy.getEnemyBody().getPosition().x * PIXELS_PER_METER - enemyFrame.getRegionWidth() / 2f;
            float enemyY = enemy.getEnemyBody().getPosition().y * PIXELS_PER_METER - enemyFrame.getRegionHeight() / 2f;
            batch.draw(enemyFrame, enemyX, enemyY);
        }

        batch.end();

        // 绘制 UI
        uiManager.drawHealthBar(batch,
            camera,
            player.getPlayerBody().getPosition(),
            100f,
            100f,
            playerFrame.getRegionWidth(),
            playerFrame.getRegionHeight(),
            (playerFrame.getRegionWidth() - 40) / 2f,
            playerFrame.getRegionHeight() + 5);

        uiManager.drawHealthBar(batch,
            camera,
            enemy.getEnemyBody().getPosition(),
            100f,
            100f,
            enemyFrame.getRegionWidth(),
            enemyFrame.getRegionHeight(),
            (enemyFrame.getRegionWidth() - 40) / 2f,
            enemyFrame.getRegionHeight() + 5);

        debugRenderer.render(worldManager.getWorld(), camera.combined.cpy().scale(PIXELS_PER_METER, PIXELS_PER_METER, 0));

        if (enemy.isPlayerCaught()) {
            uiManager.drawMessage(batch, "Caught!", camera.position.x - 50, camera.position.y);
            enemy.setGameOverTimer(enemy.getGameOverTimer() - delta);
            if (enemy.getGameOverTimer() <= 0f) {
                game.setScreen(new BattleScreen(game, player, enemy));
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new StartScreen(game));
        }
    }

    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {
        music.stop();
    }

    @Override
    public void dispose() {
        batch.dispose();
        groundTexture.dispose();
        worldManager.getWorld().dispose();
        debugRenderer.dispose();
        music.dispose();
        uiManager.dispose();
    }
}
