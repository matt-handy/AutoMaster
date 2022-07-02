package handy.rp.xml;

import handy.rp.dnd.ClassFeature;
import handy.rp.dnd.ClassFeatureHealingModifier;
import handy.rp.dnd.ClassFeature.DAMAGE_EFFECT;
import handy.rp.dnd.ClassFeature.RECHARGE_DURATION;
import handy.rp.dnd.ClassFeature.USE_TYPE;
import handy.rp.dnd.ClassResource;
import handy.rp.dnd.ClassResource.RECHARGE_INTERVAL;
import handy.rp.dnd.attacks.CoreDamageComponent;
import handy.rp.dnd.attacks.DamageComponent.DAMAGE_TYPE;
import handy.rp.dnd.character.Proficiency;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import handy.rp.Dice.DICE_TYPE;
import handy.rp.dnd.CharClass;
import handy.rp.dnd.CharClass.ESSENTIAL_ABILITY_SCORE;
import handy.rp.dnd.CharClass.SPELLCASTING_MODIFIER;
import handy.rp.dnd.spells.Spell;
import handy.rp.dnd.spells.Spell.SLOTLEVEL;

public class ClassParser {

	public static List<CharClass> charClasses;

	static {
		try {
			if(charClasses == null) {
				charClasses = ClassParser.loadAll("char_classes");
			}
		} catch (Exception ex) {
			// Shouldn't happen, test loads happen before build.
			// TODO: Add user notification if exception occurs, will be deploy issue
			ex.printStackTrace();
		}
	}

	public static List<CharClass> getBaseCharClasses() {
		return new ArrayList<>(charClasses);
	}

	public static CharClass getCharClass(String name) {
		try {
			if (charClasses == null) {
				charClasses = ClassParser.loadAll("char_classes");
			}
		} catch (Exception ex) {
			// Shouldn't happen, test loads happen before build.
			// TODO: Add user notification if exception occurs, will be deploy issue
			ex.printStackTrace();
		}
		for (CharClass cClass : charClasses) {
			if (cClass.name.equalsIgnoreCase(name)) {
				return cClass;
			}
		}
		return null;
	}

	public static List<CharClass> loadAll(String directory) throws Exception {
		List<CharClass> actions = new ArrayList<>();
		File dir = new File(directory);
		if (dir.isDirectory() && dir.exists()) {
			for (File child : dir.listFiles()) {
				actions.add(load(child.getAbsolutePath()));
			}
		} else {
			throw new IOException("Directory not found: " + directory);
		}

		return actions;
	}

	public static CharClass load(String filename) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(new InputSource(new StringReader(MonsterParser.readFile(filename))));

		NodeList nameList = document.getElementsByTagName("name");
		String name = nameList.item(0).getTextContent();
		DICE_TYPE hitDice = DICE_TYPE.getDice(document.getElementsByTagName("hit_dice").item(0).getTextContent());
		int subClassLevel = Integer
				.parseInt(document.getElementsByTagName("subclass_level_choice").item(0).getTextContent());

		List<CharClass.ESSENTIAL_ABILITY_SCORE> savingThrowProficiencies = new ArrayList<>();
		NodeList profList = document.getElementsByTagName("saving_throw_proficiency");
		for (int idx = 0; idx < profList.getLength(); idx++) {
			String prof = profList.item(idx).getTextContent();
			if (prof.equals("STR")) {
				savingThrowProficiencies.add(ESSENTIAL_ABILITY_SCORE.STRENGTH);
			} else if (prof.equals("DEX")) {
				savingThrowProficiencies.add(ESSENTIAL_ABILITY_SCORE.DEXTERITY);
			} else if (prof.equals("INT")) {
				savingThrowProficiencies.add(ESSENTIAL_ABILITY_SCORE.INTELLIGENCE);
			} else if (prof.equals("CON")) {
				savingThrowProficiencies.add(ESSENTIAL_ABILITY_SCORE.CONSTITUTION);
			} else if (prof.equals("WIS")) {
				savingThrowProficiencies.add(ESSENTIAL_ABILITY_SCORE.WISDOM);
			} else if (prof.equals("CHA")) {
				savingThrowProficiencies.add(ESSENTIAL_ABILITY_SCORE.CHARISMA);
			} else {
				throw new IllegalArgumentException(
						"Class definition does not have a valid Saving Throw Proficiency: " + prof);
			}
		}

