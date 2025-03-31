package tech.bskplu.test;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * @ClassName: StartScreen
 * @Description: 开始游戏场景
 * @Author BsKPLu
 * @Date 2025/3/15
 * @Version 1.1
 */
public class StartScreen implements Screen {
    private final Game game;
    private Stage stage;
    private Texture groundTexture;
    private SpriteBatch batch;
    private Array<Block> backgroundBlocks;

    // 淡色图块类
    private static class Block {
        float x, y, width, height;
        Color color;

        Block(float x, float y, float width, float height, Color color) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.color = color;
        }
    }

    public StartScreen(Game game) {
        this.game = game;
        groundTexture = new Texture("ground.png");
        stage = new Stage(new ScreenViewport());
        batch = new SpriteBatch();

        // 创建淡色背景图块
        backgroundBlocks = new Array<>();
        for (int i = 0; i < 20; i++) {
            float x = MathUtils.random(0, Gdx.graphics.getWidth());
            float y = MathUtils.random(0, Gdx.graphics.getHeight());
            float width = MathUtils.random(50, 150);
            float height = MathUtils.random(50, 150);
            Color color = new Color(MathUtils.random(0.5f, 1f), MathUtils.random(0.5f, 1f), MathUtils.random(0.5f, 1f), 0.5f);
            backgroundBlocks.add(new Block(x, y, width, height, color));
        }

        // 创建“开始游戏”按钮
        Texture startButtonTexture = new Texture(Gdx.files.internal("StartCircleButton2.png"));
        ImageButton startButton = new ImageButton(new TextureRegionDrawable(startButtonTexture));
        startButton.setPosition(Gdx.graphics.getWidth() / 2f - startButton.getWidth() / 2, Gdx.graphics.getHeight() / 2f);
        startButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new TransitionScreen(game));
            }
        });

        // 创建“退出游戏”按钮
        Texture exitButtonTexture = new Texture(Gdx.files.internal("ExitCircleButton2.png"));
        ImageButton exitButton = new ImageButton(new TextureRegionDrawable(exitButtonTexture));
        exitButton.setPosition(Gdx.graphics.getWidth() / 2f - exitButton.getWidth() / 2, Gdx.graphics.getHeight() / 2f - 100);
        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();

            }
        });

        stage.addActor(startButton);
        stage.addActor(exitButton);

        Gdx.app.log("StartScreen", "Start Button Position: " + startButton.getX() + ", " + startButton.getY());
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);// 设置输入处理器为Stage
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // 绘制背景图块
        batch.begin();
        for (Block block : backgroundBlocks) {
            batch.setColor(block.color);
            batch.draw(groundTexture, block.x, block.y, block.width, block.height);// 使用ground.png作为淡色图块
            batch.setColor(Color.WHITE);// 重置颜色
        }
        batch.end();

        // 绘制按钮
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);// 清空输入处理器
    }

    @Override
    public void dispose() {
        stage.dispose();
        batch.dispose();
        groundTexture.dispose();
    }
}
