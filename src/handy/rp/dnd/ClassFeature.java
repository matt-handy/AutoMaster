package handy.rp.dnd;

import java.util.Map;

import handy.rp.dnd.attacks.CoreDamageComponent;

public class ClassFeature {

	public enum USE_TYPE{
		ACTION, BONUS_ACTION, REACTION, NONCOMBAT
	}
	
	public enum RECHARGE_DURATION {
		NA, SHORT_REST, LONG_REST
	}

	public enum DAMAGE_EFFECT {
		NA, MELEE, RANGED
	}

	public final String featureName;
	public final String effectString;
	public final DAMAGE_EFFECT damageEffect;
	public final int minimumLevel;
	public final int classResourceChargesUsed;
	public final USE_TYPE useType;
	private Map<Integer, CoreDamageComponent> levelsToSpecialDamage = null;
	public final RECHARGE_DURATION recharge;
	public final int maxCharges;
	public final ClassFeatureHealingModifier otherHealingMod;
	public final ClassFeatureHealingModifier selfHealingMod;
	public final boolean isTogglableFeature;
	
	public final boolean recoverSpellSlotsOnShortRest;
	
	//This flag is used for if the feature allows for cantrips which otherwise would fail on save to 
	//Still do damage.
	public final boolean halfDamageCantrip;
	
	//This flag is used for if the feature allows for spells to be cast without using a spell slot
	//GenericFeatureData is used to determine which spells apply.
	public final boolean allowsFreeSpells;
	
	//This flag is used for when the feature allows some spells to be case without preparation
	//or counting to prep limit
	//GenericFeatureData is used to determine which spells apply.
	public final boolean allowsNoPrepSpells;
	
	public CharClass parent;
	
	public final int acBonus;
	
	private int extraAttacksUnconditional = 0;
	private Map<Integer, Integer> levelsToExtraCritDice;
	
	private boolean initiativeAdvantage;
	private boolean allowBonusActionAttack;
	private boolean allowReactionAttack;
	
	public ClassFeature(String featureName, String effectString, int minimumLevel, int classResourceChargesUsed,
			DAMAGE_EFFECT damageEffect, Map<Integer, CoreDamageComponent> levelsToSpecialDamage, USE_TYPE useType, RECHARGE_DURATION recharge, int maxCharges,
			ClassFeatureHealingModifier otherHealingMod, ClassFeatureHealingModifier selfHealingMod, int extraAttacksUnconditional,
			Map<Integer, Integer> levelsToExtraCritDice, boolean initiativeAdvantage, boolean isTogglableFeature,
			boolean allowBonusActionAttack, boolean allowReactionAttack, boolean recoverSpellSlotsOnShortRest, boolean halfDamageCantrip, boolean allowsFreeSpells, 
			boolean allowsNoPrepSpells, int acBonus) {
		this.featureName = featureName;
		this.effectString = effectString;
		this.damageEffect = damageEffect;
		this.minimumLevel = minimumLevel;
		this.classResourceChargesUsed = classResourceChargesUsed;
		this.useType = useType;
		this.levelsToSpecialDamage = levelsToSpecialDamage;
		this.recharge = recharge;
		this.maxCharges = maxCharges;
		this.otherHealingMod = otherHealingMod;
		this.selfHealingMod = selfHealingMod;
		this.extraAttacksUnconditional = extraAttacksUnconditional;
		this.levelsToExtraCritDice = levelsToExtraCritDice;
		this.initiativeAdvantage = initiativeAdvantage;
		this.isTogglableFeature = isTogglableFeature; 
		this.allowBonusActionAttack = allowBonusActionAttack;
		this.allowReactionAttack = allowReactionAttack;
		this.recoverSpellSlotsOnShortRest = recoverSpellSlotsOnShortRest;
		this.halfDamageCantrip = halfDamageCantrip;
		this.allowsFreeSpells = allowsFreeSpells;
		this.allowsNoPrepSpells = allowsNoPrepSpells;
		this.acBonus = acBonus;
	}
	
	public boolean allowBonusActionAttack(boolean isToggled) {
		if(allowBonusActionAttack) {
			if(this.isTogglableFeature && !isToggled) {
				return false;
			}else {
				return true;
			}
		}else {
			return false;
		}
	}
	
	public boolean allowReactionAttack(boolean isToggled) {
		if(allowReactionAttack) {
			if(this.isTogglableFeature && !isToggled) {
				return false;
			}else {
				return true;
			}
		}else {
			return false;
		}
	}
	
	public CoreDamageComponent getCoreDamageComponentForLevel(int level) {
		if(levelsToSpecialDamage.containsKey(level)) {
			return levelsToSpecialDamage.get(level);
		}else {
			throw new IllegalArgumentException("Unknown Level: " + level);
		}
	}
	
	public int getExtraCritDice(int level) {
		if(level < 1 || level > 20) {
			throw new IllegalArgumentException("Unknown level: " + level);
		}else {
			return levelsToExtraCritDice.get(level);
		}
	}

	public int extraAttacksGranted() {
		return extraAttacksUnconditional;
	}
	
	public String printEffect() {
		return effectString;
	}
	
	public CharClass getParentClass() {
		return parent;
	}
	
	public void setParentClass(CharClass parent) {
		this.parent = parent;
	}
	
	public boolean grantInitiativeAdvantage() {
		return initiativeAdvantage;
	}
}
