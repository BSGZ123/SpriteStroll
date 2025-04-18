package tech.bskplu.test.skills;

/**
 * @InterfaceName: Effect
 * @Description: 技能效果
 * @Author BsKPLu
 * @Date 2025/4/19
 * @Version 1.1
 */
public interface Effect {
    void apply(Character character);
    boolean isExpired();
}
