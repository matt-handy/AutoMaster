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
import handy.rp.fortyk.datamodel.Unit;
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
	
	public static final String NAME_XML = "name";
	
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
	
	public static Unit load(String filename) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(new InputSource(new StringReader(MonsterParser.readFile(filename))));
		
		NodeList leaderNode = document.getElementsByTagName(LEADER_XML);
		Model leader = null;
		if(leaderNode != null && leaderNode.item(0) != null) {
			StatBlock leaderStats = parseBlock((Element)leaderNode.item(0));
			leader = new Model(leaderStats, null);
			//TODO: What happens when a leader has downgrades? I don't think it ever happens in book, code if it does
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
			
			model = new Model(baseStats, downgrades);
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
