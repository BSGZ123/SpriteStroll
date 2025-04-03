package tech.bskplu.test.manager;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
/**
 * @ClassName: WorldManager
 * @Description: 管理游戏世界，包括物理模拟、边界和地面
 * @Author BsKPLu
 * @Date 2025/4/3
 * @Version 1.1
 */
public class WorldManager {
    private World world;                       // Box2D 物理世界
    private Array<Body> groundBodies = new Array<>(); // 地面物体列表
    private static final float PIXELS_PER_METER = 32f; // 像素到米的转换比例
    private static final float GAME_WIDTH_METERS = 800f / PIXELS_PER_METER; // 游戏宽度 (米)
    private static final float GAME_HEIGHT_METERS = 600f / PIXELS_PER_METER; // 游戏高度 (米)

    /**
     * 构造函数：初始化物理世界并创建边界和地面
     */
    public WorldManager() {
        world = new World(new Vector2(0, 0), true);
        createBoundaries();
        createGround();
    }

    /**
     * 创建游戏边界
     */
    private void createBoundaries() {
        float wallThickness = 0.5f;
        BodyDef boundaryBodyDef = new BodyDef();
        boundaryBodyDef.type = BodyDef.BodyType.StaticBody;
        PolygonShape boundaryShape = new PolygonShape();
        FixtureDef boundaryFixtureDef = new FixtureDef();
        boundaryFixtureDef.shape = boundaryShape;
        boundaryFixtureDef.friction = 0.4f;

        boundaryBodyDef.position.set(GAME_WIDTH_METERS / 2f, -wallThickness / 2f);
        boundaryShape.setAsBox(GAME_WIDTH_METERS / 2f, wallThickness / 2f);
        world.createBody(boundaryBodyDef).createFixture(boundaryFixtureDef).setUserData("boundary_bottom");

        boundaryBodyDef.position.set(GAME_WIDTH_METERS / 2f, GAME_HEIGHT_METERS + wallThickness / 2f);
        boundaryShape.setAsBox(GAME_WIDTH_METERS / 2f, wallThickness / 2f);
        world.createBody(boundaryBodyDef).createFixture(boundaryFixtureDef).setUserData("boundary_top");

        boundaryBodyDef.position.set(-wallThickness / 2f, GAME_HEIGHT_METERS / 2f);
        boundaryShape.setAsBox(wallThickness / 2f, GAME_HEIGHT_METERS / 2f);
        world.createBody(boundaryBodyDef).createFixture(boundaryFixtureDef).setUserData("boundary_left");

        boundaryBodyDef.position.set(GAME_WIDTH_METERS + wallThickness / 2f, GAME_HEIGHT_METERS / 2f);
        boundaryShape.setAsBox(wallThickness / 2f, GAME_HEIGHT_METERS / 2f);
        world.createBody(boundaryBodyDef).createFixture(boundaryFixtureDef).setUserData("boundary_right");

        boundaryShape.dispose();
    }

    /**
     * 创建随机地面物块
     */
    private void createGround() {
        float margin = 1.0f;
        float minX = margin;
        float maxX = GAME_WIDTH_METERS - margin;
        float minY = margin;
        float maxY = GAME_HEIGHT_METERS - margin;

        for (int i = 0; i < 10; i++) {
            float maxWidth = (maxX - minX) / 2;
            float maxHeight = (maxY - minY) / 2;
            float halfWidth = MathUtils.random(25 / PIXELS_PER_METER, Math.min(75 / PIXELS_PER_METER, maxWidth));
            float halfHeight = MathUtils.random(10 / PIXELS_PER_METER, Math.min(35 / PIXELS_PER_METER, maxHeight));
            float x = MathUtils.random(minX + halfWidth, maxX - halfWidth);
            float y = MathUtils.random(minY + halfHeight, maxY - halfHeight);

            BodyDef groundBodyDef = new BodyDef();
            groundBodyDef.type = BodyDef.BodyType.StaticBody;
            groundBodyDef.position.set(x, y);
            Body groundBody = world.createBody(groundBodyDef);
            PolygonShape groundBox = new PolygonShape();
            groundBox.setAsBox(halfWidth, halfHeight);
            groundBody.createFixture(groundBox, 0.0f).setUserData("ground");
            groundBodies.add(groundBody);
            groundBox.dispose();
        }
    }

    public World getWorld() { return world; }
    public Array<Body> getGroundBodies() { return groundBodies; }
}
