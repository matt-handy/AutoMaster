package handy.rp.xml.fortyk;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import handy.rp.fortyk.datamodel.Army;
import handy.rp.fortyk.datamodel.UnitInstance;
import handy.rp.xml.MonsterParser;

public class ArmyParser {
	public static final String ARMYNAME_XML = "army_name";
	public static final String NAME_XML = "name";
	public static final String UNIT_XML = "unit";
	public static final String MNEMONIC_XML = "mnemonic";
	public static final String MODELCOUNT_XML = "model_count";

	private static List<Army> allArmies = null;

	public static List<Army> getAllArmies() {
		//if (allArmies == null) {
			try {
				allArmies = loadAll(Paths.get("40k_config", "armies").toString());
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		//}
		return allArmies;

	}

	public static Army getArmyByName(String name) {
		for(Army army : getAllArmies()) {
			if(army.armyName.equalsIgnoreCase(name)) {
				return army;
			}
		}
		throw new IllegalArgumentException("Unknown army: " + name);
	}

	public static List<Army> loadAll(String directory) throws Exception {
		List<Army> units = new ArrayList<>();
		File dir = new File(directory);
		if (dir.isDirectory() && dir.exists()) {
			for (File child : dir.listFiles()) {
				units.add(load(child.getAbsolutePath()));
			}
		} else {
			throw new IOException("Directory not found: " + directory);
		}

		return units;
	}

	public static Army load(String filename) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(new InputSource(new StringReader(MonsterParser.readFile(filename))));

		String armyName = document.getElementsByTagName(ARMYNAME_XML).item(0).getTextContent();

		List<UnitInstance> units = new ArrayList<>();
		document.getElementsByTagName(ARMYNAME_XML).item(0).getTextContent();
		NodeList downgradeNode = document.getElementsByTagName(UNIT_XML);
		for (int idx = 0; idx < downgradeNode.getLength(); idx++) {
			Element nextBlock = (Element) downgradeNode.item(idx);
			String mnemonic = nextBlock.getElementsByTagName(MNEMONIC_XML).item(0).getTextContent();
			String name = nextBlock.getElementsByTagName(NAME_XML).item(0).getTextContent();
			int modelCount = Integer.parseInt(nextBlock.getElementsByTagName(MODELCOUNT_XML).item(0).getTextContent());
			units.add(UnitParser.getUnitByName(name).getInstance(modelCount, mnemonic));
		}

		return new Army(armyName, units);
	}
}
