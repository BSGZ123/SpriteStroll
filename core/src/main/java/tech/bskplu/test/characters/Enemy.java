package tech.bskplu.test.characters;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.pfa.DefaultGraphPath;
import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import tech.bskplu.test.ai.GameTiledGraph;
import tech.bskplu.test.ai.GameTiledHeuristic;
import tech.bskplu.test.ai.GameTiledNode;
/**
 * @ClassName: Enemy
 * @Description: 管理敌人逻辑，包括 AI 行为、移动和动画
 * @Author BsKPLu
 * @Date 2025/4/3
 * @Version 1.1
 */
public class Enemy extends Character{
    private Body enemyBody;// 敌人的物理身体
    private Texture guanPinMovTexture;// 敌人移动纹理
    private Texture guanPinAtkTexture;// 敌人攻击纹理
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
    private float enemyStateTime = 0f;// 动画状态时间
    private String enemyCurrentAnimation = "idle_down";// 当前动画状态
    private String enemyLastDirection = "down";// 最后移动方向
    private GameTiledGraph tiledGraph;// 导航图
    private IndexedAStarPathFinder<GameTiledNode> pathFinder;// A* 寻路器
    private GraphPath<GameTiledNode> enemyPath;// 敌人路径
    private float pathUpdateTimer = 0f;// 路径更新计决定了敌人路径更新间隔
    private static final float PATH_UPDATE_INTERVAL = 0.5f;// 路径更新间隔 (秒)
    private int currentPathIndex = 0;// 当前路径索引
    private Vector2 enemyTargetWorldPos = new Vector2();// 目标世界坐标
    private static final float ENEMY_FOLLOW_SPEED = 1.5f;// 敌人跟随速度 (米/秒)
    private static final float ENEMY_ARRIVAL_TOLERANCE = 0.2f;// 到达容忍距离 (米)
    private static final float PLAYER_CATCH_DISTANCE = 1.1f;// 抓住玩家的距离 (米)
    private boolean playerCaught = false;// 是否抓住玩家
    private float gameOverTimer = 0f;// 游戏结束计时器
    private static final float GAME_OVER_DELAY = 3.0f;// 游戏结束延迟 (秒)

    /**
     * 构造函数：初始化敌人并创建物理身体和动画
     * @param world Box2D 物理世界
     * @param tiledGraph 导航图
     * @param pathFinder A* 寻路器
     */
    public Enemy(World world, GameTiledGraph tiledGraph, IndexedAStarPathFinder<GameTiledNode> pathFinder) {
        super(world);
        this.tiledGraph = tiledGraph;
        this.pathFinder = pathFinder;
        enemyPath = new DefaultGraphPath<>();
        float margin = 2.0f;
        float randomX = MathUtils.random(margin, 800 / 32f - margin);
        float randomY = MathUtils.random(margin, 600 / 32f - margin);
        body.setTransform(randomX, randomY, 0);
        body.getFixtureList().get(0).setUserData("enemy");
        createGuanPin(world);
    }

    /**
     * 创建敌人角色及其动画和物理身体
     * @param world Box2D 物理世界
     */
    private void createGuanPin(World world) {
        guanPinMovTexture = new Texture(Gdx.files.internal("Mov_GuanPin.png"));
        int movFrameHeight = guanPinMovTexture.getHeight() / 11;
        int movFrameWidth = guanPinMovTexture.getWidth();
        TextureRegion[][] movTmp = TextureRegion.split(guanPinMovTexture, movFrameWidth, movFrameHeight);

        // 行走动画
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

        // Idle 动画
        TextureRegion[] idleDownFrames = {movTmp[6][0]};
        guanPinIdleDownAnimation = new Animation<>(0.2f, idleDownFrames);

        TextureRegion[] idleUpFrames = {movTmp[7][0]};
        guanPinIdleUpAnimation = new Animation<>(0.2f, idleUpFrames);

        TextureRegion[] idleLeftFrames = {movTmp[8][0]};
        guanPinIdleLeftAnimation = new Animation<>(0.2f, idleLeftFrames);

        TextureRegion[] idleRightFrames = {new TextureRegion(movTmp[8][0])};
        idleRightFrames[0].flip(true, false);
        guanPinIdleRightAnimation = new Animation<>(0.2f, idleRightFrames);

        // 攻击动画
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

        // 创建物理身体
        BodyDef enemyBodyDef = new BodyDef();
        enemyBodyDef.type = BodyDef.BodyType.DynamicBody;
        float margin = 2.0f;
        float randomX = MathUtils.random(margin, 800 / 32f - margin);
        float randomY = MathUtils.random(margin, 600 / 32f - margin);
        enemyBodyDef.position.set(randomX, randomY);
        enemyBody = world.createBody(enemyBodyDef);
        CircleShape enemyShape = new CircleShape();
        enemyShape.setRadius(16f / 32f);
        FixtureDef enemyFixtureDef = new FixtureDef();
        enemyFixtureDef.shape = enemyShape;
        enemyFixtureDef.density = 1f;
        enemyFixtureDef.friction = 0.4f;
        enemyFixtureDef.restitution = 0.1f;
        enemyBody.createFixture(enemyFixtureDef).setUserData("enemy");
        enemyShape.dispose();
    }

