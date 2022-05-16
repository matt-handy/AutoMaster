package handy.rp.xml;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import handy.rp.Dice.DICE_TYPE;
import handy.rp.dnd.CharClass;
import handy.rp.dnd.attacks.DamageComponent.DAMAGE_TYPE;
import handy.rp.dnd.spells.ActionSpell;
import handy.rp.dnd.spells.DiceToLevelRange;
import handy.rp.dnd.spells.Spell;
import handy.rp.dnd.spells.SpellDamageComponent;
import handy.rp.dnd.spells.SpellHealingComponent;
import handy.rp.dnd.spells.Spell.SLOTLEVEL;

public class SpellParser {

	public static final String CLASSES_XML = "classes";

	public static Spell load(String filename, boolean actionSpell) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(new InputSource(new StringReader(readFile(filename))));

		NodeList compNameList = document.getElementsByTagName("compname");
		String compName = compNameList.item(0).getTextContent();

		NodeList readableNameList = document.getElementsByTagName("readablename");
		String readableName = readableNameList.item(0).getTextContent();

		NodeList minLevelList = document.getElementsByTagName("minLevel");
		int minLevel = Integer.parseInt(minLevelList.item(0).getTextContent());

		NodeList hasDcList = document.getElementsByTagName("hasDc");
		boolean hasDc = hasDcList.item(0).getTextContent().equalsIgnoreCase("true");

		NodeList noDamageOnSaveList = document.getElementsByTagName("noDamageOnSave");
		boolean noDamageOnSave = false;
		if (noDamageOnSaveList.item(0) != null) {
			noDamageOnSave = noDamageOnSaveList.item(0).getTextContent().equalsIgnoreCase("true");
		}

		NodeList hasToHitList = document.getElementsByTagName("hasToHit");
		boolean hasToHit = hasToHitList.item(0).getTextContent().equalsIgnoreCase("true");

		NodeList concentrateList = document.getElementsByTagName("concentration");
		boolean concentrate = false;

		NodeList bonusActionList = document.getElementsByTagName("bonusAction");
		boolean bonusAction = false;

		List<CharClass> applicableClasses = new ArrayList<>();
		if (document.getElementsByTagName(CLASSES_XML).item(0) != null) {
			String listOfClasses = document.getElementsByTagName(CLASSES_XML).item(0).getTextContent();
			for (String className : listOfClasses.split(",")) {
				CharClass cClass = ClassParser.getCharClass(className);
				if (cClass != null) {
					applicableClasses.add(cClass);
				}
			}
		}

		int dc = -1;
		int toHit = -1;
		if (actionSpell) {
			Node dcNode = document.getElementsByTagName("dc").item(0);
			if (dcNode != null) {
				dc = Integer.parseInt(dcNode.getTextContent());
			}

			Node toHitNode = document.getElementsByTagName("toHit").item(0);
			if (toHitNode != null) {
				toHit = Integer.parseInt(toHitNode.getTextContent());
			}
		}

		if (concentrateList != null && concentrateList.item(0) != null) {
			concentrate = concentrateList.item(0).getTextContent().equalsIgnoreCase("true");
		}

		if (bonusActionList != null && bonusActionList.item(0) != null) {
			bonusAction = bonusActionList.item(0).getTextContent().equalsIgnoreCase("true");
		}

		NodeList readableEffectList = document.getElementsByTagName("readableeffect");
		String readableEffect = readableEffectList.item(0).getTextContent();

		NodeList recurringDamagesList = document.getElementsByTagName("recurring_damages_type");
		Spell.RECURRING_DAMAGE_TYPE recurringType = null;
		if (recurringDamagesList != null && recurringDamagesList.item(0) != null) {
			String recurringDamageTypeStr = recurringDamagesList.item(0).getTextContent();
			if (recurringDamageTypeStr.equals("bonus")) {
				recurringType = Spell.RECURRING_DAMAGE_TYPE.BONUS;
			} else if (recurringDamageTypeStr.equals("action")) {
				recurringType = Spell.RECURRING_DAMAGE_TYPE.ACTION;
			} else if (recurringDamageTypeStr.equals("automatic")) {
				recurringType = Spell.RECURRING_DAMAGE_TYPE.AUTOMATIC;
			}
		}

