package handy.rp.dnd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import handy.rp.OutcomeNotification;
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

public class DungeonMasterEncounterRunner extends EncounterRunner {
	
	private List<MonsterTemplate> monstersAvailable;
	private List<Lair> lairsAvailable;
	
	private int currentPlace;

	private Lair currentLair = null;
	
	public void initialize() throws Exception {
		super.initialize();;
		monstersAvailable = MonsterParser.loadAll("monsters");
		lairsAvailable = LairParser.loadAll("lairs");
	
	
	}
	
	OutcomeNotification doAction(String args[]) {
		if (args.length != 2) {
			return new OutcomeNotification("act <action mnemonic>", false);
		}

		if (!(currentInitiativeList.get(currentPlace) instanceof MonsterInstance)) {
			return new OutcomeNotification("Must be a monster to attack", false);
		}

		MonsterInstance monster = (MonsterInstance) currentInitiativeList.get(currentPlace);
		try {
			OutcomeNotification msg = monster.expendAction(args[1]);
			log(msg.humanMessage);
			return msg;
		} catch (IllegalArgumentException ex) {
			log(ex.getMessage());
			return new OutcomeNotification(ex.getMessage(), false);
		}
	}

	OutcomeNotification castInnateSpell(String args[]) {
		if (args.length != 2) {
			return new OutcomeNotification("icast <spellname>", false);
		}

		if (!(currentInitiativeList.get(currentPlace) instanceof MonsterInstance)) {
			return new OutcomeNotification("Must be a monster to attack", false);
		}

		MonsterInstance monster = (MonsterInstance) currentInitiativeList.get(currentPlace);
		try {
			Spell spell = monster.expendInnateSpell(args[1]);
			String result = spell.cast(spell.minimumLevel, monster.casterLevel, monster.casterInnateDc,
					monster.casterToHit, -1);
			log(currentEntity.personalName + " cast " + spell.readableName);
			log(result);
			return new OutcomeNotification(result, true);
		} catch (IllegalArgumentException ex) {
			return new OutcomeNotification("Cannot cast spell: " + ex.getMessage(), false);
		}
	}
	
	OutcomeNotification rmEntity(String args[]) {
		if (args.length != 2) {
			return new OutcomeNotification("rm <character name or index>", false);
		}

		try {
			int idx = Integer.parseInt(args[1]);
			if (idx < currentInitiativeList.size()) {
				Entity ent = currentInitiativeList.get(idx);
				removeEntity(ent);
				return new OutcomeNotification("Removed: " + ent.personalName, false);
			} else {
				return new OutcomeNotification("Invalid index supplied", false);
			}
		} catch (NumberFormatException e) {
			int idx = getIndexOfNamedEntity(args[1]);
			if (idx >= 0) {
				Entity ent = currentInitiativeList.get(idx);
				removeEntity(ent);
				return new OutcomeNotification("Removed: " + ent.personalName, false);
			} else {
				return new OutcomeNotification("Invalid name", false);
			}
		}
	}

	OutcomeNotification addMonster(String args[]) {
		if (args.length != 3) {
			return new OutcomeNotification("amon <name or index of template> <unique name>", false);
		}
		MonsterInstance monster;
		try {
			int idx = Integer.parseInt(args[1]);
			if (idx >= monstersAvailable.size()) {
				return new OutcomeNotification("Invalid monster index supplied", false);
			}
			monster = monstersAvailable.get(idx).getInstance(args[2]);
		} catch (NumberFormatException ex) {
			// we were given a name
			MonsterTemplate mt = getMonsterByName(args[1]);
			if (mt == null) {
				return new OutcomeNotification("Unknown monster name specified", false);
			}
			monster = mt.getInstance(args[2]);
		}

		monster.rollInitiative();
		addEntity(monster, false);
		return new OutcomeNotification("Added " + monster.humanReadableName + " as " + monster.personalName
				+ " with initiative " + monster.getCurrentInitiative(), true);
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
		for (int idx = 0; idx < currentInitiativeList.size(); idx++) {
			Entity ent = currentInitiativeList.get(idx);
			if (ent.personalName.equals(name)) {
				return idx;
			}
		}
		return -1;
	}
	
	OutcomeNotification addPlayerCharacter(String[] args) {
		if (args.length != 3) {
			return new OutcomeNotification("apc <character name> <initiative>", false);
		}

		Entity pc = new Entity(args[1]);
		try {
			int init = Integer.parseInt(args[2]);
			pc.setInitiative(init);
			addEntity(pc, false);
			return new OutcomeNotification("Added " + pc.personalName, true);
		} catch (NumberFormatException e) {
			return new OutcomeNotification("Second argument must be an initiative number", false);
		}
	}
	
