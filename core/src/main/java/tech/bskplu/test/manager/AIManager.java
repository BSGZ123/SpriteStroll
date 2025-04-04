package tech.bskplu.test.manager;
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder;
import tech.bskplu.test.ai.GameTiledGraph;
import tech.bskplu.test.ai.GameTiledHeuristic;
import tech.bskplu.test.ai.GameTiledNode;
/**
 * @ClassName: AIManager
 * @Description: 管理 AI 相关的路径查找和导航逻辑
 * @Author BsKPLu
 * @Date 2025/4/3
 * @Version 1.1
 */
public class AIManager {
    private GameTiledGraph tiledGraph;// 导航图
    private IndexedAStarPathFinder<GameTiledNode> pathFinder;// A* 寻路器
    private GameTiledHeuristic heuristic;// 启发式函数

    /**
     * 构造函数：初始化 AI 管理器
     * @param tiledGraph 导航图
     */
    public AIManager(GameTiledGraph tiledGraph) {
        this.tiledGraph = tiledGraph;
        heuristic = new GameTiledHeuristic();
        pathFinder = new IndexedAStarPathFinder<>(tiledGraph, true);
    }

    public IndexedAStarPathFinder<GameTiledNode> getPathFinder() { return pathFinder; }

    public GameTiledGraph getTiledGraph() {
        return tiledGraph;
    }
}
