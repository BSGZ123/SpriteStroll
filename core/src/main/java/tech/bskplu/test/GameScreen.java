package tech.bskplu.test;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;

import java.util.Arrays;

/**
 * @ClassName: GameScreen
 * @Description: 战斗场景
 * @Author BsKPLu
 * @Date 2025/3/15
 * @Version 1.1
 */
public class GameScreen implements Screen {
    private final Game game;
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private World world;
    private Box2DDebugRenderer debugRenderer;
    private static final float PIXELS_PER_METER = 32f;
    private float timeStep = 1 / 60f;
    private int velocityIterations = 6;
    private int positionIterations = 2;

    private static final float WORLD_WIDTH = 800 / PIXELS_PER_METER + 4f;// 25..
    private static final float WORLD_HEIGHT = 600 / PIXELS_PER_METER + 4f;// 18.75..

    // Player1（猫猫）
    private Body playerBody;
    private Texture playerTexture;
    private Animation<TextureRegion> playerIdleAnimation;
    private Animation<TextureRegion> playerWalkAnimation;
    private Animation<TextureRegion> playerRunAnimation;

    // Player2（武士）
    private Texture samuraiTexture;
    private Animation<TextureRegion> samuraiWalkDownAnimation;
    private Animation<TextureRegion> samuraiWalkUpAnimation;
    private Animation<TextureRegion> samuraiWalkLeftAnimation;
    private Animation<TextureRegion> samuraiWalkRightAnimation;
    private Animation<TextureRegion> samuraiIdleDownAnimation;
    private Animation<TextureRegion> samuraiIdleUpAnimation;
    private Animation<TextureRegion> samuraiIdleLeftAnimation;
    private Animation<TextureRegion> samuraiIdleRightAnimation;

    // 武士攻击动画
    private Texture attackTexture;
    private Animation<TextureRegion> samuraiAttackDownAnimation;
    private Animation<TextureRegion> samuraiAttackUpAnimation;
    private Animation<TextureRegion> samuraiAttackLeftAnimation;
    private Animation<TextureRegion> samuraiAttackRightAnimation;

    // 敌方将领 GuanPin
    private Texture guanPinMovTexture;
    private Texture guanPinAtkTexture;
    private Animation<TextureRegion> guanPinWalkDownAnimation;
    private Animation<TextureRegion> guanPinWalkUpAnimation;
    private Animation<TextureRegion> guanPinWalkLeftAnimation;
    private Animation<TextureRegion> guanPinWalkRightAnimation;
    private Animation<TextureRegion> guanPinIdleDownAnimation;
    private Animation<TextureRegion> guanPinIdleUpAnimation;
    private Animation<TextureRegion> guanPinIdleLeftAnimation;
    private Animation<TextureRegion> guanPinIdleRightAnimation;
    private Animation<TextureRegion> guanPinAttackDownAnimation;
    private Animation<TextureRegion> guanPinAttackUpAnimation;
    private Animation<TextureRegion> guanPinAttackLeftAnimation;
    private Animation<TextureRegion> guanPinAttackRightAnimation;
    private Body enemyBody;
    private float enemyStateTime = 0f;

    // 角色状态
    private float playerStateTime = 0f;
    private String currentAnimation = "idle_down";
    private String lastDirection = "down";
    private float playerSpeed = 2f;
    private float playerRunSpeed = 5f;
    private boolean isPlayer1Active = false;
    private boolean isAttacking = false;

    // 背景（地面）
    private Array<Body> groundBodies = new Array<>();
    private int groundCount = 10;

    private Music music;

    // 血量属性
    private float playerHealth = 100f;
    private float playerMaxHealth = 100f;
    private float enemyHealth = 100f;
    private float enemyMaxHealth = 100f;

    // 绘制工具
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;

    public GameScreen(Game game) {
        this.game = game;
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 600);

        world = new World(new Vector2(0, 0), true);
        debugRenderer = new Box2DDebugRenderer();

