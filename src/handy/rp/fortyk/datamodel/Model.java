package handy.rp.fortyk.datamodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import handy.rp.Dice.DICE_TYPE;
import handy.rp.fortyk.datamodel.StatBlock.StatElement;
import handy.rp.fortyk.datamodel.WeaponDamageProfile.WEAPON_TYPE;

public class Model {

	public final StatBlock baseStats;
	Map<Integer, StatBlock> degradedStats;
	private int currentWounds;
	private List<Weapon> templateWeapons;
	private List<String> chosenWeaponProfiles;
	private UnitInstance parent;

	public Model(StatBlock baseStats, Map<Integer, StatBlock> degradedStats, List<Weapon> templateWeapons,
			UnitInstance parent) {
		this.baseStats = baseStats;
		// No copy needed as XML parser can pass ownership
		this.degradedStats = degradedStats;
		this.currentWounds = baseStats.wounds;
		this.templateWeapons = templateWeapons;
		this.parent = parent;
	}

	public void selectWeapons(List<String> weapons) {
		for (String weapon : weapons) {
			if (getWeaponProfile(weapon) == null) {
				throw new IllegalArgumentException("Model does not have this weapon: " + weapon);
			}
		}
		chosenWeaponProfiles = new ArrayList<>(weapons);
	}

	public List<FortyKDamageOutcome> rollShootingAttacks() {
		if (chosenWeaponProfiles == null)
			throw new IllegalStateException("Must configure object with chosen weapon profiles");

		List<FortyKDamageOutcome> outcomes = new ArrayList<>();
		for (String weaponName : chosenWeaponProfiles) {
			Weapon weapon = getWeaponProfile(weaponName);
			for (WeaponDamageProfile selectedProfile : weapon.getProfiles()) {
				//System.out.println("Profile: " + selectedProfile.name);
				// System.out.println(weapon.name + " " + selectedProfile.attacks.getValue());
				if (selectedProfile.type == WeaponDamageProfile.WEAPON_TYPE.MELEE) {
					continue;
				}
				if (selectedProfile.type == WeaponDamageProfile.WEAPON_TYPE.GRENADE
						&& parent.hasUnitHasThrownGrenade()) {
					continue;
				} else if (selectedProfile.type == WeaponDamageProfile.WEAPON_TYPE.GRENADE) {
					parent.throwGrenade();
				}
				int optionalAttacks = selectedProfile.optionalAttacks.getValue();
				int numberOfAttacks = selectedProfile.attacks.getValue();
				int totalAttacks = numberOfAttacks;
				if (optionalAttacks > 0) {
					totalAttacks += optionalAttacks;
				}
				// System.out.println(weapon.name + " " + numberOfAttacks + " " +
				// optionalAttacks);
				for (int idx = 0; idx < totalAttacks; idx++) {
					int toHitRoll = DICE_TYPE.D6.roll();
					toHitRoll += weapon.minusToHit;
					// System.out.println("Fired");
					if (toHitRoll < getCurrentStats().weaponSkill && !selectedProfile.autoHit) {
						continue;
					}
					int woundRoll = DICE_TYPE.D6.roll();
					int damage = selectedProfile.wounds.getValue();
					if (woundRoll != 1) {
						if (idx >= numberOfAttacks) {
							outcomes.add(new FortyKDamageOutcome(weaponName, woundRoll, damage, selectedProfile, true));
						} else {
							outcomes.add(new FortyKDamageOutcome(weaponName, woundRoll, damage, selectedProfile));
						}
					}
				}
			}
		}
		return outcomes;
	}

