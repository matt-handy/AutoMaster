package handy.rp.dnd.spells;

public class DiceToLevelRange {

	public final int lowerLimitInclusive;
	public final int upperLimitInclusive;
	public final int numDice;
	
	public DiceToLevelRange(int lowerLimitInclusive, int upperLimitInclusive, int numDice) {
		this.lowerLimitInclusive = lowerLimitInclusive;
		this.upperLimitInclusive = upperLimitInclusive;
		this.numDice = numDice;
	}
}
