package handy.rp.dnd.character;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import handy.rp.Dice;
import handy.rp.Dice.DICE_TYPE;
import handy.rp.dnd.CharClass;
import handy.rp.dnd.CharClass.ESSENTIAL_ABILITY_SCORE;
import handy.rp.dnd.CharClass.SPELLCASTING_MODIFIER;
import handy.rp.dnd.ClassFeature;
import handy.rp.dnd.ClassFeature.DAMAGE_EFFECT;
import handy.rp.dnd.ClassFeature.RECHARGE_DURATION;
import handy.rp.dnd.ClassFeatureHealingModifier;
import handy.rp.dnd.ClassResource.RECHARGE_INTERVAL;
import handy.rp.dnd.Helpers;
import handy.rp.dnd.ManagedEntity;
import handy.rp.dnd.SkillCheckInfo.SKILL_CHECK;
import handy.rp.dnd.SkillCheckInfo.SKILL_MODIFIER;
import handy.rp.dnd.attacks.CharacterWeapon;
import handy.rp.dnd.attacks.Weapon.WEAPON_ATTRIBUTES;
import handy.rp.dnd.spells.Spell;
import handy.rp.dnd.spells.Spell.SLOTLEVEL;

public class PlayerCharacter extends ManagedEntity {

	private static final int[] PROFICIENCY_BONUS = { 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 5, 6, 6, 6, 6 };
	private Map<CharClass, Integer> classes;

	private List<CharacterWeapon> weapons;

	protected Map<DICE_TYPE, Integer> hitDice;

	private List<ClassFeature> applicableFeatures;
	private List<ClassFeature> activeFeatures;

	protected Map<ClassFeature, Integer> featureCharges;

	protected Map<CharClass, Integer> classResourceCounters;

	private List<SKILL_CHECK> skillProficiencies;

	private CharacterSpellInfo spellSummary = null;
	
	private final Path originalFile;
	
	public PlayerCharacter(String personalName, int str, int dex, int con, int inte, int wis, int cha,
			Map<SLOTLEVEL, List<Spell>> spells, Map<CharClass, Integer> classes, int maxHp, int currentHp,
			List<CharacterWeapon> weapons, List<SKILL_CHECK> skillProficiencies, Path originalFile, Map<Spell.SLOTLEVEL, Integer> restoredSlotsRemaining,
			List<String> activeFeatureNames, Map<String, Integer> classToResource, Map<String, Integer> featureCharges,
			Map<DICE_TYPE, Integer> hitDice) {
		super(personalName, str, dex, con, inte, wis, cha, spells, deriveSlotMapping(classes), maxHp, currentHp, refreshSpellCastingModifier(classes));
		this.classes = classes;
		this.weapons = weapons;
		this.skillProficiencies = skillProficiencies;
		attacksPerTurn = 1;
		refreshSavingThrowProficiencies();
		notifyNewTurn();
		replenishHitDice();
		addClassBonusSpells();
		regenerateFeatureList();
		refreshExtraAttacksPerTurn();
		refreshClassResourceCounters();
		attacksRemaining = attacksPerTurn;
		activeFeatures = new ArrayList<>();
		populateSpellInfo(spells);
		this.originalFile = originalFile;
		
		if(restoredSlotsRemaining != null) {
			restoreSpellSlots(restoredSlotsRemaining);
		}
		if(activeFeatureNames != null) {
			restoreFeatures(activeFeatureNames);
		}
		restoreClassResources(classToResource);
		restoreFeatureCharges(featureCharges);
		restoreHitDice(hitDice);
	}
	
	private void restoreHitDice(Map<DICE_TYPE, Integer> hitDice) {
		for(DICE_TYPE dice : hitDice.keySet()) {
			this.hitDice.put(dice, hitDice.get(dice));
		}
	}
	
