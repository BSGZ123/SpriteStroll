package tech.bskplu.test.skills;

import tech.bskplu.test.characters.BattleCharacter;

/**
 * @InterfaceName: Skill
 * @Description: 技能接口
 * @Author BsKPLu
 * @Date 2025/4/10
 * @Version 1.1
 */
public interface ISkill {
    void execute(BattleCharacter attacker, BattleCharacter target);
    String getDescription();
}
