package tech.bskplu.test.skills;

import tech.bskplu.test.characters.Character;
/**
 * @ClassName: AttackSkill
 * @Description: 普通攻击
 * @Author BsKPLu
 * @Date 2025/4/19
 * @Version 1.1
 */
public class AttackSkill implements Skill{
    @Override
    public void use(Character user, Character target) {
        int damage = user.getAttack();
        target.takeDamage(damage);
        System.out.println(user.getClass().getSimpleName() + " uses Attack on " + target.getClass().getSimpleName() + ", dealing " + damage + " damage.");
    }

    @Override
    public String getName() {
        return "Attack";
    }
}
