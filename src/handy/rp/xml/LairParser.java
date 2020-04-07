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

import handy.rp.dnd.Action;
import handy.rp.dnd.lair.Lair;
import handy.rp.dnd.lair.LairAction;
import handy.rp.dnd.monsters.MonsterInstance;

public class LairParser {
	public static List<Action> actionList;
	
	static {
		try {
			actionList = ActionParser.loadAll("actions");
		}catch(Exception ex) {
			//Shouldn't happen, test loads happen before build. 
			//TODO: Add user notification if exception occurs, will be deploy issue
			ex.printStackTrace();
		}
	}
	
	public static List<Lair> loadAll(String directory) throws Exception{
		List<Lair> lairs = new ArrayList<>();
		File dir = new File(directory);
		if(dir.isDirectory() && dir.exists()) {
			for(File child : dir.listFiles()) {
				lairs.add(load(child.getAbsolutePath()));
			}
		}else {
			throw new IOException("Directory not found: " + directory);
		}
		
		return lairs;
	}
	
	public static Lair load(String filename) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(new InputSource(new StringReader(MonsterParser.readFile(filename))));
		
		String name = document.getElementsByTagName("name").item(0).getTextContent();
		NodeList actionList = document.getElementsByTagName("action");
		Lair lair = new Lair(name);
		for (int idx = 0; idx < actionList.getLength(); idx++) {
			Node setItem = actionList.item(idx);
			Element setElement = (Element) setItem;
			String interval = setElement.getAttribute("interval");
			String charges = setElement.getAttribute("charges");
			String actionCName = setElement.getTextContent();
			Action action = null;
			for(Action lAction : LairParser.actionList) {
				if(lAction.cname.equals(actionCName)) {
					action = lAction;
					break;
				}
			}
			if(action == null) {
				throw new Exception("Can't find action: " + actionCName);
			}
			
			int chargesVal = MonsterInstance.AT_WILL;
			if(charges != null && charges.length() != 0) {
				chargesVal = Integer.parseInt(charges);
			}
			int intervalVal = 1;
			if(interval != null  && interval.length() != 0) {
				intervalVal = Integer.parseInt(interval);
			}
			lair.addAction(new LairAction(action, intervalVal, chargesVal));
		}
		
		return lair;
	}
	
}
