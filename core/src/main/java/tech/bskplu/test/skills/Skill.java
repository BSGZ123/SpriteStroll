package tech.bskplu.test.skills;

/**
 * @InterfaceName: Skill
 * @Description: 技能接口
 * @Author BsKPLu
 * @Date 2025/4/19
 * @Version 1.1
 */
public interface Skill {
    void use(Character user, Character target);
    String getName();
}
