package handy.rp.dnd.attacks;

import handy.rp.dnd.attacks.DamageComponent.DAMAGE_TYPE;

public class CoreDamage {

	public final DAMAGE_TYPE damageType;
	public final int damage;

	public CoreDamage(DAMAGE_TYPE damageType, int damage) {
		this.damageType = damageType;
		this.damage = damage;
	}

	public String getHumanReadableDamage() {
		if(damageType == null) {
			return damage + " damage";
		}else {
		return damage + " " + damageType.readableName + " damage";
		}
	}
	
}
