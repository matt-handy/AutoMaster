package handy.rp.dnd.attacks;

import handy.rp.Dice;
import handy.rp.Dice.DICE_TYPE;

public class DamageComponent {
	public enum DAMAGE_TYPE {
		BLUDGEONING("Bludgeoning"), PIERCING("Piercing"), SLASHING("Slashing"), FIRE("Fire"), COLD("Cold"),
		NECROTIC("Necrotic"), POISON("Poison"), PSYCHIC("Psychic"), ACID("Acid"), FORCE("Force"),
		LIGHTNING("Lightning"), RADIANT("Radiant"), THUNDER("Thunder");

		public final String readableName;

		private DAMAGE_TYPE(String readableName) {
			this.readableName = readableName;
		};
		
		public static DAMAGE_TYPE getDamage(String name) {
			switch(name) {
			case "BLUDGEONING":
				return BLUDGEONING;
			case "PIERCING":
				return PIERCING;
			case "SLASHING":
				return SLASHING;
			case "FIRE":
				return FIRE;
			case "COLD":
				return COLD;
			case "NECROTIC":
				return NECROTIC;
			case "POISON":
				return POISON;
			case "PSYCHIC":
				return PSYCHIC;
			case "ACID":
				return ACID;
			case "FORCE":
				return FORCE;
			case "LIGHTNING":
				return LIGHTNING;
			case "RADIANT":
				return RADIANT;
			case "THUNDER":
				return THUNDER;
			default:
				throw new IllegalArgumentException("Unknown damage type: " + name);
			}
				
		}
	};
	
	public final DAMAGE_TYPE damageType;
	public final DICE_TYPE diceType;
	public final int diceCount;
	public final int modifier;
	public final int toHit;
	
	public DamageComponent(DAMAGE_TYPE damageType, DICE_TYPE diceType, int diceCount, int modifier, int toHit) {
		this.damageType = damageType;
		this.diceCount = diceCount;
		this.diceType = diceType;
		this.modifier = modifier;
		this.toHit = toHit;
	}
	
	public Damage rollDamage() {
		return new Damage(damageType, (diceCount * diceType.roll()) + modifier, Dice.d20() + toHit);
	}
}
