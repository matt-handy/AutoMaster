package handy.rp.xml;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import handy.rp.Dice.DICE_TYPE;
import handy.rp.dnd.attacks.AttackBuilder;
import handy.rp.dnd.attacks.DamageComponent;
import handy.rp.dnd.attacks.DamageComponent.DAMAGE_TYPE;
import handy.rp.dnd.monsters.MonsterBuilder;
import handy.rp.dnd.monsters.MonsterTemplate;
import handy.rp.dnd.spells.Spell;
import handy.rp.dnd.spells.Spell.SLOTLEVEL;

public class MonsterParser {

	public static List<Spell> spellsList;
	
	static {
		try {
			spellsList = SpellParser.loadAll("spells");
		}catch(Exception ex) {
			//Shouldn't happen, test loads happen before build. 
			//TODO: Add user notification if exception occurs, will be deploy issue
			ex.printStackTrace();
		}
	}
	
	public static MonsterTemplate load(String filename) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(new InputSource(new StringReader(readFile(filename))));
		
		NodeList manmeList = document.getElementsByTagName("mname");
		String mname = manmeList.item(0).getTextContent();
		
		MonsterBuilder monsterBuilder = new MonsterBuilder(mname);
		
		monsterBuilder.addMaxHP(Integer.parseInt(document.getElementsByTagName("hp").item(0).getTextContent()));
		monsterBuilder.addStr(Integer.parseInt(document.getElementsByTagName("str").item(0).getTextContent()));
		monsterBuilder.addDex(Integer.parseInt(document.getElementsByTagName("dex").item(0).getTextContent()));
		monsterBuilder.addCon(Integer.parseInt(document.getElementsByTagName("con").item(0).getTextContent()));
		monsterBuilder.addInt(Integer.parseInt(document.getElementsByTagName("int").item(0).getTextContent()));
		monsterBuilder.addWis(Integer.parseInt(document.getElementsByTagName("wis").item(0).getTextContent()));
		monsterBuilder.addCha(Integer.parseInt(document.getElementsByTagName("cha").item(0).getTextContent()));
		
		try {
			monsterBuilder.addCasterLevel(Integer.parseInt(document.getElementsByTagName("casterLevel").item(0).getTextContent()));
			monsterBuilder.addCasterDc(Integer.parseInt(document.getElementsByTagName("casterDc").item(0).getTextContent()));
			monsterBuilder.addCasterToHit(Integer.parseInt(document.getElementsByTagName("casterToHit").item(0).getTextContent()));
		}catch(Exception ex) {
			//Caster Level not given, ignore
		}
		
		NodeList setList = document.getElementsByTagName("set");
		
		for(int idx = 0; idx < setList.getLength(); idx++){
			Node setItem = setList.item(idx);
			Element setElement = (Element) setItem;
			
			NodeList attackLists = setElement.getElementsByTagName("attack");
			//Attacks
			for(int jdx = 0; jdx < attackLists.getLength(); jdx++){
				Node attackItem = attackLists.item(jdx);
				Element attackElement = (Element) attackItem;
				
				NodeList aname = attackElement.getElementsByTagName("name");
				String attackName = aname.item(0).getTextContent();
				
				int toHit = Integer.parseInt(attackElement.getElementsByTagName("toHit").item(0).getTextContent());;
				
				Set<DamageComponent> dcs = new HashSet<DamageComponent>();
				NodeList damages = attackElement.getElementsByTagName("damage");
				
				for(int mdx = 0; mdx < damages.getLength(); mdx++) {
					Node damageNode = damages.item(mdx);
					Element damageElement = (Element)damageNode;
					DICE_TYPE dice = DICE_TYPE.getDice(damageElement.getElementsByTagName("dice").item(0).getTextContent());
					DAMAGE_TYPE damageType = DAMAGE_TYPE.getDamage(damageElement.getElementsByTagName("type").item(0).getTextContent());
					int diceCount = Integer.parseInt(damageElement.getElementsByTagName("diceCount").item(0).getTextContent());
					int modifier = Integer.parseInt(damageElement.getElementsByTagName("modifier").item(0).getTextContent());
					
					DamageComponent dc = new DamageComponent(damageType, dice, diceCount, modifier, toHit);
					dcs.add(dc);
				}
				
				AttackBuilder ab = new AttackBuilder(attackName, toHit);
				for(DamageComponent dc : dcs) {
					ab.addDamageComponent(dc);
				}
				monsterBuilder.addAttack(ab.build(), idx);
			}
			
		}
		