		NodeList armorProfList = document.getElementsByTagName("armor_proficiencies");
		List<Proficiency> armorProficiencies = new ArrayList<>();
		if (armorProfList != null && armorProfList.item(0) != null) {
			armorProficiencies = getApplicableProficencies(ProficiencyParser.armorProficiencies,
					(Element) armorProfList.item(0));
		}
		
		
		NodeList toolProfList = document.getElementsByTagName("tool_proficiencies");
		List<Proficiency> toolProfienciesList = new ArrayList<>();
		if (toolProfList != null && toolProfList.item(0) != null) {
			toolProfienciesList = getApplicableProficencies(ProficiencyParser.toolProficiencies,
					(Element) toolProfList.item(0));
		}

		ClassResource resource = null;
		NodeList resourceNode = document.getElementsByTagName("class-resource");
		if (resourceNode != null && resourceNode.item(0) != null) {
			Node resourceNodeXml = resourceNode.item(0);
			Element resourceNodeElem = (Element) resourceNodeXml;
			String rName = resourceNodeElem.getElementsByTagName("name").item(0).getTextContent();
			String rechargeStr = resourceNodeElem.getElementsByTagName("recharge").item(0).getTextContent();
			ClassResource.RECHARGE_INTERVAL recharge = null;
			if (rechargeStr.equals("short")) {
				recharge = RECHARGE_INTERVAL.SHORT_REST;
			} else if (rechargeStr.equals("long")) {
				recharge = RECHARGE_INTERVAL.LONG_REST;
			} else {
				throw new Exception("class resource does not have a valid recharge duration");
			}
			Map<Integer, Integer> charges = new HashMap<>();
			NodeList chargesList = resourceNodeElem.getElementsByTagName("charges");
			for (int jdx = 0; jdx < chargesList.getLength(); jdx++) {
				Node chargeXml = chargesList.item(jdx);
				Element charegElem = (Element) chargeXml;
				int level = Integer.parseInt(charegElem.getElementsByTagName("level").item(0).getTextContent());
				int count = Integer.parseInt(charegElem.getElementsByTagName("count").item(0).getTextContent());
				charges.put(level, count);
			}
			resource = new ClassResource(rName, recharge, charges);
		}

		List<ClassFeature> features = new ArrayList<>();
		NodeList featuresNode = document.getElementsByTagName("feature");
		for (int jdx = 0; jdx < featuresNode.getLength(); jdx++) {
			Node slotsXml = featuresNode.item(jdx);
			Element slotsXmlElem = (Element) slotsXml;
			features.add(getFeature(slotsXmlElem));
		}

		SPELLCASTING_MODIFIER spellcastingModifier = SPELLCASTING_MODIFIER.NA;
		NodeList spellcastingModifierList = document.getElementsByTagName("spellcast_mod");
		if (spellcastingModifierList != null && spellcastingModifierList.item(0) != null) {
			String modStr = spellcastingModifierList.item(0).getTextContent();
			if (modStr.equals("WISDOM")) {
				spellcastingModifier = SPELLCASTING_MODIFIER.WISDOM;
			} else if (modStr.equals("CHARISMA")) {
				spellcastingModifier = SPELLCASTING_MODIFIER.CHARISMA;
			} else if (modStr.equals("INTELLIGENCE")) {
				spellcastingModifier = SPELLCASTING_MODIFIER.INTELLIGENCE;
			}
		}

		Map<Integer, Map<Spell.SLOTLEVEL, Integer>> slotsPerLevel = new HashMap<>();

