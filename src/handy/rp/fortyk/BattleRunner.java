package handy.rp.fortyk;

import java.io.BufferedReader;
import java.io.Console;
import java.io.PrintWriter;

import handy.rp.GameRunner;
import handy.rp.OutcomeNotification;
import handy.rp.fortyk.datamodel.Army;
import handy.rp.fortyk.datamodel.UnitInstance;
import handy.rp.xml.fortyk.ArmyParser;

public class BattleRunner extends GameRunner {

	public static final String TAKEWOUNDS_HELP = "takewounds <unit name> <would number> <wound severity> <mortal wounds m/mn - optional, assume nm>";
	public static final String GETMOVEMENT_HELP = "getmovement <unit name> - gets the greatest common movement of the unit";
	public static final String LOADARMY_HELP = "loadarmy <army name> - loads the army named in the command";
	public static final String MELEE_HELP = "melee <unit name> - rolls melee damage for the unit named";
	public static final String RANGED_HELP = "ranged <unit name> - rolls ranged damage for the unit named";
	public static final String GETPOINTS_HELP = "getpoints - get the point value of the current army as configured";
	
	private Army currentArmy = null;

	public static void main(String args[]) {
		Console console = System.console();
		BattleRunner runner = new BattleRunner();
		runner.mainGameLoop(console.writer(), new BufferedReader(console.reader()));
	}
	
	@Override
	public String getHelp() {
		StringBuilder sb = new StringBuilder();
		sb.append(GETMOVEMENT_HELP + System.lineSeparator());
		sb.append(GETPOINTS_HELP + System.lineSeparator());
		sb.append(LOADARMY_HELP + System.lineSeparator());
		sb.append(MELEE_HELP + System.lineSeparator());
		sb.append(RANGED_HELP + System.lineSeparator());
		sb.append(TAKEWOUNDS_HELP + System.lineSeparator());
		return sb.toString();
	}

	@Override
	public boolean processCommand(String[] args, PrintWriter pw, BufferedReader br, String rawCommand) {
		// TODO: have way to indicate unknown command and have a common unknown command
		// in parent's loop

		String command = args[0];
		switch (command) {
		case "loadarmy":
			String armyName = rawCommand.substring("loadarmy".length() + 1);
			try {
				currentArmy = ArmyParser.getArmyByName(armyName);
				pw.println("Army loaded!");
			} catch (IllegalArgumentException ex) {
				pw.println(ex.getMessage());
			}
			break;
		case "getpoints":
			if(currentArmy == null) {
				pw.println("Army not loaded");
			}else {
				pw.println(currentArmy.getTotalPointCount());
			}
			break;
		case "melee":
			if (currentArmy == null) {
				pw.println("Army not loaded");
			} else {
				String unitName = rawCommand.substring("melee ".length());
				try {
					UnitInstance unit = currentArmy.getUnitByMnemonic(unitName);
					pw.println(unit.rollAndFormatMeleeAttack());
				} catch (IllegalArgumentException ex) {
					pw.println("Unknown unit: " + unitName);
				}
			}
			break;
		case "ranged":
			if (currentArmy == null) {
				pw.println("Army not loaded");
			} else {
				String unitName = rawCommand.substring("ranged ".length());
				try {
					UnitInstance unit = currentArmy.getUnitByMnemonic(unitName);
					pw.println(unit.rollAndFormatRangedAttack());
				} catch (IllegalArgumentException ex) {
					pw.println("Unknown unit: " + unitName);
				}
			}
			break;
		case "getmovement":
			if (args.length != 2) {
				pw.println(GETMOVEMENT_HELP);
			} else {
				UnitInstance targetUnit = null;

				if (currentArmy == null) {
					pw.println("Current army not set");
					break;
				}

				for (UnitInstance unit : currentArmy.armyComposition) {
					if (unit.mnemonic.equals(args[1])) {
						targetUnit = unit;
					}
				}

				if (targetUnit == null) {
					pw.println("Unit not found");
					break;
				}
				pw.println(targetUnit.mnemonic + ": " + targetUnit.getGreatestCommonMovement());
			}
			break;
		case "takewounds":
			if (args.length != 4 && args.length != 5) {
				pw.println(
						TAKEWOUNDS_HELP);
			} else {
				UnitInstance targetUnit = null;

				if (currentArmy == null) {
					pw.println("Current army not set");
					break;
				}

				for (UnitInstance unit : currentArmy.armyComposition) {
					if (unit.mnemonic.equals(args[1])) {
						targetUnit = unit;
					}
				}

				if (targetUnit == null) {
					pw.println("Unit not found");
					break;
				}

				if (!targetUnit.isUnitAlive()) {
					pw.println("Unit no longer combat effective");
					break;
				}
				boolean isMortalWound = false;
				int apModification = 0;
				if (args.length == 5) {
					if (args[4].equalsIgnoreCase("m")) {
						isMortalWound = true;
					}
				}
				try {
					int numberOfWounds = Integer.parseInt(args[2]);
					int woundSeverity = Integer.parseInt(args[3]);
					if (!isMortalWound) {
						apModification = Integer.parseInt(args[4]);
					}
					for (int idx = 0; idx < numberOfWounds; idx++) {
						OutcomeNotification outcome = targetUnit.takeOrSaveWound(woundSeverity, isMortalWound,
								apModification);
						if (outcome.outcome) {
							pw.println(outcome.humanMessage);
						} else {
							pw.println("Unable to try wound: " + outcome.humanMessage);
						}
						if (!targetUnit.isUnitAlive()) {
							pw.println(targetUnit.mnemonic + " is no longer combat effective after wound.");
							break;
						}
					}

				} catch (Exception ex) {
					pw.println("Unknown command: " + rawCommand);
				}

			}

			break;
		default:
			return false;
		}
		pw.flush();
		return true;
	}

}