	public List<FortyKDamageOutcome> rollMeleeAttacks() {
		if (chosenWeaponProfiles == null)
			throw new IllegalStateException("Must configure object with chosen weapon profiles");

		List<FortyKDamageOutcome> outcomes = new ArrayList<>();
		int attacksMade = 0;
		int modelAttacks = getCurrentStats().attacks.getValue();
		for (String weaponName : chosenWeaponProfiles) {
			Weapon weapon = getWeaponProfile(weaponName);
			WeaponDamageProfile selectedProfile = weapon.getProfile(weaponName);
			boolean usedFreeAttack = false;
			if (selectedProfile.type != WeaponDamageProfile.WEAPON_TYPE.MELEE) {
				continue;
			}
			if (weapon.countsAgainstMeleeAttacks) {
				FortyKDamageOutcome outcome = roll(weapon, selectedProfile, getCurrentStats().weaponSkill);
				if (outcome != null && outcome.woundRoll != 1) {
					outcomes.add(outcome);
				}
			} else {
				if (weapon.attackLimit != 0) {
					int currentAttacksMade = 0;
					while (attacksMade < getCurrentStats().attacks.getValue()
							&& currentAttacksMade < weapon.attackLimit) {
						FortyKDamageOutcome outcome = roll(weapon, selectedProfile, getCurrentStats().weaponSkill);
						if (outcome != null && outcome.woundRoll != 1) {
							outcomes.add(outcome);
						}
						if (!weapon.freeattack || usedFreeAttack) {
							attacksMade++;
						} else {
							usedFreeAttack = true;
						}
						currentAttacksMade++;
					}
				} else {
					while (attacksMade < modelAttacks) {
						FortyKDamageOutcome outcome = roll(weapon, selectedProfile, getCurrentStats().weaponSkill);
						if (outcome != null && outcome.woundRoll != 1) {
							outcomes.add(outcome);
						}
						if (!weapon.freeattack || usedFreeAttack) {
							attacksMade++;
						} else {
							usedFreeAttack = true;
						}
					}
				}
			}
		}

		while (attacksMade < modelAttacks) {
			FortyKDamageOutcome outcome = rollDefaultMeleeAttack();
			if (outcome != null && outcome.woundRoll != 1) {
				outcomes.add(outcome);
			}
			attacksMade++;
		}

		return outcomes;
	}

	private FortyKDamageOutcome rollDefaultMeleeAttack() {
		int toHitRoll = DICE_TYPE.D6.roll();
		if (toHitRoll < getCurrentStats().weaponSkill) {
			return null;
		}
		int woundRoll = DICE_TYPE.D6.roll();
		WeaponDamageProfile selectedProfile = new WeaponDamageProfile("Default Melee", WEAPON_TYPE.MELEE, 0,
				getCurrentStats().strength, 0, StatElement.ONE, getCurrentStats().attacks, StatElement.ZERO, false);
		return new FortyKDamageOutcome("Default Melee", woundRoll, 1, selectedProfile);
	}

	private FortyKDamageOutcome roll(Weapon weapon, WeaponDamageProfile selectedProfile, int applicableWeaponSkill) {
		int toHitRoll = DICE_TYPE.D6.roll();
		toHitRoll += weapon.minusToHit;
		if (toHitRoll < applicableWeaponSkill && !selectedProfile.autoHit) {
			return null;
		}
		int woundRoll = DICE_TYPE.D6.roll();
		int damage = selectedProfile.wounds.getValue();
		return new FortyKDamageOutcome(selectedProfile.name, woundRoll, damage, selectedProfile);
	}

	private Weapon getWeaponProfile(String weaponProfileName) {
		for (Weapon weapon : templateWeapons) {
			if (weaponProfileName.equalsIgnoreCase(weapon.name)) {
				return weapon;
			}
		}
		return null;
	}

	public int getCurrentWounds() {
		return currentWounds;
	}

	public void wound(int wounds) {
		if (currentWounds - wounds > 0) {
			currentWounds = currentWounds - wounds;
		} else {
			currentWounds = 0;
		}
	}

	public StatBlock getCurrentStats() {
		if (degradedStats == null) {
			return baseStats;
		}
		int currentLowestThreshold = baseStats.wounds;
		boolean metAThreshold = false;
		for (Integer threshold : degradedStats.keySet()) {
			if (threshold < currentLowestThreshold && threshold >= currentWounds) {
				metAThreshold = true;
				currentLowestThreshold = threshold;
			}
		}
		if (metAThreshold) {
			return degradedStats.get(currentLowestThreshold);
		} else {
			return baseStats;
		}
	}

	public Model clone(UnitInstance unit) {
		return new Model(baseStats, degradedStats, new ArrayList<>(templateWeapons), unit);
	}
}
