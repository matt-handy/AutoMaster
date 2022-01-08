package handy.rp.dnd.attacks;

public class CharacterWeapon {

	public final boolean isProficient;
	public final Weapon weapon;
	
	public CharacterWeapon(Weapon weapon, boolean isProficient) {
		this.isProficient = isProficient;
		this.weapon = weapon;
	}
}
