package tech.bskplu.test;


import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * @ClassName: TransitionScreen
 * @Description: 过渡场景
 * @Author BsKPLu
 * @Date 2025/3/15
 * @Version 1.1
 */
public class TransitionScreen implements Screen {
    private final Game game;
    private SpriteBatch batch;
    private Texture image;
    private float elapsedTime;

    public TransitionScreen(Game game) {
        this.game = game;
        batch = new SpriteBatch();
        image = new Texture(Gdx.files.internal("WhiteFoxSex1.png"));
        elapsedTime = 0f;
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        elapsedTime += delta;

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        batch.draw(image, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.end();

        // 延时3秒后切换到战斗场景
        if (elapsedTime >= 3f) {
            game.setScreen(new GameScreen(game));
        }
    }

    @Override
    public void resize(int width, int height) {
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
        image.dispose();
    }
}
