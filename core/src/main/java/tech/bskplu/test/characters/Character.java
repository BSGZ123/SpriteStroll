package tech.bskplu.test.characters;

import com.badlogic.gdx.physics.box2d.*;
import tech.bskplu.test.skills.AttackSkill;
import tech.bskplu.test.skills.DefendSkill;
import tech.bskplu.test.skills.Effect;
import tech.bskplu.test.skills.Skill;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @ClassName: Character
 * @Description: 角色对战信息基类
 * @Author BsKPLu
 * @Date 2025/4/19
 * @Version 1.1
 */
public abstract class Character {
    protected Body body;
    protected int attack = 50;
    protected int defense = 5;
    protected float health = 100f;
    protected float maxHealth = 100f;
    protected List<Skill> skills = new ArrayList<>();
    protected List<Effect> effects = new ArrayList<>();

    public  Character(World world){

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(0, 0);
        this.body = world.createBody(bodyDef);
        CircleShape shape = new CircleShape();
        shape.setRadius(16f / 32f);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1f;
        fixtureDef.friction = 0.4f;
        fixtureDef.restitution = 0.1f;
        body.createFixture(fixtureDef);
        shape.dispose();


        skills.add(new AttackSkill());
        skills.add(new DefendSkill());

    }

    public void addSkill(Skill skill) {
        skills.add(skill);
    }

    public List<Skill> getSkills() {
        return skills;
    }

    public void useSkill(int index, Character target) {
        System.out.println("666");
        if (index >= 0 && index < skills.size()) {
            skills.get(index).use(this, target);
        }
    }

    public void takeDamage(int damage) {
        int effectiveDamage = Math.max(damage - defense, 0);
        health -= effectiveDamage;
        if (health < 0) health = 0;
        System.out.println(this.getClass().getSimpleName() + " takes " + effectiveDamage + " damage, health now: " + health);
    }

    public void applyEffect(Effect effect) {
        effects.add(effect);
    }

    public void applyEffects() {
        Iterator<Effect> iterator = effects.iterator();
        while (iterator.hasNext()) {
            Effect effect = iterator.next();
            effect.apply(this);
            if (effect.isExpired()) {
                iterator.remove();
            }
        }
    }

    public int getAttack() { return attack; }
    public void setAttack(int attack) { this.attack = attack; }
    public int getDefense() { return defense; }
    public void setDefense(int defense) { this.defense = defense; }
    public float getHealth() { return health; }
    public float getMaxHealth() { return maxHealth; }
    public Body getBody() { return body; }
}