	public void levelUp(int hpIncrease, List<ESSENTIAL_ABILITY_SCORE> asis, CharClass cClass, int newLevel) {
		//Fix HP
		maxHP += hpIncrease;
		currentHp += hpIncrease;
		
		//Adjust asis
		for(ESSENTIAL_ABILITY_SCORE asi : asis) {
			if(asi == ESSENTIAL_ABILITY_SCORE.STRENGTH) {
				str++;
			}else if(asi == ESSENTIAL_ABILITY_SCORE.DEXTERITY) {
				dex++;
			}else if(asi == ESSENTIAL_ABILITY_SCORE.CONSTITUTION) {
				con++;
			}else if(asi == ESSENTIAL_ABILITY_SCORE.INTELLIGENCE) {
				inte++;
			}else if(asi == ESSENTIAL_ABILITY_SCORE.WISDOM) {
				wis++;
			}else if(asi == ESSENTIAL_ABILITY_SCORE.CHARISMA) {
				cha++;
			}
		}
		
		//For cClass, do a simple lookup and increment. If cClass level is > 1 and not present, then replaces
		//parent
		if(classes.keySet().contains(cClass) || newLevel == 1) {
			classes.put(cClass, newLevel);
		}else { //It's a new subclass
			if(classes.containsKey(cClass.getRootClass())) {
				classes.remove(cClass.getRootClass());
				classes.put(cClass, newLevel);
			}else {
				throw new IllegalArgumentException("Improper class given, no parent available for replacement");
			}
		}
		
		regenerateSpellSlots(deriveSlotMapping(classes));
		spellcastingMod = refreshSpellCastingModifier(classes);
		refreshSavingThrowProficiencies();
		notifyNewTurn();
		replenishHitDice();
		addClassBonusSpells();
		regenerateFeatureList();
		refreshExtraAttacksPerTurn();
		refreshClassResourceCounters();
		
		PlayerCharacterSaver.saveCharacter(this, originalFile);
	}
	
	private void restoreFeatureCharges(Map<String, Integer> featureCharges) {
		for(String featureName : featureCharges.keySet()) {
			for(ClassFeature feature : applicableFeatures) {
				if(featureName.equals(feature.featureName)) {
					this.featureCharges.put(feature, featureCharges.get(featureName));
				}
			}
		}
	}
	
	private void restoreClassResources(Map<String, Integer> classToResource) {
		for(String rsc : classToResource.keySet()) {
			for(CharClass cClass : classes.keySet()) {
				if(rsc.equals(cClass.getRootClass().name)) {
					classResourceCounters.put(cClass.getRootClass(), classToResource.get(rsc));
				}
			}
		}
	}
	
	private void restoreFeatures(List<String> activeFeatureNames) {
		for(String name : activeFeatureNames) {
			for(ClassFeature feature : applicableFeatures) {
				if(feature.featureName.equals(name)) {
					activeFeatures.add(feature);
				}
			}
		}
	}
	
	//TODO: This object faciliates XML output. homogenize the XML input as well to also use this object to streamline
	//spell info transmission
	private void populateSpellInfo(Map<SLOTLEVEL, List<Spell>> spells) {
		if(spells != null) {
			List<Spell> cantrips = new ArrayList<>();
			List<Spell> preparedSpell = new ArrayList<>();
			for(SLOTLEVEL level : spells.keySet()) {
				List<Spell> spellList = spells.get(level);
				if(spellList != null) {
					if(level == SLOTLEVEL.CANTRIP) {
						cantrips.addAll(spellList);
					}else {
						preparedSpell.addAll(spellList);
					}
				}
			}
			spellSummary = new CharacterSpellInfo(cantrips, preparedSpell, null);
			spellSummary.update(slotsRemaining);
		}
	}
	
	@Override
	public Spell expendSpell(String spellName) {
		Spell spell = super.expendSpell(spellName);
		spellSummary.update(slotsRemaining);
		PlayerCharacterSaver.saveCharacter(this, originalFile);
		return spell;
	}
	
	@Override
	public Spell expendSpell(String spellName, Spell.SLOTLEVEL slotLvl) {
		Spell spell = super.expendSpell(spellName, slotLvl);
		spellSummary.update(slotsRemaining);
		PlayerCharacterSaver.saveCharacter(this, originalFile);
		return spell;
	}
	
	public CharacterSpellInfo getSpellSummary() {
		return spellSummary;
	}

