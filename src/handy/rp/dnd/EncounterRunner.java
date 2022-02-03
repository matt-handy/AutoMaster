package handy.rp.dnd;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Console;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import handy.rp.Dice;
import handy.rp.dnd.EntityCondition.CONDITIONS;
import handy.rp.dnd.SkillCheckInfo.SKILL_CHECK;
import handy.rp.dnd.attacks.Attack;
import handy.rp.dnd.attacks.Damage;
import handy.rp.dnd.character.LevelUpWizard;
import handy.rp.dnd.character.PlayerCharacter;
import handy.rp.dnd.lair.Lair;
import handy.rp.dnd.lair.LairAction;
import handy.rp.dnd.monsters.MonsterInstance;
import handy.rp.dnd.monsters.MonsterSetLoader;
import handy.rp.dnd.monsters.MonsterTemplate;
import handy.rp.dnd.spells.Spell;
import handy.rp.xml.LairParser;
import handy.rp.xml.MonsterParser;
import handy.rp.xml.PlayerCharacterParser;

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
			Console console = System.console();
			if (console == null) {
				throw new Error("Cannot start console");
			}
			if(args.length > 0) {//Single player mode
				main.singlePlayerMode(console.writer(), new BufferedReader(console.reader()), args[0]);
			}else {
				main.initialize();
				main.runEncounter(console.writer(), new BufferedReader(console.reader()));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	public void singlePlayerMode(PrintWriter pw, BufferedReader br, String pcName) throws IOException{
		PlayerCharacter pc = null;
		try {
		List<PlayerCharacter> characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
		for(PlayerCharacter pcs : characters) {
			if(pcs.personalName.equals(pcName)) {
				pc = pcs;
			}
		}
		}catch(Exception ex) {
			pw.println("Unable to load player characters");
			pw.flush();
			return;
		}
		currentEntity = pc;
		
		if(pc == null) {
			pw.println("Unable to find player character");
			pw.flush();
			return;
		}
		
		boolean stayInLoop = true;

		String nextCommand;
		while ((nextCommand = br.readLine()) != null &&
				stayInLoop) {
			String args[] = nextCommand.split(" ");

			if (args.length < 1) {
				pw.println("Improper command");
				continue;
			}

			String command = args[0];
			switch (command) {
			case "quit":
				stayInLoop = false;
				break;
			case "help":
				pw.println("advturn | advanceturn => advances play to the next turn");
				pw.println("at | attack <weapon_name> <throw - optional y/n> => attack with weapon");
				pw.println("cast <spellname> <level - optional, defaults to lowers possible level> => cast spell");
				pw.println("curhp => gives hp of current entity in initiative");
				pw.println("ef | endfeature <active feature index> => ends a feature");
				pw.println("hit | heal <hp> => hit or heal character for <hp>");
				pw.println("hitdice => lists available hit dice");
				pw.println("lcr | listclassresources => lists all class resource counters (channel divinity, rage, etc)");
				pw.println("lf | listfeatures => list features available to character");
				pw.println("lfa | listfeaturesactive => list features available to character");
				pw.println("lr | longrest => player takes a long rest");
				pw.println("ls | listspells => prints list of current monster spells");
				pw.println("lsa | listattacks => prints list attack options");
				pw.println("lss | listspellslots => prints list of current monster spell slots");
				pw.println("lvl | levelup => begins the level up process");
				pw.println("react <reaction string. oppAtt for opportunity attack> <argument - weapon name for oppAtt>");
				pw.println("rollInit | rollInitiative => roll initiative for character");
				pw.println("savethrow <STR|DEX|CON|INT|WIS|CHA> => roll a saving throw for character");
				pw.println("shd | spendhitdice => Uses a hit dice. You should only be using this if you are going to take a short rest");
				pw.println("skillcheck <skill> => player rolls a skill check");
				pw.println("sr | shortrest => player takes a short rest");
				pw.println("uf | usefeature <idx> => player uses a feature");
				break;
			case "advturn":
			case "advanceturn":
				pc.notifyNewTurn();
				if(pc.concentratedSpell() != null) {
					String conSpellMessage = pc.concentratedSpell().getRecurringEffectOnTurn(pc.getConcentratedSpellCastLevel(), pc.getCharacterLevel(), pc.getSpellSaveDC(), pc.getSpellToHit(), pc.getSpellcastingModifierValue());
					if(conSpellMessage != null) {
						pw.println(conSpellMessage);
					}
				}
				for(ClassFeature feature : pc.getActiveFeatures()) {
					pw.println("Active feature: " + feature.featureName);
				}
				break;
			case "at":
			case "attack":
				if(args.length == 2) {
					pw.println(pc.attack(args[1], false, false));
				}else if(args.length == 3) {
					if(args[2].equals("yes") || args[2].equals("y")) {
						pw.println(pc.attack(args[1], true, false));
					}else {
						pw.println(pc.attack(args[1], false, false));
					}
				}else {
					pw.println("at | attack <weapon_name> <throw - optional y/n> => attack with weapon");
				}
				
				break;
			case "ef":
			case "endfeature":
				if(args.length == 2) {
					try {
						Integer index = Integer.parseInt(args[1]);
						List<ClassFeature> features = pc.getActiveFeatures();
						if(index >= 0 && index < features.size()) {
							ClassFeature f = features.get(index);
							pc.clearFeature(index);
							pw.println(f.featureName + " cleared.");
						}else {
							pw.println("Invalid index: " + args[1]);
						}
					}catch(Exception ex) {
						pw.println("Unable to drop feature at idx: " + args[1]);
					}
				}else {
					pw.println("ef | endfeature <active feature index> => ends a feature");
				}
				break;
			case "lcr":
			case "listclassresources":
				pw.println(pc.printResourceCounters());
				break;
			case "lf":
			case "listfeatures":
				pw.println(pc.printFeatures());
				break;
			case "lfa":
			case "listfeaturesactive":
				int jdx = 0;
				for(ClassFeature feature : pc.getActiveFeatures()) {
					pw.println(jdx + " : " + feature.featureName);
					jdx++;
				}
				break;
			case "lr":
			case "longrest":
				pc.takeLongRest();
				pw.println(pc.personalName + " has taken a long rest.");
				break;
			case "ls":
			case "listspells":
				listSpells(pw);
				break;
			case "lsa":
			case "listattacks":
				pw.print(pc.listAttackOptions());
				break;
			case "lss":
			case "listspellslots":
				pw.println(pc.listSpellSlotsRemaining());
				break;
			case "lvl":
			case "levelup":
				LevelUpWizard.levelUpCharacter(pc, pw, br);
				break;
			case "react":
				if(args.length != 3) {
					pw.println("Unknown react command: " + nextCommand);
				}else {
					if(args[1].equals("oppAtt")) {
						pw.println(pc.attack(args[2], false, true));
					}else {
						pw.println("Unknown react argument: " + args[1]);
					}
				}
				break;
			case "rollInit":
			case "rollInitiative":
				pw.println("Player initiative is: " + pc.rollInitiative());
				if(pc.hasInitiativeAdvantage()) {
					pw.println("Player rolled initiative with advantage.");
				}
				break;
			case "hit":
			case "heal":
				String result = processHitHealCommand(nextCommand, pc);
				pw.println(result);
				break;
			case "hitdice":
				pw.println(pc.printRemainingHitDice());
				break;
			case "curhp":
				pw.println("Currently at " + pc.getCurrentHp() + " out of " + pc.maxHP);
				break;
			case "cast":
				pw.println(castSpell(args));
				break;
			case "savethrow":
				if(args.length != 2) {
					pw.println("savethrow <STR|DEX|CON|INT|WIS|CHA> => roll a saving throw for character");
				}else {
					pw.println(rollSave(args));
				}
				break;
			case "sr":
			case "shortrest":
				pc.takeShortRest();
				pw.print(pc.personalName + " has taken a short rest.");
				break;
			case "skillcheck":
				pw.println(processSkillRoll(args, pc));
				break;
			case "shd":
			case "spendhitdice":
				if(pc.spendHitDice()) {
					pw.println("Player spent a hit dice and now has " + pc.getCurrentHp() + " HP.");
				}else {
					pw.println("Player is out of hit dice");
				}
				break;
			case "ur":
			case "usefeature":
				if(args.length != 2) {
					pw.println("usefeature <index> => use feature at index");
				}else {
					try {
						int idx = Integer.parseInt(args[1]);
						pw.println(pc.expendFeature(idx).printEffect());
					}catch(Exception ex) {
						pw.println("Unable to use feature at idx: " + args[1] + " " + ex.getMessage());
					}
				}
				break;
			default:
				pw.println("Unknown command: " + command);
				break;
			}
			pw.flush();
		}
	}
	
	private String processSkillRoll(String args[], PlayerCharacter pc) {
		if(args.length != 2) {
			return "skillcheck <skill> => player rolls a skill check";
		}
		try {
			SKILL_CHECK check = SKILL_CHECK.getSkillFromString(args[1]);
			int rollValue = pc.rollSkillCheck(check);
			return pc.personalName + " rolls " + rollValue;
		}catch(IllegalArgumentException ex) {
			return ex.getMessage();
		}
	}
	
	private static String processHitHealCommand(String command, ManagedEntity entity) {
		String commandElements[] = command.split(" ");
		if(commandElements.length != 2) {
			return "hit | heal <number of hp>";
		}
		try {
		int hpVal = Integer.parseInt(commandElements[1]);
		if(commandElements[0].equals("hit")) {
			String hitMessage = entity.hit(hpVal);
			String message = entity.personalName + " hit for " + hpVal;
			if(hitMessage != null) {
				message = message + System.lineSeparator() + hitMessage;
			}
			return message;
		}else if(commandElements[0].equals("heal")) {
			entity.heal(hpVal);
			return entity.personalName + " healed for " + hpVal;
		}else {
			return "Unknown hit|heal command";
		}
		}catch(NumberFormatException ex) {
			return "HP value not an integer";
		}
		
		
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
			logFile = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void runEncounter(PrintWriter pw, BufferedReader br) throws IOException {
		
		boolean continueRunning = true;
		String nextCommand;
		while ((nextCommand = br.readLine()) != null && continueRunning) {
			String args[] = nextCommand.split(" ");

			if (args.length < 1) {
				pw.println("Improper command");
				continue;
			}

			String command = args[0];

			switch (command) {
			case "help":
				pw.println("advturn | advanceturn => advances play to the next entity");
				pw.println("amon | addmonster <monster index or name> <personal name> => adds monster");
				pw.println("apc | addplayercharacter <personal name> <initiative> => add character");
				pw.println("at | attack <attack index> => makes attack by current monster");
				pw.println("cast <spellname> <level - optional, defaults to lowers possible level> => cast spell but current monster");
				pw.println("cur | currententity => gives name of current in initiative");
				pw.println("curhp => gives hp of current entity in initiative");
				pw.println("heal <index of entity> <hp> => heals monster");
				pw.println("hit <index of entity> <hp> => removes monster hp");
				pw.println("gr | getround => gives round number");
				pw.println("la | listattack => prints list of current monster attacks");
				pw.println("lam | listallmonsters => prints list of monster templates available for play");
				pw.println("li | listinitiative => gives print of characters/monsters in initiative order");
				pw.println("lms | load_monster_set <filename> => loads set of monsters into initiative order");
				pw.println("ls | listspells => prints list of current monster spells");
				pw.println("lss | listspellslots => prints list of current monster spell slots");
				pw.println("rm | remove <index or personal name> => removes entity from initiative order");
				pw.println("sc | startcombat => starts play");
				
				pw.println("act <action name> => take action");
				pw.println("listact => lists actions");
				pw.println("listattr <index> => lists attributes of entity");
				
				pw.println("lact <actor index> <action name> <optional - actee name> => take legendary action");
				pw.println("listlact <optional idx> => lists legendary actions, assumes current actor");
				
				pw.println("listlairs => all available lairs");
				pw.println("setlair <Index> => Sets Lair");
				pw.println("lairact <Index> => Takes Lair Action");
				
				pw.println("addCon|rmCon <Condition abbreviation> <optional - entity index>");
				pw.println("rollSave <str|dex|con|int|wis|cha> <optional - entity index>");
				pw.println("react <reaction string. oppAtt for opportunity attack> <entity index> <optional - target index>");
				pw.println("quit => end program");
				break;
			case "quit":
				continueRunning = false;
				break;
			case "listAttr": 	
				pw.println(getAttrs(args));
			case "breakSpell":
				if(currentEntity instanceof MonsterInstance) {
					MonsterInstance mi = (MonsterInstance) currentEntity;
					if(mi.concentratedSpell() == null) {
						pw.println("Monster was not concetrating on anything");
					}else {
						Spell spell = mi.concentratedSpell();
						String str = mi.personalName + " breaks concentration on " + spell.readableName;
						mi.breakConcentration();
						pw.println(str);
						log(str);
					}
				}else {
					pw.println("Can only break concentration for monsters");
				}
				break;
			case "listlact":
				pw.println(listLegendaryActions(args));
				break;
			case "lact":
				pw.println(doLegendaryAction(args));
				break;
			case "listact":
				pw.println(listActions());
				break;
			case "act":
				pw.println(doAction(args));
				break;
			case "lms":
			case "load_monster_set":
				pw.println(loadMonsterSet(args));
				break;
			case "amon":
			case "addmonster":
				pw.println(addMonster(args));
				break;
			case "rm":
			case "remove":
				pw.println(rmEntity(args));
				break;
			case "apc":
			case "addplayercharacter":
				pw.println(addPlayerCharacter(args));
				break;
			case "gr":
			case "getround":
				pw.println("Current round is: " + roundCount);
				break;
			case "sc":
			case "startcombat":
				startCombat();
				pw.println("First in order: " + currentEntity.personalName);
				pw.println(currentEntity.listAvailableActionsAttackSpells());
				break;
			case "advturn":
			case "advanceturn":
				pw.println(advanceTurn());
				break;
			case "li":
			case "listinitiative":
				int idx = 0;
				for (Entity ent : currentInitiativeList) {
					if(ent instanceof MonsterInstance) {
						MonsterInstance mi = (MonsterInstance) ent;
						pw.println(idx + " " + ent.personalName + " " + ent.getCurrentInitiative() + " HP: " + mi.getCurrentHp());
					}else {
						pw.println(idx + " " + ent.personalName + " " + ent.getCurrentInitiative());
					}
					idx++;
				}
				break;
			case "lam":
			case "listmonsters":
				int jdx = 0;
				for (MonsterTemplate m : monstersAvailable) {
					pw.println("Monster: " + jdx + " - " + m.humanReadableName);
					jdx++;
				}
				break;
			case "listlairs":
				int kdx = 0;
				for (Lair lair : lairsAvailable) {
					pw.println("Lair: " + kdx + " - " + lair.personalName);
					kdx++;
				}
				break;
			case "la":
			case "listattack":
				if(currentInitiativeList.get(currentPlace) instanceof MonsterInstance) {
					MonsterInstance mi = (MonsterInstance) currentInitiativeList.get(currentPlace);
					pw.println(mi.listRemainingAttacksReadable());
				}else {
					pw.println("Must be a monster to list attacks");
				}
				break;
			case "at":
			case "attack":
				pw.println(attack(args));
				break;
			case "cur":
			case "currententity":
				pw.println("Current actor: " + currentEntity.personalName);
				break;
			case "curhp":
				if(currentEntity instanceof MonsterInstance) {
					MonsterInstance mi = (MonsterInstance) currentEntity;
					pw.println("Current HP: " + mi.getCurrentHp());
				}else {
					pw.println("Current actor does not have managed HP");
				}
				break;
			case "heal":
			case "hit":
				pw.println(hpMod(args));
				break;
			case "cast":
				pw.println(castSpell(args));
				break;
			case "icast":
				pw.println(castInnateSpell(args));
				break;
			case "listspells":
			case "ls":
				listSpells(pw);
				break;
			case "listspellslots":
			case "lss":
				if(currentEntity instanceof MonsterInstance) {
					MonsterInstance mi = (MonsterInstance) currentEntity;
					pw.println(mi.listSpellSlotsRemaining());
				}else {
					pw.println("Current actor does not have managed spells");
				}
				break;
			case "setlair":
				pw.println(setLair(args));
				break;
			case "lairact":
				pw.println(lairAct(args));
				break;
			case "addCon":
			case "rmCon":
				pw.println(addOrRemoveCondition(args));
				break;
			case "rollSave":
				String monsterSavingThrowResult = rollSave(args);
				log(monsterSavingThrowResult);
				pw.println(monsterSavingThrowResult);
				break;
			case "react":
				pw.println(takeReaction(args));
				break;
			default:
				pw.println("Unknown command: " + command);
				break;
			}
			pw.flush();
		}
	}
	
	
	void listSpells(PrintWriter pw) {
		if(currentEntity instanceof ManagedEntity) {
			ManagedEntity mi = (ManagedEntity) currentEntity;
			
			String spells = mi.listSpells();
			if(spells.length() != 0) {
				pw.println("Available Spells:");
				pw.println(spells);
			}
		}else {
			pw.println("Current actor does not have managed spells");
		}
		if(currentEntity instanceof MonsterInstance) {
			MonsterInstance mi = (MonsterInstance) currentEntity;
			
			String innateSpells = mi.listInnateSpells();
			if(innateSpells.length() != 0) {
				pw.println("Innate Spells:");
				pw.println(innateSpells);
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
		
		if(entity instanceof ManagedEntity) {
			ManagedEntity mi = (ManagedEntity) entity;
			if(saveType.equalsIgnoreCase("str")) {
				return mi.personalName + " rolls a strength saving throw of " + mi.strSaveThrow();
			}else if(saveType.equalsIgnoreCase("dex")) {
				return mi.personalName + " rolls a dexterity saving throw of " + mi.dexSaveThrow();
			}else if(saveType.equalsIgnoreCase("con")) {
				return mi.personalName + " rolls a constitution saving throw of " + mi.conSaveThrow();
			}else if(saveType.equalsIgnoreCase("wis")) {
				return mi.personalName + " rolls a wisdom saving throw of " + mi.wisSaveThrow();
			}else if(saveType.equalsIgnoreCase("int")) {
				return mi.personalName + " rolls a intelligence saving throw of " + mi.intSaveThrow();
			}else if(saveType.equalsIgnoreCase("cha")) {
				return mi.personalName + " rolls a charisma saving throw of " + mi.chaSaveThrow();
			}else {
				return "Invalid saving throw type: " + saveType;
			}
		}else {
			return "Only monsters and managed players can roll saving throws for now";
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
					String hitMsg = mi.hit(hp);
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
					if(hitMsg != null) {
						log(hitMsg);
						output.append(hitMsg + System.lineSeparator());
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
			String result = spell.cast(spell.minimumLevel, monster.casterLevel, monster.casterInnateDc, monster.casterToHit, -1);
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
		
		if (!(currentEntity instanceof ManagedEntity)) {
			return "Must be a ManagedEntity to attack";
		}
		
		ManagedEntity monster = (ManagedEntity) currentEntity;
		
		if(args.length == 3) {
			try {
				int spellLevel = Integer.parseInt(args[2]);
				try {
					Spell.SLOTLEVEL slotLevel = Spell.SLOTLEVEL.get(spellLevel);
					Spell spell = monster.expendSpell(args[1], slotLevel);
					PlayerCharacter optionalPc = null;
					String result = spell.cast(slotLevel, monster.getCasterLevel(), monster.getSpellSaveDC(), monster.getSpellToHit(), monster.getSpellcastingModifierValue(), optionalPc);
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
				String result = spell.cast(spell.minimumLevel, monster.getCasterLevel(), monster.getSpellSaveDC(), monster.getSpellToHit(), monster.getSpellcastingModifierValue());
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
