package handy.rp.dnd;

import java.io.BufferedWriter;
import java.io.Console;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import handy.rp.Dice;
import handy.rp.dnd.EntityCondition.CONDITIONS;
import handy.rp.dnd.attacks.Attack;
import handy.rp.dnd.attacks.Damage;
import handy.rp.dnd.lair.Lair;
import handy.rp.dnd.lair.LairAction;
import handy.rp.dnd.monsters.MonsterInstance;
import handy.rp.dnd.monsters.MonsterSetLoader;
import handy.rp.dnd.monsters.MonsterTemplate;
import handy.rp.dnd.spells.Spell;
import handy.rp.xml.LairParser;
import handy.rp.xml.MonsterParser;

public class EncounterRunner {

	protected List<Entity> currentInitiativeList = new ArrayList<>();
	private Entity currentEntity;
	private int currentPlace;
	private int roundCount;

	private List<MonsterTemplate> monstersAvailable;
	private List<Lair> lairsAvailable;
	
	private Lair currentLair = null;

	private BufferedWriter logFile = null;
	
	private void log (String message) {
		if(logFile != null) {
			try {
				logFile.write(message + System.lineSeparator());
				logFile.flush();
			} catch (IOException e) {
				
			}
		}
	}
	
	public void addEntity(Entity entity, boolean assignStart) {
		currentInitiativeList.add(entity);
		Collections.sort(currentInitiativeList, new EntityComparator());

		if (currentEntity == null) {
			if (assignStart) {
				startCombat();
			}
		} else {
			currentPlace = currentInitiativeList.indexOf(currentEntity);
		}
	}
	
	public Entity getCurrentEntity() {
		return currentEntity;
	}
	
	public void removeEntity(Entity entity) {
		if(entity == currentEntity) {
			if(currentInitiativeList.indexOf(entity) == currentInitiativeList.size() - 1) {
				if(currentInitiativeList.size() > 1) {
					roundCount++;
					currentEntity = currentInitiativeList.get(0);
					currentPlace = 0;
				}else {
					currentEntity = null;
					currentPlace = -1;
				}
			}else {
				currentEntity = currentInitiativeList.get(currentPlace + 1);
				currentInitiativeList.remove(entity);
				currentPlace = currentInitiativeList.indexOf(currentEntity);
			}
		}else {
			currentInitiativeList.remove(entity);
			currentPlace = currentInitiativeList.indexOf(currentEntity);
		}
	}

	public void startCombat() {
		currentEntity = currentInitiativeList.get(0);
		currentPlace = 0;
		roundCount = 1;
	}

