package handy.rp.dnd;

import java.util.EnumSet;

import handy.rp.dnd.EntityCondition.CONDITIONS;

public class Entity {

	public final String personalName;
	protected int currentInitiative;
	private EnumSet<CONDITIONS> conditions = EnumSet.noneOf(CONDITIONS.class);
	
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
		
	}
	
	public String listAvailableActionsAttackSpells() {
		return "Cannot list actions, entity is not managed by this tool"; 
	}
	
	public String listStats() {
		return "Cannot list stats, entity is not managed by this tool";
	}
	
	public EnumSet<CONDITIONS> getConditions(){
		return conditions;
	}
	
	public void addCondition(CONDITIONS condition) {
		conditions.add(condition);
	}
	
	public void removeConditions(CONDITIONS condition) {
		conditions.remove(condition);
	}
}
