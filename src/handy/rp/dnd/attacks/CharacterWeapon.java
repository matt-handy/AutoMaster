package handy.rp.dnd.attacks;

public class CharacterWeapon {

	public final boolean isProficient;
	public final Weapon weapon;
	private final int plusWeaponMod;//This is the permanent weapon mod
	private int currentPlusWeaponMod;//This is temporary, modifiable by features, etc
	
	public CharacterWeapon(Weapon weapon, boolean isProficient, int plusWeaponMod) {
		this.isProficient = isProficient;
		this.weapon = weapon;
		this.plusWeaponMod = plusWeaponMod;
		this.currentPlusWeaponMod = plusWeaponMod;
	}

	public int getPlusWeaponMod() {
		return plusWeaponMod;
	}

	public void setTempPlusWeaponMod(int plusWeaponMod) {
		this.currentPlusWeaponMod = plusWeaponMod;
	}
	
	public int getCurrentPlusWeaponMod() {
		return currentPlusWeaponMod;
	}
	
	public void resetTempPlusWeapon() {
		currentPlusWeaponMod = plusWeaponMod;
	}
	
	
}
