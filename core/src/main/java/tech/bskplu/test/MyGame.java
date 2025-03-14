package tech.bskplu.test;

import com.badlogic.gdx.Game;

/**
 * @ClassName: MyGame
 * @Description: 游戏主类
 * @Author BsKPLu
 * @Date 2025/3/15
 * @Version 1.1
 */
public class MyGame extends Game{
    @Override
    public void create() {
        setScreen(new StartScreen(this)); // 默认进入开始游戏场景
    }
}
