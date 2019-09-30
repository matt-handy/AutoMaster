package handy.rp.dnd.spells;

import java.util.List;

import handy.rp.Dice;
import handy.rp.Dice.DICE_TYPE;
import handy.rp.dnd.attacks.Damage;
import handy.rp.dnd.attacks.DamageComponent;

public class SpellDamageComponent extends DamageComponent {

	protected List<DiceToLevelRange> ranges;
	
	public SpellDamageComponent(DAMAGE_TYPE damageType, DICE_TYPE diceType, int modifier, int toHit, List<DiceToLevelRange> ranges) {
		super(damageType, diceType, ranges.get(0).numDice, modifier, toHit);
		this.ranges = ranges;
	}

	public Damage rollDamage(int casterLevel) {
		if(casterLevel < 1) {
			throw new IllegalArgumentException("Must have level >= 1");
		}
		for(DiceToLevelRange range : ranges) {
			if(casterLevel >= range.lowerLimitInclusive && casterLevel <= range.upperLimitInclusive) {
				int damageSum = 0;
				for(int i = 0; i < range.numDice; i++) {
					damageSum += diceType.roll();
				}
				return new Damage(damageType, damageSum + modifier, Dice.d20() + toHit);
			}
		}
		throw new IllegalArgumentException("Invalid caster level, not in range. Ranges should be 1 to MAX_INT, so thats really impressive");
	}
}
