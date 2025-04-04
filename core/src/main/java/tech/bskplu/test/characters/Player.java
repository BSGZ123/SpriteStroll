package tech.bskplu.test.characters;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import java.util.Arrays;
/**
 * @ClassName: Player
 * @Description: 管理玩家逻辑，包括移动、动画和攻击
 * @Author BsKPLu
 * @Date 2025/4/2
 * @Version 1.1
 */
public class Player {
    private Body playerBody;// 玩家的物理身体
    private Texture playerTexture;// 猫猫角色纹理
    private Animation<TextureRegion> playerIdleAnimation;
    private Animation<TextureRegion> playerWalkAnimation;
    private Animation<TextureRegion> playerRunAnimation;
    private Texture samuraiTexture;// 武士角色纹理
    private Animation<TextureRegion> samuraiWalkDownAnimation;
    private Animation<TextureRegion> samuraiWalkUpAnimation;
    private Animation<TextureRegion> samuraiWalkLeftAnimation;
    private Animation<TextureRegion> samuraiWalkRightAnimation;
    private Animation<TextureRegion> samuraiIdleDownAnimation;
    private Animation<TextureRegion> samuraiIdleUpAnimation;
    private Animation<TextureRegion> samuraiIdleLeftAnimation;
    private Animation<TextureRegion> samuraiIdleRightAnimation;
    private Texture attackTexture;// 武士攻击纹理
    private Animation<TextureRegion> samuraiAttackDownAnimation;
    private Animation<TextureRegion> samuraiAttackUpAnimation;
    private Animation<TextureRegion> samuraiAttackLeftAnimation;
    private Animation<TextureRegion> samuraiAttackRightAnimation;
    private float playerStateTime = 0f;// 动画状态时间
    private String currentAnimation = "idle_down";// 当前动画状态
    private String lastDirection = "down";// 最后移动方向
    private float playerSpeed = 2f;// 普通移动速度 (米/秒)
    private float playerRunSpeed = 5f;// 奔跑速度 (米/秒)
    private boolean isPlayer1Active = false;// 是否激活猫猫角色 (否则为武士)
    private boolean isAttacking = false;// 是否正在攻击

    /**
     * 构造函数：初始化玩家并创建物理身体和动画
     * @param world Box2D 物理世界
     */
    public Player(World world) {
        createPlayer(world);
        createSamurai();
    }

    /**
     * 创建猫猫角色及其动画和初始物理身体
     * @param world Box2D 物理世界
     */
    private void createPlayer(World world) {
        playerTexture = new Texture(Gdx.files.internal("cat_SpriteSheet.png"));
        int frameWidth = playerTexture.getWidth() / 6;
        int frameHeight = playerTexture.getHeight() / 3;
        TextureRegion[][] tmp = TextureRegion.split(playerTexture, frameWidth, frameHeight);

        // Idle 动画
        TextureRegion[] idleFrames = new TextureRegion[5];
        System.arraycopy(tmp[0], 0, idleFrames, 0, 5);
        playerIdleAnimation = new Animation<>(0.2f, idleFrames);

        // Walk 动画
        TextureRegion[] walkFrames = new TextureRegion[6];
        System.arraycopy(tmp[1], 0, walkFrames, 0, 6);
        playerWalkAnimation = new Animation<>(0.1f, walkFrames);

        // Run 动画
        TextureRegion[] runFrames = new TextureRegion[6];
        System.arraycopy(tmp[2], 0, runFrames, 0, 6);
        playerRunAnimation = new Animation<>(0.05f, runFrames);

        // 设置动画循环播放
        for (Animation<TextureRegion> anim : Arrays.asList(playerIdleAnimation, playerWalkAnimation, playerRunAnimation)) {
            anim.setPlayMode(Animation.PlayMode.LOOP);
        }

        // 创建物理身体 (初始为猫猫，将被武士覆盖)
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(800 / 2 / 32f, 600 / 2 / 32f); // 屏幕中心 (米)
        playerBody = world.createBody(bodyDef);
        CircleShape circleShape = new CircleShape();
        circleShape.setRadius(16f / 32f); // 半径 0.5 米
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circleShape;
        fixtureDef.density = 1f;
        fixtureDef.friction = 0.4f;
        fixtureDef.restitution = 0.1f;
        playerBody.createFixture(fixtureDef).setUserData("player");
        circleShape.dispose();
    }

