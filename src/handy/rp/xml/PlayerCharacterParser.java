package handy.rp.xml;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Paths;
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
import handy.rp.dnd.SkillCheckInfo.SKILL_CHECK;
import handy.rp.dnd.attacks.CharacterWeapon;
import handy.rp.dnd.attacks.Weapon;
import handy.rp.dnd.character.PlayerCharacter;
import handy.rp.dnd.spells.Spell;
import handy.rp.dnd.spells.Spell.SLOTLEVEL;

public class PlayerCharacterParser {

	public static List<Spell> spellsList;
	static {
		try {
			spellsList = SpellParser.loadAllSpells("spells");
		} catch (Exception ex) {
			// Shouldn't happen, test loads happen before build.
			// TODO: Add user notification if exception occurs, will be deploy issue
			ex.printStackTrace();
		}
	}

	public static List<PlayerCharacter> loadAllPlayerCharacters(String directory) throws Exception {
		List<PlayerCharacter> chars = new ArrayList<>();
		File dir = new File(directory);
		if (dir.isDirectory() && dir.exists()) {
			for (File child : dir.listFiles()) {
				chars.add(load(child.getAbsolutePath()));
			}
		} else {
			throw new IOException("Directory not found: " + directory);
		}
		return chars;
	}

	public static PlayerCharacter load(String file) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(new InputSource(new StringReader(MonsterParser.readFile(file))));

		NodeList nameList = document.getElementsByTagName("name");
		String name = nameList.item(0).getTextContent();

		int maxHp = Integer.parseInt(document.getElementsByTagName("maxhp").item(0).getTextContent());
		int currentHp = Integer.parseInt(document.getElementsByTagName("currenthp").item(0).getTextContent());
		int str = Integer.parseInt(document.getElementsByTagName("str").item(0).getTextContent());
		int dex = Integer.parseInt(document.getElementsByTagName("dex").item(0).getTextContent());
		int con = Integer.parseInt(document.getElementsByTagName("con").item(0).getTextContent());
		int inte = Integer.parseInt(document.getElementsByTagName("int").item(0).getTextContent());
		int wis = Integer.parseInt(document.getElementsByTagName("wis").item(0).getTextContent());
		int cha = Integer.parseInt(document.getElementsByTagName("cha").item(0).getTextContent());

		List<CharacterWeapon> weapons = new ArrayList<>();

		NodeList profWeapList = document.getElementsByTagName("proficient_weapons");
		if (profWeapList != null && profWeapList.item(0) != null) {
			Node profWeapListItem = profWeapList.item(0);
			Element profWeapListItemElement = (Element) profWeapListItem;
			NodeList profWeaponsList = profWeapListItemElement.getElementsByTagName("weapon");
			{
				for (int idx = 0; idx < profWeaponsList.getLength(); idx++) {
					String weaponName = profWeaponsList.item(idx).getTextContent();
					Weapon weapon = WeaponParser.getWeapon(weaponName);
					if (weapon != null) {
						weapons.add(new CharacterWeapon(weapon, true));
					} else {
						throw new IllegalArgumentException(
								weaponName + ": is not a valid weapon for character: " + name);
					}
				}
			}
		}

		profWeapList = document.getElementsByTagName("nonproficient_weapons");
		if (profWeapList != null && profWeapList.item(0) != null) {
			Node profWeapListItem = profWeapList.item(0);
			Element profWeapListItemElement = (Element) profWeapListItem;
			NodeList profWeaponsList = profWeapListItemElement.getElementsByTagName("weapon");
			{
				for (int idx = 0; idx < profWeaponsList.getLength(); idx++) {
					String weaponName = profWeaponsList.item(idx).getTextContent();
					Weapon weapon = WeaponParser.getWeapon(weaponName);
					if (weapon != null) {
						weapons.add(new CharacterWeapon(weapon, false));
					} else {
						throw new IllegalArgumentException(
								weaponName + ": is not a valid weapon for character: " + name);
					}
				}
			}
		}