	OutcomeNotification lairAct(String[] args) {
		if (currentLair != currentEntity) {
			return new OutcomeNotification("Lair is not active entity", false);
		}

		try {
			int actionIdx = Integer.parseInt(args[1]);
			LairAction action = currentLair.getActions().get(actionIdx);
			return currentLair.expendAction(action);
		} catch (Exception ex) {
			return new OutcomeNotification("Cannot execute action idx: " + args[1], false);
		}
	}

	OutcomeNotification setLair(String[] args) {
		if (currentLair == null) {
			if (args.length != 2) {
				return new OutcomeNotification("setlair <index of lair>", false);
			}

			try {
				int idx = Integer.parseInt(args[1]);
				currentLair = lairsAvailable.get(idx);
				addEntity(currentLair, false);
				return new OutcomeNotification("Added Lair: " + currentLair.personalName, true);
			} catch (Exception ex) {
				return new OutcomeNotification("Unable to use provided index: " + args[1], false);
			}
		} else {
			return new OutcomeNotification("Cannot set lair, already set", false);
		}
	}
	
	public void runEncounter(PrintWriter pw, BufferedReader br) throws IOException {

		boolean continueRunning = true;
		String nextCommand;
		while (continueRunning && (nextCommand = br.readLine()) != null) {
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
				pw.println(
						"cast <spellname> <level - optional, defaults to lowers possible level> => cast spell but current monster");
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
				pw.println(
						"react <reaction string. oppAtt for opportunity attack> <entity index> <optional - target index>");
				pw.println("quit => end program");
				break;
			case "quit":
				continueRunning = false;
				break;
			case "listAttr":
				pw.println(getAttrs(args));
			case "breakSpell":
				if (currentEntity instanceof MonsterInstance) {
					MonsterInstance mi = (MonsterInstance) currentEntity;
					if (mi.concentratedSpell() == null) {
						pw.println("Monster was not concetrating on anything");
					} else {
						Spell spell = mi.concentratedSpell();
						String str = mi.personalName + " breaks concentration on " + spell.readableName;
						mi.breakConcentration();
						pw.println(str);
						log(str);
					}
				} else {
					pw.println("Can only break concentration for monsters");
				}
				break;
			case "listlact":
				pw.println(listLegendaryActions(args));
				break;
			case "lact":
				pw.println(doLegendaryAction(args).humanMessage);
				break;
			case "listact":
				pw.println(listActions());
				break;
			case "act":
				pw.println(doAction(args).humanMessage);
				break;
			case "lms":
			case "load_monster_set":
				pw.println(loadMonsterSet(args).humanMessage);
				break;
			case "amon":
			case "addmonster":
				pw.println(addMonster(args).humanMessage);
				break;
			case "rm":
			case "remove":
				pw.println(rmEntity(args).humanMessage);
				break;
			case "apc":
			case "addplayercharacter":
				pw.println(addPlayerCharacter(args).humanMessage);
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
					if (ent instanceof MonsterInstance) {
						MonsterInstance mi = (MonsterInstance) ent;
						pw.println(idx + " " + ent.personalName + " " + ent.getCurrentInitiative() + " HP: "
								+ mi.getCurrentHp());
					} else {
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
				if (currentInitiativeList.get(currentPlace) instanceof MonsterInstance) {
					MonsterInstance mi = (MonsterInstance) currentInitiativeList.get(currentPlace);
					pw.println(mi.listRemainingAttacksReadable());
				} else {
					pw.println("Must be a monster to list attacks");
				}
				break;
			case "at":
			case "attack":
				pw.println(attack(args).humanMessage);
				break;
			case "cur":
			case "currententity":
				if (currentEntity != null) {
					pw.println("Current actor: " + currentEntity.personalName);
				} else {
					pw.println("Combat has not started");
				}
				break;
			case "curhp":
				if (currentEntity instanceof MonsterInstance) {
					MonsterInstance mi = (MonsterInstance) currentEntity;
					pw.println("Current HP: " + mi.getCurrentHp());
				} else {
					pw.println("Current actor does not have managed HP");
				}
				break;
			case "heal":
			case "hit":
				pw.println(hpMod(args).humanMessage);
				break;
			case "cast":
				pw.println(castSpell(args).humanMessage);
				break;
			case "icast":
				pw.println(castInnateSpell(args).humanMessage);
				break;
			case "listspells":
			case "ls":
				listSpells(pw);
				break;
			case "listspellslots":
			case "lss":
				if (currentEntity instanceof MonsterInstance) {
					MonsterInstance mi = (MonsterInstance) currentEntity;
					pw.println(mi.listSpellSlotsRemaining());
				} else {
					pw.println("Current actor does not have managed spells");
				}
				break;
			case "setlair":
				pw.println(setLair(args).humanMessage);
				break;
			case "lairact":
				pw.println(lairAct(args).humanMessage);
				break;
			case "addCon":
			case "rmCon":
				pw.println(addOrRemoveCondition(args).humanMessage);
				break;
			case "rollSave":
				String monsterSavingThrowResult = rollSave(args).humanMessage;
				log(monsterSavingThrowResult);
				pw.println(monsterSavingThrowResult);
				break;
			case "react":
				pw.println(takeReaction(args).humanMessage);
				break;
			default:
				pw.println("Unknown command: " + command);
				break;
			}
			pw.flush();
		}
	}
	
