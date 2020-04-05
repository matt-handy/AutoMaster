package handy.rp.xml;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import handy.rp.Dice;
import handy.rp.Dice.DICE_TYPE;
import handy.rp.dnd.Action;
import handy.rp.dnd.attacks.Attack;
import handy.rp.dnd.attacks.AttackBuilder;
import handy.rp.dnd.attacks.DamageComponent;
import handy.rp.dnd.attacks.DamageComponent.DAMAGE_TYPE;
import handy.rp.dnd.spells.ActionSpell;

public class ActionParser {

	public static List<ActionSpell> actionSpellsList;
	
	static {
		try {
			actionSpellsList = SpellParser.loadAllActionSpells("action_spells");
		}catch(Exception ex) {
			//Shouldn't happen, test loads happen before build. 
			//TODO: Add user notification if exception occurs, will be deploy issue
			ex.printStackTrace();
		}
	}
	
	public static List<Action> loadAll(String directory) throws Exception{
		List<Action> actions = new ArrayList<>();
		File dir = new File(directory);
		if(dir.isDirectory() && dir.exists()) {
			for(File child : dir.listFiles()) {
				actions.add(load(child.getAbsolutePath()));
			}
		}else {
			throw new IOException("Directory not found: " + directory);
		}
		
		return actions;
	}
	
	public static Action load(String filename) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(new InputSource(new StringReader(MonsterParser.readFile(filename))));

		NodeList setList = document.getElementsByTagName("set");
		List<Attack> attacks = null;
		if (setList != null) {
			attacks = new ArrayList<>();
			for (int idx = 0; idx < setList.getLength(); idx++) {
				Node setItem = setList.item(idx);
				Element setElement = (Element) setItem;

				NodeList attackLists = setElement.getElementsByTagName("attack");
				// Attacks
				for (int jdx = 0; jdx < attackLists.getLength(); jdx++) {
					Node attackItem = attackLists.item(jdx);
					Element attackElement = (Element) attackItem;

					NodeList aname = attackElement.getElementsByTagName("name");
					String attackName = aname.item(0).getTextContent();

					int toHit = Integer.parseInt(attackElement.getElementsByTagName("toHit").item(0).getTextContent());
					;

					Set<DamageComponent> dcs = new HashSet<DamageComponent>();
					NodeList damages = attackElement.getElementsByTagName("damage");

					for (int mdx = 0; mdx < damages.getLength(); mdx++) {
						Node damageNode = damages.item(mdx);
						Element damageElement = (Element) damageNode;
						DICE_TYPE dice = DICE_TYPE
								.getDice(damageElement.getElementsByTagName("dice").item(0).getTextContent());
						DAMAGE_TYPE damageType = DAMAGE_TYPE
								.getDamage(damageElement.getElementsByTagName("type").item(0).getTextContent());
						int diceCount = Integer
								.parseInt(damageElement.getElementsByTagName("diceCount").item(0).getTextContent());
						int modifier = Integer
								.parseInt(damageElement.getElementsByTagName("modifier").item(0).getTextContent());

						DamageComponent dc = new DamageComponent(damageType, dice, diceCount, modifier, toHit);
						dcs.add(dc);
					}

					AttackBuilder ab = new AttackBuilder(attackName, toHit);
					for (DamageComponent dc : dcs) {
						ab.addDamageComponent(dc);
					}
					attacks.add(ab.build());
				}

			}
		}
		
		String actionName = document.getElementsByTagName("aname").item(0).getTextContent();
		String actionComputerName = document.getElementsByTagName("acname").item(0).getTextContent();
		ActionSpell actionSpell = null;
		String actionText = null;
		DICE_TYPE rechargeDice = null;
		int rechargeDiceMeets = -1;
		if(document.getElementsByTagName("aspell").item(0) != null) {
			String spellCompId = document.getElementsByTagName("aspell").item(0).getTextContent();
			for(ActionSpell spell : actionSpellsList) {
				if(spell.computerName.equalsIgnoreCase(spellCompId)) {
					actionSpell = spell;
					break;
				}
			}
		}
		if(document.getElementsByTagName("atext").item(0) != null) {
			actionText = document.getElementsByTagName("atext").item(0).getTextContent();
		}
		/*
		int actionCharges = 0;
		if(document.getElementsByTagName("acharges").item(0) != null) {
			String tagText = document.getElementsByTagName("acharges").item(0).getTextContent();
			if(tagText.contentEquals("will")) {
				actionCharges = MonsterInstance.AT_WILL;
			}else {
				actionCharges = Integer.parseInt(tagText);
			}
		}
		*/
		if(document.getElementsByTagName("recharge").item(0) != null) {
			String[] rechargeArgs = document.getElementsByTagName("recharge").item(0).getTextContent().split("-");
			if(rechargeArgs.length != 2) {
				throw new IllegalArgumentException("Need recharge args of the format: 'X-Y'");
			}
			rechargeDiceMeets = Integer.parseInt(rechargeArgs[0]);
			rechargeDice = Dice.DICE_TYPE.getDice(rechargeArgs[1]);
		}
		
		return new Action(actionName, actionComputerName, actionText, actionSpell, attacks,
				rechargeDice, rechargeDiceMeets);
	}
}
