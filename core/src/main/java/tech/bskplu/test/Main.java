package tech.bskplu.test;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    private SpriteBatch batch;
    private Texture image;
    private Music music;


    @Override
    public void create() {
        batch = new SpriteBatch();
        image = new Texture("WhiteFoxSex.png");
        music = Gdx.audio.newMusic(Gdx.files.internal("LanTingXu.mp3"));
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        batch.begin();
        batch.draw(image, 0, 0);
        batch.end();
        music.setVolume(0.3f);
        music.play();
    }

    @Override
    public void dispose() {
        batch.dispose();
        image.dispose();
    }
}
