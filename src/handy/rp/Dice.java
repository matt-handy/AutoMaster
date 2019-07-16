package handy.rp;

import java.security.SecureRandom;
import java.util.Random;

public class Dice {
	
	public enum DICE_TYPE{
		D4(4), D6(6), D8(8), D10(10), D12(12), D20(20), D100(100);
		private final int faces;
		private DICE_TYPE(int faces) {this.faces = faces;};
		public int roll() {return Dice.dX(faces);};
		
		public static DICE_TYPE getDice(String dice) {
			switch (dice) {
			case "D4":
				return D4;
			case "D6":
				return D6;
			case "D8":
				return D8;
			case "D10":
				return D10;
			case "D12":
				return D12;
			case "D20":
				return D20;
			case "D100":
				return D100;
			default:
				throw new IllegalArgumentException("Unknown dice: " + dice);
			}
		}
	};
	
	//TODO: Migrate to Java Commons
	private static Random rng = new SecureRandom();
	
	public static int d4() {
		return dX(4);
	}
	
	public static int d6() {
		return dX(6);
	}
	
	public static int d8() {
		return dX(8);
	}
	
	public static int d10() {
		return dX(10);
	}
	
	public static int d12() {
		return dX(12);
	}
	
	public static int d20() {
		return dX(20);
	}
	
	public static int d100() {
		return dX(100);
	}
	
	public static int dX(int x) {
		return rng.nextInt(x - 1) + 1;
	}
}
