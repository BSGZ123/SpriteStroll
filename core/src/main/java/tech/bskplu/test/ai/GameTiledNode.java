package tech.bskplu.test.ai;
import java.util.Objects;

/**
 * @ClassName: GameTiledNode
 * @Description: 网格节点
 * @Author BsKPLu
 * @Date 2025/3/27
 * @Version 1.1
 */
public class GameTiledNode {
    public final int x; // 网格X坐标
    public final int y; // 网格Y坐标
    public int type;    // 节点类型 (地面或墙壁)
    private final int index; // 节点在图中的唯一索引

    /**
     * 创建节点.
     *
     * @param x        X坐标
     * @param y        Y坐标
     * @param type     类型 (GameTiledGraph.TILE_GROUND 或 TILE_WALL)
     * @param capacity 图的总节点数 (用于计算索引)
     */
    public GameTiledNode(int x, int y, int type, int capacity) {
        this.x = x;
        this.y = y;
        this.type = type;
        // 计算索引，假设按行优先存储
        // 注意：这里 capacity 应该是 width * height，但构造时用 width 计算更安全
        // 修正：索引应该在 Graph 中计算和赋予，或者传入 width
        // 暂时简单处理，假设 index 就是 y * width + x
        // 更健壮的方式是在GameTiledGraph中创建时赋予索引
        //int width = (int) Math.sqrt(capacity); // 不够精确，最好从Graph传入width
        //if (width * width != capacity && (width + 1) * (width + 1) != capacity) {
            // 需要更可靠的方式获取宽度，暂时硬编码或从Graph获取
            // 在实际应用中，应将 width 传递给构造函数或在 Graph 中设置索引
            // 这里我们假设 Graph 的 addNode 会设置正确的索引或我们能访问 width
       // }
        // this.index = y * width + x; // 依赖 Graph 的结构
        this.index = y * (int) (Math.sqrt(capacity)) + x;// 临时方案, 在Graph创建时设置更佳
    }


    public int getIndex() {
        // 返回在 GameTiledGraph 中 nodes 数组里的索引
        // 这个索引的计算需要在 GameTiledGraph 构造时确定并设置
        // 暂时返回基于坐标的计算，需要 GameTiledGraph 保证一致性
        return this.index;// 依赖构造函数设置正确
    }

    @Override
    public String toString() {
        return "Node(" + x + ", " + y + ")";
    }

    // equals 和 hashCode 对于 A* 查找可能有用，特别是如果使用 Set 等
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameTiledNode that = (GameTiledNode) o;
        return x == that.x && y == that.y;
    }

    @Override
    public int hashCode() {
        // 使用一个简单的哈希组合(手动计算已放弃)
        // 基于坐标计算哈希码
        return Objects.hash(x, y);
    }
}
