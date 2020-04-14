package handy.rp.dnd.spells;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import handy.rp.Dice;
import handy.rp.dnd.attacks.Attack;
import handy.rp.dnd.attacks.Damage;

public class Spell {
	
	public enum SLOTLEVEL {CANTRIP(0), ONE(1), TWO(2), THREE(3), FOUR(4), FIVE(5), SIX(6), SEVEN(7), EIGHT(8), NINE(9);
		
		public final int level;
		private SLOTLEVEL(int level) { this.level = level; }
		
		@Override
		public String toString() {
			if(level == 0) {
				return "Cantrip";
			}else {
				return level + "";
			}
		}
		
		public static SLOTLEVEL get(int level) {
			switch(level) {
			case 0: return CANTRIP;
			case 1: return ONE;
			case 2: return TWO;
			case 3: return THREE;
			case 4: return FOUR;
			case 5: return FIVE;
			case 6: return SIX;
			case 7: return SEVEN;
			case 8: return EIGHT;
			case 9: return NINE;
			default: throw new IllegalArgumentException("Invalid slot number");
			}
		}
		
	};

	protected Map<SLOTLEVEL, List<SpellDamageComponent>> damagers = null;
	public final String readableName;
	public final String computerName;
	public SLOTLEVEL minimumLevel;
	public final boolean saveDc;
	public final boolean toHit;
	public final String readableEffect;
	public final boolean concentrate;
	public final boolean bonusAction;
	
	public Spell(String computerName, String readableName, SLOTLEVEL minimumLevel, boolean saveDc, boolean toHit, Map<SLOTLEVEL, List<SpellDamageComponent>> damagers,
			String readableEffect, boolean concentrate, boolean bonusAction){
		this.readableEffect = readableEffect;
		this.damagers = damagers;
		this.minimumLevel = minimumLevel;
		this.saveDc = saveDc;
		this.toHit = toHit;
		this.readableName = readableName;
		this.computerName = computerName;
		this.concentrate = concentrate;
		this.bonusAction = bonusAction;
	}

	
	public String cast(SLOTLEVEL level, int casterLevel, int casterDc, int casterToHit) {
		if(level.level < minimumLevel.level) {
			throw new IllegalArgumentException("This is a " + minimumLevel + " spell");
		}
		
		StringBuilder attackBuilder = new StringBuilder(readableName + ": ");
		
		if(readableEffect != null) {
			attackBuilder.append(readableEffect);
			attackBuilder.append(System.lineSeparator());
		}
		
		if(damagers != null) {
			Set<Damage> damages = rollDamage(level, casterLevel);
			attackBuilder.append(Spell.read(this, damages));
		}
		if(saveDc) {
			attackBuilder.append(System.lineSeparator());
			attackBuilder.append("Spell Save: " + casterDc);
		}
		if(toHit) {
			attackBuilder.append(System.lineSeparator());
			attackBuilder.append("To hit roll: " + (Dice.d20() + casterToHit));
		}
		
		return attackBuilder.toString();
	}
	
	private Set<Damage> rollDamage(SLOTLEVEL level, int casterLevel){
		if(damagers.get(level) == null) {
			throw new IllegalArgumentException("Improper level supplied");
		}
		Set<Damage> damages = new HashSet<>();
		for(SpellDamageComponent dc : damagers.get(level)) {
			damages.add(dc.rollDamage(casterLevel));
		}
		return damages;
	}
	
	public static String read(Spell spell, Set<Damage> damages) {
		StringBuilder sBuilder = new StringBuilder();
		
		if(damages != null && damages.size() != 0) {
			sBuilder.append(" hits for ");
			boolean firstDamage = true;
			
			for(Damage damage : damages) {
				if(firstDamage) {
					firstDamage = false;
				}else {
					sBuilder.append(" and ");
				}
				sBuilder.append(damage.getHumanReadableDamage());
			}
		}
		
		return sBuilder.toString();
	}
}
