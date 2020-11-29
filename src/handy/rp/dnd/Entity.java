package handy.rp.dnd;

public class Entity {

	public final String personalName;
	protected int currentInitiative;
	
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
	
}
