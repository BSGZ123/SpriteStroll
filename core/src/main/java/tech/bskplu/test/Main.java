package tech.bskplu.test;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;

import java.util.Random;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {

    //游戏视口宽度
    private static final float VIEWPORT_WIDTH = 800;
    //游戏视口高度度
    private static final float VIEWPORT_HEIGHT = 600;
    //Box2D 物理世界中 "米" 与像素之间的比例
    private static final float PIXELS_PER_METER = 32f;

    private SpriteBatch batch;
    // 正交相机
    private OrthographicCamera camera;

    // Box2D 物理世界
    private World world;
    // Box2D 调试渲染器，用于显示 Box2D 的物理形状
    private Box2DDebugRenderer debugRenderer;
    // Box2D 时间步长，用于控制物理模拟的精度和性能
    private float timeStep = 1 / 60f;
    // Box2D 速度迭代次数
    private int velocityIterations = 6;
    // Box2D 位置迭代次数
    private int positionIterations = 2;

    // Player（玩家）
    private Body playerBody;  // 角色的 Box2D Body，用于物理模拟
    private Texture playerTexture; // 角色的纹理（精灵表）
    private Animation<TextureRegion> playerIdleAnimation; // 角色站立时的动画
    private Animation<TextureRegion> playerWalkAnimation; // 角色行走时的动画
    private Animation<TextureRegion> playerRunAnimation;  // 角色跑步时的动画
    private float playerStateTime = 0f; // 角色动画状态时间，用于控制动画播放
    private String currentAnimation = "idle"; // 当前角色动画状态（idle, walk, run）
    private float playerSpeed = 2f; // 角色行走速度（米/秒）
    private float playerRunSpeed = 5f; // 角色跑步速度（米/秒）

    // Background（背景）
    private Array<Body> groundBodies = new Array<>(); // 地面的Box2D Body数组
    private int groundCount = 10; // 地面块的数量

    private Texture image;
    private Music music;


    @Override
    public void create() {
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, VIEWPORT_WIDTH, VIEWPORT_HEIGHT);

        // Box2D 创建设定
        world = new World(new Vector2(0, 0), true); //无重力
        debugRenderer = new Box2DDebugRenderer();

        createPlayer();
        createGround();

        image = new Texture("WhiteFoxSex.png");
        music = Gdx.audio.newMusic(Gdx.files.internal("LanTingXu.mp3"));
    }

    /**
     *  创建玩家对象，包括加载纹理、创建动画、创建 Box2D Body 和 Fixture
     */
    private void createPlayer() {

        playerTexture = new Texture(Gdx.files.internal("cat_SpriteSheet.png"));

        // 将精灵表拆分为帧
        int frameWidth = playerTexture.getWidth() / 6;
        int frameHeight = playerTexture.getHeight() / 3;

        TextureRegion[][] tmp = TextureRegion.split(playerTexture, frameWidth, frameHeight);

        // 转换为一维数组以用于动画
        // 创建站立动画帧数组
        TextureRegion[] idleFrames = new TextureRegion[5];
        System.arraycopy(tmp[0], 0, idleFrames, 0, 5);

        // 创建行走动画帧数组
        TextureRegion[] walkFrames = new TextureRegion[6];
        System.arraycopy(tmp[1], 0, walkFrames, 0, 6);

        // 创建跑步动画帧数组
        TextureRegion[] runFrames = new TextureRegion[6];
        System.arraycopy(tmp[2], 0, runFrames, 0, 6);

        // 创建动画
        playerIdleAnimation = new Animation<>(0.2f, idleFrames);
        playerWalkAnimation = new Animation<>(0.1f, walkFrames);
        playerRunAnimation = new Animation<>(0.05f, runFrames);

        // 设置动作动画循环播放
        playerIdleAnimation.setPlayMode(Animation.PlayMode.LOOP);
        playerWalkAnimation.setPlayMode(Animation.PlayMode.LOOP);
        playerRunAnimation.setPlayMode(Animation.PlayMode.LOOP);

        // 定义角色
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody; // 设置Body类型为动态，可以移动
        bodyDef.position.set(VIEWPORT_WIDTH / 2 / PIXELS_PER_METER, VIEWPORT_HEIGHT / 2 / PIXELS_PER_METER); // 设置Body初始位置

        // 在 Box2D 物理世界中创建 Body
        playerBody = world.createBody(bodyDef);

        // 定义角色参数
        CircleShape circleShape = new CircleShape();
        circleShape.setRadius(16f / PIXELS_PER_METER);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circleShape;
        fixtureDef.density = 1f;
        fixtureDef.friction = 0.4f;
        fixtureDef.restitution = 0.1f;

        playerBody.createFixture(fixtureDef).setUserData("player");
        circleShape.dispose();
    }

    /**
     *  创建地面对象，包括创建 Box2D Body 和 Fixture
     */
    private void createGround() {
        Random random = new Random();
        for (int i = 0; i < groundCount; i++) {
            float x = random.nextFloat() * VIEWPORT_WIDTH / PIXELS_PER_METER;
            float y = random.nextFloat() * VIEWPORT_HEIGHT / PIXELS_PER_METER;
            float width = random.nextFloat() * 100 / PIXELS_PER_METER + 50 / PIXELS_PER_METER;
            float height = random.nextFloat() * 50 / PIXELS_PER_METER + 20 / PIXELS_PER_METER;

            BodyDef groundBodyDef = new BodyDef();
            groundBodyDef.type = BodyDef.BodyType.StaticBody;
            groundBodyDef.position.set(x, y);

            Body groundBody = world.createBody(groundBodyDef);

            PolygonShape groundBox = new PolygonShape();
            groundBox.setAsBox(width / 2, height / 2);

            groundBody.createFixture(groundBox, 0.0f).setUserData("ground");

            groundBodies.add(groundBody);
            groundBox.dispose();
        }
    }

    /**
     * 处理用户输入，控制玩家的移动和动画
     */
    private void handleInput() {
        Vector2 velocity = new Vector2(0, 0);
        float speed = playerSpeed;

        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            speed = playerRunSpeed;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            velocity.y = speed;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            velocity.y = -speed;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            velocity.x = -speed;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            velocity.x = speed;
        }

        if (velocity.isZero()) {
            currentAnimation = "idle";
        } else if (speed == playerSpeed) {
            currentAnimation = "walk";
        } else {
            currentAnimation = "run";
        }

        playerBody.setLinearVelocity(velocity);
    }


    @Override
    public void render() {
//        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
//        batch.begin();
//        batch.draw(image, 0, 0);
//        batch.end();

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        handleInput();

        world.step(timeStep, velocityIterations, positionIterations);

        playerStateTime += Gdx.graphics.getDeltaTime();

        camera.position.set(playerBody.getPosition().x * PIXELS_PER_METER, playerBody.getPosition().y * PIXELS_PER_METER, 0);
        camera.update();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // 绘制地面
        for (Body groundBody : groundBodies) { // 遍历地面 Body 数组
            float x = groundBody.getPosition().x * PIXELS_PER_METER; // 获取地面 Body 的 X 坐标（像素）
            float y = groundBody.getPosition().y * PIXELS_PER_METER; // 获取地面 Body 的 Y 坐标（像素）
            PolygonShape shape = (PolygonShape) groundBody.getFixtureList().get(0).getShape(); // 获取地面 Body 的形状

            // 创建 Vector2 对象来存储顶点信息
            Vector2 vertex0 = new Vector2();
            Vector2 vertex1 = new Vector2();

            // 获取矩形的顶点信息
            shape.getVertex(0, vertex0);
            shape.getVertex(1, vertex1);

            // 计算地面块的宽度和高度（像素）
            float width = vertex0.x * 2 * PIXELS_PER_METER;
            float height = vertex1.y * 2 * PIXELS_PER_METER;

            // 释放Vector2资源
            vertex0.setZero();
            vertex1.setZero();

            Texture groundTexture = new Texture(Gdx.files.internal("ground.png")); // 加载地面纹理

            batch.draw(groundTexture, x - width / 2, y - height / 2, width, height); // 绘制地面纹理
        }

        // 绘制角色
        TextureRegion currentFrame = getCurrentFrame(playerStateTime);
        float x = playerBody.getPosition().x * PIXELS_PER_METER - currentFrame.getRegionWidth() / 2f;
        float y = playerBody.getPosition().y * PIXELS_PER_METER - currentFrame.getRegionHeight() / 2f;
        batch.draw(currentFrame, x, y);

        batch.end();

        music.setVolume(0.3f);
        music.play();

        debugRenderer.render(world, camera.combined);
    }

    private TextureRegion getCurrentFrame(float stateTime) {
        switch (currentAnimation) {
            case "walk":
                return playerWalkAnimation.getKeyFrame(stateTime, true);
            case "run":
                return playerRunAnimation.getKeyFrame(stateTime, true);
            case "idle":
            default:
                return playerIdleAnimation.getKeyFrame(stateTime, true);
        }



    }

    @Override
    public void dispose() {
        batch.dispose();
        playerTexture.dispose();
        world.dispose();
        debugRenderer.dispose();
        image.dispose();
        music.dispose();
    }
}
