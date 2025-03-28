package tech.bskplu.test.ai;

import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.ai.pfa.DefaultConnection;
import com.badlogic.gdx.ai.pfa.indexed.IndexedGraph;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.Array;

/**
 * @ClassName: GameTiledGraph
 * @Description: 游戏世界的导航图
 * @Author BsKPLu
 * @Date 2025/3/27
 * @Version 1.1
 */
public class GameTiledGraph implements IndexedGraph<GameTiledNode> {

    // 节点类型
    public static final int TILE_GROUND = 0;
    public static final int TILE_WALL = 1;

    private final int width;// 图的宽度 (网格数)
    private final int height;// 图的高度 (网格数)
    private final float tileSize;// 每个网格的大小 (米)

    private final Array<GameTiledNode> nodes;// 所有节点的列表 (用于索引访问)

    // 临时变量，避免重复创建
    private final Array<Connection<GameTiledNode>> connectionsTemp = new Array<>();
    public static final Vector2 tmpVec = new Vector2();// 用于计算AABB

    /**
     * 创建导航图.
     * @param worldWidth 世界宽度 (米)
     * @param worldHeight 世界高度 (米)
     * @param tileSize 每个网格的大小 (米)
     * @param obstacles 网格物列表 (Box2D Bodies)
     */
    public GameTiledGraph(float worldWidth, float worldHeight, float tileSize, Array<Body> obstacles) {
        this.width = (int) (worldWidth / tileSize);
        this.height = (int) (worldHeight / tileSize);
        this.tileSize = tileSize;
        this.nodes = new Array<>(width * height);

        // 1. 创建所有节点，初始为地面
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                nodes.add(new GameTiledNode(x, y, TILE_GROUND, width * height));
            }
        }

        // 2. 标记网格物覆盖的节点为墙壁
        markObstacles(obstacles);

        // 3. (可选) 建立节点间的连接 (A*会动态查询邻居) 放弃
        // 如果需要，可以在这里预计算连接，但 IndexedAStarPathFinder 会调用 getConnections
    }

    private void markObstacles(Array<Body> obstacles) {
        Rectangle obstacleRect = new Rectangle();// 用于存储网格物的包围盒
        for (Body obstacleBody : obstacles) {
            if (obstacleBody.getFixtureList().isEmpty()) continue;

            Fixture fixture = obstacleBody.getFixtureList().get(0);// 假设只有一个Fixture
            if (!(fixture.getShape() instanceof PolygonShape)) continue;// 只处理矩形网格物

            PolygonShape shape = (PolygonShape) fixture.getShape();
            // 获取网格物在世界坐标系中的AABB (近似)
            // Box2D的setAsBox用的是半宽半高，需要计算实际顶点
            Vector2 vertex0 = new Vector2();
            Vector2 vertex1 = new Vector2();
            shape.getVertex(0, vertex0);// 左下角相对中心
            shape.getVertex(2, vertex1);// 右上角相对中心

            // 转换到世界坐标
            Vector2 worldPos = obstacleBody.getPosition();
            float minX = worldPos.x + vertex0.x;
            float minY = worldPos.y + vertex0.y;
            float maxX = worldPos.x + vertex1.x;
            float maxY = worldPos.y + vertex1.y;

            obstacleRect.set(minX, minY, maxX - minX, maxY - minY);

            // 找出被这个矩形覆盖的网格范围
            int startX = worldToTileX(minX);
            int endX = worldToTileX(maxX);
            int startY = worldToTileY(minY);
            int endY = worldToTileY(maxY);

            // 将范围内的网格标记为墙壁
            for (int y = startY; y <= endY; y++) {
                for (int x = startX; x <= endX; x++) {
                    if (isValidTile(x, y)) {
                        GameTiledNode node = getNode(x, y);
                        if (node != null) {
                            node.type = TILE_WALL;
                        }
                    }
                }
            }
        }
    }

    /**
     * 获取指定网格坐标的节点.
     * @param x 网格X坐标
     * @param y 网格Y坐标
     * @return 节点，如果坐标无效则返回null
     */
    public GameTiledNode getNode(int x, int y) {
        if (!isValidTile(x, y)) {
            return null;
        }
        return nodes.get(y * width + x);
    }

    /**
     * 获取指定索引的节点.
     * @param index 节点索引
     * @return 节点
     */

    public GameTiledNode getNode(int index) {
        return nodes.get(index);
    }

    @Override
    public int getIndex(GameTiledNode node) {
        return node.getIndex();
    }

    @Override
    public int getNodeCount() {
        return nodes.size;
    }

    /**
     * 获取一个节点的所有可达邻居连接.
     * @param fromNode 起始节点
     * @return 连接列表
     */
    @Override
    public Array<Connection<GameTiledNode>> getConnections(GameTiledNode fromNode) {
        connectionsTemp.clear();
        int x = fromNode.x;
        int y = fromNode.y;

        // 定义8个方向的偏移量 (上下左右和对角线)
        int[] dx = {-1, 0, 1, -1, 1, -1, 0, 1};
        int[] dy = {-1, -1, -1, 0, 0, 1, 1, 1};

        for (int i = 0; i < 8; i++) {
            int neighborX = x + dx[i];
            int neighborY = y + dy[i];

            // 检查邻居是否有效且不是墙壁
            if (isValidTile(neighborX, neighborY)) {
                GameTiledNode neighborNode = getNode(neighborX, neighborY);
                if (neighborNode != null && neighborNode.type == TILE_GROUND) {
                    // 检查对角线移动是否穿墙 (可选但推荐)
                    if (i < 3 || i > 4) { // 如果是对角线移动 (i=0,1,2,5,6,7)
                        GameTiledNode node1 = getNode(x, neighborY);
                        GameTiledNode node2 = getNode(neighborX, y);
                        if ((node1 != null && node1.type == TILE_WALL) || (node2 != null && node2.type == TILE_WALL)) {
                            continue; // 穿墙了，不允许对角线移动
                        }
                    }
                    // 添加连接 (对角线距离为 sqrt(2) ~= 1.414, 直线距离为 1)
                    float cost = (dx[i] != 0 && dy[i] != 0) ? 1.414f : 1.0f;
                    connectionsTemp.add(new DefaultConnection<>(fromNode, neighborNode));
                }
            }
        }
        return connectionsTemp;
    }

    // --- 坐标转换和工具方法 ---

    public boolean isValidTile(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    public int worldToTileX(float worldX) {
        return Math.max(0, Math.min(width - 1, (int) (worldX / tileSize)));
    }

    public int worldToTileY(float worldY) {
        return Math.max(0, Math.min(height - 1, (int) (worldY / tileSize)));
    }

    public float tileToWorldX(int tileX) {
        return (tileX + 0.5f) * tileSize;// 返回网格中心的世界X坐标
    }

    public float tileToWorldY(int tileY) {
        return (tileY + 0.5f) * tileSize;// 返回网格中心的世界Y坐标
    }

    public Vector2 tileToWorldCenter(int tileX, int tileY, Vector2 out) {
        out.x = (tileX + 0.5f) * tileSize;
        out.y = (tileY + 0.5f) * tileSize;
        return out;
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public float getTileSize() { return tileSize; }
    public Array<GameTiledNode> getNodes() { return nodes; }
}