		SpellHealingComponent healingComponent = null;
		NodeList healingList = document.getElementsByTagName("healing");
		if (healingList != null && healingList.item(0) != null) {
			Node healingListItem = healingList.item(0);
			Element healingListElement = (Element) healingListItem;

			DICE_TYPE dice = DICE_TYPE
					.getDice(healingListElement.getElementsByTagName("dice").item(0).getTextContent());
			int diceCount = Integer
					.parseInt(healingListElement.getElementsByTagName("diceCount").item(0).getTextContent());
			String useSpellMod = healingListElement.getElementsByTagName("modifier").item(0).getTextContent();
			boolean useSpellModifer = false;
			if (useSpellMod.equals("SPELLCAST")) {
				useSpellModifer = true;
			}
			String extraDicePerLevel = healingListElement.getElementsByTagName("extraDicePerLevel").item(0)
					.getTextContent();
			boolean extraDicePerLevelB = false;
			if (extraDicePerLevel.equalsIgnoreCase("true")) {
				extraDicePerLevelB = true;
			}

			healingComponent = new SpellHealingComponent(dice, useSpellModifer, diceCount, extraDicePerLevelB);
		}

		NodeList damagesList = document.getElementsByTagName("damages");

		Map<SLOTLEVEL, List<SpellDamageComponent>> damagers = null;

		Node damageListItem = damagesList.item(0);
		Element damageListElement = (Element) damageListItem;

		// Will be null if there is no <damagers> tag
		if (damageListElement != null) {

			damagers = new HashMap<>();

			String damageStrs[] = { "damage", "damage1", "damage2" };

			for (String damage : damageStrs) {
				getDamage(damageListElement, damage, damagers, minLevel);

			}

		}

		NodeList altDamagesList = document.getElementsByTagName("altdamages");
		Map<SLOTLEVEL, List<SpellDamageComponent>> altDamagers = null;
		Node altDamageListItem = altDamagesList.item(0);
		Element altDamageListElement = (Element) altDamageListItem;
		if (altDamageListElement != null) {
			altDamagers = new HashMap<>();
			getDamage(altDamageListElement, "altdamage", altDamagers, minLevel);
		}