	public int rollSkillCheck(SKILL_CHECK skill) {
		int d20Roll = Dice.d20();
		if (skill.modifier == SKILL_MODIFIER.STR) {
			d20Roll += Helpers.getModifierFromAbility(str);
		} else if (skill.modifier == SKILL_MODIFIER.DEX) {
			d20Roll += Helpers.getModifierFromAbility(dex);
		} else if (skill.modifier == SKILL_MODIFIER.CON) {
			d20Roll += Helpers.getModifierFromAbility(con);
		} else if (skill.modifier == SKILL_MODIFIER.INT) {
			d20Roll += Helpers.getModifierFromAbility(inte);
		} else if (skill.modifier == SKILL_MODIFIER.WIS) {
			d20Roll += Helpers.getModifierFromAbility(wis);
		} else if (skill.modifier == SKILL_MODIFIER.CHA) {
			d20Roll += Helpers.getModifierFromAbility(cha);
		}
		if (skillProficiencies.contains(skill)) {
			d20Roll += getProficiencyBonus();
		}
		return d20Roll;
	}
	
	public List<SKILL_CHECK> getSkillProficiencies(){
		return new ArrayList<>(skillProficiencies);
	}

	public String printResourceCounters() {
		StringBuilder sb = new StringBuilder();
		for (CharClass cClass : classes.keySet()) {
			if (cClass.resource != null) {
				int classPoints = classResourceCounters.get(cClass.getRootClass());
				if (sb.length() != 0) {
					sb.append(System.lineSeparator());
				}
				sb.append(cClass.resource.name + " " + classPoints + " available out of "
						+ cClass.resource.getCharges(classes.get(cClass)));
			}
		}
		if (sb.length() == 0) {
			sb.append("No class resources available");
		}
		return sb.toString();
	}

	private void refreshClassResourceCounters() {
		classResourceCounters = new HashMap<>();
		for (CharClass cClass : classes.keySet()) {
			if (cClass.resource != null) {
				int classPoints = cClass.resource.getCharges(classes.get(cClass));
				classResourceCounters.put(cClass.getRootClass(), classPoints);
			}
		}
	}

	private void regenerateClassResourceCountersLongRest() {
		refreshClassResourceCounters();
	}

	private void regenerateClassResourceCountersShortRest() {
		for (CharClass cClass : classes.keySet()) {
			if (cClass.resource != null && cClass.resource.rechargeInterval == RECHARGE_INTERVAL.SHORT_REST) {
				int classPoints = cClass.resource.getCharges(classes.get(cClass));
				classResourceCounters.put(cClass.getRootClass(), classPoints);
			}
		}
	}

	public List<ClassFeature> getFeatureMeleeBonus() {
		List<ClassFeature> features = new ArrayList<>();
		for (ClassFeature feature : applicableFeatures) {
			if (feature.damageEffect == DAMAGE_EFFECT.MELEE &&
					!feature.isTogglableFeature) {
				features.add(feature);
			}
		}
		for(ClassFeature feature : activeFeatures) {
			if (feature.damageEffect == DAMAGE_EFFECT.MELEE &&
					!features.contains(feature)) {
				features.add(feature);
			}
		}
		return features;
	}

	public String printFeatures() {
		StringBuilder sb = new StringBuilder();
		for (int idx = 0; idx < applicableFeatures.size(); idx++) {
			if (idx > 0) {
				sb.append(System.lineSeparator());
			}
			sb.append(idx + " : " + applicableFeatures.get(idx).featureName);
		}
		return sb.toString();
	}

	public ClassFeature expendFeature(int idx) throws Exception {
		if (idx < 0 || idx > applicableFeatures.size() - 1) {
			throw new Exception("Invalid feature ID");
		}
		ClassFeature feature = applicableFeatures.get(idx);
		if (featureCharges.containsKey(feature)) {
			int charges = featureCharges.get(feature);
			if (charges == 0) {
				throw new Exception("Insufficient charges");
			} else {
				featureCharges.put(feature, charges - 1);
			}

		}
		if (feature.classResourceChargesUsed > 0) {
			CharClass key = feature.getParentClass().getRootClass();
			if (classResourceCounters.containsKey(key)) {
				int chargesAvailable = classResourceCounters.get(key);
				if (chargesAvailable - feature.classResourceChargesUsed < 0) {
					throw new Exception("Insufficient charges for class: " + key.name);
				} else {
					classResourceCounters.put(key, chargesAvailable - feature.classResourceChargesUsed);
				}
			} else {
				throw new Exception("No charges for feature for class: " + key.name);
			}

		}
		if (feature.isTogglableFeature && !activeFeatures.contains(feature)) {
			activeFeatures.add(feature);
		}
		
		PlayerCharacterSaver.saveCharacter(this, originalFile);
		return feature;
	}

