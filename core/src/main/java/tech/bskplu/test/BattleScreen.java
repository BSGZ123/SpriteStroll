package tech.bskplu.test;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import tech.bskplu.test.characters.Enemy;
import tech.bskplu.test.characters.Player;
import tech.bskplu.test.manager.UIManager;
import tech.bskplu.test.manager.WorldManager;

/**
 * @ClassName: BattleScreen
 * @Description: 回合制战斗场景
 * @Author BsKPLu
 * @Date 2025/4/4
 * @Version 1.1
 */
public class BattleScreen implements Screen {
    private final Game game;
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private World world;
    private Box2DDebugRenderer debugRenderer;
    private Texture groundTexture;
    private BitmapFont font;
    private WorldManager worldManager; // 新增
    private UIManager uiManager; // 新增

    // 角色
    private Player player;
    private Enemy enemy;

    // 对战状态
    private float playerHealth = 100f;
    private float enemyHealth = 100f;
    private boolean isPlayerTurn = false;
    private boolean playerBleeding = false;
    private boolean enemyBleeding = false;
    private boolean playerDefending = false;
    private boolean enemyDefending = false;
    private String battleMessage = "Enemy's turn";

    // Constants
    public static final float PPM = 32f;
    private static final float SCENE_WIDTH = 12f;// 场景宽度12米
    public static final float SCENE_HEIGHT = 9f;// 场景高度9米
    private static final float WALL_LENGTH = 3f;// 每段墙壁长度3米
    private static final float WALL_THICKNESS = 0.5f;// 墙壁厚度0.5米

