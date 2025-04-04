package tech.bskplu.test;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

import tech.bskplu.test.characters.Enemy;
import tech.bskplu.test.characters.Player;

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

    // 角色
    private Player player;
    private Enemy enemy;


    // 常量定义
    private static final float PPM = 32f;
    private static final float SCENE_WIDTH = 12f;// 场景宽度 24 米
    private static final float SCENE_HEIGHT = 9f;// 场景高度 18 米
    private static final float WALL_LENGTH = 3f;// 每段墙壁长度 6 米
    private static final float WALL_THICKNESS = 0.5f;// 墙壁厚度 0.5 米

    public BattleScreen(Game game, Player player, Enemy enemy) {
        this.game = game;
        this.player = player;
        this.enemy = enemy;

        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        // 设置摄像机视口大小为场景的像素尺寸
        camera.setToOrtho(false, SCENE_WIDTH * PPM, SCENE_HEIGHT * PPM);
        world = new World(new Vector2(0, 0), true);// 创建物理世界，无重力
        debugRenderer = new Box2DDebugRenderer();
        groundTexture = new Texture(Gdx.files.internal("ground.png"));
        createWalls();// 创建四周墙壁
        initializeCharacters();// 初始化角色位置
    }

    /**
     * 创建四周墙壁，每段墙壁长度为 6 米
     */
    private void createWalls() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        PolygonShape shape = new PolygonShape();
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.friction = 0.4f;

        // 计算墙壁数量
        int horizontalWallCount = (int) (SCENE_WIDTH / WALL_LENGTH);// 水平方向墙壁段数
        int verticalWallCount = (int) (SCENE_HEIGHT / WALL_LENGTH);// 垂直方向墙壁段数

        // 底部墙壁
        for (int i = 0; i < horizontalWallCount; i++) {
            bodyDef.position.set(WALL_LENGTH / 2f + i * WALL_LENGTH, WALL_THICKNESS / 2f);
            shape.setAsBox(WALL_LENGTH / 2f, WALL_THICKNESS / 2f);
            world.createBody(bodyDef).createFixture(fixtureDef).setUserData("wall_bottom_" + i);
        }

        // 顶部墙壁
        for (int i = 0; i < horizontalWallCount; i++) {
            bodyDef.position.set(WALL_LENGTH / 2f + i * WALL_LENGTH, SCENE_HEIGHT - WALL_THICKNESS / 2f);
            shape.setAsBox(WALL_LENGTH / 2f, WALL_THICKNESS / 2f);
            world.createBody(bodyDef).createFixture(fixtureDef).setUserData("wall_top_" + i);
        }

        // 左侧墙壁
        for (int i = 0; i < verticalWallCount; i++) {
            bodyDef.position.set(WALL_THICKNESS / 2f, WALL_LENGTH / 2f + i * WALL_LENGTH);
            shape.setAsBox(WALL_THICKNESS / 2f, WALL_LENGTH / 2f);
            world.createBody(bodyDef).createFixture(fixtureDef).setUserData("wall_left_" + i);
        }

        // 右侧墙壁
        for (int i = 0; i < verticalWallCount; i++) {
            bodyDef.position.set(SCENE_WIDTH - WALL_THICKNESS / 2f, WALL_LENGTH / 2f + i * WALL_LENGTH);
            shape.setAsBox(WALL_THICKNESS / 2f, WALL_LENGTH / 2f);
            world.createBody(bodyDef).createFixture(fixtureDef).setUserData("wall_right_" + i);
        }

        shape.dispose();
    }

    /**
     * 初始化角色位置
     */
    private void initializeCharacters() {
        // 销毁旧的物理身体（如果存在） Box2D的规则 Body只能在创建时所属的World中销毁
//        if (player.getPlayerBody() != null) {
//            world.destroyBody(player.getPlayerBody());
//        }
//        if (enemy.getEnemyBody() != null) {
//            world.destroyBody(enemy.getEnemyBody());
//        }

        // 创建新的物理身体
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;

        // 玩家位置：场景右侧居中
        float playerX = SCENE_WIDTH - 1.2f;// 距离右侧 1 米
        float playerY = SCENE_HEIGHT / 2f;// 垂直居中
        bodyDef.position.set(playerX, playerY);
        Body playerBody = world.createBody(bodyDef);
        CircleShape playerShape = new CircleShape();
        playerShape.setRadius(0.5f);// 半径 0.5 米
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
        enemyShape.setRadius(0.5f);// 半径 0.5 米
        enemyBody.createFixture(fixtureDef).setUserData("enemy");
        enemyShape.dispose();
        enemy.setEnemyBody(enemyBody);
    }


    @Override
    public void show() {
        // 场景显示时的初始化逻辑（目前为空）
    }

    @Override
    public void render(float delta) {
        // 清除屏幕
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // 更新物理世界
        world.step(1 / 60f, 6, 2);

        // 更新摄像机
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        // 绘制地块
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

        // 渲染物理调试信息（可选）
        debugRenderer.render(world, camera.combined.cpy().scale(PPM, PPM, 0));
    }

    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        batch.dispose();
        groundTexture.dispose();
        world.dispose();
        debugRenderer.dispose();
    }
}
