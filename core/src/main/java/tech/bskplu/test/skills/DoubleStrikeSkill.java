package tech.bskplu.test.skills;
import tech.bskplu.test.characters.Character;

/**
 * @ClassName: DoubleStrikeSkill
 * @Description: 双连击
 * @Author BsKPLu
 * @Date 2025/4/19
 * @Version 1.1
 */
public class DoubleStrikeSkill implements Skill{
    @Override
    public void use(Character user, Character target) {
        int damage = user.getAttack() * 2; // Double damage
        target.takeDamage(damage);
        target.applyEffect(new BleedEffect(3, 5));
    }

    @Override
    public String getName() {
        return "Double Strike";
    }
}
