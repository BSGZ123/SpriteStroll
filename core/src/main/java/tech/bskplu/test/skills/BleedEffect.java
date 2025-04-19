package tech.bskplu.test.skills;
import tech.bskplu.test.characters.Character;

/**
 * @ClassName: BleedEffect
 * @Description: 流血效果
 * @Author BsKPLu
 * @Date 2025/4/19
 * @Version 1.1
 */
public class BleedEffect implements Effect{
    private int duration;
    private int damagePerTurn;

    public BleedEffect(int duration, int damagePerTurn) {
        this.duration = duration;
        this.damagePerTurn = damagePerTurn;
    }

    @Override
    public void apply(Character character) {
        character.takeDamage(damagePerTurn);
        duration--;
    }

    @Override
    public boolean isExpired() {
        return duration <= 0;
    }
}
