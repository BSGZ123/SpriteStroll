package tech.bskplu.test.manager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

import static tech.bskplu.test.TGameScreen.PIXELS_PER_METER;

/**
 * @ClassName: UIManager
 * @Description: 管理 UI 元素，如血条和消息
 * @Author BsKPLu
 * @Date 2025/4/3
 * @Version 1.1
 */
public class UIManager {
    private ShapeRenderer shapeRenderer;// 用于绘制血条
    private BitmapFont font;// 用于绘制文字
    private BitmapFont messageFont;// 用于绘制消息

    /**
     * 构造函数：初始化渲染器和字体
     */
    public UIManager() {
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();
        messageFont = new BitmapFont();
        messageFont.setColor(Color.YELLOW);
        messageFont.getData().setScale(2f);
    }

    /**
     * 绘制血条
     * @param batch SpriteBatch 用于绘制文字
     * @param camera 摄像机
     * @param position 实体位置
     * @param health 当前血量
     * @param maxHealth 最大血量
     * @param width 实体宽度
     * @param height 实体高度
     * @param offsetX X 偏移
     * @param offsetY Y 偏移
     */
    public void drawHealthBar(SpriteBatch batch,
                              OrthographicCamera camera,
                              Vector2 position,
                              float health,
                              float maxHealth,
                              float width,
                              float height,
                              float offsetX,
                              float offsetY) {
        // ShapeRenderer 和 SpriteBatch 需要使用相同的投影矩阵
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        float barWidth = 40;
        float barHeight = 5;
        float barX = (position.x * PIXELS_PER_METER) - width / 2f + offsetX;
        float barY = (position.y * PIXELS_PER_METER) - height / 2f + offsetY;

        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(barX, barY, barWidth, barHeight);

        float healthPercentage = health / maxHealth;
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(barX, barY, barWidth * healthPercentage, barHeight);
        shapeRenderer.end();

        //Gdx.app.log("HealthBar", "position: " + position.x + ", " + position.y + ", width: " + width + ", height: " + height);
        batch.begin();
        font.setColor(Color.WHITE);
        font.draw(batch, String.format("%.0f%%", healthPercentage * 100), barX + barWidth + 5, barY + barHeight);
        batch.end();
    }

    /**
     * 绘制消息
     * @param batch SpriteBatch 用于绘制
     * @param message 消息内容
     * @param x X 坐标
     * @param y Y 坐标
     */
    public void drawMessage(SpriteBatch batch, String message, float x, float y) {
        batch.begin();
        messageFont.draw(batch, message, x, y);
        batch.end();
    }

    /**
     * 释放资源
     */
    public void dispose() {
        shapeRenderer.dispose();
        font.dispose();
        messageFont.dispose();
    }
}
