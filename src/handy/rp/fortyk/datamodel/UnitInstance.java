package handy.rp.fortyk.datamodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import handy.rp.Dice;
import handy.rp.OutcomeNotification;

public class UnitInstance {

	public final Model leader;
	private List<Model> models;
	public final String mnemonic;
	
	protected UnitInstance(Unit base, int count, String mnemonic, List<String> leaderWeapons, List<String> standardWeapons, List<List<String>> limitedWeaponSet) {
		if(base.leadModel == null) {
			leader = null;
		}else {
			leader = base.leadModel.clone(this);
			leader.selectWeapons(leaderWeapons);
		}
		models = new ArrayList<>();
		for(int idx = 0; idx < count; idx++) {
			Model clone = base.commonModel.clone(this);
			if(idx >= limitedWeaponSet.size()) {
				clone.selectWeapons(standardWeapons);
			}else {
				clone.selectWeapons(limitedWeaponSet.get(idx));
			}
			models.add(clone);
		}
		this.mnemonic = mnemonic;
	}
	
	private boolean hasThrownGrenadeThisAttack = false;
	
	protected boolean hasUnitHasThrownGrenade() {
		return hasThrownGrenadeThisAttack;
	}
	
	protected void throwGrenade() {
		hasThrownGrenadeThisAttack = true;
	}
	
	public String rollAndFormatMeleeAttack() {
		Map<WeaponDamageProfile, Set<FortyKDamageOutcome>> aggregatedOutput = new HashMap<>();
		if(leader != null) {
			List<FortyKDamageOutcome> outcomes = leader.rollMeleeAttacks();
			sortDamage(aggregatedOutput, outcomes);
		}
		for(Model model : models) {
			List<FortyKDamageOutcome> outcomes = model.rollMeleeAttacks();
			sortDamage(aggregatedOutput, outcomes);
		}
		return buildWeaponDamageReadout(aggregatedOutput);
	}
	
	public String rollAndFormatRangedAttack() {
		hasThrownGrenadeThisAttack = false;
		Map<WeaponDamageProfile, Set<FortyKDamageOutcome>> aggregatedOutput = new HashMap<>();
		if(leader != null) {
			List<FortyKDamageOutcome> outcomes = leader.rollShootingAttacks();
			sortDamage(aggregatedOutput, outcomes);
		}
		for(Model model : models) {
			List<FortyKDamageOutcome> outcomes = model.rollShootingAttacks();
			sortDamage(aggregatedOutput, outcomes);
		}
		return buildWeaponDamageReadout(aggregatedOutput);
	}
	
	private String buildWeaponDamageReadout(Map<WeaponDamageProfile, Set<FortyKDamageOutcome>> aggregatedOutput) {
		StringBuilder sb = new StringBuilder();
		for(WeaponDamageProfile profile : aggregatedOutput.keySet()) {
			sb.append("Weapon name: " + profile.name + " with strength " + profile.strength + " and ap " + profile.ap);
			sb.append(System.lineSeparator());
			sb.append("Weapon hits unit toughness : " + (profile.strength * 2) + " or more: ");
			sb.append(buildDamagesWhenAboveWoundThreshold(aggregatedOutput.get(profile), 6));
			sb.append(System.lineSeparator());
			sb.append("Weapon hits unit toughness : " + (profile.strength + 1) + " or more: ");
			sb.append(buildDamagesWhenAboveWoundThreshold(aggregatedOutput.get(profile), 5));
			sb.append(System.lineSeparator());
			sb.append("Weapon hits unit toughness is " + profile.strength + " : ");
			sb.append(buildDamagesWhenAboveWoundThreshold(aggregatedOutput.get(profile), 4));
			sb.append(System.lineSeparator());
			sb.append("Weapon hits unit toughness is less than " + profile.strength + " : ");
			sb.append(buildDamagesWhenAboveWoundThreshold(aggregatedOutput.get(profile), 3));
			sb.append(System.lineSeparator());
			sb.append("Weapon hits unit toughness is " + profile.strength / 2 + " or less: ");
			sb.append(buildDamagesWhenAboveWoundThreshold(aggregatedOutput.get(profile), 2));
			sb.append(System.lineSeparator());
		}
		if(sb.length() == 0) {
			sb.append("No hits connected with the enemy");
		}
		return sb.toString();
	}
	
	private String buildDamagesWhenAboveWoundThreshold(Set<FortyKDamageOutcome> damages, int rollThreshold) {
		StringBuilder sb = new StringBuilder();
		for(FortyKDamageOutcome damage : damages) {
			if(damage.woundRoll >= rollThreshold && !damage.optionalAttack) {
				sb.append(damage.damageRoll + " ");
			}
		}
		
		boolean firstOptional = true;
		for(FortyKDamageOutcome damage : damages) {
			if(damage.woundRoll >= rollThreshold && damage.optionalAttack) {
				if(firstOptional) {
					sb.append("Optional attacks: ");
					firstOptional = false;
				}
				sb.append(damage.damageRoll + " ");
			}
		}
		return sb.toString();
	}
	
	private static void sortDamage(Map<WeaponDamageProfile, Set<FortyKDamageOutcome>> aggregatedOutput, List<FortyKDamageOutcome> outcomes) {
		for(FortyKDamageOutcome damage : outcomes) {
			if(!aggregatedOutput.containsKey(damage.profile)) {
				aggregatedOutput.put(damage.profile, new HashSet<>());
			}
			aggregatedOutput.get(damage.profile).add(damage);
		}
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