    /**
     * 更新敌人 AI，包括路径查找和移动
     * @param delta 时间增量
     * @param playerBody 玩家物理身体
     */
    public void updateEnemyAI(float delta, Body playerBody) {
        if (playerCaught || enemyBody == null || playerBody == null) {
            if (enemyBody != null) enemyBody.setLinearVelocity(0, 0);
            return;
        }

        float distanceToPlayer = enemyBody.getPosition().dst(playerBody.getPosition());
        if (distanceToPlayer <= PLAYER_CATCH_DISTANCE) {
            playerCaught = true;
            gameOverTimer = GAME_OVER_DELAY;
            Gdx.app.log("AI", "Player Caught!");
            enemyBody.setLinearVelocity(0, 0);
            return;
        }

        pathUpdateTimer -= delta;
        if (pathUpdateTimer <= 0f) {
            pathUpdateTimer = PATH_UPDATE_INTERVAL;
            updateEnemyPath(playerBody);
        }
        followPath(delta);
        updateEnemyAnimation();
    }

    /**
     * 更新敌人路径
     * @param playerBody 玩家物理身体
     */
    private void updateEnemyPath(Body playerBody) {
        Vector2 enemyPos = enemyBody.getPosition();
        Vector2 playerPos = playerBody.getPosition();
        int startX = tiledGraph.worldToTileX(enemyPos.x);
        int startY = tiledGraph.worldToTileY(enemyPos.y);
        int endX = tiledGraph.worldToTileX(playerPos.x);
        int endY = tiledGraph.worldToTileY(playerPos.y);

        GameTiledNode startNode = tiledGraph.getNode(startX, startY);
        GameTiledNode endNode = tiledGraph.getNode(endX, endY);

        if (startNode != null && startNode.type != GameTiledGraph.TILE_WALL &&
            endNode != null && endNode.type != GameTiledGraph.TILE_WALL) {
            enemyPath.clear();
            pathFinder.searchNodePath(startNode, endNode, new GameTiledHeuristic(), enemyPath);
            currentPathIndex = 0;
            if (enemyPath.getCount() > 1) {
                currentPathIndex = 1;
                GameTiledNode nextNode = enemyPath.get(currentPathIndex);
                tiledGraph.tileToWorldCenter(nextNode.x, nextNode.y, enemyTargetWorldPos);
            } else {
                currentPathIndex = 0;
                enemyPath.clear();
            }
        } else {
            enemyPath.clear();
            currentPathIndex = 0;
            Gdx.app.log("AI", "Path calculation failed: Invalid start/end node or wall.");
        }
    }