    /**
     * 创建武士角色及其动画，并更新物理身体
     */
    private void createSamurai() {
        samuraiTexture = new Texture(Gdx.files.internal("Samurai_SpriteSheet.png"));
        int frameWidth = samuraiTexture.getWidth();
        int frameHeight = samuraiTexture.getHeight() / 11;
        TextureRegion[][] tmp = TextureRegion.split(samuraiTexture, frameWidth, frameHeight);

        // 行走动画
        TextureRegion[] walkDownFrames = {tmp[0][0], tmp[1][0]};
        TextureRegion[] walkUpFrames = {tmp[2][0], tmp[3][0]};
        TextureRegion[] walkLeftFrames = {tmp[4][0], tmp[5][0]};
        TextureRegion[] walkRightFrames = {new TextureRegion(tmp[4][0]), new TextureRegion(tmp[5][0])};
        walkRightFrames[0].flip(true, false);
        walkRightFrames[1].flip(true, false);

        // Idle 动画
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

        // 设置循环播放
        for (Animation<TextureRegion> anim : Arrays.asList(samuraiWalkDownAnimation, samuraiWalkUpAnimation,
            samuraiWalkLeftAnimation, samuraiWalkRightAnimation, samuraiIdleDownAnimation,
            samuraiIdleUpAnimation, samuraiIdleLeftAnimation, samuraiIdleRightAnimation)) {
            anim.setPlayMode(Animation.PlayMode.LOOP);
        }

        // 攻击动画
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

        // 更新物理身体为武士
        if (playerBody != null) {
            if (!playerBody.getFixtureList().isEmpty()) {
                playerBody.destroyFixture(playerBody.getFixtureList().get(0));
            }
            CircleShape circleShape = new CircleShape();
            circleShape.setRadius(16f / 32f);
            FixtureDef fixtureDef = new FixtureDef();
            fixtureDef.shape = circleShape;
            fixtureDef.density = 1f;
            fixtureDef.friction = 0.4f;
            fixtureDef.restitution = 0.1f;
            playerBody.createFixture(fixtureDef).setUserData("samurai");
            circleShape.dispose();
            playerBody.setTransform(800 / 32f / 2f, 600 / 32f / 2f, 0);
        }
    }

    /**
     * 处理玩家输入，控制移动和攻击
     */
    public void handleInput() {
        Vector2 velocity = playerBody.getLinearVelocity();
        velocity.set(0, 0);
        float speed = playerSpeed;

        if (Gdx.input.isKeyJustPressed(Input.Keys.CONTROL_LEFT)) {
            isPlayer1Active = !isPlayer1Active;
        }

        if (isPlayer1Active) { // 猫猫控制
            if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) speed = playerRunSpeed;
            if (Gdx.input.isKeyPressed(Input.Keys.W)) velocity.y = speed;
            if (Gdx.input.isKeyPressed(Input.Keys.S)) velocity.y = -speed;
            if (Gdx.input.isKeyPressed(Input.Keys.A)) velocity.x = -speed;
            if (Gdx.input.isKeyPressed(Input.Keys.D)) velocity.x = speed;

            if (velocity.isZero(0.1f)) {
                currentAnimation = "idle";
            } else if (speed == playerSpeed) {
                currentAnimation = "walk";
            } else {
                currentAnimation = "run";
            }
            if (!velocity.isZero()) {
                velocity.nor().scl(speed);
            }
        } else { // 武士控制
            if (!isAttacking) {
                boolean isMoving = false;
                if (Gdx.input.isKeyPressed(Input.Keys.W)) {
                    velocity.y = speed;
                    lastDirection = "up";
                    isMoving = true;
                }
                if (Gdx.input.isKeyPressed(Input.Keys.S)) {
                    velocity.y = (velocity.y > 0) ? 0 : -speed;
                    if (!isMoving) lastDirection = "down";
                    isMoving = true;
                }
                if (Gdx.input.isKeyPressed(Input.Keys.A)) {
                    velocity.x = -speed;
                    lastDirection = "left";
                    isMoving = true;
                }
                if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                    velocity.x = (velocity.x < 0) ? 0 : speed;
                    if (!isMoving) lastDirection = "right";
                    isMoving = true;
                }

                if (isMoving) {
                    currentAnimation = "walk_" + lastDirection;
                    velocity.nor().scl(speed);
                } else {
                    currentAnimation = "idle_" + lastDirection;
                }

                if (Gdx.input.isKeyJustPressed(Input.Keys.J)) {
                    currentAnimation = "attack_" + lastDirection;
                    isAttacking = true;
                    playerStateTime = 0f;
                    velocity.set(0, 0);
                }
            } else {
                velocity.set(0, 0);
                Animation<TextureRegion> currentAnim = getCurrentAnimation();
                if (currentAnim != null && currentAnim.isAnimationFinished(playerStateTime)) {
                    isAttacking = false;
                    currentAnimation = "idle_" + lastDirection;
                }
            }
        }
        playerBody.setLinearVelocity(velocity);
    }

    /**
     * 获取当前动画
     * @return 当前播放的动画
     */
    public Animation<TextureRegion> getCurrentAnimation() {
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

    /**
     * 获取当前动画帧
     * @param stateTime 动画状态时间
     * @return 当前帧
     */
    public TextureRegion getCurrentFrame(float stateTime) {
        Animation<TextureRegion> anim = getCurrentAnimation();
        return isAttacking ? anim.getKeyFrame(stateTime, false) : anim.getKeyFrame(stateTime, true);
    }

    public Body getPlayerBody() { return playerBody; }
    public void setPlayerBody(Body playerBody) {
        this.playerBody = playerBody;
    }
    public float getPlayerStateTime() { return playerStateTime; }
    public void setPlayerStateTime(float playerStateTime) { this.playerStateTime = playerStateTime; }
}