	public static void main(String args[]) {
		EncounterRunner main = new EncounterRunner();
		try {
			main.initialize();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		main.runEncounter();
	}

	public void initialize() throws Exception {
		monstersAvailable = MonsterParser.loadAll("monsters");
		lairsAvailable = LairParser.loadAll("lairs");
		
		try {
			logFile = new BufferedWriter(new FileWriter("log", true));
		} catch (IOException e) {
			e.printStackTrace();
			logFile = null;
		}
	}
	
	public void shutdown() {
		try {
			logFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void runEncounter() {
		Console console = System.console();
		if (console == null) {
			throw new Error("Cannot start console");
		}

		String nextCommand;
		while ((nextCommand = console.readLine()) != null) {
			String args[] = nextCommand.split(" ");

			if (args.length < 1) {
				console.printf("Improper command");
				continue;
			}

			String command = args[0];

			switch (command) {
			case "help":
				console.writer().println("advturn | advanceturn => advances play to the next entity");
				console.writer().println("amon | addmonster <monster index or name> <personal name> => adds monster");
				console.writer().println("apc | addplayercharacter <personal name> <initiative> => add character");
				console.writer().println("at | attack <attack index> => makes attack by current monster");
				console.writer().println("cast <spellname> <level - optional, defaults to lowers possible level> => cast spell but current monster");
				console.writer().println("cur | currententity => gives name of current in initiative");
				console.writer().println("curhp => gives hp of current entity in initiative");
				console.writer().println("heal <index of entity> <hp> => heals monster");
				console.writer().println("hit <index of entity> <hp> => removes monster hp");
				console.writer().println("gr | getround => gives round number");
				console.writer().println("la | listattack => prints list of current monster attacks");
				console.writer().println("lam | listallmonsters => prints list of monster templates available for play");
				console.writer().println("li | listinitiative => gives print of characters/monsters in initiative order");
				console.writer().println("lms | load_monster_set <filename> => loads set of monsters into initiative order");
				console.writer().println("ls | listspells => prints list of current monster spells");
				console.writer().println("lss | listspellslots => prints list of current monster spell slots");
				console.writer().println("rm | remove <index or personal name> => removes entity from initiative order");
				console.writer().println("sc | startcombat => starts play");
				
				console.writer().println("act <action name> => take action");
				console.writer().println("listact => lists actions");
				console.writer().println("listattr <index> => lists attributes of entity");
				
				console.writer().println("lact <actor index> <action name> <optional - actee name> => take legendary action");
				console.writer().println("listlact <optional idx> => lists legendary actions, assumes current actor");
				
				console.writer().println("listlairs => all available lairs");
				console.writer().println("setlair <Index> => Sets Lair");
				console.writer().println("lairact <Index> => Takes Lair Action");
				
				console.writer().println("addCon|rmCon <Condition abbreviation> <optional - entity index>");
				console.writer().println("rollSave <str|dex|con|int|wis|cha> <optional - entity index>");
				console.writer().println("react <reaction string. oppAtt for opportunity attack> <entity index> <optional - target index>");
				console.writer().println("Ctrl-c for quit");
				break;
			case "listAttr": 	
				console.writer().println(getAttrs(args));
			case "breakSpell":
				if(currentEntity instanceof MonsterInstance) {
					MonsterInstance mi = (MonsterInstance) currentEntity;
					if(mi.concentratedSpell() == null) {
						console.writer().println("Monster was not concetrating on anything");
					}else {
						Spell spell = mi.concentratedSpell();
						String str = mi.personalName + " breaks concentration on " + spell.readableName;
						mi.breakConcentration();
						console.writer().println(str);
						log(str);
					}
				}else {
					console.writer().println("Can only break concentration for monsters");
				}
				break;
			case "listlact":
				console.writer().println(listLegendaryActions(args));
				break;
			case "lact":
				console.writer().println(doLegendaryAction(args));
				break;
			case "listact":
				console.writer().println(listActions());
				break;
			case "act":
				console.writer().println(doAction(args));
				break;
			case "lms":
			case "load_monster_set":
				console.writer().println(loadMonsterSet(args));
				break;
			case "amon":
			case "addmonster":
				console.writer().println(addMonster(args));
				break;
			case "rm":
			case "remove":
				console.writer().println(rmEntity(args));
				break;
			case "apc":
			case "addplayercharacter":
				console.writer().println(addPlayerCharacter(args));
				break;
			case "gr":
			case "getround":
				console.writer().println("Current round is: " + roundCount);
				break;
			case "sc":
			case "startcombat":
				startCombat();
				console.writer().println("First in order: " + currentEntity.personalName);
				console.writer().println(currentEntity.listAvailableActionsAttackSpells());
				break;
			case "advturn":
			case "advanceturn":
				console.writer().println(advanceTurn());
				break;
			case "li":
			case "listinitiative":
				int idx = 0;
				for (Entity ent : currentInitiativeList) {
					if(ent instanceof MonsterInstance) {
						MonsterInstance mi = (MonsterInstance) ent;
						console.writer().println(idx + " " + ent.personalName + " " + ent.getCurrentInitiative() + " HP: " + mi.getCurrentHp());
					}else {
						console.writer().println(idx + " " + ent.personalName + " " + ent.getCurrentInitiative());
					}
					idx++;
				}
				break;
			case "lam":
			case "listmonsters":
				int jdx = 0;
				for (MonsterTemplate m : monstersAvailable) {
					console.writer().println("Monster: " + jdx + " - " + m.humanReadableName);
					jdx++;
				}
				break;
			case "listlairs":
				int kdx = 0;
				for (Lair lair : lairsAvailable) {
					console.writer().println("Lair: " + kdx + " - " + lair.personalName);
					kdx++;
				}
				break;
			case "la":
			case "listattack":
				if(currentInitiativeList.get(currentPlace) instanceof MonsterInstance) {
					MonsterInstance mi = (MonsterInstance) currentInitiativeList.get(currentPlace);
					console.writer().println(mi.listRemainingAttacksReadable());
				}else {
					console.writer().println("Must be a monster to list attacks");
				}
				break;
			case "at":
			case "attack":
				console.writer().println(attack(args));
				break;
			case "cur":
			case "currententity":
				console.writer().println("Current actor: " + currentEntity.personalName);
				break;
			case "curhp":
				if(currentEntity instanceof MonsterInstance) {
					MonsterInstance mi = (MonsterInstance) currentEntity;
					console.writer().println("Current HP: " + mi.getCurrentHp());
				}else {
					console.writer().println("Current actor does not have managed HP");
				}
				break;
			case "heal":
			case "hit":
				console.writer().println(hpMod(args));
				break;
			case "cast":
				console.writer().println(castSpell(args));
				break;
			case "icast":
				console.writer().println(castInnateSpell(args));
				break;
			case "listspells":
			case "ls":
				if(currentEntity instanceof MonsterInstance) {
					MonsterInstance mi = (MonsterInstance) currentEntity;
					
					String innateSpells = mi.listInnateSpells();
					if(innateSpells.length() != 0) {
						console.writer().println("Innate Spells:");
						console.writer().println(innateSpells);
					}
					
					console.writer().println(mi.listSpells());
				}else {
					console.writer().println("Current actor does not have managed spells");
				}
				break;
			case "listspellslots":
			case "lss":
				if(currentEntity instanceof MonsterInstance) {
					MonsterInstance mi = (MonsterInstance) currentEntity;
					console.writer().println(mi.listSpellSlotsRemaining());
				}else {
					console.writer().println("Current actor does not have managed spells");
				}
				break;
			case "setlair":
				console.writer().println(setLair(args));
				break;
			case "lairact":
				console.writer().println(lairAct(args));
				break;
			case "addCon":
			case "rmCon":
				console.writer().println(addOrRemoveCondition(args));
				break;
			case "rollSave":
				console.writer().println(rollSave(args));
				break;
			case "react":
				console.writer().println(takeReaction(args));
				break;
			default:
				console.writer().println("Unknown command: " + command);
				break;
			}
		}
	}
	
	String advanceTurn() {
		StringBuilder sb = new StringBuilder();
		if(currentEntity instanceof MonsterInstance) {
			MonsterInstance mi = (MonsterInstance) currentInitiativeList.get(currentPlace);
			mi.resetTurn();
		}
		if (currentPlace + 1 == currentInitiativeList.size()) {
			roundCount++;
			sb.append("New round! Current round: " + roundCount);
			sb.append(System.lineSeparator());
			currentPlace = 0;
		}else {
			currentPlace++;
		}
		currentEntity = currentInitiativeList.get(currentPlace);
		sb.append("Next in order: " + currentEntity.personalName);
		sb.append(System.lineSeparator());
		currentEntity.notifyNewTurn();
		sb.append(currentEntity.listAvailableActionsAttackSpells());
		sb.append(System.lineSeparator());
		sb.append(currentEntity.listStats());
		sb.append(System.lineSeparator());
		sb.append(currentEntity.getConditions());
		return sb.toString();
	}
	
	String takeReaction(String[] args) {
		String reaction = args[1];
		int midx;
		try {
			midx = Integer.parseInt(args[2]); 
		}catch(NumberFormatException ex) {
			return "Need reactor index";
		}
		if(midx >= currentInitiativeList.size()) {
			return "Invalid index";
		}
		Entity reactorE = currentInitiativeList.get(midx);
		
		Entity target = null;
		if(args.length == 4) {
			int tidx;
			try {
				tidx = Integer.parseInt(args[3]); 
			}catch(NumberFormatException ex) {
				return "Need entity index";
			}
			target = currentInitiativeList.get(tidx);
		}
		if(reactorE instanceof MonsterInstance) {
			MonsterInstance mi = (MonsterInstance) reactorE;
			String actionString = mi.expendReaction(reaction);
			String message = mi.personalName + " takes reaction";
			if(target != null) {
				message += " on " + target.personalName;
			}
			message += System.lineSeparator();
			message += actionString;
			log(message);
			return message;
		}else {
			return "Only monsters can take reactions now";
		}
	}
	
	String rollSave(String[] args) {
		String saveType = args[1];
		Entity entity = currentEntity;
		if(args.length == 3) {
			int midx;
			try {
				midx = Integer.parseInt(args[2]); 
			}catch(NumberFormatException ex) {
				return "Need entity index";
			}
			entity = currentInitiativeList.get(midx);
		}
		
		//TODO: I really should be logging these saves
		if(entity instanceof MonsterInstance) {
			MonsterInstance mi = (MonsterInstance) entity;
			if(saveType.equalsIgnoreCase("str")) {
				return mi.personalName + " rolls a strength saving throw of " + (Dice.d20() + mi.strsave);
			}else if(saveType.equalsIgnoreCase("dex")) {
				return mi.personalName + " rolls a dexterity saving throw of " + (Dice.d20() + mi.dexsave);
			}else if(saveType.equalsIgnoreCase("con")) {
				return mi.personalName + " rolls a constitution saving throw of " + (Dice.d20() + mi.consave);
			}else if(saveType.equalsIgnoreCase("wis")) {
				return mi.personalName + " rolls a wisdom saving throw of " + (Dice.d20() + mi.wissave);
			}else if(saveType.equalsIgnoreCase("int")) {
				return mi.personalName + " rolls a intelligence saving throw of " + (Dice.d20() + mi.intsave);
			}else if(saveType.equalsIgnoreCase("cha")) {
				return mi.personalName + " rolls a charisma saving throw of " + (Dice.d20() + mi.chasave);
			}else {
				return "Invalid saving throw type: " + saveType;
			}
		}else {
			return "Only monsters can roll saving throws for now";
		}
	}
	
	String addOrRemoveCondition(String[] args) {
		boolean amAdding = false;
		if(args[0].equals("addCon")) {
			amAdding = true;
		}
		try {
			CONDITIONS condition = EntityCondition.getCondition(args[1]);
			Entity entity = currentEntity;
			if(args.length == 3) {
				int midx;
				try {
					midx = Integer.parseInt(args[1]); 
				}catch(NumberFormatException ex) {
					return "Need entity index";
				}
				entity = currentInitiativeList.get(midx);
			}
			
			String message;
			if(amAdding) {
				entity.addCondition(condition);
				message = "Adding condition to " + entity.personalName + ": " + condition;
			}else {
				entity.removeConditions(condition);
				message = "Removing condition from " + entity.personalName + ": " + condition;
			}
			log(message);
			return message;
		}catch(IllegalArgumentException ex) {
			return ex.getMessage();
		}
		
	}
	
	String getAttrs(String[] args) {
		int midx;
		try {
			midx = Integer.parseInt(args[1]); 
		}catch(NumberFormatException ex) {
			return "Need monster index";
		}
		Entity target = currentInitiativeList.get(midx);
		return target.listStats();
	}
	
	String addPlayerCharacter(String[] args) {
		if (args.length != 3) {
			return "apc <character name> <initiative>";
		}

		Entity pc = new Entity(args[1]);
		try {
			int init = Integer.parseInt(args[2]);
			pc.setInitiative(init);
			addEntity(pc, false);
			return "Added " + pc.personalName; 
		} catch (NumberFormatException e) {
			return "Second argument must be an initiative number";
		}
	}
	
	String lairAct(String[] args) {
		if(currentLair != currentEntity) {
			return "Lair is not active entity";
		}
		
		try {
			int actionIdx = Integer.parseInt(args[1]);
			LairAction action = currentLair.getActions().get(actionIdx);
			return currentLair.expendAction(action);
		}catch(Exception ex) {
			return "Cannot execute action idx: " + args[1];
		}
	}
	
	String setLair(String[] args) {
		if(currentLair == null) {
			if(args.length != 2) {
				return "setlair <index of lair>";
			}
			
			try {
				int idx = Integer.parseInt(args[1]);
				currentLair = lairsAvailable.get(idx);
				addEntity(currentLair, false);
				return "Added Lair: " + currentLair.personalName;
			}catch(Exception ex) {
				return "Unable to use provided index: " + args[1];
			}
		}else {
			return "Cannot set lair, already set";
		}
	}
	
	String listLegendaryActions(String args[]) {
		if(args.length > 2) {
			return "listlact <optional creature index>";
		}
		if(args.length == 1) {
			if(currentEntity instanceof MonsterInstance) {
				MonsterInstance mi = (MonsterInstance) currentEntity;
				return mi.listLegendaryActions();
			}else {
				return "Can only list legendary actions for monsters";
			}
		}else {
			try {
				Integer idx = Integer.parseInt(args[1]);
				if(idx >= currentInitiativeList.size()) {
					return "Index out of range of current monster set";
				}else {
					Entity entity = currentInitiativeList.get(idx);
					if(entity instanceof MonsterInstance) {
						MonsterInstance mi = (MonsterInstance) entity;
						return mi.listLegendaryActions();
					}else {
						return "Can only list legendary actions for monsters";
					}
				}
			}catch(NumberFormatException ex) {
				return "Need a proper index for the monster";
			}
		}
	}
	
	String listActions() {
		if(currentEntity instanceof MonsterInstance) {
			MonsterInstance mi = (MonsterInstance) currentEntity;
			return mi.listActions();
		}else {
			return "Can only list actions for monsters";
		}
	}
	
	String loadMonsterSet(String args[]) {
		if(args.length != 2) {
			return "lms <filename>";
		}
		
		try {
			List<MonsterInstance> mis = MonsterSetLoader.getMonsterSet(monstersAvailable, args[1]);
			for(MonsterInstance mi : mis) {
				mi.rollInitiative();
				addEntity(mi, false);
			}
			return "Loaded Successfully";
		}catch(IllegalArgumentException ex) {
			return ex.getMessage();
		}
	}
	
	String hpMod(String args[]) {
		String cmd = args[0];
	
		if(args.length != 3 && args.length != 4) {
			if(cmd.equals("heal")) {
				return "heal <monster> <hp> <option - action taker index>";
			}else {
				return "hit <monster> <hp> <option - action taker index>";
			}
		}
		
		int midx;
		try {
			midx = Integer.parseInt(args[1]); 
		}catch(NumberFormatException ex) {
			return "Need monster index";
		}
		Entity target = currentInitiativeList.get(midx);
		
		if(target instanceof MonsterInstance) {
			MonsterInstance mi = (MonsterInstance) target;
			try {
				StringBuilder output = new StringBuilder();
				int hp = Integer.parseInt(args[2]);
				if(cmd.equals("heal")) {
					mi.heal(hp);
					if(args.length == 4) {
						try {
							int healer = Integer.parseInt(args[3]);
							if(healer >= currentInitiativeList.size()) {
								return "Invalid healer specified";
							}
							log(mi.personalName + " is healed by " + currentInitiativeList.get(healer).personalName + " for " + hp);
						}catch(NumberFormatException | IndexOutOfBoundsException ex) {
							log(mi.personalName + " is healed for " + hp);
						}
					}else{
						log(mi.personalName + " is healed for " + hp);
					}
				}else {
					mi.hit(hp);
					if(args.length == 4) {
						try {
							int healer = Integer.parseInt(args[3]);
							if(healer >= currentInitiativeList.size()) {
								return "Invalid attacker specified";
							}
							log(mi.personalName + " is hit by " + currentInitiativeList.get(healer).personalName + " for " + hp);
						}catch(NumberFormatException | IndexOutOfBoundsException ex) {
							log(mi.personalName + " is hit for " + hp);
						}
					}else{
						log(mi.personalName + " is hit for " + hp);
					}
					if(mi.concentratedSpell() != null) {
						Spell currSpell = mi.concentratedSpell();
						int saveDc = 10;
						if(hp / 2 > saveDc) {
							saveDc = hp / 2;
						}
						
						int saveRoll = Dice.d20() + mi.consave;
						String outputStr;
						if(saveRoll >= saveDc) {
							outputStr = mi.personalName + " saves " + saveRoll + " against " + saveDc + " and keeps concentrating on " + currSpell.readableName;
						}else {
							outputStr = mi.personalName + " fails " + saveRoll + " against " + saveDc + " and loses concentration on " + currSpell.readableName;
							mi.breakConcentration();
						}
						log(outputStr);
						output.append(outputStr + System.lineSeparator());
					}
				}
				log(mi.personalName + " HP: " + mi.getCurrentHp());
				output.append("Current HP: " + mi.getCurrentHp());
				return output.toString();
			}catch(NumberFormatException ex) {
				return "Invalid HP supplied.";
			}
		}else {
			return "Current actor does not have managed HP";
		}
	}
	
	String attack(String args[]) {
		if (args.length != 2 && args.length != 3) {
			return "at <attack index> <option - attackee index>";
		}
		
		if (!(currentInitiativeList.get(currentPlace) instanceof MonsterInstance)) {
			return "Must be a monster to attack";
		}
		
		if(!EntityCondition.canAttack(currentEntity.getConditions())) {
			return "Monster cannot attack in its current condition";
		}
		
		MonsterInstance monster = (MonsterInstance) currentInitiativeList.get(currentPlace);
		
		try {
			int attackIdx = Integer.parseInt(args[1]);
			try {
				Attack chosenAttack = monster.expendAttack(attackIdx);
				Set<Damage> damages = chosenAttack.rollDamage();
				String result = Attack.readDamage(damages, chosenAttack, monster);
				if(args.length == 3) {
					try {
						int attackee = Integer.parseInt(args[2]);
						log(currentEntity.personalName + " attacks " + currentInitiativeList.get(attackee).personalName);
					}catch(NumberFormatException | IndexOutOfBoundsException ex) {
						log(currentEntity.personalName + " attacks");
					}
				}else{
					log(currentEntity.personalName + " attacks");
				}
				log(result);
				return result;
			}catch(IllegalArgumentException ex) {
				return "Too high an index, not a valid attack";
			}
			
		}catch(NumberFormatException e) {
			return "Invalid attack index supplied";
		}
	}
	
	String doLegendaryAction(String args[]) {
		if(args.length != 3 && args.length != 4) {
			return "lact <actor index> <action name> <optional - actee name> => take legendary action";
		}
		
		try {
			Integer monsterIdx = Integer.parseInt(args[1]);
			Integer actee = null;
			if(args.length == 4) {
				actee = Integer.parseInt(args[3]);
				if(actee >= currentInitiativeList.size()) {
					return "Invalid index for actee";
				}
			}
			if(monsterIdx >= currentInitiativeList.size()) {
				return "Monster index too high";
			}
			if(!(currentInitiativeList.get(monsterIdx) instanceof MonsterInstance)) {
				return "Must be a monster";
			}
			MonsterInstance monster = (MonsterInstance) currentInitiativeList.get(monsterIdx);
			String actionResult = monster.expandLegendaryAction(args[2]);
			if(actee != null) {
				Entity target = currentInitiativeList.get(actee);
				actionResult += "against target " + target.personalName;
			}
			log(actionResult);
			return actionResult;
		}catch(NumberFormatException ex) {
			return "Need a valid index for both actor and actee";
		}
	}
	
	//TODO: Attack support
	String doAction(String args[]) {
		if(args.length != 2) {
			return "act <action mnemonic>";
		}
		
		//TODO: replace these blocks with an exception-throwing "is monster" check
		if (!(currentInitiativeList.get(currentPlace) instanceof MonsterInstance)) {
			return "Must be a monster to attack";
		}
		
		MonsterInstance monster = (MonsterInstance) currentInitiativeList.get(currentPlace);
		try {
			String msg = monster.expendAction(args[1]);
			log(msg);
			return msg;
		}catch(IllegalArgumentException ex) {
			log(ex.getMessage());
			return ex.getMessage();
		}
	}
	
	String castInnateSpell(String args[]) {
		if(args.length != 2) {
			return "icast <spellname>";
		}
	
		if (!(currentInitiativeList.get(currentPlace) instanceof MonsterInstance)) {
			return "Must be a monster to attack";
		}
		
		MonsterInstance monster = (MonsterInstance) currentInitiativeList.get(currentPlace);
		try {
			Spell spell = monster.expendInnateSpell(args[1]);
			String result = spell.cast(spell.minimumLevel, monster.casterLevel, monster.casterInnateDc, monster.casterToHit);
			log(currentEntity.personalName + " cast " + spell.readableName);
			log(result);
			return result;
		}catch(IllegalArgumentException ex) {
			return "Cannot cast spell: " + ex.getMessage();
		}
	}
	
	String castSpell(String args[]) {
		if(args.length != 3 && args.length != 2) {
			return "cast <spellname> <level> | cast <spellname>";
		}
		
		if (!(currentInitiativeList.get(currentPlace) instanceof MonsterInstance)) {
			return "Must be a monster to attack";
		}
		
		MonsterInstance monster = (MonsterInstance) currentInitiativeList.get(currentPlace);
		
		if(args.length == 3) {
			try {
				int spellLevel = Integer.parseInt(args[2]);
				try {
					Spell.SLOTLEVEL slotLevel = Spell.SLOTLEVEL.get(spellLevel);
					Spell spell = monster.expendSpell(args[1], slotLevel);
					String result = spell.cast(slotLevel, monster.casterLevel, monster.casterDc, monster.casterToHit);
					log(currentEntity.personalName + " cast " + spell.readableName);
					log(result);
					return result;
				}catch(IllegalArgumentException ex) {
					return ex.getMessage();
				}
			
			}catch(NumberFormatException e) {
				return "Invalid attack index supplied";
			}
		}else {
			try {
				Spell spell = monster.expendSpell(args[1]);
				String result = spell.cast(spell.minimumLevel, monster.casterLevel, monster.casterDc, monster.casterToHit);
				log(currentEntity.personalName + " cast " + spell.readableName);
				log(result);
				return result;
			}catch(IllegalArgumentException ex) {
				return ex.getMessage();
			}
		}
	}
	
	String rmEntity(String args[]) {
		if (args.length != 2) {
			return "rm <character name or index>";
		}
		
		try {
			int idx = Integer.parseInt(args[1]);
			if(idx < currentInitiativeList.size()) {
				Entity ent = currentInitiativeList.get(idx);
				removeEntity(ent);
				return "Removed: " + ent.personalName;
			}else {
				return "Invalid index supplied";
			}
		} catch (NumberFormatException e) {
			int idx = getIndexOfNamedEntity(args[1]);
			if(idx >= 0) {
				Entity ent = currentInitiativeList.get(idx);
				removeEntity(ent);
				return "Removed: " + ent.personalName;
			}else {
				return "Invalid name";
			}
		}
	}

	String addMonster(String args[]) {
		if (args.length != 3) {
			return "amon <name or index of template> <unique name>";
		}
		MonsterInstance monster;
		try {
			int idx = Integer.parseInt(args[1]);
			if (idx >= monstersAvailable.size()) {
				return "Invalid monster index supplied";
			}
			monster = monstersAvailable.get(idx).getInstance(args[2]);
		} catch (NumberFormatException ex) {
			// we were given a name
			MonsterTemplate mt = getMonsterByName(args[1]);
			if (mt == null) {
				return "Unknown monster name specified";
			}
			monster = mt.getInstance(args[2]);
		}

		monster.rollInitiative();
		addEntity(monster, false);
		return "Added " + monster.humanReadableName + " as " + monster.personalName + " with initiative "
				+ monster.getCurrentInitiative();
	}

	private MonsterTemplate getMonsterByName(String name) {
		for (MonsterTemplate tm : monstersAvailable) {
			if (tm.humanReadableName.equalsIgnoreCase(name)) {
				return tm;
			}
		}
		return null;
	}
	
	private int getIndexOfNamedEntity(String name) {
		for(int idx = 0; idx < currentInitiativeList.size(); idx++) {
			Entity ent = currentInitiativeList.get(idx);
			if(ent.personalName.equals(name)) {
				return idx;
			}
		}
		return -1;
	}
}
