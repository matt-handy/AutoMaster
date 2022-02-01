package handy.rp.xml;

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

import handy.rp.dnd.CharClass;
import handy.rp.dnd.CharSubClass;
import handy.rp.dnd.ClassFeature;
import handy.rp.dnd.spells.Spell;

public class SubClassParser {

	public static List<CharClass> charClasses;

	static {
		try {
			charClasses = SubClassParser.loadAll("char_subclasses");
			charClasses.addAll(ClassParser.charClasses);
		} catch (Exception ex) {
			// Shouldn't happen, test loads happen before build.
			// TODO: Add user notification if exception occurs, will be deploy issue
			ex.printStackTrace();
		}
	}
	
	public static CharClass getCharClass(String name) {
		for(CharClass cClass : charClasses) {
			if(name.equals(cClass.name)) {
				return cClass;
			}
		}
		return null;
	}
	
	public static List<CharClass> getAllSubclassesForParent(CharClass cClass){
		List<CharClass> subclasses = new ArrayList<>();
		for(CharClass option : charClasses) {
			//Test if the candidate class is itself a root class
			if(!option.getRootClass().equals(option)) {
				if(option.getRootClass().equals(cClass)) {
					subclasses.add(option);
				}
			}
		}
		return subclasses;
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

	public static CharSubClass load(String filename) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(new InputSource(new StringReader(MonsterParser.readFile(filename))));

		String parentStr = document.getElementsByTagName("parent").item(0).getTextContent();
		String name = document.getElementsByTagName("name").item(0).getTextContent();
		CharClass parent = ClassParser.getCharClass(parentStr);
		if(parent == null) {
			throw new Exception("Unknown parent class: " + parentStr);
		}
		
		List<ClassFeature> features = new ArrayList<>();
		NodeList featuresNode = document.getElementsByTagName("feature");
		for (int jdx = 0; jdx < featuresNode.getLength(); jdx++) {
			Node slotsXml = featuresNode.item(jdx);
			Element slotsXmlElem = (Element) slotsXml;
			features.add(ClassParser.getFeature(slotsXmlElem));
		}
		
		Map<Integer, List<Spell>> spells = new HashMap<>();
		NodeList spellsNode = document.getElementsByTagName("spell");
		for (int jdx = 0; jdx < spellsNode.getLength(); jdx++) {
			Node xml = spellsNode.item(jdx);
			Element elem = (Element) xml;
			
			String spellName = elem.getElementsByTagName("name").item(0).getTextContent();
			int level = Integer.parseInt(elem.getElementsByTagName("effectLevel").item(0).getTextContent());
			boolean foundSpell = false;
			for(Spell spell : MonsterParser.spellsList) {
				if(spell.computerName.equals(spellName)) {
					if(spells.containsKey(level)) {
						spells.get(level).add(spell);
					}else {
						List<Spell> newList = new ArrayList<>();
						newList.add(spell);
						spells.put(level, newList);
					}
					foundSpell = true;
				}
			}
			if(!foundSpell) {
				throw new Exception("Could not find spell: " + spellName);
			}
			
		}

		CharSubClass newClass = new CharSubClass(name, parent, features, spells);
		for(ClassFeature feature : features) {
			feature.setParentClass(newClass);
		}
		return newClass;
	}
}
