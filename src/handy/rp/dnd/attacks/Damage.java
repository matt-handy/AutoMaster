package handy.rp.dnd.attacks;

import handy.rp.dnd.attacks.DamageComponent.DAMAGE_TYPE;

public class Damage {

	public final DAMAGE_TYPE damageType;
	public final int damage;
	public final int toHit;

	public Damage(DAMAGE_TYPE damageType, int damage, int toHit) {
		this.damageType = damageType;
		this.damage = damage;
		this.toHit = toHit;
	}

	public int toHit() {
		return toHit;
	}

	public String getHumanReadableDamage() {
		return damage + " " + damageType.readableName + " damage";
	}
}
