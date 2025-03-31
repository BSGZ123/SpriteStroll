package tech.bskplu.test.ai;

import com.badlogic.gdx.ai.pfa.Heuristic;

/**
 * @ClassName: GameTiledHeuristic
 * @Description: 启发函数
 * @Author BsKPLu
 * @Date 2025/3/27
 * @Version 1.1
 */
public class GameTiledHeuristic implements Heuristic<GameTiledNode> {

    @Override
    public float estimate(GameTiledNode node, GameTiledNode endNode) {
        // 使用曼哈顿距离作为启发式估计
        return Math.abs(node.x - endNode.x) + Math.abs(node.y - endNode.y);

    }
}
