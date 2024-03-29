package handy.rp.fortyk.datamodel;

import java.util.Random;

public class StatBlock {

	public enum StatElement {
		ZERO, ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, ELEVEN, TWELVE, THIRTEEN, FOURTEEN, FIFTEEN,
		SIXTEEN, SEVENTEEN, EIGHTEEN, NINETEEN, TWENTY, D3, D6;

		public static StatElement getElement(String name) {
			for (StatElement element : values()) {
				if (name.contentEquals(element.name())) {
					return element;
				}
			}
			throw new IllegalArgumentException("Invalid argument");
		}
		
		public static StatElement getValue(String val) {
			try {
				return getValue(Integer.parseInt(val));
			}catch(NumberFormatException ex) {
				//Ignore
			}
			return valueOf(val);
		}

		public static StatElement getValue(int val) {
			switch (val) {
			case (0): return ZERO;
			case (1):
				return ONE;
			case (2):
				return TWO;
			case (3):
				return THREE;
			case (4):
				return FOUR;
			case (5):
				return FIVE;
			case (6):
				return SIX;
			case (7):
				return SEVEN;
			case (8):
				return EIGHT;
			case (9):
				return NINE;
			case (10):
				return TEN;
			case (11):
				return ELEVEN;
			case (12):
				return TWELVE;
			case (13):
				return THIRTEEN;
			case (14):
				return FOURTEEN;
			case (15):
				return FIFTEEN;
			case (16):
				return SIXTEEN;
			case (17):
				return SEVENTEEN;
			case (18):
				return EIGHTEEN;
			case (19):
				return NINETEEN;
			case (20):
				return TWENTY;
			default:
				throw new IllegalArgumentException("Value not in range");
			}
		}

		public int getValue() {
			Random rnd = new Random();
			switch (name()) {
			case ("ONE"):
				return 1;
			case ("TWO"):
				return 2;
			case ("THREE"):
				return 3;
			case ("FOUR"):
				return 4;
			case ("FIVE"):
				return 5;
			case ("SIX"):
				return 6;
			case ("SEVEN"):
				return 7;
			case ("EIGHT"):
				return 8;
			case ("NINE"):
				return 9;
			case ("TEN"):
				return 10;
			case ("ELEVEN"):
				return 11;
			case ("TWELVE"):
				return 12;
			case ("THIRTEEN"):
				return 13;
			case ("FOURTEEN"):
				return 14;
			case ("FIFTEEN"):
				return 15;
			case ("SIXTEEN"):
				return 16;
			case ("SEVENTEEN"):
				return 17;
			case ("EIGHTEEN"):
				return 18;
			case ("NINETEEN"):
				return 19;
			case ("TWENTY"):
				return 20;
			case ("D3"):
				return rnd.nextInt(2) + 1;
			case ("D6"):
				return rnd.nextInt(2) + 1;
			}
			return -1;// We're never going to get here
		}
	}

	public final int movement;
	public final int weaponSkill;
	public final int ballisticSkill;
	public final int strength;
	public final int toughness;
	public final int wounds;
	public final StatElement attacks;
	public final int leadership;
	public final int save;
	public final int points;

	public StatBlock(int movement, int weaponSkill, int ballisticSkill, int strength, int toughness, int wounds,
			StatElement attacks, int leadership, int save, int points) {
		this.movement = movement;
		this.weaponSkill = weaponSkill;
		this.ballisticSkill = ballisticSkill;
		this.strength = strength;
		this.toughness = toughness;
		this.wounds = wounds;
		this.attacks = attacks;
		this.leadership = leadership;
		this.save = save;
		this.points = points;
	}
}
