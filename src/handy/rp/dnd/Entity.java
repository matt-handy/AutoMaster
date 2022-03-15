package handy.rp.dnd;

import java.util.EnumSet;

import handy.rp.OutcomeNotification;
import handy.rp.dnd.EntityCondition.CONDITIONS;

public class Entity {

	public final String personalName;
	protected int currentInitiative;
	private EnumSet<CONDITIONS> conditions = EnumSet.noneOf(CONDITIONS.class);
	protected int reactionsRemaining = 1;

	public boolean canTakeReaction() {
		return reactionsRemaining >= 1;
	}

	public OutcomeNotification expendReaction(String reactionName) {
		if (canTakeReaction()) {
			reactionsRemaining--;
			return new OutcomeNotification(personalName + " takes reaction: " + reactionName, true);
		} else {
			return new OutcomeNotification(personalName + " cannot take reaction: " + reactionName, false);
		}
	}

	public Entity(String personalName) {
		this.personalName = personalName;
	}

	public int getCurrentInitiative() {
		return currentInitiative;
	}

	public void setInitiative(int currentInitiative) {
		this.currentInitiative = currentInitiative;
	}

	public void notifyNewTurn() {
		reactionsRemaining = 1;
	}

	public String listAvailableActionsAttackSpells() {
		return "Cannot list actions, entity is not managed by this tool";
	}

	public String listStats() {
		return "Cannot list stats, entity is not managed by this tool";
	}

	public EnumSet<CONDITIONS> getConditions() {
		return conditions;
	}

	public void addCondition(CONDITIONS condition) {
		conditions.add(condition);
	}

	public void removeConditions(CONDITIONS condition) {
		conditions.remove(condition);
	}
}
