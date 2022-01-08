package handy.rp.dnd;

public class ClassFeatureHealingModifier {

	public final int staticModifier;
	public final boolean maximizeHealDice;
	public final boolean increaseHealBySpellLevel;
	
	public ClassFeatureHealingModifier(int staticModifier, boolean maximizeHealDice, boolean increaseHealBySpellLevel) {
		this.staticModifier = staticModifier;
		this.maximizeHealDice = maximizeHealDice;
		this.increaseHealBySpellLevel = increaseHealBySpellLevel;
	}
}
