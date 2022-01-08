package handy.rp.xml;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import handy.rp.Dice.DICE_TYPE;
import handy.rp.dnd.attacks.Weapon;
import handy.rp.dnd.attacks.Weapon.WEAPON_ATTRIBUTES;
import handy.rp.dnd.attacks.DamageComponent.DAMAGE_TYPE;

public class WeaponParser {
	
	public static List<Weapon> weapons;

	static {
		try {
			weapons = WeaponParser.loadAll("weapons");
		} catch (Exception ex) {
			// Shouldn't happen, test loads happen before build.
			// TODO: Add user notification if exception occurs, will be deploy issue
			ex.printStackTrace();
		}
	}

	public static Weapon getWeapon(String comp) {
		for(Weapon weapon : weapons) {
			if(weapon.cname.equalsIgnoreCase(comp)) {
				return weapon;
			}
		}
		return null;
	}
	
	public static List<Weapon> loadAll(String directory) throws Exception {
		List<Weapon> actions = new ArrayList<>();
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

	public static Weapon load(String filename) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(new InputSource(new StringReader(MonsterParser.readFile(filename))));

		String mname = document.getElementsByTagName("mname").item(0).getTextContent();
		String cname = document.getElementsByTagName("cname").item(0).getTextContent();
		
		DICE_TYPE dice = null;
		DAMAGE_TYPE damageType = null;
		Integer diceCount = null;
		Integer modifier = null;
		String range = null;
		List<WEAPON_ATTRIBUTES> attributes = new ArrayList<>();
		
		NodeList damageList = document.getElementsByTagName("damage");
		if(damageList != null && damageList.item(0) != null) {
			Node damageListItem = damageList.item(0);
			Element damageListElement = (Element) damageListItem;
			
			range = damageListElement.getElementsByTagName("range").item(0).getTextContent();
			dice = DICE_TYPE.getDice(damageListElement.getElementsByTagName("dice").item(0).getTextContent());
			damageType = DAMAGE_TYPE
					.getDamage(damageListElement.getElementsByTagName("type").item(0).getTextContent());
			diceCount = Integer
					.parseInt(damageListElement.getElementsByTagName("diceCount").item(0).getTextContent());
			modifier = Integer
					.parseInt(damageListElement.getElementsByTagName("modifier").item(0).getTextContent());
			String attrStr = damageListElement.getElementsByTagName("attributes").item(0).getTextContent();
			for(String attr : attrStr.split(",")) {
				if(attr.equalsIgnoreCase("finesse")) {
					attributes.add(WEAPON_ATTRIBUTES.FINESSE);
				}else if(attr.equalsIgnoreCase("light")) {
					attributes.add(WEAPON_ATTRIBUTES.LIGHT);
				}else if(attr.equalsIgnoreCase("heavy")) {
					attributes.add(WEAPON_ATTRIBUTES.HEAVY);
				}else if(attr.equalsIgnoreCase("two_handed")) {
					attributes.add(WEAPON_ATTRIBUTES.TWO_HANDED);
				}else if(attr.equalsIgnoreCase("thrown")) {
					attributes.add(WEAPON_ATTRIBUTES.THROWN);
				}else if(attr.equalsIgnoreCase("ammunition")) {
					attributes.add(WEAPON_ATTRIBUTES.AMMUNITION);
				}else if(attr.equalsIgnoreCase("loading")) {
					attributes.add(WEAPON_ATTRIBUTES.LOADING);
				}else if(attr.equalsIgnoreCase("versatile")) {
					attributes.add(WEAPON_ATTRIBUTES.VERSATILE);
				}
			}
			
			
		}else {
			throw new IllegalArgumentException("Improperly formatted weapon");
		}
		
		String thrownRange = null;
		NodeList thrownList = document.getElementsByTagName("thrown");
		if(thrownList != null && thrownList.item(0) != null) {
			Node thrownListItem = thrownList.item(0);
			Element thrownListItemElement = (Element) thrownListItem;
			thrownRange = thrownListItemElement.getElementsByTagName("range").item(0).getTextContent();
		}
		
		DICE_TYPE versatileDice = null;
		NodeList versatileList = document.getElementsByTagName("versatile");
		if(versatileList != null && versatileList.item(0) != null) {
			Node versatileListItem = versatileList.item(0);
			Element versatileListElement = (Element) versatileListItem;
			versatileDice = DICE_TYPE.getDice(versatileListElement.getElementsByTagName("dice").item(0).getTextContent());
		}
		
		return new Weapon(mname, cname, damageType, dice, diceCount, modifier, range, attributes, thrownRange, versatileDice);
	}
}