		List<SKILL_CHECK> skillProficiencies = new ArrayList<>();
		NodeList skillProfs = document.getElementsByTagName("skill_proficiencies");
		if (skillProfs != null && skillProfs.item(0) != null) {
			Node item = skillProfs.item(0);
			Element elem = (Element) item;
			NodeList list = elem.getElementsByTagName("proficiency");
			for (int idx = 0; idx < list.getLength(); idx++) {
				String profName = list.item(idx).getTextContent();
				switch(profName) {
				case "acrobatics":
					skillProficiencies.add(SKILL_CHECK.Acrobatics);
					break;
				case "animal_handling":
					skillProficiencies.add(SKILL_CHECK.Animal_Handling);
					break;
				case "arcana":
					skillProficiencies.add(SKILL_CHECK.Arcana);
					break;
				case "athletics":
					skillProficiencies.add(SKILL_CHECK.Athletics);
					break;
				case "deception":
					skillProficiencies.add(SKILL_CHECK.Acrobatics);
					break;
				case "history":
					skillProficiencies.add(SKILL_CHECK.History);
					break;
				case "insight":
					skillProficiencies.add(SKILL_CHECK.Insight);
					break;
				case "intimidation":
					skillProficiencies.add(SKILL_CHECK.Intimidation);
					break;
				case "investigation":
					skillProficiencies.add(SKILL_CHECK.Investigation);
					break;
				case "medicine":
					skillProficiencies.add(SKILL_CHECK.Medicine);
					break;
				case "nature":
					skillProficiencies.add(SKILL_CHECK.Nature);
					break;
				case "perception":
					skillProficiencies.add(SKILL_CHECK.Perception);
					break;
				case "performance":
					skillProficiencies.add(SKILL_CHECK.Performance);
					break;
				case "persuasion":
					skillProficiencies.add(SKILL_CHECK.Persuasion);
					break;
				case "religion":
					skillProficiencies.add(SKILL_CHECK.Religion);
					break;
				case "slight_of_hand":
					skillProficiencies.add(SKILL_CHECK.Sleight_of_Hand);
					break;
				case "stealth":
					skillProficiencies.add(SKILL_CHECK.Stealth);
					break;
				case "survival":
					skillProficiencies.add(SKILL_CHECK.Survival);
					break;
				default:
					throw new Exception("unknown skill proficiency: " + profName);
				}
			}
		}

		Map<SLOTLEVEL, Integer> savedSpellSlots = null;
		NodeList spellslots = document.getElementsByTagName("spellslots");
		if(spellslots != null && spellslots.item(0) != null) {
			Node xml = spellslots.item(0);
			Element elem = (Element) xml;
			savedSpellSlots = ClassParser.getSlotLevels(elem);
		}
		
		List<String> activeFeatureNames = null;
		NodeList featuresNode = document.getElementsByTagName("active_features");
		if(featuresNode != null && featuresNode.item(0) != null) {
			activeFeatureNames = new ArrayList<>();
			Node xml = featuresNode.item(0);
			Element elem = (Element) xml;
			NodeList featureNodeList = elem.getElementsByTagName("feature");
			for (int idx = 0; idx < featureNodeList.getLength(); idx++) {
				Node fItem = featureNodeList.item(idx);
				Element fElement = (Element) fItem;
				activeFeatureNames.add(fElement.getTextContent());
			}
		}
		
		Map<String, Integer> classToResource = new HashMap<>();;
		NodeList rscNode = document.getElementsByTagName("class-resources");
		if(rscNode != null && rscNode.item(0) != null) {
			Node xml = rscNode.item(0);
			Element elem = (Element) xml;
			NodeList rscNodeList = elem.getElementsByTagName("resource");
			for (int idx = 0; idx < rscNodeList.getLength(); idx++) {
				Node fItem = rscNodeList.item(idx);
				Element fElement = (Element) fItem;
				String className = fElement.getElementsByTagName("class").item(0).getTextContent();
				int counter = Integer.parseInt(fElement.getElementsByTagName("counter").item(0).getTextContent());
				classToResource.put(className, counter);
			}
		}
		
