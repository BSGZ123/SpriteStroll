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
    private static final float GAME_WIDTH_PIXELS = 800f;
    private static final float GAME_HEIGHT_PIXELS = 600f;
    private static final float GAME_WIDTH_METERS = GAME_WIDTH_PIXELS / PIXELS_PER_METER;
    private static final float GAME_HEIGHT_METERS = GAME_HEIGHT_PIXELS / PIXELS_PER_METER;

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
    private Texture groundTexture;

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
        // 使用常量设置摄像机
        camera.setToOrtho(false, GAME_WIDTH_PIXELS, GAME_HEIGHT_PIXELS);

        world = new World(new Vector2(0, 0), true);
        debugRenderer = new Box2DDebugRenderer();

        // --- 创建游戏元素 ---
        groundTexture = new Texture(Gdx.files.internal("ground.png"));// 初始化地块纹理

        createBoundaries();
        createPlayer();
        createSamurai();
        createGround();
        createGuanPin();

        music = Gdx.audio.newMusic(Gdx.files.internal("LanTingXu.mp3"));

        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();
    }

    // 创建场景边界
    private void createBoundaries() {
        float wallThickness = 0.5f;// 墙的厚度（米）

        // --- 创建边界的通用设置 ---
        BodyDef boundaryBodyDef = new BodyDef();
        boundaryBodyDef.type = BodyDef.BodyType.StaticBody;
        PolygonShape boundaryShape = new PolygonShape();
        FixtureDef boundaryFixtureDef = new FixtureDef();
        boundaryFixtureDef.shape = boundaryShape;
        boundaryFixtureDef.friction = 0.4f;// 可以给墙一些摩擦力

        // --- 创建底部边界 ---
        boundaryBodyDef.position.set(GAME_WIDTH_METERS / 2f, -wallThickness / 2f);
        boundaryShape.setAsBox(GAME_WIDTH_METERS / 2f, wallThickness / 2f);
        world.createBody(boundaryBodyDef).createFixture(boundaryFixtureDef).setUserData("boundary_bottom");

        // --- 创建顶部边界 ---
        boundaryBodyDef.position.set(GAME_WIDTH_METERS / 2f, GAME_HEIGHT_METERS + wallThickness / 2f);
        boundaryShape.setAsBox(GAME_WIDTH_METERS / 2f, wallThickness / 2f);
        world.createBody(boundaryBodyDef).createFixture(boundaryFixtureDef).setUserData("boundary_top");

        // --- 创建左侧边界 ---
        boundaryBodyDef.position.set(-wallThickness / 2f, GAME_HEIGHT_METERS / 2f);
        boundaryShape.setAsBox(wallThickness / 2f, GAME_HEIGHT_METERS / 2f);
        world.createBody(boundaryBodyDef).createFixture(boundaryFixtureDef).setUserData("boundary_left");

        // --- 创建右侧边界 ---
        boundaryBodyDef.position.set(GAME_WIDTH_METERS + wallThickness / 2f, GAME_HEIGHT_METERS / 2f);
        boundaryShape.setAsBox(wallThickness / 2f, GAME_HEIGHT_METERS / 2f);
        world.createBody(boundaryBodyDef).createFixture(boundaryFixtureDef).setUserData("boundary_right");

        // --- 释放形状资源 ---
        boundaryShape.dispose();
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

        // --- 创建 Player Body (初始为猫猫，但会被武士覆盖) ---
        // 注意：这里的 Body 会在 createSamurai 中被重新配置，
        // 所以初始位置和形状可能不重要，但必须创建它。
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
        fixtureDef.restitution = 0.1f;// 减少弹跳
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

        // --- 配置 Player Body 为武士 ---
        // 确保 playerBody 存在且是 DynamicBody
        if (playerBody != null) {
            playerBody.setType(BodyDef.BodyType.DynamicBody);
            if (!playerBody.getFixtureList().isEmpty()) {
                playerBody.destroyFixture(playerBody.getFixtureList().get(0));
            }
        }
        CircleShape circleShape = new CircleShape();
        circleShape.setRadius(16f / PIXELS_PER_METER);// 圆形碰撞体
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circleShape;
        fixtureDef.density = 1f;
        fixtureDef.friction = 0.4f;
        fixtureDef.restitution = 0.1f;
        playerBody.createFixture(fixtureDef).setUserData("samurai");
        circleShape.dispose();

        // 确保playerBody初始位置在边界内
        playerBody.setTransform(GAME_WIDTH_METERS / 2f, GAME_HEIGHT_METERS / 2f, 0);
        playerBody.setLinearVelocity(0,0);// 清除可能的速度
        playerBody.setAngularVelocity(0);// 清除可能的角速度

    }

    private void createGround() {
        // *** 修改：确保地块生成在边界内 ***
        float margin = 1.0f; // 留出一点边距，避免紧贴边界
        float minX = margin;
        float maxX = GAME_WIDTH_METERS - margin;
        float minY = margin;
        float maxY = GAME_HEIGHT_METERS - margin;

        for (int i = 0; i < groundCount; i++) {
            // 随机尺寸，但要确保整体在地块内
            float maxWidth = (maxX - minX) / 2;// 最大半宽
            float maxHeight = (maxY - minY) / 2;// 最大半高
            float halfWidth = MathUtils.random(25 / PIXELS_PER_METER, Math.min(75 / PIXELS_PER_METER, maxWidth));
            float halfHeight = MathUtils.random(10 / PIXELS_PER_METER, Math.min(35 / PIXELS_PER_METER, maxHeight));

            // 随机位置，确保加上半宽/半高后仍在边界内
            float x = MathUtils.random(minX + halfWidth, maxX - halfWidth);
            float y = MathUtils.random(minY + halfHeight, maxY - halfHeight);

            BodyDef groundBodyDef = new BodyDef();
            groundBodyDef.type = BodyDef.BodyType.StaticBody;
            groundBodyDef.position.set(x, y);
            Body groundBody = world.createBody(groundBodyDef);
            PolygonShape groundBox = new PolygonShape();
            groundBox.setAsBox(halfWidth, halfHeight);// 使用半宽半高
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

        // *** 确保敌人生成在边界内 ***
        float margin = 2.0f;// 给敌人生成留更大边距，避免生成时就卡住
        float randomX = MathUtils.random(margin, GAME_WIDTH_METERS - margin);
        float randomY = MathUtils.random(margin, GAME_HEIGHT_METERS - margin);
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
        Vector2 velocity = playerBody.getLinearVelocity();// 获取当前速度以保持平滑
        velocity.set(0, 0);// 先重置，后面根据按键设置
        float speed = playerSpeed;

        // 之前切换角色的逻辑确实有问题且CTRL键通常用于组合键。。。
        // isKeyPressed会持续触发。所以用isKeyJustPressed。
        if (Gdx.input.isKeyJustPressed(Input.Keys.CONTROL_LEFT)) {
            isPlayer1Active = !isPlayer1Active;
        }

        // 猫猫控制逻辑
        if (isPlayer1Active) {
            if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) speed = playerRunSpeed;
            if (Gdx.input.isKeyPressed(Input.Keys.W)) velocity.y = speed;
            if (Gdx.input.isKeyPressed(Input.Keys.S)) velocity.y = -speed;
            if (Gdx.input.isKeyPressed(Input.Keys.A)) velocity.x = -speed;
            if (Gdx.input.isKeyPressed(Input.Keys.D)) velocity.x = speed;

            // 简单的状态判断
            if (velocity.isZero(0.1f)) {// 加个小阈值判断静止
                currentAnimation = "idle";
            } else if (speed == playerSpeed) {
                currentAnimation = "walk";
            } else {
                currentAnimation = "run";
            }
            // 猫猫动画不需要区分方向，这里简化
            // 需要处理速度归一化，防止斜向移动过快
            if (!velocity.isZero()) {
                velocity.nor().scl(speed);
            }

        } else { // 武士控制逻辑
            if (!isAttacking) {
                boolean isMoving = false;
                if (Gdx.input.isKeyPressed(Input.Keys.W)) {
                    velocity.y = speed;
                    lastDirection = "up";
                    isMoving = true;
                }
                if (Gdx.input.isKeyPressed(Input.Keys.S)) {
                    // 如果同时按下了 W 和 S，则 Y 速度为 0
                    velocity.y = (velocity.y > 0) ? 0 : -speed;
                    if (!isMoving) lastDirection = "down";// 只有在没按W时才更新方向为down
                    isMoving = true;
                }
                if (Gdx.input.isKeyPressed(Input.Keys.A)) {
                    velocity.x = -speed;
                    lastDirection = "left";
                    isMoving = true;
                }
                if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                    // 如果同时按下了 A 和 D，则 X 速度为 0
                    velocity.x = (velocity.x < 0) ? 0 : speed;
                    if (!isMoving || velocity.x < 0) lastDirection = "right";// 没按A或覆盖A时更新方向为right
                    isMoving = true;
                }

                // 处理移动动画状态
                if (isMoving) {
                    currentAnimation = "walk_" + lastDirection;
                    // 速度归一化，防止斜向移动过快
                    velocity.nor().scl(speed);
                } else {
                    currentAnimation = "idle_" + lastDirection;
                }


                // 处理攻击输入 (J 键)
                if (Gdx.input.isKeyJustPressed(Input.Keys.J)) {// 使用 isKeyJustPressed 避免连续触发
                    currentAnimation = "attack_" + lastDirection;// 根据最后移动方向决定攻击方向
                    isAttacking = true;
                    playerStateTime = 0f;// 重置攻击动画时间
                    velocity.set(0, 0);// 攻击时停止移动
                }

            } else {// 正在攻击中
                velocity.set(0, 0);// 攻击时不允许移动
                Animation<TextureRegion> currentAnim = getCurrentAnimation();// 获取当前攻击动画
                if (currentAnim != null && currentAnim.isAnimationFinished(playerStateTime)) {
                    isAttacking = false;// 攻击动画播放完毕
                    currentAnimation = "idle_" + lastDirection;// 恢复到静止站立状态
                }
            }
        }
        playerBody.setLinearVelocity(velocity);// 应用最终计算出的速度
    }

    // 获取当前Player的动画 (区分猫猫和武士)
    private Animation<TextureRegion> getCurrentAnimation() {
        if (isPlayer1Active) {
            return switch (currentAnimation) {
                case "walk" -> playerWalkAnimation;
                case "run" -> playerRunAnimation;
                default -> playerIdleAnimation;
            };
        } else {
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

        // --- 更新摄像机位置，使其跟随玩家 ---
        // 保持摄像机中心在玩家身上，但限制摄像机不超过边界
        float camX = MathUtils.clamp(playerBody.getPosition().x * PIXELS_PER_METER,
            GAME_WIDTH_PIXELS / 2f,
            (GAME_WIDTH_METERS * PIXELS_PER_METER) - GAME_WIDTH_PIXELS / 2f); // 假设地图比屏幕大，这里需要调整
        float camY = MathUtils.clamp(playerBody.getPosition().y * PIXELS_PER_METER,
            GAME_HEIGHT_PIXELS / 2f,
            (GAME_HEIGHT_METERS * PIXELS_PER_METER) - GAME_HEIGHT_PIXELS / 2f); // 同上

        // 如果地图和屏幕一样大，则不需要 clamp，直接跟随
        // camera.position.set(playerBody.getPosition().x * PIXELS_PER_METER, playerBody.getPosition().y * PIXELS_PER_METER, 0);

        // 当前代码是固定大小的地图 (800x600)，所以直接让相机中心跟随玩家即可
        camera.position.set(playerBody.getPosition().x * PIXELS_PER_METER, playerBody.getPosition().y * PIXELS_PER_METER, 0);
        camera.update();

        // --- 绘制 ---
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // --- 绘制地块 ---
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
            batch.draw(groundTexture, x - width / 2, y - height / 2, width, height);
        }


        // --- 绘制玩家角色 ---
        TextureRegion currentFrame = getCurrentFrame(playerStateTime);
        if (currentFrame != null) {
            float playerX = playerBody.getPosition().x * PIXELS_PER_METER - currentFrame.getRegionWidth() / 2f;
            float playerY = playerBody.getPosition().y * PIXELS_PER_METER - currentFrame.getRegionHeight() / 2f;
            batch.draw(currentFrame, playerX, playerY);
        }

        // --- 绘制敌方单位 ---
        // TODO: 实现敌人 AI 和动画状态切换
        Animation<TextureRegion> enemyAnim = guanPinIdleDownAnimation;
        TextureRegion enemyFrame = enemyAnim.getKeyFrame(enemyStateTime, true);
        if (enemyFrame != null && enemyBody != null) {
            float enemyX = enemyBody.getPosition().x * PIXELS_PER_METER - enemyFrame.getRegionWidth() / 2f;
            float enemyY = enemyBody.getPosition().y * PIXELS_PER_METER - enemyFrame.getRegionHeight() / 2f;
            batch.draw(enemyFrame, enemyX, enemyY);
        }

        batch.end();


        // --- 绘制UI元素 (血条等) ---
        // 使用独立的 SpriteBatch 和 Camera 可以避免坐标转换问题，但这里继续用世界坐标
        shapeRenderer.setProjectionMatrix(camera.combined);// 确保和 batch 使用相同投影

        // --- 绘制玩家血量条 ---
        if (currentFrame != null) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            float barWidth = 40;// 血量条宽度 (像素)
            float barHeight = 5;// 血量条高度 (像素)
            float barOffsetX = (currentFrame.getRegionWidth() - barWidth) / 2f;// 居中偏移
            float barOffsetY = currentFrame.getRegionHeight() + 5;// 在头顶上方距离
            float barX = (playerBody.getPosition().x * PIXELS_PER_METER) - currentFrame.getRegionWidth() / 2f + barOffsetX;
            float barY = (playerBody.getPosition().y * PIXELS_PER_METER) - currentFrame.getRegionHeight() / 2f + barOffsetY;

            // 背景 (灰色或白色)
            shapeRenderer.setColor(Color.DARK_GRAY);
            shapeRenderer.rect(barX, barY, barWidth, barHeight);
            // 血量 (红色)
            float playerHealthPercentage = playerHealth / playerMaxHealth;
            shapeRenderer.setColor(Color.RED);
            shapeRenderer.rect(barX, barY, barWidth * playerHealthPercentage, barHeight);
            shapeRenderer.end();

            // 绘制血量百分比文字
            batch.begin();
            font.setColor(Color.WHITE);
            font.draw(batch, String.format("%.0f%%", playerHealthPercentage * 100), barX + barWidth + 5, barY + barHeight); // 调整文字位置
            batch.end();
        }


        // --- 绘制敌方血量条 ---
        if (enemyFrame != null && enemyBody != null) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            float enemyBarWidth = 40;
            float enemyBarHeight = 5;
            float enemyBarOffsetX = (enemyFrame.getRegionWidth() - enemyBarWidth) / 2f;
            float enemyBarOffsetY = enemyFrame.getRegionHeight() + 5;
            float enemyBarX = (enemyBody.getPosition().x * PIXELS_PER_METER) - enemyFrame.getRegionWidth() / 2f + enemyBarOffsetX;
            float enemyBarY = (enemyBody.getPosition().y * PIXELS_PER_METER) - enemyFrame.getRegionHeight() / 2f + enemyBarOffsetY;

            shapeRenderer.setColor(Color.DARK_GRAY);
            shapeRenderer.rect(enemyBarX, enemyBarY, enemyBarWidth, enemyBarHeight);
            float enemyHealthPercentage = enemyHealth / enemyMaxHealth;
            shapeRenderer.setColor(Color.RED);
            shapeRenderer.rect(enemyBarX, enemyBarY, enemyBarWidth * enemyHealthPercentage, enemyBarHeight);
            shapeRenderer.end();

            batch.begin();
            font.setColor(Color.WHITE);
            font.draw(batch, String.format("%.0f%%", enemyHealthPercentage * 100), enemyBarX + enemyBarWidth + 5, enemyBarY + enemyBarHeight);
            batch.end();
        }


        // --- 调试渲染器 ---
        // 绘制 Box2D 调试信息 (应该在所有 batch.end() 之后)
        debugRenderer.render(world, camera.combined.cpy().scale(PIXELS_PER_METER, PIXELS_PER_METER, 0));


        // --- 退出逻辑 ---
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new StartScreen(game));
        }
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
