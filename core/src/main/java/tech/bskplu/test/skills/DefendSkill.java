package tech.bskplu.test.skills;

import tech.bskplu.test.characters.Character;

/**
 * @ClassName: DefendSkill
 * @Description: 防御
 * @Author BsKPLu
 * @Date 2025/4/19
 * @Version 1.1
 */
public class DefendSkill implements Skill{
    @Override
    public void use(Character user, Character target) {
        user.setDefense(user.getDefense() + 10);
        System.out.println(user.getClass().getSimpleName() + " uses Defend, increasing defense by 10.");
    }

    @Override
    public String getName() {
        return "Defend";
    }
}
