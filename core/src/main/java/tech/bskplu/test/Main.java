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

    // 游戏视口宽度
    private static final float VIEWPORT_WIDTH = 800;
    // 游戏视口高度度
    private static final float VIEWPORT_HEIGHT = 600;
    // Box2D物理世界中"米"与像素之间的比例
    private static final float PIXELS_PER_METER = 32f;

    private SpriteBatch batch;
    // 正交相机
    private OrthographicCamera camera;

    // Box2D物理世界
    private World world;
    // Box2D 调试渲染器，用于显示Box2D的物理形状
    private Box2DDebugRenderer debugRenderer;
    // Box2D时间步长，用于控制物理模拟的精度和性能
    private float timeStep = 1 / 60f;
    // Box2D速度迭代次数
    private int velocityIterations = 6;
    // Box2D位置迭代次数
    private int positionIterations = 2;

    // Player1（玩家 猫猫）
    private Body playerBody;// 角色的Box2D Body，用于物理模拟
    private Texture playerTexture;// 角色的纹理（精灵表）
    private Animation<TextureRegion> playerIdleAnimation;// 角色站立时的动画
    private Animation<TextureRegion> playerWalkAnimation;// 角色行走时的动画
    private Animation<TextureRegion> playerRunAnimation;// 角色跑步时的动画


    // Player2(玩家 武士？)-----------------------------------------
    private Texture samuraiTexture;
    private Animation<TextureRegion> samuraiWalkDownAnimation;// 向下走动画
    private Animation<TextureRegion> samuraiWalkUpAnimation;// 向上走动画
    private Animation<TextureRegion> samuraiWalkSideAnimation;// 左右走动画
    private Animation<TextureRegion> samuraiIdleAnimation;// 站立动画

    private float playerStateTime = 0f;// 角色动画状态时间，用于控制动画播放
    private String currentAnimation = "idle";// 当前角色动画状态（idle, walk, run）
    private float playerSpeed = 2f;// 角色行走速度（米/秒）
    private float playerRunSpeed = 5f;// 角色跑步速度（米/秒）

    private boolean isPlayer1Active = true;// 角色开关，true为猫，false为武士

    // Background（背景）
    private Array<Body> groundBodies = new Array<>();// 地面的Box2D Body数组
    private int groundCount = 10;// 地面块的数量

    private Texture image;
    private Music music;


    @Override
    public void create() {
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, VIEWPORT_WIDTH, VIEWPORT_HEIGHT);

        // Box2D创建设定
        world = new World(new Vector2(0, 0), true);//无重力
        debugRenderer = new Box2DDebugRenderer();

        createPlayer();
        createSamurai();
        createGround();

        image = new Texture("WhiteFoxSex.png");
        music = Gdx.audio.newMusic(Gdx.files.internal("LanTingXu.mp3"));
    }

    /**
     *  创建猫猫角色对象，包括加载纹理、创建动画、创建Box2D Body和Fixture
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
        bodyDef.type = BodyDef.BodyType.DynamicBody;// 设置Body类型为动态，可以移动
        bodyDef.position.set(VIEWPORT_WIDTH / 2 / PIXELS_PER_METER, VIEWPORT_HEIGHT / 2 / PIXELS_PER_METER);// 设置Body初始位置

        // 在Box2D物理世界中创建Body
        playerBody = world.createBody(bodyDef);

        // 定义角色物理参数
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
     * 创建武士角色对象，包括加载纹理、创建动画、创建Box2D Body和Fixture
     */
    private void createSamurai(){
        samuraiTexture=new Texture(Gdx.files.internal("Samurai_SpriteSheet.png"));
        int frameWidth = samuraiTexture.getWidth();// 动作帧宽度
        int frameHeight = samuraiTexture.getHeight() / 11;// 动作帧高度

        // 根据动作帧的宽高进行裁剪分割
        TextureRegion[][] tmp=TextureRegion.split(samuraiTexture,frameWidth,frameHeight);

        // 向下走 动作帧组
        TextureRegion[] walkDownFrames = new TextureRegion[2];
        walkDownFrames[0] = tmp[0][0];
        walkDownFrames[1] = tmp[0][1];

        // 向上走 动作帧组
        TextureRegion[] walkUpFrames = new TextureRegion[2];
        walkUpFrames[0] = tmp[0][2];
        walkUpFrames[1] = tmp[0][3];

        // 向左向右 动作帧组
        TextureRegion[] walkSideFrames = new TextureRegion[2];
        walkSideFrames[0] = tmp[0][4];
        walkSideFrames[1] = tmp[0][5];

        // 待机状态 动作帧组
        TextureRegion[] idleFrames = new TextureRegion[3];
        idleFrames[0] = tmp[0][6];
        idleFrames[1] = tmp[0][7];
        idleFrames[2] = tmp[0][8];

        // 创建动画
        samuraiWalkDownAnimation = new Animation<>(0.2f, walkDownFrames);
        samuraiWalkUpAnimation = new Animation<>(0.2f, walkUpFrames);
        samuraiWalkSideAnimation = new Animation<>(0.2f, walkSideFrames);
        samuraiIdleAnimation = new Animation<>(0.2f, idleFrames);


    }

    /**
     *  创建地面对象，包括创建Box2D Body和Fixture
     */
    private void createGround() {
        Random random = new Random();

        // 创建指定数量的地面块
        for (int i = 0; i < groundCount; i++) {
            float x = random.nextFloat() * VIEWPORT_WIDTH / PIXELS_PER_METER;// 地面块x坐标
            float y = random.nextFloat() * VIEWPORT_HEIGHT / PIXELS_PER_METER;// 地面块y坐标
            float width = random.nextFloat() * 100 / PIXELS_PER_METER + 50 / PIXELS_PER_METER;// 地面块的宽度
            float height = random.nextFloat() * 50 / PIXELS_PER_METER + 20 / PIXELS_PER_METER;// 地面块的高度

            BodyDef groundBodyDef = new BodyDef();
            groundBodyDef.type = BodyDef.BodyType.StaticBody;// 地面块不可移动
            groundBodyDef.position.set(x, y);//设置位置

            Body groundBody = world.createBody(groundBodyDef);

            PolygonShape groundBox = new PolygonShape();// 创建多边形形状
            groundBox.setAsBox(width / 2, height / 2);// 为矩形

            groundBody.createFixture(groundBox, 0.0f).setUserData("ground");

            groundBodies.add(groundBody);
            groundBox.dispose();
        }
    }

    /**
     * 处理用户输入，控制角色的移动和动画
     */
    private void handleInput() {
        Vector2 velocity = new Vector2(0, 0);
        float speed = playerSpeed;

        // 按下shift 设置速度为跑步速度
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

        // 速度为零，切换站立状态
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

        // 设置相机位置跟随角色
        camera.position.set(playerBody.getPosition().x * PIXELS_PER_METER, playerBody.getPosition().y * PIXELS_PER_METER, 0);
        camera.update();

        // 设置SpriteBatch的投影矩阵为相机矩阵
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // 绘制地面
        for (Body groundBody : groundBodies) { // 遍历地面Body数组
            float x = groundBody.getPosition().x * PIXELS_PER_METER; // 获取地面Body的X坐标（像素）
            float y = groundBody.getPosition().y * PIXELS_PER_METER; // 获取地面Body的Y坐标（像素）
            PolygonShape shape = (PolygonShape) groundBody.getFixtureList().get(0).getShape(); // 获取地面Body的形状

            // 创建Vector2对象来存储顶点信息
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

            Texture groundTexture = new Texture(Gdx.files.internal("ground.png"));// 加载地面纹理

            batch.draw(groundTexture, x - width / 2, y - height / 2, width, height);// 绘制地面纹理
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

    /**
     * 根据当前动画状态和时间，获取当前动画帧
     * @param stateTime 动画状态时间
     * @return 当前动画帧
     */
    private TextureRegion getCurrentFrame(float stateTime) {
        return switch (currentAnimation) {
            case "walk" -> playerWalkAnimation.getKeyFrame(stateTime, true);
            case "run" -> playerRunAnimation.getKeyFrame(stateTime, true);
            default -> playerIdleAnimation.getKeyFrame(stateTime, true);
        };



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
