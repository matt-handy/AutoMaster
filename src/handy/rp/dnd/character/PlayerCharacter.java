package handy.rp.dnd.character;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import handy.rp.Dice;
import handy.rp.Dice.DICE_TYPE;
import handy.rp.OutcomeNotification;
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
import handy.rp.xml.PlayerCharacterParser;

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

	private Set<Proficiency> armorProficiencies;
	private Set<Proficiency> toolProficiencies;

	private List<Spell> knownSpells;

	private int maxCantrips = 0;

	protected Set<GenericFeatureData> featureDataSet;

	public PlayerCharacter(String personalName, int str, int dex, int con, int inte, int wis, int cha,
			Map<SLOTLEVEL, List<Spell>> spells, Map<CharClass, Integer> classes, int maxHp, int currentHp,
			List<CharacterWeapon> weapons, List<SKILL_CHECK> skillProficiencies, Path originalFile,
			Map<Spell.SLOTLEVEL, Integer> restoredSlotsRemaining, List<String> activeFeatureNames,
			Map<String, Integer> classToResource, Map<String, Integer> featureCharges, Map<DICE_TYPE, Integer> hitDice,
			List<Spell> knownSpells, Set<GenericFeatureData> featureDataSet) {
		super(personalName, str, dex, con, inte, wis, cha, spells, deriveSlotMapping(classes), maxHp, currentHp,
				refreshSpellCastingModifier(classes));
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
		updateMaxCantripsKnown();
		validateMaxCantripsCurrently();// Throws exception and will not construct object with too many

		this.originalFile = originalFile;
		this.knownSpells = knownSpells;
		this.featureDataSet = featureDataSet;

		if (restoredSlotsRemaining != null) {
			restoreSpellSlots(restoredSlotsRemaining);
		}
		if (activeFeatureNames != null) {
			restoreFeatures(activeFeatureNames);
		}
		restoreClassResources(classToResource);
		restoreFeatureCharges(featureCharges);
		restoreHitDice(hitDice);
		regenerateProficiencies();
		populateFeatureBasedFreeSpells();
	}

	public int getMaxCantrips() {
		return maxCantrips;
	}

	private void updateMaxCantripsKnown() {
		int temp = 0;

		for (CharClass cClass : classes.keySet()) {
			int classLevel = classes.get(cClass);
			if (cClass.slotsPerLevel != null && cClass.slotsPerLevel.get(classLevel) != null) {
				temp += cClass.slotsPerLevel.get(classLevel).get(SLOTLEVEL.CANTRIP);
			}
		}

		maxCantrips = temp;
	}

	private void validateMaxCantripsCurrently() {
		if (spellSummary.getKnownCantrips().size() > maxCantrips) {
			throw new IllegalArgumentException(
					"Character " + personalName + " created with too many cantrips for level: "
							+ spellSummary.getKnownCantrips().size() + " vs allowed " + maxCantrips);
		}
	}

	private void populateFeatureBasedFreeSpells() {
		for (ClassFeature feature : applicableFeatures) {
			if (feature.allowsNoPrepSpells) {
				for (GenericFeatureData gfd : featureDataSet) {
					if (gfd.featureName.equals(feature.featureName)) {
						for (String spellName : gfd.getFeatureData()) {
							for (Spell spell : knownSpells) {
								if (spell.computerName.equals(spellName)) {
									PlayerCharacterParser.addSpell(spells, spell);
									PlayerCharacterParser.addSpell(freeSpells, spell);
								}
							}
						}
					}
				}
			}
		}
	}

	@Override
	public boolean hasFeatureToIgnoreSpellCast(String spellName) {

		List<ClassFeature> desiredFeatures = new ArrayList<>();
		for (ClassFeature feature : applicableFeatures) {
			if (feature.allowsFreeSpells) {
				desiredFeatures.add(feature);
			}
		}
		for (ClassFeature feature : desiredFeatures) {
			if (hasSpellInFeatureDataset(feature.featureName, spellName)) {
				return true;
			}
		}
		return false;
	}

	private boolean hasSpellInFeatureDataset(String featureName, String spellName) {
		for (GenericFeatureData gfd : featureDataSet) {
			if (gfd.featureName.equals(featureName) && gfd.hasFeatureDataString(spellName)) {
				return true;
			}
		}
		return false;
	}

	public String printSpellsKnown() {
		if (knownSpells.size() == 0) {
			return "No spells known";
		} else {
			StringBuilder sb = new StringBuilder();
			for (Spell spell : knownSpells) {
				sb.append(
						spell.readableName + " (Level " + spell.minimumLevel.toString() + ")" + System.lineSeparator());
			}
			return sb.toString();
		}
	}

	public OutcomeNotification learnSpell(String spellName) {
		Spell newSpell = null;
		for (Spell spell : PlayerCharacterParser.spellsList) {
			if (spell.computerName.equals(spellName)) {
				newSpell = spell;
			}
		}
		if (newSpell == null) {
			return new OutcomeNotification("Spell not found", false);
		} else if (knownSpells.contains(newSpell) || spellSummary.getKnownCantrips().contains(newSpell)) {
			return new OutcomeNotification("Spell already known", false);
		} else {
			if (newSpell.minimumLevel == SLOTLEVEL.CANTRIP) {
				spells.get(SLOTLEVEL.CANTRIP).add(newSpell);
			} else {
				knownSpells.add(newSpell);
			}

			populateSpellInfo(spells);
			PlayerCharacterSaver.saveCharacter(this, originalFile);
			return new OutcomeNotification("Spell learned: " + newSpell.readableName, true);
		}
	}

	public OutcomeNotification swapPreparedSpell(String oldSpell, String newSpell) {
		Spell newPreparedSpell = getSpellFromKnownSpellsByName(newSpell);
		if (newPreparedSpell == null) {
			return new OutcomeNotification("Unknown spell: " + newSpell, false);
		}
		if (newPreparedSpell.minimumLevel == SLOTLEVEL.CANTRIP) {
			return new OutcomeNotification("Cannot swap cantrip: " + newSpell, false);
		}
		boolean foundRemovedOldSpell = false;
		for (SLOTLEVEL level : spells.keySet()) {
			Spell toRemove = null;
			for (Spell spell : spells.get(level)) {
				if (spell.computerName.equals(oldSpell)) {
					toRemove = spell;
					if (toRemove.minimumLevel == SLOTLEVEL.CANTRIP) {
						return new OutcomeNotification("Cannot swap cantrip: " + oldSpell, false);
					}
				}
			}
			if (toRemove != null) {
				spells.get(level).remove(toRemove);
				foundRemovedOldSpell = true;
			}
		}
		if (!foundRemovedOldSpell) {
			return new OutcomeNotification("Spell not prepared: " + oldSpell, false);
		}
		spells.get(newPreparedSpell.minimumLevel).add(newPreparedSpell);
		populateSpellInfo(spells);
		PlayerCharacterSaver.saveCharacter(this, originalFile);
		return new OutcomeNotification("Spell prepared: " + newPreparedSpell.readableName, true);
	}

	private boolean addSlots(int slotNumber, int slotCount) {
		SLOTLEVEL slotLevel = SLOTLEVEL.get(slotNumber);
		if (maxSpellSlots.get(slotLevel) == null) {
			return false;
		}
		if (slotsRemaining.get(slotLevel) + slotCount > maxSpellSlots.get(slotLevel)) {
			return false;
		} else {
			slotsRemaining.put(slotLevel, slotsRemaining.get(slotLevel) + slotCount);
			return true;
		}
	}

	public void spellSlotRecoveryWizard(ClassFeature feature, BufferedReader br, PrintWriter pw) throws IOException {
		pw.println("Welcome to the Spell Slot Recovery Wizard");
		// Get total number of slots available for recovery
		int availableForRecovery = getCasterLevel() / 2;
		int recovered = 0;
		pw.println(
				"Enter the slots you would like to recover, such as: 'slot 1 2' to recover two #1 slots, or 'slot 2' to recover a single #2 slot");
		pw.println("Enter 'quit' to return to play");
		String line = br.readLine();
		while (!line.equalsIgnoreCase("quit") && availableForRecovery > recovered) {
			String elements[] = line.split(" ");
			if (elements.length == 2) {
				try {
					int desiredSlot = Integer.parseInt(elements[1]);
					if (recovered + (1 * desiredSlot) > availableForRecovery) {
						pw.println("Insufficient recovery slots available");
					} else {
						if (addSlots(desiredSlot, 1)) {
							pw.println("Slots recovered");
							recovered += (1 * desiredSlot);
						} else {
							pw.println("Unable to recover desired slots");
						}
					}
				} catch (NumberFormatException ex) {
					pw.println("Unknown command: invalid number format");
				}
			} else if (elements.length == 3) {
				try {
					int desiredSlot = Integer.parseInt(elements[1]);
					int desiredSlotCount = Integer.parseInt(elements[2]);
					if (recovered + (desiredSlotCount * desiredSlot) > availableForRecovery) {
						pw.println("Insufficient recovery slots available");
					} else {
						if (addSlots(desiredSlot, desiredSlotCount)) {
							pw.println("Slots recovered");
							recovered += desiredSlotCount * desiredSlot;
						} else {
							pw.println("Unable to recover desired slots");
						}
					}
				} catch (NumberFormatException ex) {
					pw.println("Unknown command: invalid number format");
				}
			} else {
				pw.println("Unknown command");
			}
			line = br.readLine();
		}
		pw.println("Your slot work is complete");
	}
	
	private Spell getSpellFromKnownSpellsByName(String spellName) {
		for (CharClass cClass : classes.keySet()) {
			if (cClass.getRootClass().name.equalsIgnoreCase("wizard")) {
				for (Spell spell : knownSpells) {
					if (spell.computerName.equals(spellName)) {
						return spell;
					}
				}
			}else {
				for(Spell spell : Spell.getAllSpellsForCharClass(cClass)) {
					if (spell.computerName.equals(spellName)) {
						return spell;
					}
				}
			}
		}
		return null;
	}

	public OutcomeNotification addPreparedSpell(String spellName) {
		Spell newPreparedSpell = getSpellFromKnownSpellsByName(spellName);
		
		if (newPreparedSpell == null) {
			return new OutcomeNotification("Unknown spell: " + spellName, false);
		}
		if (currentSpellsPrepared() >= maxSpellsToPrepare()) {
			return new OutcomeNotification("Player already has maximum spell number prepared", false);
		}
		spells.get(newPreparedSpell.minimumLevel).add(newPreparedSpell);
		populateSpellInfo(spells);
		PlayerCharacterSaver.saveCharacter(this, originalFile);
		return new OutcomeNotification("Spell prepared: " + newPreparedSpell.readableName, true);
	}

	public int currentSpellsPrepared() {
		int currentCount = getSpellsCount(spells);
		
		int currentCountFreeSpells = getSpellsCount(freeSpells);
		
		return currentCount - currentCountFreeSpells;
	}
	
	private int getSpellsCount(Map<SLOTLEVEL, List<Spell>> localspells) {
		int currentCount = 0;
		for (SLOTLEVEL lvl : localspells.keySet()) {
			if (lvl != SLOTLEVEL.CANTRIP) {
				currentCount += localspells.get(lvl).size();
			}
		}
		return currentCount;
	}

	public int maxSpellsToPrepare() {
		int prepareables = 0;
		for (CharClass cClass : classes.keySet()) {
			if (cClass.spellcastingModifier != SPELLCASTING_MODIFIER.NA) {
				prepareables += classes.get(cClass);
				if (cClass.spellcastingModifier == SPELLCASTING_MODIFIER.CHARISMA) {
					prepareables += Helpers.getModifierFromAbility(cha);
				} else if (cClass.spellcastingModifier == SPELLCASTING_MODIFIER.WISDOM) {
					prepareables += Helpers.getModifierFromAbility(wis);
				} else {// Intelligence
					prepareables += Helpers.getModifierFromAbility(inte);
				}
			}
		}
		return prepareables;
	}

	private void regenerateProficiencies() {
		armorProficiencies = new HashSet<>();
		toolProficiencies = new HashSet<>();
		for (CharClass cClass : classes.keySet()) {
			if (!cClass.getRootClass().name.equals(cClass.name)) {
				for (Proficiency prof : cClass.getRootClass().getArmorProficiencies()) {
					armorProficiencies.add(prof);
				}
				for (Proficiency prof : cClass.getRootClass().getToolProficiencies()) {
					toolProficiencies.add(prof);
				}
			}
			for (Proficiency prof : cClass.getArmorProficiencies()) {
				armorProficiencies.add(prof);
			}
			for (Proficiency prof : cClass.getToolProficiencies()) {
				toolProficiencies.add(prof);
			}
		}
	}

	public Set<Proficiency> getArmorProficiencies() {
		return new HashSet<>(armorProficiencies);
	}

	public Set<Proficiency> getToolProficiencies() {
		return new HashSet<>(toolProficiencies);
	}

	private void restoreHitDice(Map<DICE_TYPE, Integer> hitDice) {
		for (DICE_TYPE dice : hitDice.keySet()) {
			this.hitDice.put(dice, hitDice.get(dice));
		}
	}

	public void levelUp(int hpIncrease, List<ESSENTIAL_ABILITY_SCORE> asis, CharClass cClass, int newLevel) {
		// Fix HP
		maxHP += hpIncrease;
		currentHp += hpIncrease;

		// Adjust asis
		for (ESSENTIAL_ABILITY_SCORE asi : asis) {
			if (asi == ESSENTIAL_ABILITY_SCORE.STRENGTH) {
				str++;
			} else if (asi == ESSENTIAL_ABILITY_SCORE.DEXTERITY) {
				dex++;
			} else if (asi == ESSENTIAL_ABILITY_SCORE.CONSTITUTION) {
				con++;
			} else if (asi == ESSENTIAL_ABILITY_SCORE.INTELLIGENCE) {
				inte++;
			} else if (asi == ESSENTIAL_ABILITY_SCORE.WISDOM) {
				wis++;
			} else if (asi == ESSENTIAL_ABILITY_SCORE.CHARISMA) {
				cha++;
			}
		}

		// For cClass, do a simple lookup and increment. If cClass level is > 1 and not
		// present, then replaces
		// parent
		if (classes.keySet().contains(cClass) || newLevel == 1) {
			classes.put(cClass, newLevel);
		} else { // It's a new subclass
			if (classes.containsKey(cClass.getRootClass())) {
				classes.remove(cClass.getRootClass());
				classes.put(cClass, newLevel);
			} else {
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
		regenerateProficiencies();
		updateMaxCantripsKnown();

		PlayerCharacterSaver.saveCharacter(this, originalFile);
	}

	private void restoreFeatureCharges(Map<String, Integer> featureCharges) {
		for (String featureName : featureCharges.keySet()) {
			for (ClassFeature feature : applicableFeatures) {
				if (featureName.equals(feature.featureName)) {
					this.featureCharges.put(feature, featureCharges.get(featureName));
				}
			}
		}
	}

	private void restoreClassResources(Map<String, Integer> classToResource) {
		for (String rsc : classToResource.keySet()) {
			for (CharClass cClass : classes.keySet()) {
				if (rsc.equals(cClass.getRootClass().name)) {
					classResourceCounters.put(cClass.getRootClass(), classToResource.get(rsc));
				}
			}
		}
	}

	private void restoreFeatures(List<String> activeFeatureNames) {
		for (String name : activeFeatureNames) {
			for (ClassFeature feature : applicableFeatures) {
				if (feature.featureName.equals(name)) {
					activeFeatures.add(feature);
				}
			}
		}
	}

	// TODO: This object faciliates XML output. homogenize the XML input as well to
	// also use this object to streamline
	// spell info transmission
	private void populateSpellInfo(Map<SLOTLEVEL, List<Spell>> spells) {
		if (spells != null) {
			List<Spell> cantrips = new ArrayList<>();
			List<Spell> preparedSpell = new ArrayList<>();
			for (SLOTLEVEL level : spells.keySet()) {
				List<Spell> spellList = spells.get(level);
				if (spellList != null) {
					if (level == SLOTLEVEL.CANTRIP) {
						cantrips.addAll(spellList);
					} else {
						preparedSpell.addAll(spellList);
					}
				}
			}
			spellSummary = new CharacterSpellInfo(cantrips, preparedSpell, knownSpells);
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

	public boolean featuresAllowHalfDamageCantrip() {
		for (ClassFeature feature : applicableFeatures) {
			if (feature.halfDamageCantrip) {
				return true;
			}
		}
		return false;
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

	public List<SKILL_CHECK> getSkillProficiencies() {
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
			if (feature.damageEffect == DAMAGE_EFFECT.MELEE && !feature.isTogglableFeature) {
				features.add(feature);
			}
		}
		for (ClassFeature feature : activeFeatures) {
			if (feature.damageEffect == DAMAGE_EFFECT.MELEE && !features.contains(feature)) {
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
				addSpell(spell, spells);
				addSpell(spell, freeSpells);
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

	public Map<CharClass, Integer> getClassInfo() {
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

		if (canBonusAttack()) {
			if (bonusActedThisTurn) {
				sb.append("Bonus action attack depleted.");
			} else {
				sb.append("Bonus action attack still available and will be automatically used on attack");
			}
			sb.append(System.lineSeparator());
		}

		if (canReactAttack()) {
			if (!canTakeReaction()) {
				sb.append("Reaction attack depleted.");
			} else {
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

	public List<CharacterWeapon> getWeaponsInfo() {
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

	public OutcomeNotification makeTempPlusWeapon(String weaponName, int plusModifier) {
		CharacterWeapon weapon = null;
		for (CharacterWeapon cur : weapons) {
			if (cur.weapon.cname.equalsIgnoreCase(weaponName)) {
				weapon = cur;
			}
		}
		if (weapon == null) {
			return new OutcomeNotification("Character does not have this weapon available", false);
		} else {
			weapon.setTempPlusWeaponMod(plusModifier);
			return new OutcomeNotification("Character weapon modifier has been set", true);
		}
	}

	public OutcomeNotification resetTempPlusWeapon(String weaponName) {
		CharacterWeapon weapon = null;
		for (CharacterWeapon cur : weapons) {
			if (cur.weapon.cname.equalsIgnoreCase(weaponName)) {
				weapon = cur;
			}
		}
		if (weapon == null) {
			return new OutcomeNotification("Character does not have this weapon available", false);
		} else {
			weapon.resetTempPlusWeapon();
			return new OutcomeNotification("Character weapon modifier has been reset", true);
		}
	}

	public OutcomeNotification attack(String weaponName, boolean throwWeapon, boolean isOpportunityAttack) {
		boolean usingBonusAttack = false;
		boolean usingReaction = false;
		if (isOpportunityAttack) {
			if (canTakeReaction()) {
				usingReaction = true;
			} else {
				return new OutcomeNotification("Character has already used reaction", false);
			}
		} else {
			if (actedThisTurn) {
				if (attacksRemaining == attacksPerTurn) {
					return new OutcomeNotification("Character has already acted this turn", false);
				} else if (attacksRemaining == 0) {
					// We've taken an action, and are seeing if we can use a
					// bonus action
					if (!bonusActedThisTurn) {
						usingBonusAttack = canBonusAttack();
					}
					if (!usingBonusAttack && canTakeReaction()) {
						usingReaction = canReactAttack();
					}
				}
			}

			if (attacksRemaining == 0 && !usingBonusAttack & !usingReaction) {
				return new OutcomeNotification("No attacks remaining this turn", false);
			}
		}

		CharacterWeapon weapon = null;
		for (CharacterWeapon cur : weapons) {
			if (cur.weapon.cname.equalsIgnoreCase(weaponName)) {
				weapon = cur;
			}
		}
		if (weapon == null) {
			return new OutcomeNotification("Character does not have this weapon available", false);
		}
		try {
			String attackInfo = weapon.weapon.rollAttack(this, weapon.isProficient, throwWeapon,
					weapon.getCurrentPlusWeaponMod());
			if (usingBonusAttack) {
				bonusActedThisTurn = true;
			} else if (usingReaction) {
				reactionsRemaining--;
			} else {
				actedThisTurn = true;
				if (weapon.weapon.getAttributes().contains(WEAPON_ATTRIBUTES.LOADING)) {
					attacksRemaining = 0;
				} else {
					attacksRemaining--;
				}
			}
			return new OutcomeNotification(attackInfo, true);
		} catch (Exception ex) {
			return new OutcomeNotification(ex.getMessage(), false);
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

	// TODO: Support automatic saving rolls on hit for things like Relentless Rage
	// or the Orc equivalent

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
			// Skip this class if they have spells/slots
			if (cClass.slotsPerLevel == null) {
				continue;
			}
			Integer classLevel = classes.get(cClass);
			for (SLOTLEVEL level : SLOTLEVEL.values()) {
				if (cClass.slotsPerLevel.get(classLevel) == null) {
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

	public String printProficiencies() {
		StringBuilder sb = new StringBuilder();
		sb.append("Skill Proficiencies" + System.lineSeparator());
		for (SKILL_CHECK skill : skillProficiencies) {
			sb.append(skill.name() + System.lineSeparator());
		}
		sb.append("Armor Proficiencies" + System.lineSeparator());
		for (Proficiency prof : armorProficiencies) {
			sb.append(prof.name + System.lineSeparator());
		}
		sb.append("Tool Proficiencies" + System.lineSeparator());
		for (Proficiency prof : toolProficiencies) {
			sb.append(prof.name + System.lineSeparator());
		}
		return sb.toString();
	}
}