	public List<ClassFeature> getActiveFeatures() {
		return new ArrayList<>(activeFeatures);
	}

	public void clearFeature(int idx) {
		if (idx < 0 || idx > activeFeatures.size() - 1) {
			throw new IllegalArgumentException("Not a valid index");
		}
		activeFeatures.remove(idx);
		PlayerCharacterSaver.saveCharacter(this, originalFile);
	}

	public List<ClassFeatureHealingModifier> getFeatureOtherHealingBonus() {
		List<ClassFeatureHealingModifier> features = new ArrayList<>();
		for (ClassFeature feature : applicableFeatures) {
			if (feature.otherHealingMod != null) {
				features.add(feature.otherHealingMod);
			}
		}
		return features;
	}

	public List<ClassFeatureHealingModifier> getFeatureSelfHealingBonus() {
		List<ClassFeatureHealingModifier> features = new ArrayList<>();
		for (ClassFeature feature : applicableFeatures) {
			if (feature.selfHealingMod != null) {
				features.add(feature.selfHealingMod);
			}
		}
		return features;
	}

	private void regenerateFeatureList() {
		applicableFeatures = new ArrayList<>();
		featureCharges = new HashMap<>();
		for (CharClass cClass : classes.keySet()) {
			for (ClassFeature feature : cClass.getFeatures()) {
				if (feature.minimumLevel <= classes.get(cClass)) {
					applicableFeatures.add(feature);
					if (feature.maxCharges > 0) {
						featureCharges.put(feature, feature.maxCharges);
					}
				}
			}
		}
	}

	@Override
	public boolean hasInitiativeAdvantage() {
		for (ClassFeature features : applicableFeatures) {
			if (features.grantInitiativeAdvantage()) {
				return true;
			}
		}
		return false;
	}

	private void addClassBonusSpells() {
		for (CharClass cClass : classes.keySet()) {
			for (Spell spell : cClass.getAutomaticSpells(classes.get(cClass))) {
				addSpell(spell);
			}
		}
	}

	public boolean spendHitDice() {
		int tempHp = this.currentHp;
		for (DICE_TYPE dice : hitDice.keySet()) {
			int diceOfThisTypeLeft = hitDice.get(dice);
			if (diceOfThisTypeLeft > 0) {
				tempHp += dice.roll();
				if (tempHp > maxHP) {
					currentHp = maxHP;
				} else {
					currentHp = tempHp;
				}
				hitDice.put(dice, diceOfThisTypeLeft - 1);
				PlayerCharacterSaver.saveCharacter(this, originalFile);
				return true;
			}
		}
		return false;
	}

	public String printRemainingHitDice() {
		StringBuilder sb = new StringBuilder();
		for (DICE_TYPE dice : hitDice.keySet()) {
			sb.append(dice.toString() + ": " + hitDice.get(dice) + ", ");
		}
		return sb.toString();
	}

	private int attacksRemaining;
	private int attacksPerTurn;

	private void replenishHitDiceLongRest() {
		hitDice = new HashMap<>();
		for (CharClass cClass : classes.keySet()) {
			DICE_TYPE dice = cClass.hitDice;
			if (hitDice.get(dice) == null) {
				hitDice.put(dice, classes.get(cClass) / 2);
			} else {
				int diceCount = hitDice.get(dice);
				diceCount += classes.get(cClass);
				hitDice.put(dice, diceCount / 2);
			}
		}
	}

	public Map<CharClass, Integer> getClassInfo(){
		return new HashMap<>(classes);
	}
	
	@Override
	public String hit(int hp) {
		String retVal = super.hit(hp);
		PlayerCharacterSaver.saveCharacter(this, originalFile);
		return retVal;
	}
	
	@Override
	public void heal(int hp) {
		super.heal(hp);
		PlayerCharacterSaver.saveCharacter(this, originalFile);
	}
	
	private void replenishHitDice() {
		hitDice = new HashMap<>();
		for (CharClass cClass : classes.keySet()) {
			DICE_TYPE dice = cClass.hitDice;
			if (hitDice.get(dice) == null) {
				hitDice.put(dice, classes.get(cClass));
			} else {
				int diceCount = hitDice.get(dice);
				diceCount += classes.get(cClass);
				hitDice.put(dice, diceCount);
			}
		}
	}

	@Override
	public void notifyNewTurn() {
		super.notifyNewTurn();
		attacksRemaining = attacksPerTurn;
	}

