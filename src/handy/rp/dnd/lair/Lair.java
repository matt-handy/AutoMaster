package handy.rp.dnd.lair;

import java.util.ArrayList;
import java.util.List;

import handy.rp.dnd.Entity;

public class Lair extends Entity {
	public List<LairAction> actions = new ArrayList<>();
	private boolean actedThisTurn = false;
	
	public Lair(String name) {
		super(name);
		currentInitiative = 20;
	}
	
	public void addAction(LairAction action) {
		actions.add(action);
	}
	
	public List<LairAction> getActions(){
		return new ArrayList<LairAction>(actions);
	}
	
	public String expendAction(LairAction action) {
		if(actedThisTurn) {
			return "Cannot act this turn, already acted";
		}else {
			try {
				String result = action.expendAction();
				actedThisTurn = true;
				return result;
			}catch(Exception ex) {
				return "Unable to act: " + ex.getMessage();
			}
		}
	}
	
	@Override
	public void notifyNewTurn() {
		for(LairAction action : actions) {
			action.notifyNewTurn();
		}
		actedThisTurn = false;
	}
	
	@Override
	public String listAvailableActionsAttackSpells() {
		StringBuilder sb = new StringBuilder("Actions Available: ");
		sb.append(System.lineSeparator());
		int idx = 0;
		for(LairAction action : actions) {
			if(action.chargesRemaining() && action.intervalAllowed()) {
				sb.append("Action: " + idx + ": " + action.action.name);
				sb.append(System.lineSeparator());
			}
			idx++;
		}
		return sb.toString();
	}
	
}
