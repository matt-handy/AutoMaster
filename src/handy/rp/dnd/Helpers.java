package handy.rp.dnd;

public class Helpers {

	public static int getModifierFromAbility(int ability) {
		return Math.floorDiv(ability - 10, 2);
	}
}
