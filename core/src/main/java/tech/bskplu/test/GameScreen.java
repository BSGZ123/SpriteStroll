package tech.bskplu.test;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
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

import java.util.Arrays;
import java.util.Random;

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

    // 攻击动画
    private Texture attackTexture;
    private Animation<TextureRegion> samuraiAttackDownAnimation;
    private Animation<TextureRegion> samuraiAttackUpAnimation;
    private Animation<TextureRegion> samuraiAttackLeftAnimation;
    private Animation<TextureRegion> samuraiAttackRightAnimation;

    // 角色状态
    private float playerStateTime = 0f;
    private String currentAnimation = "idle_down";
    private String lastDirection = "down";
    private float playerSpeed = 2f;
    private float playerRunSpeed = 5f;
    private boolean isPlayer1Active = true;
    private boolean isAttacking = false;

    // 背景（地面）
    private Array<Body> groundBodies = new Array<>();
    private int groundCount = 10;

    private Music music;

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

        music = Gdx.audio.newMusic(Gdx.files.internal("LanTingXu.mp3"));
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
        Random random = new Random();
        for (int i = 0; i < groundCount; i++) {
            float x = random.nextFloat() * 800 / PIXELS_PER_METER;
            float y = random.nextFloat() * 600 / PIXELS_PER_METER;
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

        handleInput();
        world.step(timeStep, velocityIterations, positionIterations);
        playerStateTime += delta;

        camera.position.set(playerBody.getPosition().x * PIXELS_PER_METER, playerBody.getPosition().y * PIXELS_PER_METER, 0);
        camera.update();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

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

        TextureRegion currentFrame = getCurrentFrame(playerStateTime);
        float x = playerBody.getPosition().x * PIXELS_PER_METER - currentFrame.getRegionWidth() / 2f;
        float y = playerBody.getPosition().y * PIXELS_PER_METER - currentFrame.getRegionHeight() / 2f;
        batch.draw(currentFrame, x, y);

        batch.end();

        // 检测ESC键是否按下以退出游戏
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new StartScreen(game));// 复用开始界面
            //Gdx.app.exit();
        }

        music.setVolume(0.1f);
        music.play();

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
        Gdx.input.setInputProcessor(null);// 清空输入处理器
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
        playerTexture.dispose();
        samuraiTexture.dispose();
        attackTexture.dispose();
        world.dispose();
        debugRenderer.dispose();
        music.dispose();
    }
}