		Map<String, Integer> featureCharges = new HashMap<>();;
		NodeList featuresChargeNode = document.getElementsByTagName("feature-charges");
		if(featuresChargeNode != null && featuresChargeNode.item(0) != null) {
			Node xml = featuresChargeNode.item(0);
			Element elem = (Element) xml;
			NodeList featuresChargesNodeList = elem.getElementsByTagName("feature");
			for (int idx = 0; idx < featuresChargesNodeList.getLength(); idx++) {
				Node fItem = featuresChargesNodeList.item(idx);
				Element fElement = (Element) fItem;
				String className = fElement.getElementsByTagName("name").item(0).getTextContent();
				int counter = Integer.parseInt(fElement.getElementsByTagName("charges").item(0).getTextContent());
				featureCharges.put(className, counter);
			}
		}
		
		Map<SLOTLEVEL, List<Spell>> spells = new HashMap<>();
		NodeList spellEnums = document.getElementsByTagName("spell");
		for (int idx = 0; idx < spellEnums.getLength(); idx++) {
			Node spellItem = spellEnums.item(idx);
			Element spellElement = (Element) spellItem;
			String spellName = spellElement.getTextContent();
			boolean foundSpell = false;
			for (Spell spell : spellsList) {
				if (spell.computerName.equalsIgnoreCase(spellName)) {
					addSpell(spells, spell);
					foundSpell = true;
					break;
				}
			}
			if (!foundSpell) {
				throw new IllegalArgumentException("Unknown spell: " + spellName);
			}
		}
		
		Map<DICE_TYPE, Integer> hitDice = new HashMap<>();
		NodeList hitDiceNode = document.getElementsByTagName("current_hitdice");
		if(hitDiceNode != null && hitDiceNode.item(0) != null) {
			Node xml = hitDiceNode.item(0);
			Element elem = (Element) xml;
			NodeList hitDiceNodeList = elem.getElementsByTagName("dice");
			for (int idx = 0; idx < hitDiceNodeList.getLength(); idx++) {
				Node fItem = hitDiceNodeList.item(idx);
				Element fElement = (Element) fItem;
				String diceType = fElement.getElementsByTagName("type").item(0).getTextContent();
				int counter = Integer.parseInt(fElement.getElementsByTagName("count").item(0).getTextContent());
				hitDice.put(DICE_TYPE.getDice(diceType), counter);
			}
		}

		NodeList classesNode = document.getElementsByTagName("classInfo");
		Map<CharClass, Integer> classes = new HashMap<>();
		for (int jdx = 0; jdx < classesNode.getLength(); jdx++) {
			Node classesXml = classesNode.item(jdx);
			Element classesXmlElem = (Element) classesXml;

			String classEnum = classesXmlElem.getElementsByTagName("class").item(0).getTextContent();
			String levelsStr = classesXmlElem.getElementsByTagName("levels").item(0).getTextContent();
			Integer levels = Integer.parseInt(levelsStr);

			CharClass cClass = SubClassParser.getCharClass(classEnum);
			if (cClass == null) {
				throw new IllegalArgumentException("unknown class: " + classEnum);
			}
			classes.put(cClass, levels);
		}
		if (classes.isEmpty()) {
			throw new IllegalArgumentException(
					"character " + name + " has no identifiable class. Much like your mother.");
		}

		return new PlayerCharacter(name, str, dex, con, inte, wis, cha, spells, classes, maxHp, currentHp, weapons, skillProficiencies, Paths.get(file), savedSpellSlots, activeFeatureNames, classToResource, featureCharges, hitDice);
	}

	public static void addSpell(Map<SLOTLEVEL, List<Spell>> spells, Spell spell) {
		if (spells.get(spell.minimumLevel) == null) {
			List<Spell> slotSpells = new ArrayList<>();
			slotSpells.add(spell);
			spells.put(spell.minimumLevel, slotSpells);
		} else {
			spells.get(spell.minimumLevel).add(spell);
		}
	}
}
