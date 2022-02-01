package handy.rp.dnd.character;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import handy.rp.dnd.CharClass;
import handy.rp.dnd.CharSubClass;
import handy.rp.dnd.CharClass.ESSENTIAL_ABILITY_SCORE;
import handy.rp.dnd.Helpers;
import handy.rp.xml.ClassParser;
import handy.rp.xml.SubClassParser;

public class LevelUpWizard {

	private static Set<Integer> ASI_LEVELS = new HashSet<>(Arrays.asList(4, 8, 12, 16, 19));

	public static void levelUpCharacter(PlayerCharacter pc, PrintWriter pw, BufferedReader br) throws IOException {
		// Print Class Options: existing class up, or new class
		// Add all current classes to list to allow printing and index based selection.
		List<CharClass> linearClassRepresentation = new ArrayList<>();
		linearClassRepresentation.addAll(pc.getClassInfo().keySet());
		int idx;
		for (idx = 0; idx < linearClassRepresentation.size(); idx++) {
			CharClass cClass = linearClassRepresentation.get(idx);
			pw.println(idx + ": " + cClass.name + " at level " + pc.getClassInfo().get(cClass));
		}
		pw.println("Select " + idx + " for a new multiclass option");
		pw.flush();
		String response = br.readLine();

		int userChoice = Integer.parseInt(response);
		CharClass chosenClass = null;
		int chosenClassNewNum = 0;
		if (userChoice >= idx) {
			// Select a new baseclass for multiclass
			List<CharClass> baseClasses = ClassParser.getBaseCharClasses();
			boolean validInputGiven = false;
			while (!validInputGiven) {
				pw.println("Select your new base class: ");
				for (int jdx = 0; jdx < baseClasses.size(); jdx++) {
					CharClass baseClass = baseClasses.get(jdx);
					pw.println(jdx + ": " + baseClass.name);
				}
				pw.flush();
				response = br.readLine();
				try {
					userChoice = Integer.parseInt(response);
					if (userChoice < baseClasses.size()) {
						chosenClass = baseClasses.get(userChoice);
						chosenClassNewNum = 1;
						validInputGiven = true;
					} else {
						pw.println("Invalid choice");
					}
				} catch (NumberFormatException ex) {
					pw.println("Invalid choice");
				}
				pw.flush();
			}
		} else {
			chosenClass = linearClassRepresentation.get(chosenClassNewNum);
			chosenClassNewNum = pc.getClassInfo().get(chosenClass) + 1;
		}

		// Does new class option require a subclass choice?
		if (chosenClassNewNum == chosenClass.subClassLevel) {
			// Get available subclasses for class an choose one
			List<CharClass> subclasses = SubClassParser.getAllSubclassesForParent(chosenClass);
			boolean validResponse = false;
			while (!validResponse) {
				pw.println("Select a subclass: ");
				for (int jdx = 0; jdx < subclasses.size(); jdx++) {
					pw.println(jdx + ": " + subclasses.get(jdx).name);
				}
				pw.flush();
				response = br.readLine();
				try {
					userChoice = Integer.parseInt(response);
					if (userChoice < subclasses.size()) {
						// replace chosenClass with the selectedSubclass, levelUp will determine that
						// existing class is replaced
						chosenClass = subclasses.get(userChoice);
						validResponse = true;
					} else {
						pw.println("Invalid entry");
					}
				} catch (NumberFormatException ex) {
					pw.println("Invalid entry");
				}
			}
			pw.flush();
		}

		// Does leveled class hit ASI threshold
		ESSENTIAL_ABILITY_SCORE first = null;
		ESSENTIAL_ABILITY_SCORE second = null;
		if (ASI_LEVELS.contains(chosenClassNewNum)) {
			boolean userHasGivenValidInput = false;
			while (!userHasGivenValidInput) {
				pw.println("Select first ability score area (str, dex, con, int, wis, cha): ");
				pw.flush();
				response = br.readLine();

				try {
					first = ESSENTIAL_ABILITY_SCORE.getFromName(response);
				} catch (IllegalArgumentException ex) {
					continue;
				}
				pw.println("Select second ability score area (str, dex, con, int, wis, cha): ");
				pw.flush();
				response = br.readLine();

				try {
					second = ESSENTIAL_ABILITY_SCORE.getFromName(response);
				} catch (IllegalArgumentException ex) {
					continue;
				}
				userHasGivenValidInput = true;
			}

		}

		// Update HP from chosen class hit dice
		int extraHp = chosenClass.hitDice.roll() + Helpers.getModifierFromAbility(pc.getCon());

		// Commit choices to pc object, and regenerate new max spells slots, etc
		List<ESSENTIAL_ABILITY_SCORE> asis = new ArrayList<>();
		asis.add(first);
		asis.add(second);
		pc.levelUp(extraHp, asis, chosenClass, chosenClassNewNum);

		// Do I learn new spells? Add support for a wizard to capture spell learning
	}
}