        createPlayer();
        createSamurai();
        createGround();
        createBoundaries();
        createGuanPin();

        music = Gdx.audio.newMusic(Gdx.files.internal("LanTingXu.mp3"));

        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();
    }

    // 创建场景边界
    private  void createBoundaries(){
        // 创建左边界
        BodyDef leftBoundaryDef = new BodyDef();
        leftBoundaryDef.type = BodyDef.BodyType.StaticBody;
        leftBoundaryDef.position.set(0, WORLD_HEIGHT / 2);
        Body leftBoundary = world.createBody(leftBoundaryDef);
        PolygonShape leftShape = new PolygonShape();
        leftShape.setAsBox(1 / PIXELS_PER_METER, WORLD_HEIGHT / 2);
        leftBoundary.createFixture(leftShape, 0.0f).setUserData("boundary");
        leftShape.dispose();

        // 创建右边界
        BodyDef rightBoundaryDef = new BodyDef();
        rightBoundaryDef.type = BodyDef.BodyType.StaticBody;
        rightBoundaryDef.position.set(WORLD_WIDTH, WORLD_HEIGHT / 2);
        Body rightBoundary = world.createBody(rightBoundaryDef);
        PolygonShape rightShape = new PolygonShape();
        rightShape.setAsBox(1 / PIXELS_PER_METER, WORLD_HEIGHT / 2);
        rightBoundary.createFixture(rightShape, 0.0f).setUserData("boundary");
        rightShape.dispose();

        // 创建上边界
        BodyDef topBoundaryDef = new BodyDef();
        topBoundaryDef.type = BodyDef.BodyType.StaticBody;
        topBoundaryDef.position.set(WORLD_WIDTH / 2, WORLD_HEIGHT);
        Body topBoundary = world.createBody(topBoundaryDef);
        PolygonShape topShape = new PolygonShape();
        topShape.setAsBox(WORLD_WIDTH / 2, 1 / PIXELS_PER_METER);
        topBoundary.createFixture(topShape, 0.0f).setUserData("boundary");
        topShape.dispose();

        // 创建下边界
        BodyDef bottomBoundaryDef = new BodyDef();
        bottomBoundaryDef.type = BodyDef.BodyType.StaticBody;
        bottomBoundaryDef.position.set(WORLD_WIDTH / 2, 0);
        Body bottomBoundary = world.createBody(bottomBoundaryDef);
        PolygonShape bottomShape = new PolygonShape();
        bottomShape.setAsBox(WORLD_WIDTH / 2, 1 / PIXELS_PER_METER);
        bottomBoundary.createFixture(bottomShape, 0.0f).setUserData("boundary");
        bottomShape.dispose();
    }



    private void createPlayer() {
        playerTexture = new Texture(Gdx.files.internal("cat_SpriteSheet.png"));

        int frameWidth = playerTexture.getWidth() / 6;
        int frameHeight = playerTexture.getHeight() / 3;

        TextureRegion[][] tmp = TextureRegion.split(playerTexture, frameWidth, frameHeight);
        TextureRegion[] idleFrames = new TextureRegion[5];
        System.arraycopy(tmp[0], 0, idleFrames, 0, 5);
        TextureRegion[] walkFrames = new TextureRegion[6];
        System.arraycopy(tmp[1], 0, walkFrames, 0, 6);
        TextureRegion[] runFrames = new TextureRegion[6];
        System.arraycopy(tmp[2], 0, runFrames, 0, 6);

        playerIdleAnimation = new Animation<>(0.2f, idleFrames);
        playerWalkAnimation = new Animation<>(0.1f, walkFrames);
        playerRunAnimation = new Animation<>(0.05f, runFrames);
        for (Animation<TextureRegion> anim : Arrays.asList(playerIdleAnimation, playerWalkAnimation, playerRunAnimation)) {
            anim.setPlayMode(Animation.PlayMode.LOOP);
        }

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(800 / 2 / PIXELS_PER_METER, 600 / 2 / PIXELS_PER_METER);
        playerBody = world.createBody(bodyDef);
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

    private void createSamurai() {
        samuraiTexture = new Texture(Gdx.files.internal("Samurai_SpriteSheet.png"));
        int frameWidth = samuraiTexture.getWidth();
        int frameHeight = samuraiTexture.getHeight() / 11;

        TextureRegion[][] tmp = TextureRegion.split(samuraiTexture, frameWidth, frameHeight);

        TextureRegion[] walkDownFrames = {tmp[0][0], tmp[1][0]};
        TextureRegion[] walkUpFrames = {tmp[2][0], tmp[3][0]};
        TextureRegion[] walkLeftFrames = {tmp[4][0], tmp[5][0]};
        TextureRegion[] walkRightFrames = {new TextureRegion(tmp[4][0]), new TextureRegion(tmp[5][0])};
        walkRightFrames[0].flip(true, false);
        walkRightFrames[1].flip(true, false);

        TextureRegion[] idleDownFrames = {tmp[6][0]};
        TextureRegion[] idleUpFrames = {tmp[7][0]};
        TextureRegion[] idleLeftFrames = {tmp[8][0]};
        TextureRegion[] idleRightFrames = {new TextureRegion(tmp[8][0])};
        idleRightFrames[0].flip(true, false);

        samuraiWalkDownAnimation = new Animation<>(0.2f, walkDownFrames);
        samuraiWalkUpAnimation = new Animation<>(0.2f, walkUpFrames);
        samuraiWalkLeftAnimation = new Animation<>(0.2f, walkLeftFrames);
        samuraiWalkRightAnimation = new Animation<>(0.2f, walkRightFrames);
        samuraiIdleDownAnimation = new Animation<>(0.2f, idleDownFrames);
        samuraiIdleUpAnimation = new Animation<>(0.2f, idleUpFrames);
        samuraiIdleLeftAnimation = new Animation<>(0.2f, idleLeftFrames);
        samuraiIdleRightAnimation = new Animation<>(0.2f, idleRightFrames);

        for (Animation<TextureRegion> anim : Arrays.asList(samuraiWalkDownAnimation, samuraiWalkUpAnimation,
            samuraiWalkLeftAnimation, samuraiWalkRightAnimation, samuraiIdleDownAnimation,
            samuraiIdleUpAnimation, samuraiIdleLeftAnimation, samuraiIdleRightAnimation)) {
            anim.setPlayMode(Animation.PlayMode.LOOP);
        }

        attackTexture = new Texture(Gdx.files.internal("SanGuoZhi2.png"));
        int attackFrameWidth = attackTexture.getWidth();
        int attackFrameHeight = attackTexture.getHeight() / 12;

        TextureRegion[][] attackTmp = TextureRegion.split(attackTexture, attackFrameWidth, attackFrameHeight);
        TextureRegion[] attackDownFrames = new TextureRegion[4];
        for (int i = 0; i < 4; i++) attackDownFrames[i] = attackTmp[i][0];
        samuraiAttackDownAnimation = new Animation<>(0.1f, attackDownFrames);
        samuraiAttackDownAnimation.setPlayMode(Animation.PlayMode.NORMAL);

        TextureRegion[] attackUpFrames = new TextureRegion[4];
        for (int i = 4; i < 8; i++) attackUpFrames[i - 4] = attackTmp[i][0];
        samuraiAttackUpAnimation = new Animation<>(0.1f, attackUpFrames);
        samuraiAttackUpAnimation.setPlayMode(Animation.PlayMode.NORMAL);

        TextureRegion[] attackLeftFrames = new TextureRegion[4];
        for (int i = 8; i < 12; i++) attackLeftFrames[i - 8] = attackTmp[i][0];
        samuraiAttackLeftAnimation = new Animation<>(0.1f, attackLeftFrames);
        samuraiAttackLeftAnimation.setPlayMode(Animation.PlayMode.NORMAL);

        TextureRegion[] attackRightFrames = new TextureRegion[4];
        for (int i = 0; i < 4; i++) {
            attackRightFrames[i] = new TextureRegion(attackLeftFrames[i]);
            attackRightFrames[i].flip(true, false);
        }
        samuraiAttackRightAnimation = new Animation<>(0.1f, attackRightFrames);
        samuraiAttackRightAnimation.setPlayMode(Animation.PlayMode.NORMAL);

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(800 / 2 / PIXELS_PER_METER, 600 / 2 / PIXELS_PER_METER);
        if (playerBody != null) {
            playerBody.setType(BodyDef.BodyType.DynamicBody);
            if (playerBody.getFixtureList().size > 0) {
                playerBody.destroyFixture(playerBody.getFixtureList().get(0));
            }
        }
        CircleShape circleShape = new CircleShape();
        circleShape.setRadius(16f / PIXELS_PER_METER);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circleShape;
        fixtureDef.density = 1f;
        fixtureDef.friction = 0.4f;
        fixtureDef.restitution = 0.1f;
        playerBody.createFixture(fixtureDef).setUserData("samurai");
        circleShape.dispose();
    }

    private void createGround() {
        for (int i = 0; i < groundCount; i++) {
            float x = MathUtils.random(0, 800 / PIXELS_PER_METER);
            float y = MathUtils.random(0, 600 / PIXELS_PER_METER);
            float width = MathUtils.random(50 / PIXELS_PER_METER, 150 / PIXELS_PER_METER);
            float height = MathUtils.random(20 / PIXELS_PER_METER, 70 / PIXELS_PER_METER);
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

    private void createGuanPin() {
        guanPinMovTexture = new Texture(Gdx.files.internal("Mov_GuanPin.png"));
        int movFrameHeight = guanPinMovTexture.getHeight() / 11;
        int movFrameWidth = guanPinMovTexture.getWidth();
        TextureRegion[][] movTmp = TextureRegion.split(guanPinMovTexture, movFrameWidth, movFrameHeight);

        TextureRegion[] walkDownFrames = {movTmp[0][0], movTmp[1][0]};
        guanPinWalkDownAnimation = new Animation<>(0.2f, walkDownFrames);
        guanPinWalkDownAnimation.setPlayMode(Animation.PlayMode.LOOP);

        TextureRegion[] walkUpFrames = {movTmp[2][0], movTmp[3][0]};
        guanPinWalkUpAnimation = new Animation<>(0.2f, walkUpFrames);
        guanPinWalkUpAnimation.setPlayMode(Animation.PlayMode.LOOP);

        TextureRegion[] walkLeftFrames = {movTmp[4][0], movTmp[5][0]};
        guanPinWalkLeftAnimation = new Animation<>(0.2f, walkLeftFrames);
        guanPinWalkLeftAnimation.setPlayMode(Animation.PlayMode.LOOP);

        TextureRegion[] walkRightFrames = new TextureRegion[2];
        for (int i = 0; i < 2; i++) {
            walkRightFrames[i] = new TextureRegion(movTmp[4 + i][0]);
            walkRightFrames[i].flip(true, false);
        }
        guanPinWalkRightAnimation = new Animation<>(0.2f, walkRightFrames);
        guanPinWalkRightAnimation.setPlayMode(Animation.PlayMode.LOOP);

        TextureRegion[] idleDownFrames = {movTmp[6][0]};
        guanPinIdleDownAnimation = new Animation<>(0.2f, idleDownFrames);

        TextureRegion[] idleUpFrames = {movTmp[7][0]};
        guanPinIdleUpAnimation = new Animation<>(0.2f, idleUpFrames);

        TextureRegion[] idleLeftFrames = {movTmp[8][0]};
        guanPinIdleLeftAnimation = new Animation<>(0.2f, idleLeftFrames);

        TextureRegion[] idleRightFrames = {new TextureRegion(movTmp[8][0])};
        idleRightFrames[0].flip(true, false);
        guanPinIdleRightAnimation = new Animation<>(0.2f, idleRightFrames);

        guanPinAtkTexture = new Texture(Gdx.files.internal("Atk_GuanPin.png"));
        int atkFrameHeight = guanPinAtkTexture.getHeight() / 12;
        int atkFrameWidth = guanPinAtkTexture.getWidth();
        TextureRegion[][] atkTmp = TextureRegion.split(guanPinAtkTexture, atkFrameWidth, atkFrameHeight);

        TextureRegion[] attackDownFrames = new TextureRegion[4];
        for (int i = 0; i < 4; i++) attackDownFrames[i] = atkTmp[i][0];
        guanPinAttackDownAnimation = new Animation<>(0.1f, attackDownFrames);
        guanPinAttackDownAnimation.setPlayMode(Animation.PlayMode.NORMAL);

        TextureRegion[] attackUpFrames = new TextureRegion[4];
        for (int i = 0; i < 4; i++) attackUpFrames[i] = atkTmp[4 + i][0];
        guanPinAttackUpAnimation = new Animation<>(0.1f, attackUpFrames);
        guanPinAttackUpAnimation.setPlayMode(Animation.PlayMode.NORMAL);

        TextureRegion[] attackLeftFrames = new TextureRegion[4];
        for (int i = 0; i < 4; i++) attackLeftFrames[i] = atkTmp[8 + i][0];
        guanPinAttackLeftAnimation = new Animation<>(0.1f, attackLeftFrames);
        guanPinAttackLeftAnimation.setPlayMode(Animation.PlayMode.NORMAL);

        TextureRegion[] attackRightFrames = new TextureRegion[4];
        for (int i = 0; i < 4; i++) {
            attackRightFrames[i] = new TextureRegion(atkTmp[8 + i][0]);
            attackRightFrames[i].flip(true, false);
        }
        guanPinAttackRightAnimation = new Animation<>(0.1f, attackRightFrames);
        guanPinAttackRightAnimation.setPlayMode(Animation.PlayMode.NORMAL);

        BodyDef enemyBodyDef = new BodyDef();
        enemyBodyDef.type = BodyDef.BodyType.DynamicBody;
        float randomX = MathUtils.random(0, 800 / PIXELS_PER_METER);
        float randomY = MathUtils.random(0, 600 / PIXELS_PER_METER);
        enemyBodyDef.position.set(randomX, randomY);
        enemyBody = world.createBody(enemyBodyDef);

        CircleShape enemyShape = new CircleShape();
        enemyShape.setRadius(16f / PIXELS_PER_METER);
        FixtureDef enemyFixtureDef = new FixtureDef();
        enemyFixtureDef.shape = enemyShape;
        enemyFixtureDef.density = 1f;
        enemyFixtureDef.friction = 0.4f;
        enemyFixtureDef.restitution = 0.1f;
        enemyBody.createFixture(enemyFixtureDef).setUserData("enemy");
        enemyShape.dispose();
    }

    private void handleInput() {
        Vector2 velocity = new Vector2(0, 0);
        float speed = playerSpeed;
        if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
            isPlayer1Active = !isPlayer1Active;
        }
        if (isPlayer1Active) {
            if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) speed = playerRunSpeed;
            if (Gdx.input.isKeyPressed(Input.Keys.W)) velocity.y = speed;
            if (Gdx.input.isKeyPressed(Input.Keys.S)) velocity.y = -speed;
            if (Gdx.input.isKeyPressed(Input.Keys.A)) velocity.x = -speed;
            if (Gdx.input.isKeyPressed(Input.Keys.D)) velocity.x = speed;
            if (velocity.isZero()) currentAnimation = "idle";
            else if (speed == playerSpeed) currentAnimation = "walk";
            else currentAnimation = "run";
        } else {
            if (!isAttacking) {
                if (Gdx.input.isKeyPressed(Input.Keys.J)) {
                    if (Gdx.input.isKeyPressed(Input.Keys.S)) {
                        currentAnimation = "attack_down";
                        isAttacking = true;
                        playerStateTime = 0f;
                    } else if (Gdx.input.isKeyPressed(Input.Keys.W)) {
                        currentAnimation = "attack_up";
                        isAttacking = true;
                        playerStateTime = 0f;
                    } else if (Gdx.input.isKeyPressed(Input.Keys.A)) {
                        currentAnimation = "attack_left";
                        isAttacking = true;
                        playerStateTime = 0f;
                    } else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                        currentAnimation = "attack_right";
                        isAttacking = true;
                        playerStateTime = 0f;
                    }
                } else {
                    if (Gdx.input.isKeyPressed(Input.Keys.W)) {
                        velocity.y = speed;
                        currentAnimation = "walk_up";
                        lastDirection = "up";
                    } else if (Gdx.input.isKeyPressed(Input.Keys.S)) {
                        velocity.y = -speed;
                        currentAnimation = "walk_down";
                        lastDirection = "down";
                    } else if (Gdx.input.isKeyPressed(Input.Keys.A)) {
                        velocity.x = -speed;
                        currentAnimation = "walk_left";
                        lastDirection = "left";
                    } else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                        velocity.x = speed;
                        currentAnimation = "walk_right";
                        lastDirection = "right";
                    } else {
                        currentAnimation = "idle_" + lastDirection;
                    }
                }
            } else {
                velocity.set(0, 0);
                Animation<TextureRegion> currentAnim = getCurrentAnimation();
                if (currentAnim.isAnimationFinished(playerStateTime)) {
                    isAttacking = false;
                    currentAnimation = "idle_" + lastDirection;
                }
            }
        }
        playerBody.setLinearVelocity(velocity);
    }

    private Animation<TextureRegion> getCurrentAnimation() {
        return switch (currentAnimation) {
            case "walk_down" -> samuraiWalkDownAnimation;
            case "walk_up" -> samuraiWalkUpAnimation;
            case "walk_left" -> samuraiWalkLeftAnimation;
            case "walk_right" -> samuraiWalkRightAnimation;
            case "idle_down" -> samuraiIdleDownAnimation;
            case "idle_up" -> samuraiIdleUpAnimation;
            case "idle_left" -> samuraiIdleLeftAnimation;
            case "idle_right" -> samuraiIdleRightAnimation;
            case "attack_down" -> samuraiAttackDownAnimation;
            case "attack_up" -> samuraiAttackUpAnimation;
            case "attack_left" -> samuraiAttackLeftAnimation;
            case "attack_right" -> samuraiAttackRightAnimation;
            default -> samuraiIdleDownAnimation;
        };
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // 处理输入和物理模拟
        handleInput();
        world.step(timeStep, velocityIterations, positionIterations);
        playerStateTime += delta;
        enemyStateTime += delta;

        // 更新摄像机位置，跟随玩家
        camera.position.set(playerBody.getPosition().x * PIXELS_PER_METER, playerBody.getPosition().y * PIXELS_PER_METER, 0);
        camera.update();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // 绘制地块
        for (Body groundBody : groundBodies) {
            float x = groundBody.getPosition().x * PIXELS_PER_METER;
            float y = groundBody.getPosition().y * PIXELS_PER_METER;
            PolygonShape shape = (PolygonShape) groundBody.getFixtureList().get(0).getShape();
            Vector2 vertex0 = new Vector2();
            Vector2 vertex1 = new Vector2();
            shape.getVertex(0, vertex0);
            shape.getVertex(1, vertex1);
            float width = vertex0.x * 2 * PIXELS_PER_METER;
            float height = vertex1.y * 2 * PIXELS_PER_METER;
            Texture groundTexture = new Texture(Gdx.files.internal("ground.png"));
            batch.draw(groundTexture, x - width / 2, y - height / 2, width, height);
        }

        // 绘制玩家角色
        TextureRegion currentFrame = getCurrentFrame(playerStateTime);
        float x = playerBody.getPosition().x * PIXELS_PER_METER - currentFrame.getRegionWidth() / 2f;
        float y = playerBody.getPosition().y * PIXELS_PER_METER - currentFrame.getRegionHeight() / 2f;
        batch.draw(currentFrame, x, y);

        // 绘制敌方单位
        TextureRegion enemyFrame = guanPinIdleDownAnimation.getKeyFrame(enemyStateTime, true);
        float enemyX = enemyBody.getPosition().x * PIXELS_PER_METER - enemyFrame.getRegionWidth() / 2f;
        float enemyY = enemyBody.getPosition().y * PIXELS_PER_METER - enemyFrame.getRegionHeight() / 2f;
        batch.draw(enemyFrame, enemyX, enemyY);

        batch.end();

        // 绘制玩家血量条
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // 计算血量条的世界坐标（玩家头顶上方）
        float barWidth = 50;// 血量条宽度
        float barHeight = 5;// 血量条高度
        float barX = x + (currentFrame.getRegionWidth() - barWidth) / 2;// 居中对齐玩家
        float barY = y + currentFrame.getRegionHeight() + 6;// 在玩家头顶上方10个单位

        // 先绘制背景矩形（白色）
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(barX, barY, barWidth, barHeight);

        // 再绘制血量矩形（红色）
        float playerHealthPercentage = playerHealth / playerMaxHealth;
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(barX, barY, barWidth * playerHealthPercentage, barHeight);

        shapeRenderer.end();

        // 绘制玩家血量百分比
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        font.draw(batch, String.format("%.0f%%", playerHealthPercentage * 100), barX + barWidth + 5, barY + barHeight / 2);
        batch.end();

        // 绘制敌方血量条
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // 先绘制背景矩形（白色）
        float enemyHealthBarWidth = enemyFrame.getRegionWidth();
        float enemyHealthBarHeight = 5;
        float enemyHealthBarX = enemyX;
        float enemyHealthBarY = enemyY + enemyFrame.getRegionHeight() + 5;
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(enemyHealthBarX, enemyHealthBarY, enemyHealthBarWidth, enemyHealthBarHeight);

        // 再绘制血量矩形（红色）
        float enemyHealthPercentage = enemyHealth / enemyMaxHealth;
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(enemyHealthBarX, enemyHealthBarY, enemyHealthBarWidth * enemyHealthPercentage, enemyHealthBarHeight);

        shapeRenderer.end();

        // 绘制敌方血量百分比
        batch.begin();
        font.draw(batch, String.format("%.0f%%", enemyHealthPercentage * 100), enemyHealthBarX + enemyHealthBarWidth + 5, enemyHealthBarY + enemyHealthBarHeight / 2);
        batch.end();

        // 检测ESC键是否按下以退出游戏
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new StartScreen(game));
        }

        debugRenderer.render(world, camera.combined);
    }

    private TextureRegion getCurrentFrame(float stateTime) {
        if (isPlayer1Active) {
            return switch (currentAnimation) {
                case "walk" -> playerWalkAnimation.getKeyFrame(stateTime, true);
                case "run" -> playerRunAnimation.getKeyFrame(stateTime, true);
                default -> playerIdleAnimation.getKeyFrame(stateTime, true);
            };
        } else {
            Animation<TextureRegion> anim = getCurrentAnimation();
            return isAttacking ? anim.getKeyFrame(stateTime, false) : anim.getKeyFrame(stateTime, true);
        }
    }

    @Override
    public void show() {
        //Gdx.input.setInputProcessor(null);
        music.setVolume(0.1f);
        music.setLooping(true);
        music.play();
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
        music.stop();
    }

    @Override
    public void dispose() {
        batch.dispose();
        playerTexture.dispose();
        samuraiTexture.dispose();
        attackTexture.dispose();
        guanPinMovTexture.dispose();
        guanPinAtkTexture.dispose();
        world.dispose();
        debugRenderer.dispose();
        music.dispose();
        shapeRenderer.dispose();
        font.dispose();
    }
}
