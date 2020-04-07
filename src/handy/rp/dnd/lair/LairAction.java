package handy.rp.dnd.lair;

import handy.rp.dnd.Action;
import handy.rp.dnd.monsters.MonsterInstance;

public class LairAction {
	public final Action action;
	public final int rechargeInterval;
	public final int chargeTotal;
	private int chargesRemaining;
	private int rechargeCount;
	
	public LairAction(Action action, int rechargeInterval, int chargeTotal) {
		this.action = action;
		this.rechargeInterval = rechargeInterval;
		this.chargeTotal = chargeTotal;
		chargesRemaining = chargeTotal;
		rechargeCount = rechargeInterval;
	}
	
	public boolean chargesRemaining() {
		return chargesRemaining > 0;
	}
	
	public boolean intervalAllowed() {
		return rechargeCount == rechargeInterval;
	}
	
	//TODO - decrement only after checking both conditions
	public boolean useAction() {
		boolean useCharge = false;
		boolean resetRecharge = false;
		if(chargesRemaining != MonsterInstance.AT_WILL) {
			if(chargesRemaining > 0) {
				useCharge = true;
			}else {
				return false;
			}
		}
		if(rechargeInterval != 1) {
			if(intervalAllowed()) {
				resetRecharge = true;
			}else {
				return false;
			}
		}
		if(useCharge) {
			chargesRemaining--;
		}
		if(resetRecharge) {
			rechargeCount = 0;
		}
		return true;
	}
	
	public String expendAction() throws Exception{
		if(useAction()) {
			return action.expendAction();
		}else {
			throw new Exception("Unable to expend action " + action.name + ", not ready");
		}
	}
	
	public void notifyNewTurn() {
		if(rechargeCount != rechargeInterval) {
			rechargeCount++;
		}
	}
	
	
}