		NodeList slotLevels = document.getElementsByTagName("slot-level");
		for (int jdx = 0; jdx < slotLevels.getLength(); jdx++) {
			// Map<Spell.SLOTLEVEL, Integer> slots = new HashMap<>();
			Node slotsXml = slotLevels.item(jdx);
			Element slotsXmlElem = (Element) slotsXml;

			NodeList levelNumNode = slotsXmlElem.getElementsByTagName("level");
			String levelString = levelNumNode.item(0).getTextContent();
			Integer level = Integer.parseInt(levelString);

			Map<Spell.SLOTLEVEL, Integer> slots = getSlotLevels(slotsXmlElem);

			slotsPerLevel.put(level, slots);
		}

		CharClass newClass = new CharClass(name, slotsPerLevel, spellcastingModifier, savingThrowProficiencies, hitDice,
				features, resource, subClassLevel, armorProficiencies, toolProfienciesList);
		for (ClassFeature feature : features) {
			feature.setParentClass(newClass);
		}
		return newClass;
	}

	protected static List<Proficiency> getApplicableProficencies(List<Proficiency> canonList, Element listContainer) {
		List<Proficiency> applicables = new ArrayList<>();
		NodeList children = listContainer.getElementsByTagName("proficiency");
		for (int idx = 0; idx < children.getLength(); idx++) {
			String prof = children.item(idx).getTextContent();
			for (Proficiency profiency : canonList) {
				if (prof.equals(profiency.name)) {
					applicables.add(profiency);
				}
			}
		}
		return applicables;
	}

	public static ClassFeature getFeature(Element slotsXmlElem) throws Exception {
		String name = slotsXmlElem.getElementsByTagName("name").item(0).getTextContent();
		String description = slotsXmlElem.getElementsByTagName("description").item(0).getTextContent();
		ClassFeature.USE_TYPE useType = USE_TYPE.NONCOMBAT;
		if (slotsXmlElem.getElementsByTagName("use_type") != null
				&& slotsXmlElem.getElementsByTagName("use_type").item(0) != null) {
			String useTypeStr = slotsXmlElem.getElementsByTagName("use_type").item(0).getTextContent();
			if (useTypeStr.equalsIgnoreCase("action")) {
				useType = USE_TYPE.ACTION;
			} else if (useTypeStr.equalsIgnoreCase("bonus_action")) {
				useType = USE_TYPE.BONUS_ACTION;
			} else if (useTypeStr.equalsIgnoreCase("reaction")) {
				useType = USE_TYPE.REACTION;
			} else if (useTypeStr.equalsIgnoreCase("noncombat")) {
				useType = USE_TYPE.NONCOMBAT;
			} else {
				throw new Exception("Improper feature use type specified: " + useTypeStr);
			}
		}
		Integer classResourceChargesUsed = 0;
		if (slotsXmlElem.getElementsByTagName("charges_used") != null
				&& slotsXmlElem.getElementsByTagName("charges_used").item(0) != null) {
			classResourceChargesUsed = Integer
					.parseInt(slotsXmlElem.getElementsByTagName("charges_used").item(0).getTextContent());
		}

		Integer extraAttacksUnconditional = 0;
		if (slotsXmlElem.getElementsByTagName("extra_unconditional_attacks") != null
				&& slotsXmlElem.getElementsByTagName("extra_unconditional_attacks").item(0) != null) {
			extraAttacksUnconditional = Integer.parseInt(
					slotsXmlElem.getElementsByTagName("extra_unconditional_attacks").item(0).getTextContent());
		}

		boolean toggle = false;
		if (slotsXmlElem.getElementsByTagName("toggle") != null
				&& slotsXmlElem.getElementsByTagName("toggle").item(0) != null) {
			String val = slotsXmlElem.getElementsByTagName("toggle").item(0).getTextContent();

			if (val.equalsIgnoreCase("true")) {
				toggle = true;
			}
		}

		boolean bonusActionAttack = false;
		if (slotsXmlElem.getElementsByTagName("allow_bonus_action_attack") != null
				&& slotsXmlElem.getElementsByTagName("allow_bonus_action_attack").item(0) != null) {
			String val = slotsXmlElem.getElementsByTagName("allow_bonus_action_attack").item(0).getTextContent();

			if (val.equalsIgnoreCase("true")) {
				bonusActionAttack = true;
			}
		}

		boolean reactionAttack = false;
		if (slotsXmlElem.getElementsByTagName("allow_reaction_attack") != null
				&& slotsXmlElem.getElementsByTagName("allow_reaction_attack").item(0) != null) {
			String val = slotsXmlElem.getElementsByTagName("allow_reaction_attack").item(0).getTextContent();

			if (val.equalsIgnoreCase("true")) {
				reactionAttack = true;
			}
		}

		boolean halfDamageCantrip = false;
		if (slotsXmlElem.getElementsByTagName("half_damage_cantrip") != null
				&& slotsXmlElem.getElementsByTagName("half_damage_cantrip").item(0) != null) {
			String val = slotsXmlElem.getElementsByTagName("half_damage_cantrip").item(0).getTextContent();

			if (val.equalsIgnoreCase("true")) {
				halfDamageCantrip = true;
			}
		}

		boolean allowsFreeSpells = false;
		if (slotsXmlElem.getElementsByTagName("allows_free_spells") != null
				&& slotsXmlElem.getElementsByTagName("allows_free_spells").item(0) != null) {
			String val = slotsXmlElem.getElementsByTagName("allows_free_spells").item(0).getTextContent();

			if (val.equalsIgnoreCase("true")) {
				allowsFreeSpells = true;
			}
		}

		boolean initiativeAdvantage = false;
		if (slotsXmlElem.getElementsByTagName("initiative_advantage") != null
				&& slotsXmlElem.getElementsByTagName("initiative_advantage").item(0) != null) {
			String val = slotsXmlElem.getElementsByTagName("initiative_advantage").item(0).getTextContent();

			if (val.equalsIgnoreCase("true")) {
				initiativeAdvantage = true;
			}
		}

		boolean allowsNoPrepSpells = false;
		if (slotsXmlElem.getElementsByTagName("allows_no_prep_spells") != null
				&& slotsXmlElem.getElementsByTagName("allows_no_prep_spells").item(0) != null) {
			String val = slotsXmlElem.getElementsByTagName("allows_no_prep_spells").item(0).getTextContent();

			if (val.equalsIgnoreCase("true")) {
				allowsNoPrepSpells = true;
			}
		}

		boolean recoverSpellSlotsOnShortRest = false;
		if (slotsXmlElem.getElementsByTagName("short_rest_spell_recharge") != null
				&& slotsXmlElem.getElementsByTagName("short_rest_spell_recharge").item(0) != null) {
			String val = slotsXmlElem.getElementsByTagName("short_rest_spell_recharge").item(0).getTextContent();

			if (val.equalsIgnoreCase("true")) {
				recoverSpellSlotsOnShortRest = true;
			}
		}

		int minLevel = Integer.parseInt(slotsXmlElem.getElementsByTagName("minLevel").item(0).getTextContent());

		RECHARGE_DURATION recharge = RECHARGE_DURATION.NA;
		int charges = 0;
		if (slotsXmlElem.getElementsByTagName("recharge-type") != null
				&& slotsXmlElem.getElementsByTagName("recharge-type").item(0) != null) {
			String rechargeStr = slotsXmlElem.getElementsByTagName("recharge-type").item(0).getTextContent();
			if (rechargeStr.equalsIgnoreCase("long")) {
				recharge = RECHARGE_DURATION.LONG_REST;
			} else if (rechargeStr.equalsIgnoreCase("short")) {
				recharge = RECHARGE_DURATION.SHORT_REST;
			} else if (rechargeStr.equalsIgnoreCase("N/A")) {
				recharge = RECHARGE_DURATION.NA;
			} else {
				throw new Exception("Unknown recharge type: " + rechargeStr);
			}
			charges = Integer.parseInt(slotsXmlElem.getElementsByTagName("feature-charges").item(0).getTextContent());
		}

		Map<Integer, Integer> levelsToExtraCritDice = new HashMap<>();
		for (int i = 1; i <= 20; i++) {
			levelsToExtraCritDice.put(i, 0);
		}
		if (slotsXmlElem.getElementsByTagName("extra_crit_dice") != null
				&& slotsXmlElem.getElementsByTagName("extra_crit_dice").item(0) != null) {
			Node xml = slotsXmlElem.getElementsByTagName("extra_crit_dice").item(0);
			Element elem = (Element) xml;

			List<Integer> listOfUppers = new ArrayList<>();
			int count = 1;
			String base = "increase";
			while (elem.getElementsByTagName(base + count) != null
					&& elem.getElementsByTagName(base + count).item(0) != null) {
				int upper = Integer.parseInt(elem.getElementsByTagName(base + count).item(0).getTextContent());
				listOfUppers.add(upper);
				count++;
			}
			int currentDieCount = 0;
			for (int i = minLevel; i <= 20; i++) {
				if (listOfUppers.contains(i)) {
					currentDieCount++;
				}
				levelsToExtraCritDice.put(i, currentDieCount);
			}

		}

		DAMAGE_EFFECT damageEffect = DAMAGE_EFFECT.NA;
		Map<Integer, CoreDamageComponent> levelsToSpecialDamage = null;
		if (slotsXmlElem.getElementsByTagName("augdamages") != null
				&& slotsXmlElem.getElementsByTagName("augdamages").item(0) != null) {
			Node xml = slotsXmlElem.getElementsByTagName("augdamages").item(0);
			Element elem = (Element) xml;
			String hitTypeStr = elem.getElementsByTagName("hittype").item(0).getTextContent();
			levelsToSpecialDamage = new HashMap<>();
			if (hitTypeStr.equalsIgnoreCase("melee")) {
				damageEffect = DAMAGE_EFFECT.MELEE;
			} else if (hitTypeStr.equalsIgnoreCase("ranged")) {
				damageEffect = DAMAGE_EFFECT.RANGED;
			} else if (hitTypeStr.equalsIgnoreCase("N/A")) {
				damageEffect = DAMAGE_EFFECT.NA;
			} else {
				throw new Exception("Unknown damage effect type");
			}
			String base = "dam-level";
			int newDamage = 0;
			for (int i = minLevel; i <= 20; i++) {
				if (elem.getElementsByTagName(base + i) != null
						&& elem.getElementsByTagName(base + i).item(0) != null) {
					Element newLimit = (Element) elem.getElementsByTagName(base + i).item(0);
					newDamage = Integer.parseInt(newLimit.getElementsByTagName("damage").item(0).getTextContent());
				}
				levelsToSpecialDamage.put(i, new CoreDamageComponent(null, null, 0, newDamage));
			}

		}

		if (slotsXmlElem.getElementsByTagName("augdamage") != null
				&& slotsXmlElem.getElementsByTagName("augdamage").item(0) != null) {
			Node augDamageXml = slotsXmlElem.getElementsByTagName("augdamage").item(0);
			Element augDamageElem = (Element) augDamageXml;
			String hitTypeStr = augDamageElem.getElementsByTagName("hittype").item(0).getTextContent();
			levelsToSpecialDamage = new HashMap<>();
			if (hitTypeStr.equalsIgnoreCase("melee")) {
				damageEffect = DAMAGE_EFFECT.MELEE;
			} else if (hitTypeStr.equalsIgnoreCase("ranged")) {
				damageEffect = DAMAGE_EFFECT.RANGED;
			} else if (hitTypeStr.equalsIgnoreCase("N/A")) {
				damageEffect = DAMAGE_EFFECT.NA;
			} else {
				throw new Exception("Unknown damage effect type");
			}
			DAMAGE_TYPE damageType = DAMAGE_TYPE
					.getDamage(augDamageElem.getElementsByTagName("type").item(0).getTextContent());
			DICE_TYPE dice = DICE_TYPE.getDice(augDamageElem.getElementsByTagName("dice").item(0).getTextContent());
			List<Integer> listOfUppers = new ArrayList<>();
			int count = 1;
			String base = "increaseDiceCountLevel";
			while (augDamageElem.getElementsByTagName(base + count) != null
					&& augDamageElem.getElementsByTagName(base + count).item(0) != null) {
				int upper = Integer.parseInt(augDamageElem.getElementsByTagName(base + count).item(0).getTextContent());
				listOfUppers.add(upper);
				count++;
			}
			int currentDieCount = 1;
			for (int i = minLevel; i <= 20; i++) {
				if (listOfUppers.contains(i)) {
					currentDieCount++;
				}
				levelsToSpecialDamage.put(i, new CoreDamageComponent(damageType, dice, currentDieCount, 0));
			}
		}

		ClassFeatureHealingModifier selfHealingModifier = null;
		if (slotsXmlElem.getElementsByTagName("selfHealMod") != null
				&& slotsXmlElem.getElementsByTagName("selfHealMod").item(0) != null) {
			Node xml = slotsXmlElem.getElementsByTagName("selfHealMod").item(0);
			Element elem = (Element) xml;
			selfHealingModifier = getHealingInfo(elem);
		}
		
		int acBonus = 0;
		NodeList acBonusList = slotsXmlElem.getElementsByTagName("ac_bonus");
		if (acBonusList.item(0) != null) {
			acBonus = Integer.parseInt(acBonusList.item(0).getTextContent());
		}

		ClassFeatureHealingModifier otherHealingModifier = null;
		if (slotsXmlElem.getElementsByTagName("otherHealMod") != null
				&& slotsXmlElem.getElementsByTagName("otherHealMod").item(0) != null) {
			Node xml = slotsXmlElem.getElementsByTagName("otherHealMod").item(0);
			Element elem = (Element) xml;
			otherHealingModifier = getHealingInfo(elem);
		}

		ClassFeature newFeature = new ClassFeature(name, description, minLevel, classResourceChargesUsed, damageEffect,
				levelsToSpecialDamage, useType, recharge, charges, otherHealingModifier, selfHealingModifier,
				extraAttacksUnconditional, levelsToExtraCritDice, initiativeAdvantage, toggle, bonusActionAttack,
				reactionAttack, recoverSpellSlotsOnShortRest, halfDamageCantrip, allowsFreeSpells, allowsNoPrepSpells, acBonus);
		return newFeature;
	}

	public static Map<SLOTLEVEL, Integer> getSlotLevels(Element slotsXmlElem) {
		Map<SLOTLEVEL, Integer> slots = new HashMap<>();
		NodeList cantripNode = slotsXmlElem.getElementsByTagName("cantrips");
		if (cantripNode != null && cantripNode.getLength() > 0) {
			String cantrip = cantripNode.item(0).getTextContent();
			Integer cantripNum = Integer.parseInt(cantrip);
			slots.put(SLOTLEVEL.CANTRIP, cantripNum);
		}

		NodeList level1NumNode = slotsXmlElem.getElementsByTagName("slevel1");
		if (level1NumNode != null && level1NumNode.getLength() > 0) {
			String level1String = level1NumNode.item(0).getTextContent();
			Integer level1Slot = Integer.parseInt(level1String);
			slots.put(SLOTLEVEL.ONE, level1Slot);
		}

		NodeList level2NumNode = slotsXmlElem.getElementsByTagName("slevel2");
		if (level2NumNode != null && level2NumNode.getLength() > 0) {
			String level2String = level2NumNode.item(0).getTextContent();
			Integer level2Slot = Integer.parseInt(level2String);
			slots.put(SLOTLEVEL.TWO, level2Slot);
		}

		NodeList level3NumNode = slotsXmlElem.getElementsByTagName("slevel3");
		if (level3NumNode != null && level3NumNode.getLength() > 0) {
			String level3String = level3NumNode.item(0).getTextContent();
			Integer level3Slot = Integer.parseInt(level3String);
			slots.put(SLOTLEVEL.THREE, level3Slot);
		}

		NodeList level4NumNode = slotsXmlElem.getElementsByTagName("slevel4");
		if (level4NumNode != null && level4NumNode.getLength() > 0) {
			String level4String = level4NumNode.item(0).getTextContent();
			Integer level4Slot = Integer.parseInt(level4String);
			slots.put(SLOTLEVEL.FOUR, level4Slot);
		}

		NodeList level5NumNode = slotsXmlElem.getElementsByTagName("slevel5");
		if (level5NumNode != null && level5NumNode.getLength() > 0) {
			String level5String = level5NumNode.item(0).getTextContent();
			Integer level5Slot = Integer.parseInt(level5String);
			slots.put(SLOTLEVEL.FIVE, level5Slot);
		}

		NodeList level6NumNode = slotsXmlElem.getElementsByTagName("slevel6");
		if (level6NumNode != null && level6NumNode.getLength() > 0) {
			String level6String = level6NumNode.item(0).getTextContent();
			Integer level6Slot = Integer.parseInt(level6String);
			slots.put(SLOTLEVEL.SIX, level6Slot);
		}

		NodeList level7NumNode = slotsXmlElem.getElementsByTagName("slevel7");
		if (level7NumNode != null && level7NumNode.getLength() > 0) {
			String level7String = level7NumNode.item(0).getTextContent();
			Integer level7Slot = Integer.parseInt(level7String);
			slots.put(SLOTLEVEL.SEVEN, level7Slot);
		}

		NodeList level8NumNode = slotsXmlElem.getElementsByTagName("slevel8");
		if (level8NumNode != null && level8NumNode.getLength() > 0) {
			String level8String = level8NumNode.item(0).getTextContent();
			Integer level8Slot = Integer.parseInt(level8String);
			slots.put(SLOTLEVEL.EIGHT, level8Slot);
		}

		NodeList level9NumNode = slotsXmlElem.getElementsByTagName("slevel9");
		if (level9NumNode != null && level9NumNode.getLength() > 0) {
			String level9String = level9NumNode.item(0).getTextContent();
			Integer level9Slot = Integer.parseInt(level9String);
			slots.put(SLOTLEVEL.NINE, level9Slot);
		}
		return slots;
	}

	private static ClassFeatureHealingModifier getHealingInfo(Element element) throws Exception {
		int modifier = 0;
		if (element.getElementsByTagName("modifier") != null
				&& element.getElementsByTagName("modifier").item(0) != null) {
			modifier = Integer.parseInt(element.getElementsByTagName("modifier").item(0).getTextContent());
		}
		boolean maxHealDice = false;
		if (element.getElementsByTagName("maxSpellHealDice") != null
				&& element.getElementsByTagName("maxSpellHealDice").item(0) != null) {
			String boolRep = element.getElementsByTagName("maxSpellHealDice").item(0).getTextContent();
			if (boolRep.equalsIgnoreCase("true")) {
				maxHealDice = true;
			} else if (boolRep.equalsIgnoreCase("false")) {
				maxHealDice = false;
			} else {
				throw new Exception("Unknown maxSpellHealDice value: " + boolRep);
			}
		}
		boolean useSpellDice = false;
		if (element.getElementsByTagName("spellLevelIncrease") != null
				&& element.getElementsByTagName("spellLevelIncrease").item(0) != null) {
			String boolRep = element.getElementsByTagName("spellLevelIncrease").item(0).getTextContent();
			if (boolRep.equalsIgnoreCase("true")) {
				useSpellDice = true;
			} else if (boolRep.equalsIgnoreCase("false")) {
				useSpellDice = false;
			} else {
				throw new Exception("Unknown spellLevelIncrease value: " + boolRep);
			}
		}

		return new ClassFeatureHealingModifier(modifier, maxHealDice, useSpellDice);
	}
}