    public BattleScreen(Game game, Player player, Enemy enemy) {
        this.game = game;
        this.player = player;
        this.enemy = enemy;

        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, SCENE_WIDTH * PPM, SCENE_HEIGHT * PPM);
        world = new World(new Vector2(0, 0), true);
        debugRenderer = new Box2DDebugRenderer();
        groundTexture = new Texture(Gdx.files.internal("ground.png"));
        font = new BitmapFont();
        uiManager = new UIManager();
        worldManager=new WorldManager();
        worldManager.createBattleWalls(SCENE_WIDTH, SCENE_HEIGHT, WALL_LENGTH, WALL_THICKNESS);
        initializeCharacters();
    }

    private void initializeCharacters() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;

        // 玩家位置：场景右侧居中
        float playerX = SCENE_WIDTH - 1.2f;
        float playerY = SCENE_HEIGHT / 2f;
        bodyDef.position.set(playerX, playerY);
        Body playerBody = world.createBody(bodyDef);
        CircleShape playerShape = new CircleShape();
        playerShape.setRadius(0.5f);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = playerShape;
        fixtureDef.density = 1f;
        fixtureDef.friction = 0.4f;
        fixtureDef.restitution = 0.1f;
        playerBody.createFixture(fixtureDef).setUserData("player");
        playerShape.dispose();
        player.setPlayerBody(playerBody);

        // 敌人位置：场景左侧居中
        float enemyX = 1.2f;// 距离左侧 1 米
        float enemyY = SCENE_HEIGHT / 2f;// 垂直居中
        bodyDef.position.set(enemyX, enemyY);
        Body enemyBody = world.createBody(bodyDef);
        CircleShape enemyShape = new CircleShape();
        enemyShape.setRadius(0.5f);// 半径0.5米
        enemyBody.createFixture(fixtureDef).setUserData("enemy");
        enemyShape.dispose();
        enemy.setEnemyBody(enemyBody);

        // 重置动作状态
        player.setPlayerStateTime(0f);
        enemy.setEnemyStateTime(0f);
    }

    @Override
    public void show() {
        // 开始敌人回合
        performEnemyAction();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        world.step(1 / 60f, 6, 2);

        // 更新动画状态
        player.setPlayerStateTime(player.getPlayerStateTime() + delta);
        enemy.setEnemyStateTime(enemy.getEnemyStateTime() + delta);
        player.updateAnimation(delta);
        enemy.updateAnimation(delta);

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        int tileWidth = groundTexture.getWidth();
        int tileHeight = groundTexture.getHeight();
        for (float x = 0; x < SCENE_WIDTH * PPM; x += tileWidth) {
            for (float y = 0; y < SCENE_HEIGHT * PPM; y += tileHeight) {
                batch.draw(groundTexture, x, y);
            }
        }

        // 绘制玩家
        TextureRegion playerFrame = player.getCurrentFrame(player.getPlayerStateTime());
        if (playerFrame != null) {
            float playerX = player.getPlayerBody().getPosition().x * PPM - playerFrame.getRegionWidth() / 2f;
            float playerY = player.getPlayerBody().getPosition().y * PPM - playerFrame.getRegionHeight() / 2f;
            batch.draw(playerFrame, playerX, playerY);
        }

        // 绘制敌人
        TextureRegion enemyFrame = enemy.getEnemyCurrentFrame(enemy.getEnemyStateTime());
        if (enemyFrame != null) {
            float enemyX = enemy.getEnemyBody().getPosition().x * PPM - enemyFrame.getRegionWidth() / 2f;
            float enemyY = enemy.getEnemyBody().getPosition().y * PPM - enemyFrame.getRegionHeight() / 2f;
            batch.draw(enemyFrame, enemyX, enemyY);
        }
        batch.end();

        // 使用UIManager绘制UI
        uiManager.drawBattleUI(batch, playerHealth, enemyHealth, battleMessage, isPlayerTurn);

        // 处理玩家输入
        if (isPlayerTurn) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
                performPlayerNormalAttack();
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
                performPlayerDoubleStrike();
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
                performPlayerDefend();
            }
        }

        // 可选：渲染物理调试信息
        // debugRenderer.render(world, camera.combined.cpy().scale(PPM, PPM, 0));
    }

    // 玩家行动
    private void performPlayerNormalAttack() {
        float damage = 10f;
        if (enemyDefending) {
            damage *= 0.5f;
            enemyDefending = false;
        }
        enemyHealth -= damage;
        if (enemyHealth < 0) enemyHealth = 0;
        battleMessage = "Player attacks for " + damage + "% damage!";
        player.startAttackAnimation(player.getLastDirection());
        isPlayerTurn = false;
        checkBattleEnd();
    }

    private void performPlayerDoubleStrike() {
        float damage = 15f;
        if (enemyDefending) {
            damage *= 0.5f;
            enemyDefending = false;
        }
        enemyHealth -= damage;
        if (enemyHealth < 0) enemyHealth = 0;
        enemyBleeding = true;
        battleMessage = "Player double strikes for " + damage + "% damage, enemy bleeds!";
        player.startAttackAnimation(player.getLastDirection());
        isPlayerTurn = false;
        checkBattleEnd();
    }

    private void performPlayerDefend() {
        playerDefending = true;
        battleMessage = "Player defends!";
        isPlayerTurn = false;
        checkBattleEnd();
    }

    // 敌人行动
    private void performEnemyAction() {
        if (MathUtils.randomBoolean()) {
            float damage = 10f;
            if (playerDefending) {
                damage *= 0.5f;
                playerDefending = false;
            }
            playerHealth -= damage;
            if (playerHealth < 0) playerHealth = 0;
            battleMessage = "Enemy attacks for " + damage + "% damage!";
            enemy.startAttackAnimation(enemy.getEnemyLastDirection());
        } else {
            enemyDefending = true;
            battleMessage = "Enemy defends!";
        }
        isPlayerTurn = true;
        checkBattleEnd();
    }

    // 应用流血伤害
    private void applyBleeding() {
        if (playerBleeding) {
            float bleedDamage = 5f;
            playerHealth -= bleedDamage;
            if (playerHealth < 0) playerHealth = 0;
            battleMessage += " Player bleeds for " + bleedDamage + "%!";
        }
        if (enemyBleeding) {
            float bleedDamage = 5f;
            enemyHealth -= bleedDamage;
            if (enemyHealth < 0) enemyHealth = 0;
            battleMessage += " Enemy bleeds for " + bleedDamage + "%!";
        }
    }

    // 检查对战是否已结束
    private void checkBattleEnd() {
        if (playerHealth <= 0) {
            battleMessage = "Player defeated!";
            Gdx.app.exit();
        } else if (enemyHealth <= 0) {
            battleMessage = "Enemy defeated! Victory!";
            Gdx.app.exit();
        } else {
            applyBleeding();
            if (!isPlayerTurn) {
                performEnemyAction();
            }
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
    public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        groundTexture.dispose();
        world.dispose();
        debugRenderer.dispose();
        font.dispose();
        uiManager.dispose();
    }
}
