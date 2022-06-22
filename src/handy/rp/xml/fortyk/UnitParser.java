package handy.rp.xml.fortyk;

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
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import handy.rp.fortyk.datamodel.Model;
import handy.rp.fortyk.datamodel.StatBlock;
import handy.rp.fortyk.datamodel.StatBlock.StatElement;
import handy.rp.fortyk.datamodel.WeaponDamageProfile.WEAPON_TYPE;
import handy.rp.fortyk.datamodel.Unit;
import handy.rp.fortyk.datamodel.Weapon;
import handy.rp.fortyk.datamodel.WeaponDamageProfile;
import handy.rp.xml.MonsterParser;


public class UnitParser {
	public static final String MOVEMENT_XML = "movement";
	public static final String WEAPONSKILL_XML = "weapon_skill";
	public static final String BALLISTICSKILL_XML = "ballistic_skill";
	public static final String STRENGTH_XML = "strength";
	public static final String TOUGHNESS_XML = "toughness";
	public static final String WOUNDS_XML = "wounds";
	public static final String ATTACKS_XML = "attacks";
	public static final String LEADERSHIP_XML = "leadership";
	public static final String SAVE_XML = "save";
	public static final String POINTS_XML = "points";
	
	public static final String LEADER_XML = "leader";
	
	public static final String BASE_XML = "base";
	public static final String DOWNGRADE_XML = "downgrade";
	public static final String THRESHOLD_XML = "wound_threshold";
	
	public static final String RANGE_XML = "range";
	public static final String AP_XML = "ap";
	
	public static final String AUTOHIT_XML = "autoHit";
	
	public static final String NAME_XML = "name";
	public static final String WEAPON_XML = "weapon";
	
	private static List<Unit> allOrkUnits = null;
	
	public static List<Unit> getAllOrkUnits(){
		if(allOrkUnits == null) {
			try {
				allOrkUnits = loadAll(Paths.get("40k_config", "units", "ork").toString());
			}catch(Exception ex) {
				ex.printStackTrace();
			}
		}
		return allOrkUnits;
	}
	
	public static Unit getUnitByName(String name) {
		for(Unit unit : getAllOrkUnits()) {
			if(unit.name.equalsIgnoreCase(name)) {
				return unit;
			}
		}
		throw new IllegalArgumentException("Unknown unit: " + name);
	}
	
	public static List<Unit> loadAll(String directory) throws Exception{
		List<Unit> units = new ArrayList<>();
		File dir = new File(directory);
		if(dir.isDirectory() && dir.exists()) {
			for(File child : dir.listFiles()) {
				units.add(load(child.getAbsolutePath()));
			}
		}else {
			throw new IOException("Directory not found: " + directory);
		}
		
		return units;
	}
	
