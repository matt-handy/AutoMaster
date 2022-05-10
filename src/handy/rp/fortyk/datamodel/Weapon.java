package handy.rp.fortyk.datamodel;

import java.util.ArrayList;
import java.util.List;

public class Weapon {

	private List<WeaponDamageProfile> profiles;
	public final int points;
	public final boolean countsAgainstMeleeAttacks;
	public final int attackLimit;
	public final boolean freeattack;
	public final int replace;
	public final int limitCanHave;
	public final int limitOutOf;
	public final int minusToHit;
	public final String name;
	
	public Weapon(String name, int points, boolean countsAgainstMeleeAttacks, int attackLimit, boolean freeattack, int replace, 
			int limitCanHave, int limitOutOf, int minusToHit, List<WeaponDamageProfile> profiles) {
		this.profiles = profiles;
		this.points = points;
		this.countsAgainstMeleeAttacks = countsAgainstMeleeAttacks;
		this.attackLimit = attackLimit;
		this.freeattack = freeattack;
		this.replace = replace;
		this.limitCanHave = limitCanHave;
		this.limitOutOf = limitOutOf;
		this.minusToHit = minusToHit;
		this.name = name;
	}
	
	public List<WeaponDamageProfile> getProfiles(){
		return new ArrayList<>(profiles);
	}
	
	public WeaponDamageProfile getProfile(String name) {
		for(WeaponDamageProfile profile : profiles) {
			if(profile.name.equalsIgnoreCase(name)) {
				return profile;
			}
		}
		return null;
	}
}
