package handy.rp.xml;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

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

public class MonsterParser {

	public static MonsterTemplate load(String filename) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(new InputSource(new StringReader(readFile(filename))));
		
		NodeList manmeList = document.getElementsByTagName("mname");
		
		//Just assume one node returned
		String mname = null;
		for(int idx = 0; idx < manmeList.getLength(); idx++){
			mname = manmeList.item(idx).getTextContent();
		}
		
		MonsterBuilder monsterBuilder = new MonsterBuilder(mname);
		
		monsterBuilder.addMaxHP(Integer.parseInt(document.getElementsByTagName("hp").item(0).getTextContent()));
		monsterBuilder.addStr(Integer.parseInt(document.getElementsByTagName("str").item(0).getTextContent()));
		monsterBuilder.addDex(Integer.parseInt(document.getElementsByTagName("dex").item(0).getTextContent()));
		monsterBuilder.addCon(Integer.parseInt(document.getElementsByTagName("con").item(0).getTextContent()));
		monsterBuilder.addInt(Integer.parseInt(document.getElementsByTagName("int").item(0).getTextContent()));
		monsterBuilder.addWis(Integer.parseInt(document.getElementsByTagName("wis").item(0).getTextContent()));
		monsterBuilder.addCha(Integer.parseInt(document.getElementsByTagName("cha").item(0).getTextContent()));
		
		
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
				
				Set<DamageComponent> dcs = new HashSet<DamageComponent>();
				NodeList damages = attackElement.getElementsByTagName("damage");
				
				for(int mdx = 0; mdx < damages.getLength(); mdx++) {
					Node damageNode = damages.item(mdx);
					Element damageElement = (Element)damageNode;
					DICE_TYPE dice = DICE_TYPE.getDice(damageElement.getElementsByTagName("dice").item(0).getTextContent());
					DAMAGE_TYPE damageType = DAMAGE_TYPE.getDamage(damageElement.getElementsByTagName("type").item(0).getTextContent());
					int diceCount = Integer.parseInt(damageElement.getElementsByTagName("diceCount").item(0).getTextContent());
					int modifier = Integer.parseInt(damageElement.getElementsByTagName("modifier").item(0).getTextContent());
					int toHit = Integer.parseInt(damageElement.getElementsByTagName("toHit").item(0).getTextContent());;
					
					DamageComponent dc = new DamageComponent(damageType, dice, diceCount, modifier, toHit);
					dcs.add(dc);
				}
				
				AttackBuilder ab = new AttackBuilder(attackName);
				for(DamageComponent dc : dcs) {
					ab.addDamageComponent(dc);
				}
				monsterBuilder.addAttack(ab.build(), idx);
			}
			
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
	private static String readFile(String pathname) throws IOException {

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
