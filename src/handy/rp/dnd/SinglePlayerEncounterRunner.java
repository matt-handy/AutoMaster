package handy.rp.dnd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import handy.rp.OutcomeNotification;
import handy.rp.dnd.SkillCheckInfo.SKILL_CHECK;
import handy.rp.dnd.character.LevelUpWizard;
import handy.rp.dnd.character.PlayerCharacter;
import handy.rp.xml.PlayerCharacterParser;

public class SinglePlayerEncounterRunner extends EncounterRunner {

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
		while (stayInLoop && (nextCommand = br.readLine()) != null) {
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
					pw.println(pc.addPreparedSpell(args[1]).humanMessage);
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
					pw.println(pc.swapPreparedSpell(args[1], args[2]).humanMessage);
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
}
