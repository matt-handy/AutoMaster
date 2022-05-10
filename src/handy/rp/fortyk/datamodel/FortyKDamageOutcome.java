package handy.rp.fortyk.datamodel;

public class FortyKDamageOutcome {
	public final String weaponName;
	public final int woundRoll;
	public final int damageRoll;
	public final WeaponDamageProfile profile;
	public final boolean optionalAttack;
	
	public FortyKDamageOutcome(String weaponName, int woundRoll, int damageRoll, WeaponDamageProfile profile) {
		this.weaponName = weaponName;
		this.woundRoll = woundRoll;
		this.damageRoll = damageRoll;
		this.profile = profile;
		this.optionalAttack = false;
	}
	
	public FortyKDamageOutcome(String weaponName, int woundRoll, int damageRoll, WeaponDamageProfile profile, boolean optionalAttack) {
		this.weaponName = weaponName;
		this.woundRoll = woundRoll;
		this.damageRoll = damageRoll;
		this.profile = profile;
		this.optionalAttack = optionalAttack;
	}
}
