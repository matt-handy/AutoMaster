package handy.rp.dnd.character;

public class PlayerEquipment {

	public enum SHIELDS{
		SHIELD(2), PLUS_ONE_SHIELD(3), PLUS_TWO_SHIELD(4), PLUS_THREE_SHIELD(5);
		public final int acBonus;
		private SHIELDS(int acBonus) {
			this.acBonus = acBonus;
		}
		public static SHIELDS getShield(String shield) {
			if(shield.equalsIgnoreCase("Shield")) {
				return SHIELD;
			}else if(shield.equalsIgnoreCase("Shield +1")) {
				return PLUS_ONE_SHIELD;
			}else if(shield.equalsIgnoreCase("Shield +2")) {
				return PLUS_TWO_SHIELD;
			}else if(shield.equalsIgnoreCase("Shield +3")) {
				return PLUS_THREE_SHIELD;
			}else {
				throw new IllegalArgumentException("Unknown shield");
			}
		}
	}
	
	public enum ARMOR{
		PADDED(11, -1, false), LEATHER(11, -1, false), PADDED_LEATHER(12, -1, false), HIDE(12, -1, false),
		CHAIN_SHIRT(13, 2, false), SCALE_MAIL(14, 2, false), BREASTPLATE(14, 2, false), HALF_PLATE(15, 2, false),
		RING_MAIL(14, -1, true), CHAIN_MAIL(16, -1, true), SPLINT(17, -1, true), PLATE(18, -1, true);
		
		@Override
		public String toString() {
			if(this == PADDED_LEATHER) {
				return "Padded Leather";
			}else if(this == CHAIN_SHIRT) {
				return "Chain Shirt";
			}else if(this == SCALE_MAIL) {
				return "Scale Male";
			}else if(this == HALF_PLATE) {
				return "Half Plate";
			}else if(this == RING_MAIL) {
				return "Ring Male";
			}else if(this == CHAIN_MAIL) {
				return "Chain Male";
			}else {
				return this.name().toLowerCase();
			}
		}
		
		public static ARMOR getArmor(String armor) {
			if(armor.equalsIgnoreCase("Padded")) {
				return PADDED;
			}else if(armor.equalsIgnoreCase("Leather")) {
				return LEATHER;
			}else if(armor.equalsIgnoreCase("Padded Leather")) {
				return PADDED_LEATHER;
			}else if(armor.equalsIgnoreCase("Hide")) {
				return HIDE;
			}else if(armor.equalsIgnoreCase("Chain Shirt")) {
				return CHAIN_SHIRT;
			}else if(armor.equalsIgnoreCase("Scale Mail")) {
				return SCALE_MAIL;
			}else if(armor.equalsIgnoreCase("Breastplate")) {
				return BREASTPLATE;
			}else if(armor.equalsIgnoreCase("Half Plate")) {
				return HALF_PLATE;
			}else if(armor.equalsIgnoreCase("Ring Mail")) {
				return RING_MAIL;
			}else if(armor.equalsIgnoreCase("Chain Mail")) {
				return CHAIN_MAIL;
			}else if(armor.equalsIgnoreCase("Splint")) {
				return SPLINT;
			}else if(armor.equalsIgnoreCase("Plate")) {
				return PLATE;
			}else {
				throw new IllegalArgumentException("Unknown shield");
			}
		}
		
		private int ac;
		private int dexCap;
		private boolean isACStatic;
		
		private ARMOR(int ac, int dexCap, boolean isACStatic) {
			this.ac = ac;
			this.dexCap = dexCap;
			this.isACStatic = isACStatic;
		}
		
		public boolean hasACCap() {
			return dexCap == -1;
		}
		
		public int ac() {
			return ac;
		}
		
		public int getDexCap() {
			return dexCap;
		}
		
		public boolean isACStatic() {
			return isACStatic;
		}
	}
	
	private SHIELDS myShield = null;
	private ARMOR myArmor = null;
	
	public void addEquipment(String equipment) {
		try {
			SHIELDS shield = SHIELDS.getShield(equipment);
			if(hasShield()) {
				throw new IllegalStateException("Cannot add a second shield");
			}
			myShield = shield;
			return;
		}catch(IllegalArgumentException ex) {
			//Continue, we don't have a shield
		}
		
		try {
			ARMOR armor = ARMOR.getArmor(equipment);
			if(myArmor != null) {
				throw new IllegalStateException("Cannot add a second armor");
			}
			myArmor = armor;
			return;
		}catch(IllegalArgumentException ex) {
			//Continue, we don't have a shield
		}
		
		throw new IllegalArgumentException("Invalid equipment: " + equipment);
	}
	
	public boolean hasShield() {
		return myShield != null;
	}
	
	public int getShieldAC() {
		if(hasShield()) {
			return myShield.acBonus;
		}else {
			return 0;
		}
	}
	
	public boolean hasStaticAC() {
		if(myArmor != null) {
			return myArmor.isACStatic();
		}else {
			return false;
		}
	}
	
	public boolean doesLimitDexACBonux() {
		if(myArmor != null) {
			return myArmor.dexCap != -1;
		}else {
			return false;
		}
	}
	
	public int getLimitedACDexBonus() {
		if(!hasArmorACMod()) {
			throw new IllegalStateException("No armor dex limit");
		}else {
			return myArmor.dexCap;
		}
	}
	
	public boolean hasArmorACMod() {
		if(myArmor != null) {
			return !myArmor.isACStatic();
		}else {
			return false;
		}
	}
	
	public int getArmorACMod() {
		if(!hasArmorACMod()) {
			throw new IllegalStateException("No armor modification");
		}else {
			return myArmor.ac;
		}
	}
	
	public int getStaticAC() {
		if(!hasStaticAC()) {
			throw new IllegalStateException("AC is not static");
		}else {
			return myArmor.ac;
		}
	}
	
	public String getXML() {
		StringBuilder sb = new StringBuilder();
		if(hasShield()) {
			sb.append("<equipment>" + myShield.toString() + "</equipment>");
		}
		if(myArmor != null) {
			sb.append("<equipment>" + myArmor.toString() + "</equipment>");
		}
		return sb.toString();
	}
}