	public static WeaponDamageProfile parseProfile(Element element, String defaultName, int modelStrength) {
		WeaponDamageProfile.WEAPON_TYPE type = WEAPON_TYPE.NORMAL;
		
		String name = defaultName;
		if (element.getElementsByTagName(NAME_XML) != null
				&& element.getElementsByTagName(NAME_XML).item(0) != null) {
			name = element.getElementsByTagName(NAME_XML).item(0).getTextContent();
		}
		int range = -1;
		String rangeStr = element.getElementsByTagName(RANGE_XML).item(0).getTextContent();
		if(!rangeStr.equalsIgnoreCase("Melee")) {
			range = Integer.parseInt(rangeStr);
		}
		
		StatElement attacks = StatElement.ZERO;
		StatElement optionalAttacks = StatElement.ZERO;
		String attacksStr = element.getElementsByTagName(ATTACKS_XML).item(0).getTextContent();
		String elements[] = attacksStr.split(" ");
		if(elements.length == 1) {
			if(attacksStr.equalsIgnoreCase("MELEE")) {
				type = WEAPON_TYPE.MELEE;
			}else {
				if(attacksStr.contains("/")) {
					String attackElements[] = attacksStr.split("/");
					try {
						int maxAttacks = Integer.parseInt(attackElements[0]);
						int normalAttacks = Integer.parseInt(attackElements[1]);
						attacks = StatBlock.StatElement.getValue(normalAttacks);
						optionalAttacks = StatBlock.StatElement.getValue(maxAttacks - normalAttacks);
					}catch(NumberFormatException ex) {
						throw new IllegalArgumentException("Invalid attacks format: " + attacksStr);
					}
				}else {
					attacks = StatBlock.StatElement.getValue(attacksStr);
				}
			}
		}else if(elements.length == 2) {
			if(elements[0].equalsIgnoreCase("Assault")) {
				type = WEAPON_TYPE.ASSAULT; 
			}else if(elements[0].equalsIgnoreCase("Heavy")) {
				type = WEAPON_TYPE.HEAVY;
			}else if(elements[0].equalsIgnoreCase("Pistol")) {
				type = WEAPON_TYPE.PISTOL;
			}else if(elements[0].equalsIgnoreCase("Grenade")) {
				type = WEAPON_TYPE.GRENADE;
			}else {
				throw new IllegalArgumentException("Unknown attack option: " + attacksStr);
			}
			
			attacks = StatBlock.StatElement.getValue(elements[1]);
		}else {
			throw new IllegalArgumentException("Unknown attack option: " + attacksStr);
		}
		
		int strength;
		String strengthStr = element.getElementsByTagName(STRENGTH_XML).item(0).getTextContent();
		if(strengthStr.equalsIgnoreCase("x2")) {
			strength = modelStrength * 2;
		}else if(strengthStr.equalsIgnoreCase("user")) {
			strength = modelStrength;
		}else if(strengthStr.startsWith("USER+")) {
			String tmp = strengthStr.substring("USER+".length());
			strength = modelStrength + Integer.parseInt(tmp);
		}else {
			strength = Integer.parseInt(strengthStr);
		}
		
		int ap = Integer.parseInt(element.getElementsByTagName(AP_XML).item(0).getTextContent());
		StatElement wounds = StatElement.getValue(element.getElementsByTagName(WOUNDS_XML).item(0).getTextContent());
		
		boolean autoHit = false;
		if (element.getElementsByTagName(AUTOHIT_XML) != null
				&& element.getElementsByTagName(AUTOHIT_XML).item(0) != null) {
			String tmp = element.getElementsByTagName(AUTOHIT_XML).item(0).getTextContent();
			if(tmp.equalsIgnoreCase("true")) {
				autoHit = true;
			}
		}
		
		return new WeaponDamageProfile(name, type, range, strength, ap, wounds, attacks, optionalAttacks, autoHit);
	}
	
	public static Weapon loadWeapon(Element element, int modelStrength) {
		String name = element.getElementsByTagName(NAME_XML).item(0).getTextContent();
		
		boolean countsAgainstMeleeAttacks = false;
		if (element.getElementsByTagName("countsAgainstMeleeAttacks") != null
				&& element.getElementsByTagName("countsAgainstMeleeAttacks").item(0) != null) {
			String tmp = element.getElementsByTagName("countsAgainstMeleeAttacks").item(0).getTextContent();
			if(tmp.equalsIgnoreCase("true")) {
				countsAgainstMeleeAttacks = true;
			}
		}
		
		int attackLimit = 0;
		if (element.getElementsByTagName("attack_limit") != null
				&& element.getElementsByTagName("attack_limit").item(0) != null) {
			attackLimit = Integer.parseInt(element.getElementsByTagName("attack_limit").item(0).getTextContent());
		}
		
		int replace = 0;
		if (element.getElementsByTagName("replace") != null
				&& element.getElementsByTagName("replace").item(0) != null) {
			attackLimit = Integer.parseInt(element.getElementsByTagName("replace").item(0).getTextContent());
		}
		
		int minusToHit = 0;
		if (element.getElementsByTagName("minusToHit") != null
				&& element.getElementsByTagName("minusToHit").item(0) != null) {
			minusToHit = Integer.parseInt(element.getElementsByTagName("minusToHit").item(0).getTextContent());
		}
		
		int limitCanHave = 0;
		int limitOutOf = 0;
		if (element.getElementsByTagName("limitCanHave") != null
				&& element.getElementsByTagName("limitCanHave").item(0) != null &&
				element.getElementsByTagName("limitOutOf") != null
				&& element.getElementsByTagName("limitOutOf").item(0) != null) {
			String tmp = element.getElementsByTagName("limit").item(0).getTextContent();
			String elements[] = tmp.split("/");
			if(elements.length != 2) {
				throw new IllegalArgumentException("Unable to parse 40k limit definition: " + tmp);
			}
			try {
				limitCanHave = Integer.parseInt(elements[0]);
				limitOutOf = Integer.parseInt(elements[1]);
			}catch(NumberFormatException ex) {
				throw new IllegalArgumentException("Unable to parse 40k limit definition: " + tmp);
			}
		}
		
		boolean freeattack = false;
		if (element.getElementsByTagName("freeattack") != null
				&& element.getElementsByTagName("freeattack").item(0) != null) {
			String tmp = element.getElementsByTagName("freeattack").item(0).getTextContent();
			if(tmp.equalsIgnoreCase("true")) {
				freeattack = true;
			}
		}
		
		int points = 0;
		if (element.getElementsByTagName(POINTS_XML).item(0) != null) {
			String tmp = element.getElementsByTagName(POINTS_XML).item(0).getTextContent();
			points = Integer.parseInt(tmp);
		}
		
		List<WeaponDamageProfile> profiles = new ArrayList<>();
		NodeList profileNodes = element.getElementsByTagName("profile"); 
		for(int idx = 0; idx < profileNodes.getLength(); idx++) {
			profiles.add(parseProfile((Element)profileNodes.item(idx), name, modelStrength));
		}
		return new Weapon(name, points, countsAgainstMeleeAttacks, attackLimit, freeattack, replace, limitCanHave, limitOutOf, minusToHit, profiles);
	}
	
