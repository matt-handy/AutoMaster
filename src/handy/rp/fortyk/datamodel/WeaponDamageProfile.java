package handy.rp.fortyk.datamodel;

import handy.rp.fortyk.datamodel.StatBlock.StatElement;

public class WeaponDamageProfile {

	
	public enum WEAPON_TYPE{MELEE, ASSAULT, HEAVY, NORMAL, PISTOL, GRENADE};
	
	public final String name;
	public final WEAPON_TYPE type;
	public final int range;
	public final int strength;
	public final int ap;
	public final StatElement wounds;
	public final StatElement attacks;
	public final StatElement optionalAttacks;
	public final boolean autoHit;
	
	@Override
	public boolean equals(Object other) {
		if(other instanceof WeaponDamageProfile) {
			WeaponDamageProfile otherProfile = (WeaponDamageProfile) other;
			return name.equalsIgnoreCase(otherProfile.name) && strength == otherProfile.strength;
		}else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return name.hashCode() + strength;
	}
	
	public WeaponDamageProfile(String name, WEAPON_TYPE type, int range, int strength, int ap, StatElement wounds, StatElement attacks, StatElement optionalAttacks, boolean autoHit) {
		this.name = name;
		this.type = type;
		this.range = range;
		this.strength = strength;
		this.ap = ap;
		this.wounds = wounds;
		this.attacks = attacks;
		this.optionalAttacks = optionalAttacks;
		this.autoHit = autoHit;
	}
}
