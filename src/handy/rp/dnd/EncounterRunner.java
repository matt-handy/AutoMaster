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

import handy.rp.OutcomeNotification;
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

	private void log(String message) {
		if (logFile != null) {
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

	public static void main(String args[]) {
		EncounterRunner main = new EncounterRunner();

		try {
			Console console = System.console();
			if (console == null) {
				throw new Error("Cannot start console");
			}
			if (args.length > 0) {// Single player mode
				main.singlePlayerMode(console.writer(), new BufferedReader(console.reader()), args[0]);
			} else {
				main.initialize();
				main.runEncounter(console.writer(), new BufferedReader(console.reader()));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	public void singlePlayerMode(PrintWriter pw, BufferedReader br, String pcName) throws IOException {
		PlayerCharacter pc = null;
		try {
			List<PlayerCharacter> characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals(pcName)) {
					pc = pcs;
				}
			}
		} catch (Exception ex) {
			pw.println("Unable to load player characters");
			pw.flush();
			return;
		}
		currentEntity = pc;

		if (pc == null) {
			pw.println("Unable to find player character");
			pw.flush();
			return;
		}

		boolean stayInLoop = true;

		String nextCommand;
		while ((nextCommand = br.readLine()) != null && stayInLoop) {
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
				pw.println(
						"lcr | listclassresources => lists all class resource counters (channel divinity, rage, etc)");
				pw.println("learnSpell <spellname> => Wizards add a new spell to their spell books");
				pw.println("lf | listfeatures => list features available to character");
				pw.println("lfa | listfeaturesactive => list features available to character");
				pw.println("lp | listproficiencies => list proficiencies");
				pw.println("lr | longrest => player takes a long rest");
				pw.println("ls | listspells => prints list of current monster spells");
				pw.println("lsa | listattacks => prints list attack options");
				pw.println("lss | listspellslots => prints list of current monster spell slots");
				pw.println("lvl | levelup => begins the level up process");
				pw.println("makeplusweapon <name> <modifier> => temporarily make a plus weapon");
				pw.println("prepareSpell <spell name> => prepare a spell from known spell list");
				pw.println(
						"react <reaction string. oppAtt for opportunity attack> <argument - weapon name for oppAtt>");
				pw.println("rollInit | rollInitiative => roll initiative for character");
				pw.println("savethrow <STR|DEX|CON|INT|WIS|CHA> => roll a saving throw for character");
				pw.println(
						"shd | spendhitdice => Uses a hit dice. You should only be using this if you are going to take a short rest");
				pw.println("skillcheck <skill> => player rolls a skill check");
				pw.println("sr | shortrest => player takes a short rest");
				pw.println("swapSpell <current prepared spell> <new prepared spell> => player prepares a spell from list");
				pw.println("uf | usefeature <idx> => player uses a feature");
				pw.println("unmakeplusweapon <name> => remove buff on weapon");
				break;
			case "advturn":
			case "advanceturn":
				pc.notifyNewTurn();
				if (pc.concentratedSpell() != null) {
					String conSpellMessage = pc.concentratedSpell().getRecurringEffectOnTurn(
							pc.getConcentratedSpellCastLevel(), pc.getCharacterLevel(), pc.getSpellSaveDC(),
							pc.getSpellToHit(), pc.getSpellcastingModifierValue());
					if (conSpellMessage != null) {
						pw.println(conSpellMessage);
					}
				}
				for (ClassFeature feature : pc.getActiveFeatures()) {
					pw.println("Active feature: " + feature.featureName);
				}
				break;
			case "at":
			case "attack":
				if (args.length == 2) {
					pw.println(pc.attack(args[1], false, false).humanMessage);
				} else if (args.length == 3) {
					if (args[2].equals("yes") || args[2].equals("y")) {
						pw.println(pc.attack(args[1], true, false).humanMessage);
					} else {
						pw.println(pc.attack(args[1], false, false).humanMessage);
					}
				} else {
					pw.println("at | attack <weapon_name> <throw - optional y/n> => attack with weapon");
				}

				break;
			case "ef":
			case "endfeature":
				if (args.length == 2) {
					try {
						Integer index = Integer.parseInt(args[1]);
						List<ClassFeature> features = pc.getActiveFeatures();
						if (index >= 0 && index < features.size()) {
							ClassFeature f = features.get(index);
							pc.clearFeature(index);
							pw.println(f.featureName + " cleared.");
						} else {
							pw.println("Invalid index: " + args[1]);
						}
					} catch (Exception ex) {
						pw.println("Unable to drop feature at idx: " + args[1]);
					}
				} else {
					pw.println("ef | endfeature <active feature index> => ends a feature");
				}
				break;
			case "lcr":
			case "listclassresources":
				pw.println(pc.printResourceCounters());
				break;
			case "learnSpell":
				if (args.length == 2) {
					boolean foundWizard = false;
					for (CharClass cClass : pc.getClassInfo().keySet()) {
						if (cClass.getRootClass().name.equalsIgnoreCase("Wizard")) {
							foundWizard = true;
							pw.println(pc.learnSpell(args[1]).humanMessage);
						}
					}
					if (!foundWizard) {
						pw.println("Only wizards can learn spells");
					}
				} else {
					pw.println("learnSpell <spell name>");
				}
				break;
			case "lf":
			case "listfeatures":
				pw.println(pc.printFeatures());
				break;
			case "lfa":
			case "listfeaturesactive":
				int jdx = 0;
				for (ClassFeature feature : pc.getActiveFeatures()) {
					pw.println(jdx + " : " + feature.featureName);
					jdx++;
				}
				break;
			case "lp":
			case "listproficiencies":
				pw.println(pc.printProficiencies());
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
			case "makeplusweapon":
				if (args.length != 3) {
					pw.println("Improper format: " + nextCommand);
				} else {
					try {
						int modifier = Integer.parseInt(args[2]);
						pw.println(pc.makeTempPlusWeapon(args[1], modifier).humanMessage);
					} catch (NumberFormatException ex) {
						pw.println("Must supply a number: " + args[2]);
					}
				}
				break;
			case "unmakeplusweapon":
				if (args.length != 2) {
					pw.println("Improper format: " + nextCommand);
				} else {
					pw.println(pc.resetTempPlusWeapon(args[1]).humanMessage);
				}
				break;
			case "react":
				if (args.length != 3) {
					pw.println("Unknown react command: " + nextCommand);
				} else {
					if (args[1].equals("oppAtt")) {
						pw.println(pc.attack(args[2], false, true).humanMessage);
					} else {
						pw.println("Unknown react argument: " + args[1]);
					}
				}
				break;
			case "rollInit":
			case "rollInitiative":
				pw.println("Player initiative is: " + pc.rollInitiative());
				if (pc.hasInitiativeAdvantage()) {
					pw.println("Player rolled initiative with advantage.");
				}
				break;
			case "hit":
			case "heal":
				String result = processHitHealCommand(nextCommand, pc).humanMessage;
				pw.println(result);
				break;
			case "hitdice":
				pw.println(pc.printRemainingHitDice());
				break;
			case "curhp":
				pw.println("Currently at " + pc.getCurrentHp() + " out of " + pc.maxHP);
				break;
			case "cast":
				pw.println(castSpell(args).humanMessage);
				break;
			case "prepareSpell":
				if (args.length == 2) {
					boolean foundWizard = false;
					for (CharClass cClass : pc.getClassInfo().keySet()) {
						if (cClass.getRootClass().name.equalsIgnoreCase("Wizard")) {
							foundWizard = true;
							pw.println(pc.addPreparedSpell(args[1]).humanMessage);
						}
					}
					if (!foundWizard) {
						pw.println("Only wizards can prepare spells for now");
					}
				} else {
					pw.println("prepareSpell <spell name>");
				}
				break;
			case "savethrow":
				if (args.length != 2) {
					pw.println("savethrow <STR|DEX|CON|INT|WIS|CHA> => roll a saving throw for character");
				} else {
					pw.println(rollSave(args).humanMessage);
				}
				break;
			case "sr":
			case "shortrest":
				pc.takeShortRest();
				pw.print(pc.personalName + " has taken a short rest.");
				break;
			case "skillcheck":
				pw.println(processSkillRoll(args, pc).humanMessage);
				break;
			case "shd":
			case "spendhitdice":
				if (pc.spendHitDice()) {
					pw.println("Player spent a hit dice and now has " + pc.getCurrentHp() + " HP.");
				} else {
					pw.println("Player is out of hit dice");
				}
				break;
			case "swapSpell":
				if (args.length == 3) {
					boolean foundWizard = false;
					for (CharClass cClass : pc.getClassInfo().keySet()) {
						if (cClass.getRootClass().name.equalsIgnoreCase("Wizard")) {
							foundWizard = true;
							pw.println(pc.swapPreparedSpell(args[1], args[2]).humanMessage);
						}
					}
					if (!foundWizard) {
						pw.println("Only wizards can swap spells for now");
					}
				} else {
					pw.println("swapSpell <old spell name> <new spell name>");
				}
				break;
			case "ur":
			case "usefeature":
				if (args.length != 2) {
					pw.println("usefeature <index> => use feature at index");
				} else {
					try {
						int idx = Integer.parseInt(args[1]);
						ClassFeature feature = pc.expendFeature(idx); 
						pw.println(feature.printEffect());
						if(feature.recoverSpellSlotsOnShortRest) {
							pc.spellSlotRecoveryWizard(feature, br, pw);
						}
					} catch (Exception ex) {
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

	private OutcomeNotification processSkillRoll(String args[], PlayerCharacter pc) {
		if (args.length != 2) {
			return new OutcomeNotification("skillcheck <skill> => player rolls a skill check", false);
		}
		try {
			SKILL_CHECK check = SKILL_CHECK.getSkillFromString(args[1]);
			int rollValue = pc.rollSkillCheck(check);
			return new OutcomeNotification(pc.personalName + " rolls " + rollValue, true);
		} catch (IllegalArgumentException ex) {
			return new OutcomeNotification(ex.getMessage(), false);
		}
	}

	private static OutcomeNotification processHitHealCommand(String command, ManagedEntity entity) {
		String commandElements[] = command.split(" ");
		if (commandElements.length != 2) {
			return new OutcomeNotification("hit | heal <number of hp>", false);
		}
		try {
			int hpVal = Integer.parseInt(commandElements[1]);
			if (commandElements[0].equals("hit")) {
				String hitMessage = entity.hit(hpVal);
				String message = entity.personalName + " hit for " + hpVal;
				if (hitMessage != null) {
					message = message + System.lineSeparator() + hitMessage;
				}
				return new OutcomeNotification(message, true);
			} else if (commandElements[0].equals("heal")) {
				entity.heal(hpVal);
				return new OutcomeNotification(entity.personalName + " healed for " + hpVal, true);
			} else {
				return new OutcomeNotification("Unknown hit|heal command", false);
			}
		} catch (NumberFormatException ex) {
			return new OutcomeNotification("HP value not an integer", false);
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

	void listSpells(PrintWriter pw) {
		if (currentEntity instanceof ManagedEntity) {
			ManagedEntity mi = (ManagedEntity) currentEntity;

			String spells = mi.listSpells();
			if (spells.length() != 0) {
				pw.println("Available Spells:");
				pw.println(spells);
			}
		} else {
			pw.println("Current actor does not have managed spells");
		}
		if (currentEntity instanceof MonsterInstance) {
			MonsterInstance mi = (MonsterInstance) currentEntity;

			String innateSpells = mi.listInnateSpells();
			if (innateSpells.length() != 0) {
				pw.println("Innate Spells:");
				pw.println(innateSpells);
			}
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

	OutcomeNotification takeReaction(String[] args) {
		String reaction = args[1];
		int midx;
		try {
			midx = Integer.parseInt(args[2]);
		} catch (NumberFormatException ex) {
			return new OutcomeNotification("Need reactor index", false);
		}
		if (midx >= currentInitiativeList.size()) {
			return new OutcomeNotification("Invalid index", false);
		}
		Entity reactorE = currentInitiativeList.get(midx);

		Entity target = null;
		if (args.length == 4) {
			int tidx;
			try {
				tidx = Integer.parseInt(args[3]);
			} catch (NumberFormatException ex) {
				return new OutcomeNotification("Need entity index", false);
			}
			target = currentInitiativeList.get(tidx);
		}
		if (reactorE instanceof MonsterInstance) {
			MonsterInstance mi = (MonsterInstance) reactorE;
			OutcomeNotification outcome = mi.expendReaction(reaction);
			if (outcome.outcome) {
				String actionString = outcome.humanMessage;
				String message = mi.personalName + " takes reaction";
				if (target != null) {
					message += " on " + target.personalName;
				}
				message += System.lineSeparator();
				message += actionString;
				log(message);
				return new OutcomeNotification(message, true);
			} else {
				return outcome;
			}
		} else {
			return new OutcomeNotification("Only monsters can take reactions now", false);
		}
	}

	OutcomeNotification rollSave(String[] args) {
		if (args.length != 2 && args.length != 3) {
			return new OutcomeNotification("rollSave <str|dex|con|int|wis|cha> <optional - entity index>", false);
		}
		String saveType = args[1];
		Entity entity = currentEntity;
		if (args.length == 3) {
			int midx;
			try {
				midx = Integer.parseInt(args[2]);
			} catch (NumberFormatException ex) {
				return new OutcomeNotification("Need entity index", false);
			}
			entity = currentInitiativeList.get(midx);
		}

		if (entity instanceof ManagedEntity) {
			ManagedEntity mi = (ManagedEntity) entity;
			if (saveType.equalsIgnoreCase("str")) {
				return new OutcomeNotification(
						mi.personalName + " rolls a strength saving throw of " + mi.strSaveThrow(), true);
			} else if (saveType.equalsIgnoreCase("dex")) {
				return new OutcomeNotification(
						mi.personalName + " rolls a dexterity saving throw of " + mi.dexSaveThrow(), true);
			} else if (saveType.equalsIgnoreCase("con")) {
				return new OutcomeNotification(
						mi.personalName + " rolls a constitution saving throw of " + mi.conSaveThrow(), true);
			} else if (saveType.equalsIgnoreCase("wis")) {
				return new OutcomeNotification(mi.personalName + " rolls a wisdom saving throw of " + mi.wisSaveThrow(),
						true);
			} else if (saveType.equalsIgnoreCase("int")) {
				return new OutcomeNotification(
						mi.personalName + " rolls a intelligence saving throw of " + mi.intSaveThrow(), true);
			} else if (saveType.equalsIgnoreCase("cha")) {
				return new OutcomeNotification(
						mi.personalName + " rolls a charisma saving throw of " + mi.chaSaveThrow(), true);
			} else {
				return new OutcomeNotification("Invalid saving throw type: " + saveType, false);
			}
		} else {
			return new OutcomeNotification("Only monsters and managed players can roll saving throws for now", false);
		}
	}

	OutcomeNotification addOrRemoveCondition(String[] args) {
		boolean amAdding = false;
		if (args[0].equals("addCon")) {
			amAdding = true;
		}
		try {
			CONDITIONS condition = EntityCondition.getCondition(args[1]);
			Entity entity = currentEntity;
			if (args.length == 3) {
				int midx;
				try {
					midx = Integer.parseInt(args[1]);
				} catch (NumberFormatException ex) {
					return new OutcomeNotification("Need entity index", false);
				}
				entity = currentInitiativeList.get(midx);
			}

			String message;
			if (amAdding) {
				entity.addCondition(condition);
				message = "Adding condition to " + entity.personalName + ": " + condition;
			} else {
				entity.removeConditions(condition);
				message = "Removing condition from " + entity.personalName + ": " + condition;
			}
			log(message);
			return new OutcomeNotification(message, true);
		} catch (IllegalArgumentException ex) {
			return new OutcomeNotification(ex.getMessage(), false);
		}

	}

	String getAttrs(String[] args) {
		int midx;
		try {
			midx = Integer.parseInt(args[1]);
		} catch (NumberFormatException ex) {
			return "Need monster index";
		}
		Entity target = currentInitiativeList.get(midx);
		return target.listStats();
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

	String listLegendaryActions(String args[]) {
		if (args.length > 2) {
			return "listlact <optional creature index>";
		}
		if (args.length == 1) {
			if (currentEntity instanceof MonsterInstance) {
				MonsterInstance mi = (MonsterInstance) currentEntity;
				return mi.listLegendaryActions();
			} else {
				return "Can only list legendary actions for monsters";
			}
		} else {
			try {
				Integer idx = Integer.parseInt(args[1]);
				if (idx >= currentInitiativeList.size()) {
					return "Index out of range of current monster set";
				} else {
					Entity entity = currentInitiativeList.get(idx);
					if (entity instanceof MonsterInstance) {
						MonsterInstance mi = (MonsterInstance) entity;
						return mi.listLegendaryActions();
					} else {
						return "Can only list legendary actions for monsters";
					}
				}
			} catch (NumberFormatException ex) {
				return "Need a proper index for the monster";
			}
		}
	}

	String listActions() {
		if (currentEntity instanceof MonsterInstance) {
			MonsterInstance mi = (MonsterInstance) currentEntity;
			return mi.listActions();
		} else {
			return "Can only list actions for monsters";
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

	OutcomeNotification hpMod(String args[]) {
		String cmd = args[0];

		if (args.length != 3 && args.length != 4) {
			if (cmd.equals("heal")) {
				return new OutcomeNotification("heal <monster> <hp> <option - action taker index>", false);
			} else {
				return new OutcomeNotification("hit <monster> <hp> <option - action taker index>", false);
			}
		}

		int midx;
		try {
			midx = Integer.parseInt(args[1]);
		} catch (NumberFormatException ex) {
			return new OutcomeNotification("Need monster index", false);
		}
		Entity target = currentInitiativeList.get(midx);

		if (target instanceof MonsterInstance) {
			MonsterInstance mi = (MonsterInstance) target;
			try {
				StringBuilder output = new StringBuilder();
				int hp = Integer.parseInt(args[2]);
				if (cmd.equals("heal")) {
					mi.heal(hp);
					if (args.length == 4) {
						try {
							int healer = Integer.parseInt(args[3]);
							if (healer >= currentInitiativeList.size()) {
								return new OutcomeNotification("Invalid healer specified", false);
							}
							log(mi.personalName + " is healed by " + currentInitiativeList.get(healer).personalName
									+ " for " + hp);
						} catch (NumberFormatException | IndexOutOfBoundsException ex) {
							log(mi.personalName + " is healed for " + hp);
						}
					} else {
						log(mi.personalName + " is healed for " + hp);
					}
				} else {
					String hitMsg = mi.hit(hp);
					if (args.length == 4) {
						try {
							int healer = Integer.parseInt(args[3]);
							if (healer >= currentInitiativeList.size()) {
								return new OutcomeNotification("Invalid attacker specified", false);
							}
							log(mi.personalName + " is hit by " + currentInitiativeList.get(healer).personalName
									+ " for " + hp);
						} catch (NumberFormatException | IndexOutOfBoundsException ex) {
							log(mi.personalName + " is hit for " + hp);
						}
					} else {
						log(mi.personalName + " is hit for " + hp);
					}
					if (hitMsg != null) {
						log(hitMsg);
						output.append(hitMsg + System.lineSeparator());
					}
				}
				log(mi.personalName + " HP: " + mi.getCurrentHp());
				output.append("Current HP: " + mi.getCurrentHp());
				return new OutcomeNotification(output.toString(), true);
			} catch (NumberFormatException ex) {
				return new OutcomeNotification("Invalid HP supplied.", false);
			}
		} else {
			return new OutcomeNotification("Current actor does not have managed HP", false);
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

	OutcomeNotification doLegendaryAction(String args[]) {
		if (args.length != 3 && args.length != 4) {
			return new OutcomeNotification(
					"lact <actor index> <action name> <optional - actee name> => take legendary action", false);
		}

		try {
			Integer monsterIdx = Integer.parseInt(args[1]);
			Integer actee = null;
			if (args.length == 4) {
				actee = Integer.parseInt(args[3]);
				if (actee >= currentInitiativeList.size()) {
					return new OutcomeNotification("Invalid index for actee", false);
				}
			}
			if (monsterIdx >= currentInitiativeList.size()) {
				return new OutcomeNotification("Monster index too high", false);
			}
			if (!(currentInitiativeList.get(monsterIdx) instanceof MonsterInstance)) {
				return new OutcomeNotification("Must be a monster", false);
			}
			MonsterInstance monster = (MonsterInstance) currentInitiativeList.get(monsterIdx);
			OutcomeNotification notice = monster.expandLegendaryAction(args[2]);
			String actionResult = notice.humanMessage;
			if (actee != null) {
				Entity target = currentInitiativeList.get(actee);
				actionResult += "against target " + target.personalName;
			}
			log(actionResult);
			return new OutcomeNotification(actionResult, notice.outcome);
		} catch (NumberFormatException ex) {
			return new OutcomeNotification("Need a valid index for both actor and actee", false);
		}
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

	OutcomeNotification castSpell(String args[]) {
		if (args.length != 3 && args.length != 2) {
			return new OutcomeNotification("cast <spellname> <level> | cast <spellname>", false);
		}

		if (!(currentEntity instanceof ManagedEntity)) {
			return new OutcomeNotification("Must be a ManagedEntity to attack", false);
		}

		ManagedEntity monster = (ManagedEntity) currentEntity;

		if (args.length == 3) {
			try {
				int spellLevel = Integer.parseInt(args[2]);
				try {
					Spell.SLOTLEVEL slotLevel = Spell.SLOTLEVEL.get(spellLevel);
					Spell spell = monster.expendSpell(args[1], slotLevel);
					PlayerCharacter optionalPc = null;
					String result = spell.cast(slotLevel, monster.getCasterLevel(), monster.getSpellSaveDC(),
							monster.getSpellToHit(), monster.getSpellcastingModifierValue(), optionalPc);
					log(currentEntity.personalName + " cast " + spell.readableName);
					log(result);
					return new OutcomeNotification(result, true);
				} catch (IllegalArgumentException ex) {
					return new OutcomeNotification(ex.getMessage(), false);
				}

			} catch (NumberFormatException e) {
				return new OutcomeNotification("Invalid attack index supplied", false);
			}
		} else {
			try {
				Spell spell = monster.expendSpell(args[1]);
				String result = spell.cast(spell.minimumLevel, monster.getCasterLevel(), monster.getSpellSaveDC(),
						monster.getSpellToHit(), monster.getSpellcastingModifierValue());
				log(currentEntity.personalName + " cast " + spell.readableName);
				log(result);
				return new OutcomeNotification(result, true);
			} catch (IllegalArgumentException ex) {
				return new OutcomeNotification(ex.getMessage(), false);
			}
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
}
