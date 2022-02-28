package handy.rp.xml;

import java.io.StringReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import handy.rp.dnd.character.Proficiency;

public class ProficiencyParser {

	public static List<Proficiency> armorProficiencies;
	public static List<Proficiency> toolProficiencies;

	static {
		try {
			armorProficiencies = load("armor_proficiencies", Paths.get("misc_configs", "armor_proficiencies.xml"));
			toolProficiencies = load("tool_proficiencies", Paths.get("misc_configs", "tool_proficiencies.xml"));
		} catch (Exception ex) {
			// Shouldn't happen, test loads happen before build.
			// TODO: Add user notification if exception occurs, will be deploy issue
			ex.printStackTrace();
		}
	}
	
	public static List<Proficiency> load(String name, Path file) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(new InputSource(new StringReader(MonsterParser.readFile(file.toAbsolutePath().toString()))));
		List<Proficiency> proficiencies = new ArrayList<>();
		NodeList proficiencyNode = document.getElementsByTagName("proficiency");
		for (int jdx = 0; jdx < proficiencyNode.getLength(); jdx++) {
			String profName = proficiencyNode.item(jdx).getTextContent();
			proficiencies.add(new Proficiency(profName));
		}
		return proficiencies;
	}
}