    /**
     * 跟随计算出的路径移动
     * @param delta 时间增量
     */
    private void followPath(float delta) {
        if (enemyPath.getCount() == 0 || currentPathIndex >= enemyPath.getCount()) {
            enemyBody.setLinearVelocity(0, 0);
            return;
        }

        Vector2 enemyPos = enemyBody.getPosition();
        float distanceToTarget = enemyPos.dst(enemyTargetWorldPos);
        if (distanceToTarget <= ENEMY_ARRIVAL_TOLERANCE) {
            currentPathIndex++;
            if (currentPathIndex >= enemyPath.getCount()) {
                enemyBody.setLinearVelocity(0, 0);
                return;
            } else {
                GameTiledNode nextNode = enemyPath.get(currentPathIndex);
                tiledGraph.tileToWorldCenter(nextNode.x, nextNode.y, enemyTargetWorldPos);
            }
        }

        Vector2 direction = GameTiledGraph.tmpVec.set(enemyTargetWorldPos).sub(enemyPos).nor();
        enemyBody.setLinearVelocity(direction.scl(ENEMY_FOLLOW_SPEED));
    }

    /**
     * 更新敌人动画状态
     */
    private void updateEnemyAnimation() {
        if (enemyBody == null) return;
        Vector2 velocity = enemyBody.getLinearVelocity();
        boolean isMoving = !velocity.isZero(0.1f);

        if (isMoving) {
            if (Math.abs(velocity.x) > Math.abs(velocity.y)) {
                enemyLastDirection = (velocity.x > 0) ? "right" : "left";
            } else {
                enemyLastDirection = (velocity.y > 0) ? "up" : "down";
            }
            enemyCurrentAnimation = "walk_" + enemyLastDirection;
        } else {
            enemyCurrentAnimation = "idle_" + enemyLastDirection;
        }
    }

    /**
     * 获取当前动画帧
     * @param stateTime 动画状态时间
     * @return 当前帧
     */
    public TextureRegion getEnemyCurrentFrame(float stateTime) {
        Animation<TextureRegion> anim = switch (enemyCurrentAnimation) {
            case "walk_down" -> guanPinWalkDownAnimation;
            case "walk_up" -> guanPinWalkUpAnimation;
            case "walk_left" -> guanPinWalkLeftAnimation;
            case "walk_right" -> guanPinWalkRightAnimation;
            case "idle_down" -> guanPinIdleDownAnimation;
            case "idle_up" -> guanPinIdleUpAnimation;
            case "idle_left" -> guanPinIdleLeftAnimation;
            case "idle_right" -> guanPinIdleRightAnimation;
            default -> guanPinIdleDownAnimation;
        };
        return anim.getKeyFrame(stateTime, true);
    }

    public Body getEnemyBody() { return enemyBody; }
    public void setEnemyBody(Body enemyBody) {
        this.enemyBody = enemyBody;
    }
    public float getEnemyStateTime() { return enemyStateTime; }
    public void setEnemyStateTime(float enemyStateTime) { this.enemyStateTime = enemyStateTime; }
    public boolean isPlayerCaught() { return playerCaught; }
    public float getGameOverTimer() { return gameOverTimer; }
    public void setGameOverTimer(float gameOverTimer) { this.gameOverTimer = gameOverTimer; }

    public void startAttackAnimation(String direction) {
        enemyCurrentAnimation = "attack_" + direction;
        enemyStateTime = 0f;
    }

    public void updateAnimation(float delta) {
        Animation<TextureRegion> currentAnim = getCurrentAnimation();
        if (currentAnim != null && currentAnim.isAnimationFinished(enemyStateTime)) {
            enemyCurrentAnimation = "idle_" + enemyLastDirection;
        }
    }

    public String getEnemyLastDirection() {
        return enemyLastDirection;
    }

    private Animation<TextureRegion> getCurrentAnimation() {
        return switch (enemyCurrentAnimation) {
            case "walk_down" -> guanPinWalkDownAnimation;
            case "walk_up" -> guanPinWalkUpAnimation;
            case "walk_left" -> guanPinWalkLeftAnimation;
            case "walk_right" -> guanPinWalkRightAnimation;
            case "idle_down" -> guanPinIdleDownAnimation;
            case "idle_up" -> guanPinIdleUpAnimation;
            case "idle_left" -> guanPinIdleLeftAnimation;
            case "idle_right" -> guanPinIdleRightAnimation;
            case "attack_down" -> guanPinAttackDownAnimation;
            case "attack_up" -> guanPinAttackUpAnimation;
            case "attack_left" -> guanPinAttackLeftAnimation;
            case "attack_right" -> guanPinAttackRightAnimation;
            default -> guanPinIdleDownAnimation;
        };
    }
}
