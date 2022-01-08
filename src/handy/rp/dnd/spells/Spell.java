package handy.rp.dnd.spells;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import handy.rp.Dice;
import handy.rp.dnd.ClassFeatureHealingModifier;
import handy.rp.dnd.attacks.Attack;
import handy.rp.dnd.attacks.Damage;
import handy.rp.dnd.character.PlayerCharacter;

public class Spell {

	public enum RECURRING_DAMAGE_TYPE{AUTOMATIC, BONUS, ACTION};
	
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
	protected Map<SLOTLEVEL, List<SpellDamageComponent>> altDamages = null;
	protected SpellHealingComponent healingComponent = null;
	public final String readableName;
	public final String computerName;
	public SLOTLEVEL minimumLevel;
	public final boolean saveDc;
	public final boolean toHit;
	public final String readableEffect;
	public final boolean concentrate;
	public final boolean bonusAction;
	
	private RECURRING_DAMAGE_TYPE recurringOnTurnType = null;
	
	public Spell(String computerName, String readableName, SLOTLEVEL minimumLevel, boolean saveDc, boolean toHit, Map<SLOTLEVEL, List<SpellDamageComponent>> damagers,
			String readableEffect, boolean concentrate, boolean bonusAction, RECURRING_DAMAGE_TYPE recurringOnTurnType, SpellHealingComponent healingComponent, Map<SLOTLEVEL, List<SpellDamageComponent>> altDamages){
		this.readableEffect = readableEffect;
		this.damagers = damagers;
		this.minimumLevel = minimumLevel;
		this.saveDc = saveDc;
		this.toHit = toHit;
		this.readableName = readableName;
		this.computerName = computerName;
		this.concentrate = concentrate;
		this.bonusAction = bonusAction;
		this.recurringOnTurnType = recurringOnTurnType;
		this.healingComponent = healingComponent;
		this.altDamages = altDamages;
	}
	
	@Override
	public boolean equals(Object other) {
		if(other instanceof Spell) {
			if(((Spell) other).computerName.equals(computerName)) {
				return true;
			}
		}
		return false;
	}
	
	public String getRecurringEffectOnTurn(SLOTLEVEL level, int casterLevel, int casterDc, int casterToHit, int casterModifier) {
		String returnMsg = null;
		if(recurringOnTurnType != null) {
			StringBuilder msgBuilder = new StringBuilder();
			if(recurringOnTurnType == RECURRING_DAMAGE_TYPE.AUTOMATIC) {
				msgBuilder.append("On your turn, the spell " + readableName + " inflicts: " + System.lineSeparator());
			}else if(recurringOnTurnType == RECURRING_DAMAGE_TYPE.BONUS) {
				msgBuilder.append("On your turn, you may use the spell " + readableName + " as a bonus action to inflict: " + System.lineSeparator());
			}else if(recurringOnTurnType == RECURRING_DAMAGE_TYPE.ACTION) {
				msgBuilder.append("On your turn, you may use the spell " + readableName + " as an action to inflict: " + System.lineSeparator());
			}
			
			msgBuilder.append(cast(minimumLevel, casterLevel, casterDc, casterToHit, casterModifier));
			returnMsg = msgBuilder.toString();
		}
		return returnMsg;
	}
	
	public String cast(SLOTLEVEL level, int casterLevel, int casterDc, int casterToHit, int casterModifier) {
		return this.cast(level, casterLevel, casterDc, casterToHit, casterModifier, null);
	}
	
	//TODO: Migrate this code from here to Managed Entity
	public String cast(SLOTLEVEL level, int casterLevel, int casterDc, int casterToHit, int casterModifier, PlayerCharacter pc) {
		if(level.level < minimumLevel.level) {
			throw new IllegalArgumentException("This is a " + minimumLevel + " spell");
		}
		
		StringBuilder attackBuilder = new StringBuilder(readableName + ": ");
		
		if(readableEffect != null) {
			attackBuilder.append(readableEffect);
			attackBuilder.append(System.lineSeparator());
		}
		
		if(damagers != null) {
			Set<Damage> damages = rollDamage(level, casterLevel, false);
			attackBuilder.append(Spell.read(this, damages));
		}
		if(altDamages != null) {
			Set<Damage> damages = rollDamage(level, casterLevel, true);
			attackBuilder.append(System.lineSeparator());
			attackBuilder.append("Alternatively: " + Spell.read(this, damages));
		}
		if(healingComponent != null) {
			int upCastNum = level.level - minimumLevel.level;
			if(casterModifier == -1) {
				attackBuilder.append("WARNING: HEALING NOT YET SUPPORTED FOR MONSTERS");
			}else {
				attackBuilder.append("Healed for : " + healingComponent.rollHealing(casterModifier, upCastNum));
			}
			if(pc != null) {
				List<ClassFeatureHealingModifier> otherHeals = pc.getFeatureOtherHealingBonus();
				for(ClassFeatureHealingModifier heal : otherHeals) {
					int extraHeals = heal.staticModifier;
					if(heal.increaseHealBySpellLevel) {
						extraHeals += level.level;
					}
					if(heal.maximizeHealDice) {
						//TODO: Make this automaticly done for player
						attackBuilder.append(" Class feature: ignore prior heal roll and count each max possible die for healing");
					}
					attackBuilder.append(" Class feature, extra heals: " + extraHeals);
				}
				
				List<ClassFeatureHealingModifier> selfHeals = pc.getFeatureSelfHealingBonus();
				for(ClassFeatureHealingModifier heal : selfHeals) {
					int extraHeals = heal.staticModifier;
					if(heal.increaseHealBySpellLevel) {
						extraHeals += level.level;
					}
					if(heal.maximizeHealDice) {
						//TODO: Make this automaticly done for player
						attackBuilder.append(" Class feature: heal self for max possible die for healing");
					}
					attackBuilder.append(" Class feature, heals self: " + extraHeals + " (applied automatically)");
					pc.heal(extraHeals);
				}
			}
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
	
	private Set<Damage> rollDamage(SLOTLEVEL level, int casterLevel, boolean useAlternates){
		if(damagers.get(level) == null) {
			throw new IllegalArgumentException("Improper level supplied");
		}
		Set<Damage> damages = new HashSet<>();
		List<SpellDamageComponent> damageList = damagers.get(level);
		if(useAlternates) {
			damageList = altDamages.get(level);
		}
		for(SpellDamageComponent dc : damageList) {
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
