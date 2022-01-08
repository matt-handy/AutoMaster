package handy.rp.dnd.spells;

import handy.rp.Dice.DICE_TYPE;

public class SpellHealingComponent {

	private DICE_TYPE diceType;
	private boolean useSpellCasterModifier;
	private int numDice;
	private boolean useExtraDicePerLevel;
	
	public SpellHealingComponent(DICE_TYPE diceType, boolean useSpellCasterModifier, int numDice, boolean useExtraDicePerLevel) {
		this.diceType = diceType;
		this.useSpellCasterModifier = useSpellCasterModifier;
		this.numDice = numDice;
		this.useExtraDicePerLevel = useExtraDicePerLevel;
	}
	
	public int rollHealing(int casterModifier, int extraLevels) {
		int healSum = 0;
		int numRolls = numDice;
		if(useExtraDicePerLevel) {
			numRolls += extraLevels;
		}
		for(int i = 0; i < numRolls; i++) {
			healSum += diceType.roll();
		}
		if(useSpellCasterModifier) {
			healSum += casterModifier;
		}
		return healSum;
	}
}