		NodeList spellSet = document.getElementsByTagName("spells");
		Node spellSetItem = spellSet.item(0);
		Element spellSetElement = (Element) spellSetItem;
		
		NodeList spellEnums = document.getElementsByTagName("spell");
		for(int idx = 0; idx < spellEnums.getLength(); idx++){
			Node spellItem = spellEnums.item(idx);
			Element spellElement = (Element) spellItem;
			String spellName = spellElement.getTextContent();
			for(Spell spell : spellsList) {
				if(spell.computerName.equalsIgnoreCase(spellName)) {
					monsterBuilder.addSpell(spell);
					break;
				}
			}
		}
		
		if(spellSetElement != null) {
		NodeList slotSet = spellSetElement.getElementsByTagName("slots");
		Node slotSetItem = slotSet.item(0);
		Element slotSetElement = (Element) slotSetItem;
		
		Map<Spell.SLOTLEVEL, Integer> slotCounts = new HashMap<>();
		
		if(slotSetElement.getElementsByTagName("first").item(0) != null) {
			Integer count = Integer.parseInt(slotSetElement.getElementsByTagName("first").item(0).getTextContent());
			slotCounts.put(SLOTLEVEL.ONE, count);
		}
		if(slotSetElement.getElementsByTagName("second").item(0) != null) {
			Integer count = Integer.parseInt(slotSetElement.getElementsByTagName("second").item(0).getTextContent());
			slotCounts.put(SLOTLEVEL.TWO, count);
		}
		if(slotSetElement.getElementsByTagName("third").item(0) != null) {
			Integer count = Integer.parseInt(slotSetElement.getElementsByTagName("third").item(0).getTextContent());
			slotCounts.put(SLOTLEVEL.THREE, count);
		}
		if(slotSetElement.getElementsByTagName("fourth").item(0) != null) {
			Integer count = Integer.parseInt(slotSetElement.getElementsByTagName("fourth").item(0).getTextContent());
			slotCounts.put(SLOTLEVEL.FOUR, count);
		}
		if(slotSetElement.getElementsByTagName("fifth").item(0) != null) {
			Integer count = Integer.parseInt(slotSetElement.getElementsByTagName("fifth").item(0).getTextContent());
			slotCounts.put(SLOTLEVEL.FIVE, count);
		}
		if(slotSetElement.getElementsByTagName("sixth").item(0) != null) {
			Integer count = Integer.parseInt(slotSetElement.getElementsByTagName("sixth").item(0).getTextContent());
			slotCounts.put(SLOTLEVEL.SIX, count);
		}
		if(slotSetElement.getElementsByTagName("seventh").item(0) != null) {
			Integer count = Integer.parseInt(slotSetElement.getElementsByTagName("seventh").item(0).getTextContent());
			slotCounts.put(SLOTLEVEL.SEVEN, count);
		}
		if(slotSetElement.getElementsByTagName("eighth").item(0) != null) {
			Integer count = Integer.parseInt(slotSetElement.getElementsByTagName("eighth").item(0).getTextContent());
			slotCounts.put(SLOTLEVEL.EIGHT, count);
		}
		if(slotSetElement.getElementsByTagName("ninth").item(0) != null) {
			Integer count = Integer.parseInt(slotSetElement.getElementsByTagName("ninth").item(0).getTextContent());
			slotCounts.put(SLOTLEVEL.NINE, count);
		}
		monsterBuilder.addSpellSlots(slotCounts);
		}
		return monsterBuilder.build();
	}
	
	public static List<MonsterTemplate> loadAll(String directory) throws Exception{
		List<MonsterTemplate> monsters = new ArrayList<>();
		File dir = new File(directory);
		if(dir.isDirectory() && dir.exists()) {
			for(File child : dir.listFiles()) {
				monsters.add(load(child.getAbsolutePath()));
			}
		}else {
			throw new IOException("Directory not found: " + directory);
		}
		
		return monsters;
	}
	
	
	//TODO: Move this function to Java commons
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