	public static Unit load(String filename) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(new InputSource(new StringReader(MonsterParser.readFile(filename))));
		
		NodeList leaderNode = document.getElementsByTagName(LEADER_XML);
		Model leader = null;
		if(leaderNode != null && leaderNode.item(0) != null) {
			StatBlock leaderStats = parseBlock((Element)leaderNode.item(0));
			
			NodeList weaponsNodes = ((Element)leaderNode.item(0)).getElementsByTagName(WEAPON_XML);
			List<Weapon> weapons = new ArrayList<>();
			for(int idx = 0; idx < weaponsNodes.getLength(); idx++) {
				weapons.add(loadWeapon((Element)weaponsNodes.item(idx), leaderStats.strength));
			}
			
			leader = new Model(leaderStats, null, weapons, null);
			//What happens when a leader has downgrades? I don't think it ever happens in book, code if it does
		}
		
		String name =document.getElementsByTagName(NAME_XML).item(0).getTextContent(); 
		
		Model model = null;
		NodeList baseNode = document.getElementsByTagName(BASE_XML);
		if(baseNode != null && baseNode.item(0) != null) {
			StatBlock baseStats = parseBlock((Element)baseNode.item(0));
			
			Map<Integer, StatBlock> downgrades = null;
			NodeList downgradeNode = document.getElementsByTagName(DOWNGRADE_XML);
			for(int idx = 0; idx < downgradeNode.getLength(); idx++) {
				Element nextBlock = (Element)downgradeNode.item(idx);
				StatBlock downgradeBlock = parseBlock(nextBlock);
				int threshold = Integer.parseInt(nextBlock.getElementsByTagName(THRESHOLD_XML).item(0).getTextContent());
				if(downgrades == null) {
					downgrades = new HashMap<>();
				}
				downgrades.put(threshold, downgradeBlock);
			}
			
			NodeList weaponsNodes = ((Element)baseNode.item(0)).getElementsByTagName(WEAPON_XML);
			List<Weapon> weapons = new ArrayList<>();
			for(int idx = 0; idx < weaponsNodes.getLength(); idx++) {
				weapons.add(loadWeapon((Element)weaponsNodes.item(idx), baseStats.strength));
			}
			
			model = new Model(baseStats, downgrades, weapons, null);
		}
		
		return new Unit(name, leader, model);
	}
	
	private static StatBlock parseBlock (Element block) {
		int movement = Integer.parseInt(block.getElementsByTagName(MOVEMENT_XML).item(0).getTextContent());
		int weaponSkill = Integer.parseInt(block.getElementsByTagName(WEAPONSKILL_XML).item(0).getTextContent());
		int ballisticSkill = Integer.parseInt(block.getElementsByTagName(BALLISTICSKILL_XML).item(0).getTextContent());
		int strength = Integer.parseInt(block.getElementsByTagName(STRENGTH_XML).item(0).getTextContent());
		int toughness = Integer.parseInt(block.getElementsByTagName(TOUGHNESS_XML).item(0).getTextContent());
		StatBlock.StatElement attacks = StatElement.getElement(block.getElementsByTagName(ATTACKS_XML).item(0).getTextContent());
		int wounds = Integer.parseInt(block.getElementsByTagName(WOUNDS_XML).item(0).getTextContent());
		int leadership = Integer.parseInt(block.getElementsByTagName(LEADERSHIP_XML).item(0).getTextContent());
		int save = Integer.parseInt(block.getElementsByTagName(SAVE_XML).item(0).getTextContent());
		int points = Integer.parseInt(block.getElementsByTagName(POINTS_XML).item(0).getTextContent());
		return new StatBlock(movement, weaponSkill, ballisticSkill, strength, toughness, wounds, attacks, leadership, save, points);
	}
}