	OutcomeNotification loadMonsterSet(String args[]) {
		if (args.length != 2) {
			return new OutcomeNotification("lms <filename>", false);
		}

		try {
			List<MonsterInstance> mis = MonsterSetLoader.getMonsterSet(monstersAvailable, args[1]);
			for (MonsterInstance mi : mis) {
				mi.rollInitiative();
				addEntity(mi, false);
			}
			return new OutcomeNotification("Loaded Successfully", true);
		} catch (IllegalArgumentException ex) {
			return new OutcomeNotification(ex.getMessage(), false);
		}
	}
	
	OutcomeNotification attack(String args[]) {
		if (args.length != 2 && args.length != 3) {
			return new OutcomeNotification("at <attack index> <option - attackee index>", false);
		}

		if (!(currentInitiativeList.get(currentPlace) instanceof MonsterInstance)) {
			return new OutcomeNotification("Must be a monster to attack", false);
		}

		if (!EntityCondition.canAttack(currentEntity.getConditions())) {
			return new OutcomeNotification("Monster cannot attack in its current condition", false);
		}

		MonsterInstance monster = (MonsterInstance) currentInitiativeList.get(currentPlace);

		try {
			int attackIdx = Integer.parseInt(args[1]);
			try {
				Attack chosenAttack = monster.expendAttack(attackIdx);
				Set<Damage> damages = chosenAttack.rollDamage();
				String result = Attack.readDamage(damages, chosenAttack, monster);
				if (args.length == 3) {
					try {
						int attackee = Integer.parseInt(args[2]);
						log(currentEntity.personalName + " attacks "
								+ currentInitiativeList.get(attackee).personalName);
					} catch (NumberFormatException | IndexOutOfBoundsException ex) {
						log(currentEntity.personalName + " attacks");
					}
				} else {
					log(currentEntity.personalName + " attacks");
				}
				log(result);
				return new OutcomeNotification(result, true);
			} catch (IllegalArgumentException ex) {
				return new OutcomeNotification("Too high an index, not a valid attack", false);
			}

		} catch (NumberFormatException e) {
			return new OutcomeNotification("Invalid attack index supplied", false);
		}
	}
	
	String advanceTurn() {
		StringBuilder sb = new StringBuilder();
		if (currentEntity instanceof MonsterInstance) {
			MonsterInstance mi = (MonsterInstance) currentInitiativeList.get(currentPlace);
			mi.resetTurn();
		}
		if (currentPlace + 1 == currentInitiativeList.size()) {
			roundCount++;
			sb.append("New round! Current round: " + roundCount);
			sb.append(System.lineSeparator());
			currentPlace = 0;
		} else {
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
		Set<CONDITIONS> conditions = currentEntity.getConditions();
		if (!conditions.isEmpty()) {
			sb.append(conditions);
		}
		return sb.toString();
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
	
	public void removeEntity(Entity entity) {
		if (entity == currentEntity) {
			if (currentInitiativeList.indexOf(entity) == currentInitiativeList.size() - 1) {
				if (currentInitiativeList.size() > 1) {
					roundCount++;
					currentEntity = currentInitiativeList.get(0);
					currentPlace = 0;
				} else {
					currentEntity = null;
					currentPlace = -1;
				}
			} else {
				currentEntity = currentInitiativeList.get(currentPlace + 1);
				currentInitiativeList.remove(entity);
				currentPlace = currentInitiativeList.indexOf(currentEntity);
			}
		} else {
			currentInitiativeList.remove(entity);
			currentPlace = currentInitiativeList.indexOf(currentEntity);
		}
	}

	public void startCombat() {
		currentEntity = currentInitiativeList.get(0);
		currentPlace = 0;
		roundCount = 1;
	}
}