	public void takeShortRest() {
		for (ClassFeature feature : featureCharges.keySet()) {
			if (feature.recharge == RECHARGE_DURATION.SHORT_REST) {
				featureCharges.put(feature, feature.maxCharges);
			}
		}
		regenerateClassResourceCountersShortRest();
		PlayerCharacterSaver.saveCharacter(this, originalFile);
	}

	public void takeLongRest() {
		notifyNewTurn();
		currentHp = maxHP;
		replenishSpellSlots();
		replenishHitDiceLongRest();
		for (ClassFeature feature : featureCharges.keySet()) {
			// Both long rest regeneration covers both long and short rests
			featureCharges.put(feature, feature.maxCharges);
		}
		regenerateClassResourceCountersLongRest();
		PlayerCharacterSaver.saveCharacter(this, originalFile);
	}

	public String listAttackOptions() {
		StringBuilder sb = new StringBuilder();

		sb.append("Available attacks: " + attacksRemaining + " out of a max per turn: " + attacksPerTurn);
		sb.append(System.lineSeparator());

		if(canBonusAttack()) {
			if(bonusActedThisTurn) {
				sb.append("Bonus action attack depleted.");
			}else {
				sb.append("Bonus action attack still available and will be automatically used on attack");
			}
			sb.append(System.lineSeparator());
		}
		
		if(canReactAttack()) {
			if(!canTakeReaction()) {
				sb.append("Reaction attack depleted.");
			}else {
				sb.append("Reaction attack still available and will be automatically used on attack");
			}
			sb.append(System.lineSeparator());
		}
		
		for (CharacterWeapon weapon : weapons) {
			if (weapon.isProficient) {
				sb.append("(proficient) ");
			}
			sb.append(weapon.weapon.getReadableInfo());
			sb.append(System.lineSeparator());
		}

		return sb.toString();
	}
	
	public List<CharacterWeapon> getWeaponsInfo(){
		return new ArrayList<>(weapons);
	}
	