		if (actionSpell) {
			return new ActionSpell(compName, readableName, Spell.SLOTLEVEL.CANTRIP, hasDc, hasToHit, damagers,
					readableEffect, concentrate, dc, toHit, healingComponent, altDamagers, noDamageOnSave,
					applicableClasses);
		} else {
			/* 
			 * TODO Reactivate this case. Right now some spells are defined, but the parent classes aren't there yet.
			 * Need to implement those character classes first. 
			if(applicableClasses.isEmpty()) {
				throw new IllegalArgumentException("Spell: " + readableName + " must have at least one applicable class");
			}
			*/
			return new Spell(compName, readableName, Spell.SLOTLEVEL.get(minLevel), hasDc, hasToHit, damagers,
					readableEffect, concentrate, bonusAction, recurringType, healingComponent, altDamagers,
					noDamageOnSave, applicableClasses);
		}

	}

	private static void getDamage(Element parentElement, String damageTagName,
			Map<SLOTLEVEL, List<SpellDamageComponent>> damagers, int minLevel) {
		NodeList damageNodeLists = parentElement.getElementsByTagName(damageTagName);

		Node damageItem = damageNodeLists.item(0);
		Element damageElement = (Element) damageItem;

		if (damageElement == null) {
			return;
		}

		DICE_TYPE dice = DICE_TYPE.getDice(damageElement.getElementsByTagName("dice").item(0).getTextContent());
		DAMAGE_TYPE damageType = DAMAGE_TYPE
				.getDamage(damageElement.getElementsByTagName("type").item(0).getTextContent());
		int diceCount = Integer.parseInt(damageElement.getElementsByTagName("diceCount").item(0).getTextContent());
		int modifier = Integer.parseInt(damageElement.getElementsByTagName("modifier").item(0).getTextContent());
		boolean hasDieIncreasePerLevel = false;
		try {
			hasDieIncreasePerLevel = damageElement.getElementsByTagName("extraDicePerLevel").item(0).getTextContent()
					.equalsIgnoreCase("true");
		} catch (Exception ex) {
			// No incremental jump defined, continue
		}

		boolean hasStdCantripIncrease = false;
		try {
			hasStdCantripIncrease = damageElement.getElementsByTagName("stdCantripIncrease").item(0).getTextContent()
					.equalsIgnoreCase("true");
		} catch (Exception ex) {
			// No incremental jump defined, continue
		}

		if (hasDieIncreasePerLevel) {
			for (int kdx = minLevel; kdx <= 9; kdx++) {
				DiceToLevelRange dtr = new DiceToLevelRange(minLevel, Integer.MAX_VALUE, diceCount + kdx - minLevel);
				List<DiceToLevelRange> ranges = new ArrayList<>();
				ranges.add(dtr);
				SpellDamageComponent sdc = new SpellDamageComponent(damageType, dice, modifier, -1, ranges);
				if (damagers.containsKey(Spell.SLOTLEVEL.get(kdx))) {
					damagers.get(Spell.SLOTLEVEL.get(kdx)).add(sdc);
				} else {
					List<SpellDamageComponent> newComps = new ArrayList<>();
					newComps.add(sdc);
					damagers.put(Spell.SLOTLEVEL.get(kdx), newComps);
				}

			}
		} else {
			if (hasStdCantripIncrease) {
				List<DiceToLevelRange> ranges = new ArrayList<>();
				ranges.add(new DiceToLevelRange(0, 4, diceCount));
				ranges.add(new DiceToLevelRange(5, 10, diceCount + 1));
				ranges.add(new DiceToLevelRange(11, 16, diceCount + 2));
				ranges.add(new DiceToLevelRange(17, Integer.MAX_VALUE, diceCount + 3));
				SpellDamageComponent sdc = new SpellDamageComponent(damageType, dice, modifier, -1, ranges);
				for (int kdx = minLevel; kdx <= 9; kdx++) {
					if (damagers.containsKey(Spell.SLOTLEVEL.get(kdx))) {
						damagers.get(Spell.SLOTLEVEL.get(kdx)).add(sdc);
					} else {
						List<SpellDamageComponent> newComps = new ArrayList<>();
						newComps.add(sdc);
						damagers.put(Spell.SLOTLEVEL.get(kdx), newComps);
					}
				}
			} else {
				for (int kdx = minLevel; kdx <= 9; kdx++) {
					DiceToLevelRange dtr = new DiceToLevelRange(minLevel, Integer.MAX_VALUE, diceCount);
					List<DiceToLevelRange> ranges = new ArrayList<>();
					ranges.add(dtr);
					SpellDamageComponent sdc = new SpellDamageComponent(damageType, dice, modifier, -1, ranges);
					if (damagers.containsKey(Spell.SLOTLEVEL.get(kdx))) {
						damagers.get(Spell.SLOTLEVEL.get(kdx)).add(sdc);
					} else {
						List<SpellDamageComponent> newComps = new ArrayList<>();
						newComps.add(sdc);
						damagers.put(Spell.SLOTLEVEL.get(kdx), newComps);
					}
				}
			}
		}
	}

	public static List<Spell> loadAllSpells(String directory) throws Exception {
		return loadAll(directory, false);
	}

	public static List<ActionSpell> loadAllActionSpells(String directory) throws Exception {
		List<ActionSpell> actionSpells = new ArrayList<>();
		for (Spell spell : loadAll(directory, true)) {
			actionSpells.add((ActionSpell) spell);
		}
		return actionSpells;
	}

	public static List<Spell> loadAll(String directory, boolean actionSpell) throws Exception {
		List<Spell> spells = new ArrayList<>();
		File dir = new File(directory);
		if (dir.isDirectory() && dir.exists()) {
			for (File child : dir.listFiles()) {
				try {
					spells.add(load(child.getAbsolutePath(), actionSpell));
				} catch (Exception ex) {
					System.out.println("Error on file: " + child.getAbsolutePath());
					throw ex;
				}
			}
		} else {
			throw new IOException("Directory not found: " + directory);
		}

		return spells;
	}

	// TODO: Move this function to Java commons
	public static String readFile(String pathname) throws IOException {

		File file = new File(pathname);
		StringBuilder fileContents = new StringBuilder((int) file.length());
		Scanner scanner = new Scanner(file);
		String lineSeparator = System.getProperty("line.separator");

		try {
			while (scanner.hasNextLine()) {
				if (scanner.hasNextLine()) {
					fileContents.append(scanner.nextLine() + lineSeparator);
				} else {
					fileContents.append(scanner.nextLine());
				}
			}
		} finally {
			scanner.close();
		}
		return fileContents.toString();
	}
}
