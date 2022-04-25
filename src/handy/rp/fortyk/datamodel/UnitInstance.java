package handy.rp.fortyk.datamodel;

import java.util.ArrayList;
import java.util.List;

import handy.rp.Dice;
import handy.rp.OutcomeNotification;

public class UnitInstance {

	public final Model leader;
	private List<Model> models;
	public final String mnemonic;
	
	protected UnitInstance(Unit base, int count, String mnemonic) {
		if(base.leadModel == null) {
			leader = null;
		}else {
			leader = base.leadModel.clone();
		}
		models = new ArrayList<>();
		for(int idx = 0; idx < count; idx++) {
			models.add(base.commonModel.clone());
		}
		this.mnemonic = mnemonic;
	}
	
	public List<Model> getModels(){
		return new ArrayList<>(models);
	}

	public boolean isUnitAlive() {
		if((leader != null && leader.getCurrentWounds() > 0) || !models.isEmpty()) {
			return true;
		}else {
			return false;
		}
	}

	public int getGreatestCommonMovement() {
		int gcm = 0;
		if(leader != null && leader.getCurrentWounds() > 0) {
			gcm = leader.getCurrentStats().movement;
		}
		
		for(Model model : models) {
			if(model.getCurrentStats().movement < gcm || gcm == 0) {
				gcm = model.getCurrentStats().movement;
			}
		}
		
		return gcm;
	}
	
	public OutcomeNotification takeOrSaveWound(int woundDamage, boolean isMortal, int apModifier) {
		if(!isUnitAlive()) {
			return new OutcomeNotification("Unit is already not combat effective", false);
		}
		if(models.isEmpty()) {
			if(!isMortal) {
				int saveRoll = Dice.d6() + apModifier;
				if(saveRoll >= leader.getCurrentStats().save) {
					return new OutcomeNotification(mnemonic + " saves with " + saveRoll, true);
				}
			}
			leader.wound(woundDamage);
			if(leader.getCurrentWounds() == 0) {
				return new OutcomeNotification("Unit leader takes the wound and dies.", true);
			}else{
				return new OutcomeNotification("Unit leader takes the wound.", true);
			}
			
		}else {
			Model hitModel = models.get(0);
			if(!isMortal) {
				int saveRoll = Dice.d6() + apModifier;
				if(saveRoll >= hitModel.getCurrentStats().save) {
					return new OutcomeNotification(mnemonic + " saves with " + saveRoll, true);
				}
			}
			hitModel.wound(woundDamage);
			if(hitModel.getCurrentWounds() == 0) {
				models.remove(0);
				return new OutcomeNotification("Unit takes the wound and a model dies.", true);
			}else {
				return new OutcomeNotification("Unit takes the wound.", true);
			}
		}
		
	}
}