	private boolean canBonusAttack() {
		for (ClassFeature feature : activeFeatures) {
			if (feature.allowBonusActionAttack(true)) {
				return true;
			}
		}
		for (ClassFeature feature : applicableFeatures) {
			if (feature.allowBonusActionAttack(false)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean canReactAttack() {
		for (ClassFeature feature : activeFeatures) {
			if (feature.allowReactionAttack(true)) {
				return true;
			}
		}
		for (ClassFeature feature : applicableFeatures) {
			if (feature.allowReactionAttack(false)) {
				return true;
			}
		}
		return false;
	}

	public String attack(String weaponName, boolean throwWeapon, boolean isOpportunityAttack) {
		boolean usingBonusAttack = false;
		boolean usingReaction = false;
		if(isOpportunityAttack) {
			if(canTakeReaction()) {
				usingReaction = true;
			}else {
				return "Character has already used reaction";
			}
		}else {
		if (actedThisTurn) {
			if (attacksRemaining == attacksPerTurn) {
				return "Character has already acted this turn";
			}else if(attacksRemaining == 0) {
				//We've taken an action, and are seeing if we can use a
				//bonus action
				if (!bonusActedThisTurn) {
					usingBonusAttack = canBonusAttack();
				}
				if(!usingBonusAttack && canTakeReaction()) {
					usingReaction = canReactAttack();
				}
			}
		}

		if (attacksRemaining == 0 && !usingBonusAttack & !usingReaction) {
			return "No attacks remaining this turn";
		}
		}

		CharacterWeapon weapon = null;
		for (CharacterWeapon cur : weapons) {
			if (cur.weapon.cname.equalsIgnoreCase(weaponName)) {
				weapon = cur;
			}
		}
		if (weapon == null) {
			return "Character does not have this weapon available";
		}
		try {
			String attackInfo = weapon.weapon.rollAttack(this, weapon.isProficient, throwWeapon);
			if (usingBonusAttack) {
				bonusActedThisTurn = true;
			}else if(usingReaction) {
				reactionsRemaining--;
			} else {
				actedThisTurn = true;
				if (weapon.weapon.getAttributes().contains(WEAPON_ATTRIBUTES.LOADING)) {
					attacksRemaining = 0;
				} else {
					attacksRemaining--;
				}
			}
			return attackInfo;
		} catch (Exception ex) {
			return ex.getMessage();
		}

	}

	private void refreshExtraAttacksPerTurn() {
		for (ClassFeature feature : applicableFeatures) {
			attacksPerTurn += feature.extraAttacksGranted();
		}
	}

	public Integer getSpellSaveDC() {
		if (spellcastingMod == SPELLCASTING_MODIFIER.NA) {
			return null;
		}
		int baseModifier = 8 + getProficiencyBonus() + getSpellcastingModifierValue();
		return baseModifier;
	}

	//TODO: Support automatic saving rolls on hit for things like Relentless Rage or the Orc equivalent
	
	public Integer getSpellToHit() {
		if (spellcastingMod == SPELLCASTING_MODIFIER.NA) {
			return null;
		}
		int baseModifier = getProficiencyBonus();
		if (spellcastingMod == SPELLCASTING_MODIFIER.WISDOM) {
			baseModifier += Helpers.getModifierFromAbility(wis);
		} else if (spellcastingMod == SPELLCASTING_MODIFIER.INTELLIGENCE) {
			baseModifier += Helpers.getModifierFromAbility(inte);
		}
		if (spellcastingMod == SPELLCASTING_MODIFIER.CHARISMA) {
			baseModifier += Helpers.getModifierFromAbility(cha);
		}
		return baseModifier;
	}

	private void refreshSavingThrowProficiencies() {
		for (CharClass cClass : classes.keySet()) {
			for (ESSENTIAL_ABILITY_SCORE prof : cClass.savingThrowProficiencies) {
				switch (prof) {
				case STRENGTH:
					strsave = getProficiencyBonus();
					break;
				case DEXTERITY:
					dexsave = getProficiencyBonus();
					break;
				case CONSTITUTION:
					consave = getProficiencyBonus();
					break;
				case WISDOM:
					wissave = getProficiencyBonus();
					break;
				case INTELLIGENCE:
					intsave = getProficiencyBonus();
					break;
				case CHARISMA:
					chasave = getProficiencyBonus();
					break;
				}
			}
		}
	}

	private static SPELLCASTING_MODIFIER refreshSpellCastingModifier(Map<CharClass, Integer> classes) {
		for (CharClass cClass : classes.keySet()) {
			if (cClass.spellcastingModifier != SPELLCASTING_MODIFIER.NA) {
				return cClass.spellcastingModifier;
			}
		}
		return SPELLCASTING_MODIFIER.NA;
	}

	public int getCharacterLevel() {
		int level = 0;
		for (CharClass cClass : classes.keySet()) {
			level += classes.get(cClass);
		}
		return level;
	}

	@Override
	public int getCasterLevel() {
		return getCharacterLevel();
	}

	public int getProficiencyBonus() {
		return PROFICIENCY_BONUS[getCharacterLevel() - 1];
	}

	private static Map<SLOTLEVEL, Integer> deriveSlotMapping(Map<CharClass, Integer> classes) {
		Map<SLOTLEVEL, Integer> slotMapping = new HashMap<>();
		for (CharClass cClass : classes.keySet()) {
			//Skip this class if they have spells/slots
			if(cClass.slotsPerLevel == null) {
				continue;
			}
			Integer classLevel = classes.get(cClass);
			for (SLOTLEVEL level : SLOTLEVEL.values()) {
				if(cClass.slotsPerLevel.get(classLevel) == null) {
					continue;
				}
				if (slotMapping.containsKey(level)) {
					slotMapping.put(level, slotMapping.get(level) + cClass.slotsPerLevel.get(classLevel).get(level));
				} else {
					if (cClass.slotsPerLevel.get(classLevel) != null) {
						Integer count = cClass.slotsPerLevel.get(classLevel).get(level);
						if (count != null) {
							slotMapping.put(level, cClass.slotsPerLevel.get(classLevel).get(level));
						}
					}
				}
			}
		}
		return slotMapping;
	}

	public int extraCritDice() {
		int critDice = 0;
		for (ClassFeature feature : applicableFeatures) {
			Integer targetLevel = classes.get(feature.parent);
			if (targetLevel == null) {
				for (CharClass cClass : classes.keySet()) {
					if (cClass.getRootClass().name.equals(feature.parent.name)) {
						targetLevel = classes.get(cClass);
					}
				}
			}
			critDice += feature.getExtraCritDice(targetLevel);
		}
		return critDice;
	}
}
