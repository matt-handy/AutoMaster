package handy.rp.dnd.attacks;

import handy.rp.Dice.DICE_TYPE;
import handy.rp.dnd.attacks.DamageComponent.DAMAGE_TYPE;

public class CoreDamageComponent {
	public final DAMAGE_TYPE damageType;
	public final DICE_TYPE diceType;
	public final int diceCount;
	public final int modifier;

	public CoreDamageComponent(DAMAGE_TYPE damageType, DICE_TYPE diceType, int diceCount, int modifier) {
		this.damageType = damageType;
		this.diceCount = diceCount;
		this.diceType = diceType;
		this.modifier = modifier;
	}

	public CoreDamage rollDamage() {
		int damage = 0;
		if (diceType != null) {
			for (int i = 0; i < diceCount; i++) {
				damage += diceType.roll();
			}
		}
		return new CoreDamage(damageType, damage + modifier);
	}
}
