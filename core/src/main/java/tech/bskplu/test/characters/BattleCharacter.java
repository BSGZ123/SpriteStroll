package tech.bskplu.test.characters;

import com.badlogic.gdx.physics.box2d.Body;

/**
 * @ClassName: BattleCharacter
 * @Description: 角色基类
 * @Author BsKPLu
 * @Date 2025/4/10
 * @Version 1.1
 */

public class BattleCharacter {
    private float health = 100f;
    private boolean isDefending = false;
    private boolean isBleeding = false;
    private Body body;
    private String name;

    public BattleCharacter(String name, Body body) {
        this.name = name;
        this.body = body;
    }

    public float getHealth() { return health; }
    public void setHealth(float health) { this.health = health; }
    public boolean isDefending() { return isDefending; }
    public void setDefending(boolean defending) { this.isDefending = defending; }
    public boolean isBleeding() { return isBleeding; }
    public void setBleeding(boolean bleeding) { isBleeding = bleeding; }
    public Body getBody() { return body; }
    public String getName() { return name; }
}
