package handy.rp.fortyk;

import java.io.BufferedReader;
import java.io.PrintWriter;

import handy.rp.GameRunner;
import handy.rp.OutcomeNotification;
import handy.rp.fortyk.datamodel.Army;
import handy.rp.fortyk.datamodel.UnitInstance;
import handy.rp.xml.fortyk.ArmyParser;

public class BattleRunner extends GameRunner {

	private Army currentArmy = null;

	@Override
	public String getHelp() {
//TODO fill in
		return null;
	}

	@Override
	public void processCommand(String[] args, PrintWriter pw, BufferedReader br, String rawCommand) {
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
		case "getmovement":
			if(args.length != 2) {
				pw.println("getmovement <unit name>");
			}else {
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
						"takewounds <unit name> <would number> <wound severity> <mortal wounds m/mn - optional, assume nm>");
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
			pw.println("Unknown command: " + command);
			break;
		}
		pw.flush();
	}

}
