package handy.rp.dnd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SinglePlayerModeMainTest {

	@BeforeEach
	void setupDurnt() {
		try {
			Files.copy(Paths.get("player_chars_backup", "durnt_reference.xml"),
					Paths.get("player_chars", "durnt_reference.xml"), StandardCopyOption.REPLACE_EXISTING);
			Files.deleteIfExists(Paths.get("log"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@AfterEach
	void resetDurnt() {
		try {
			Files.copy(Paths.get("player_chars_backup", "durnt_reference.xml"),
					Paths.get("player_chars", "durnt_reference.xml"), StandardCopyOption.REPLACE_EXISTING);
			Files.deleteIfExists(Paths.get("log"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	void testLevelUpWizardIsIntegrated() {
		SinglePlayerEncounterRunner main = new SinglePlayerEncounterRunner();
		try {
			main.initialize();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		ByteArrayOutputStream cmdBuffer = new ByteArrayOutputStream();
		BufferedOutputStream bos = new BufferedOutputStream(cmdBuffer);
		PrintWriter builder = new PrintWriter(bos);
		builder.println("lvl");
		builder.println("0");
		builder.println("narf");
		builder.println("wis");
		builder.println("WIS");
		builder.println("quit");
		builder.flush();

		BufferedReader br = new BufferedReader(
				new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
		cmdBuffer.reset();
		bos = new BufferedOutputStream(cmdBuffer);
		builder = new PrintWriter(bos);
		try {
			main.singlePlayerMode(builder, br, "Durnt-reference");
		} catch (IOException e) {
			fail(e.getMessage());
		}

		try {
			br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
			assertEquals(br.readLine(), "0: Forge Domain Cleric at level 7");
			assertEquals(br.readLine(), "Select 1 for a new multiclass option");

			assertEquals(br.readLine(), "Select first ability score area (str, dex, con, int, wis, cha): ");
			assertEquals(br.readLine(), "That wasn't a valid ability score, try again");
			assertEquals(br.readLine(), "Select first ability score area (str, dex, con, int, wis, cha): ");
			assertEquals(br.readLine(), "Select second ability score area (str, dex, con, int, wis, cha): ");
		} catch (IOException ex) {
			fail(ex.getMessage());
		}
		main.shutdown();
	}

	@Test
	void testMainHelp() {
		SinglePlayerEncounterRunner main = new SinglePlayerEncounterRunner();
		try {
			main.initialize();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		ByteArrayOutputStream cmdBuffer = new ByteArrayOutputStream();
		BufferedOutputStream bos = new BufferedOutputStream(cmdBuffer);
		PrintWriter builder = new PrintWriter(bos);
		builder.println("help");
		builder.println("quit");
		builder.flush();

		BufferedReader br = new BufferedReader(
				new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
		cmdBuffer.reset();
		bos = new BufferedOutputStream(cmdBuffer);
		builder = new PrintWriter(bos);
		try {
			main.singlePlayerMode(builder, br, "Durnt-reference");
		} catch (IOException e) {
			fail(e.getMessage());
		}

		try {
			br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
			assertEquals(br.readLine(), "advturn | advanceturn => advances play to the next turn");
			assertEquals(br.readLine(), "at | attack <weapon_name> <throw - optional y/n> => attack with weapon");
			assertEquals(br.readLine(),
					"cast <spellname> <level - optional, defaults to lowers possible level> => cast spell");
			assertEquals(br.readLine(), "curhp => gives hp of current entity in initiative");
			assertEquals(br.readLine(), "ef | endfeature <active feature index> => ends a feature");
			assertEquals(br.readLine(), "hit | heal <hp> => hit or heal character for <hp>");
			assertEquals(br.readLine(), "hitdice => lists available hit dice");
			assertEquals("lcr | listclassresources => lists all class resource counters (channel divinity, rage, etc)",
					br.readLine());
			assertEquals("learnSpell <spellname> => Wizards add a new spell to their spell books", br.readLine());
			assertEquals("lf | listfeatures => list features available to character", br.readLine());
			assertEquals("lfa | listfeaturesactive => list features available to character", br.readLine());
			assertEquals("lp | listproficiencies => list proficiencies", br.readLine());
			assertEquals(br.readLine(), "lr | longrest => player takes a long rest");
			assertEquals(br.readLine(), "ls | listspells => prints list of current monster spells");
			assertEquals(br.readLine(), "lsa | listattacks => prints list attack options");
			assertEquals(br.readLine(), "lss | listspellslots => prints list of current monster spell slots");
			assertEquals(br.readLine(), "lvl | levelup => begins the level up process");
			assertEquals(br.readLine(), "makeplusweapon <name> <modifier> => temporarily make a plus weapon");
			assertEquals(br.readLine(), "prepareSpell <spell name> => prepare a spell from known spell list");
			assertEquals(br.readLine(),
					"react <reaction string. oppAtt for opportunity attack> <argument - weapon name for oppAtt>");
			assertEquals(br.readLine(), "rollInit | rollInitiative => roll initiative for character");
			assertEquals(br.readLine(), "savethrow <STR|DEX|CON|INT|WIS|CHA> => roll a saving throw for character");
			assertEquals(br.readLine(),
					"shd | spendhitdice => Uses a hit dice. You should only be using this if you are going to take a short rest");
			assertEquals("skillcheck <skill> => player rolls a skill check", br.readLine());
			assertEquals(br.readLine(), "sr | shortrest => player takes a short rest");
			assertEquals(br.readLine(), "swapSpell <current prepared spell> <new prepared spell> => player prepares a spell from list");
			assertEquals("uf | usefeature <idx> => player uses a feature", br.readLine());
			assertEquals(br.readLine(), "unmakeplusweapon <name> => remove buff on weapon");
		} catch (IOException ex) {
			fail(ex.getMessage());
		}
		main.shutdown();
	}

	@Test
	void testSpendHitDice() {
		SinglePlayerEncounterRunner main = new SinglePlayerEncounterRunner();
		try {
			main.initialize();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		ByteArrayOutputStream cmdBuffer = new ByteArrayOutputStream();
		BufferedOutputStream bos = new BufferedOutputStream(cmdBuffer);
		PrintWriter builder = new PrintWriter(bos);
		builder.println("hitdice");
		builder.println("shd");
		builder.println("hitdice");
		builder.println("quit");
		builder.flush();

		BufferedReader br = new BufferedReader(
				new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
		cmdBuffer.reset();
		bos = new BufferedOutputStream(cmdBuffer);
		builder = new PrintWriter(bos);
		try {
			main.singlePlayerMode(builder, br, "Durnt-reference");
		} catch (IOException e) {
			fail(e.getMessage());
		}

		try {
			br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
			assertEquals(br.readLine(), "D8: 7, ");
			assertEquals(br.readLine(), "Player spent a hit dice and now has 51 HP.");
			assertEquals(br.readLine(), "D8: 6, ");
		} catch (IOException ex) {
			fail(ex.getMessage());
		}
		main.shutdown();
	}

	@Test
	void testPlayerSkillCheck() {
		SinglePlayerEncounterRunner main = new SinglePlayerEncounterRunner();
		try {
			main.initialize();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		ByteArrayOutputStream cmdBuffer = new ByteArrayOutputStream();
		BufferedOutputStream bos = new BufferedOutputStream(cmdBuffer);
		PrintWriter builder = new PrintWriter(bos);
		builder.println("skillcheck barf");
		builder.println("skillcheck acrobatics");
		builder.println("quit");
		builder.flush();

		BufferedReader br = new BufferedReader(
				new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
		cmdBuffer.reset();
		bos = new BufferedOutputStream(cmdBuffer);
		builder = new PrintWriter(bos);
		try {
			main.singlePlayerMode(builder, br, "Durnt-reference");
		} catch (IOException e) {
			fail(e.getMessage());
		}

		try {
			br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
			assertEquals(br.readLine(), "unknown skill proficiency: barf");
			assertTrue(br.readLine().startsWith("Durnt-reference rolls "));
		} catch (IOException ex) {
			fail(ex.getMessage());
		}
		main.shutdown();
	}

	@Test
	void takeLongRest() {
		SinglePlayerEncounterRunner main = new SinglePlayerEncounterRunner();
		try {
			main.initialize();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		ByteArrayOutputStream cmdBuffer = new ByteArrayOutputStream();
		BufferedOutputStream bos = new BufferedOutputStream(cmdBuffer);
		PrintWriter builder = new PrintWriter(bos);
		builder.println("lss");
		builder.println("cast cure_wounds");
		builder.println("lr");
		builder.println("lss");
		builder.println("quit");
		builder.flush();

		BufferedReader br = new BufferedReader(
				new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
		cmdBuffer.reset();
		bos = new BufferedOutputStream(cmdBuffer);
		builder = new PrintWriter(bos);
		try {
			main.singlePlayerMode(builder, br, "Durnt-reference");
		} catch (IOException e) {
			fail(e.getMessage());
		}

		try {
			br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
			assertEquals(br.readLine(), "Level 1: 4, Level 2: 3, Level 3: 3, Level 4: 1, ");
			br.readLine();// Blank line
			assertEquals(br.readLine(), "Cure Wounds: ");
			assertEquals(br.readLine(),
					"A creature you touch regains a number of hit points equal to 1d8 + your spellcasting ability modifier. This spell has no effect on undead or constructs.");
			assertEquals(br.readLine(),
					"At Higher Levels. When you cast this spell using a spell slot of 2nd level or higher, the healing increases by 1d8 for each slot level above 1st.");
			System.out.println(br.readLine());// Blank Line
			assertTrue(br.readLine().startsWith("Healed for : "));
			assertEquals(br.readLine(), "Durnt-reference has taken a long rest.");
			assertEquals(br.readLine(), "Level 1: 4, Level 2: 3, Level 3: 3, Level 4: 1, ");
			System.out.println(br.readLine());// blank line
		} catch (IOException ex) {
			fail(ex.getMessage());
		}
		main.shutdown();
	}

	@Test
	void takeShortRest() {
		SinglePlayerEncounterRunner main = new SinglePlayerEncounterRunner();
		try {
			main.initialize();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		ByteArrayOutputStream cmdBuffer = new ByteArrayOutputStream();
		BufferedOutputStream bos = new BufferedOutputStream(cmdBuffer);
		PrintWriter builder = new PrintWriter(bos);
		builder.println("sr");
		builder.println("quit");
		builder.flush();

		BufferedReader br = new BufferedReader(
				new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
		cmdBuffer.reset();
		bos = new BufferedOutputStream(cmdBuffer);
		builder = new PrintWriter(bos);
		try {
			main.singlePlayerMode(builder, br, "Durnt-reference");
		} catch (IOException e) {
			fail(e.getMessage());
		}

		try {
			br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
			assertEquals(br.readLine(), "Durnt-reference has taken a short rest.");
		} catch (IOException ex) {
			fail(ex.getMessage());
		}
		main.shutdown();
	}

	@Test
	void testFakeCharName() {
		SinglePlayerEncounterRunner main = new SinglePlayerEncounterRunner();
		try {
			main.initialize();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		ByteArrayOutputStream cmdBuffer = new ByteArrayOutputStream();
		BufferedOutputStream bos = new BufferedOutputStream(cmdBuffer);
		PrintWriter builder = new PrintWriter(bos);

		BufferedReader br = new BufferedReader(
				new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
		cmdBuffer.reset();
		bos = new BufferedOutputStream(cmdBuffer);
		builder = new PrintWriter(bos);
		try {
			main.singlePlayerMode(builder, br, "non-existant char");
		} catch (IOException e) {
			fail(e.getMessage());
		}

		try {
			br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
			assertEquals(br.readLine(), "Unable to find player character");
		} catch (IOException ex) {
			fail(ex.getMessage());
		}
		main.shutdown();
	}

	@Test
	void testAdvanceTurn() {
		SinglePlayerEncounterRunner main = new SinglePlayerEncounterRunner();
		try {
			main.initialize();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		ByteArrayOutputStream cmdBuffer = new ByteArrayOutputStream();
		BufferedOutputStream bos = new BufferedOutputStream(cmdBuffer);
		PrintWriter builder = new PrintWriter(bos);
		builder.println("cast cure_wounds");
		builder.println("cast cure_wounds");
		builder.println("advturn");
		builder.println("cast cure_wounds");
		builder.println("quit");
		builder.flush();

		BufferedReader br = new BufferedReader(
				new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
		cmdBuffer.reset();
		bos = new BufferedOutputStream(cmdBuffer);
		builder = new PrintWriter(bos);
		try {
			main.singlePlayerMode(builder, br, "Durnt-reference");
		} catch (IOException e) {
			fail(e.getMessage());
		}

		try {
			br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
			// Test Spell list
			assertEquals(br.readLine(), "Cure Wounds: ");
			assertEquals(br.readLine(),
					"A creature you touch regains a number of hit points equal to 1d8 + your spellcasting ability modifier. This spell has no effect on undead or constructs.");
			assertEquals(br.readLine(),
					"At Higher Levels. When you cast this spell using a spell slot of 2nd level or higher, the healing increases by 1d8 for each slot level above 1st.");
			br.readLine();// Blank Line
			assertTrue(br.readLine().startsWith("Healed for : "));
			assertEquals(br.readLine(),
					"Can only cast cantrip after casting a spell on the same turn, and only if prior spell was a bonus action spell");
			assertEquals(br.readLine(), "Cure Wounds: ");
			assertEquals(br.readLine(),
					"A creature you touch regains a number of hit points equal to 1d8 + your spellcasting ability modifier. This spell has no effect on undead or constructs.");
			assertEquals(br.readLine(),
					"At Higher Levels. When you cast this spell using a spell slot of 2nd level or higher, the healing increases by 1d8 for each slot level above 1st.");
			br.readLine();// Blank Line
			assertTrue(br.readLine().startsWith("Healed for : "));
		} catch (IOException ex) {
			fail(ex.getMessage());
		}
		main.shutdown();
	}

	@Test
	void testSavingThrows() {
		SinglePlayerEncounterRunner main = new SinglePlayerEncounterRunner();
		try {
			main.initialize();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		ByteArrayOutputStream cmdBuffer = new ByteArrayOutputStream();
		BufferedOutputStream bos = new BufferedOutputStream(cmdBuffer);
		PrintWriter builder = new PrintWriter(bos);
		builder.println("savethrow narf blank blurt");
		builder.println("savethrow STR");
		builder.println("savethrow DEX");
		builder.println("savethrow CON");
		builder.println("savethrow INT");
		builder.println("savethrow WIS");
		builder.println("savethrow CHA");
		builder.println("savethrow NA");
		builder.println("quit");
		builder.flush();

		BufferedReader br = new BufferedReader(
				new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
		cmdBuffer.reset();
		bos = new BufferedOutputStream(cmdBuffer);
		builder = new PrintWriter(bos);
		try {
			main.singlePlayerMode(builder, br, "Durnt-reference");
		} catch (IOException e) {
			fail(e.getMessage());
		}

		try {
			br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
			assertEquals(br.readLine(), "savethrow <STR|DEX|CON|INT|WIS|CHA> => roll a saving throw for character");
			assertTrue(br.readLine().startsWith("Durnt-reference rolls a strength saving throw of "));
			assertTrue(br.readLine().startsWith("Durnt-reference rolls a dexterity saving throw of "));
			assertTrue(br.readLine().startsWith("Durnt-reference rolls a constitution saving throw of "));
			assertTrue(br.readLine().startsWith("Durnt-reference rolls a intelligence saving throw of "));
			assertTrue(br.readLine().startsWith("Durnt-reference rolls a wisdom saving throw of "));
			assertTrue(br.readLine().startsWith("Durnt-reference rolls a charisma saving throw of "));
			assertEquals(br.readLine(), "Invalid saving throw type: NA");

		} catch (IOException ex) {
			fail(ex.getMessage());
		}
		main.shutdown();
	}

	@Test
	void testConcentrationSavingThrowOnDamage() {
		SinglePlayerEncounterRunner main = new SinglePlayerEncounterRunner();
		try {
			main.initialize();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		ByteArrayOutputStream cmdBuffer = new ByteArrayOutputStream();
		BufferedOutputStream bos = new BufferedOutputStream(cmdBuffer);
		PrintWriter builder = new PrintWriter(bos);
		builder.println("cast heat_metal");
		builder.println("hit 50");
		builder.println("quit");
		builder.flush();

		BufferedReader br = new BufferedReader(
				new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
		cmdBuffer.reset();
		bos = new BufferedOutputStream(cmdBuffer);
		builder = new PrintWriter(bos);
		try {
			main.singlePlayerMode(builder, br, "Durnt-reference");
		} catch (IOException e) {
			fail(e.getMessage());
		}

		try {
			br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
			assertEquals(br.readLine(),
					"Heat Metal: Choose a manufactured metal object, such as a metal weapon or a suit of heavy or medium metal armor, that you can see within range. You cause the object to glow red-hot. Any creature in physical contact with the object takes 2d8 fire damage when you cast the spell. Until the spell ends, you can use a bonus action on each of your subsequent turns to cause this damage again.");
			assertEquals(br.readLine(),
					"If a creature is holding or wearing the object and takes the damage from it, the creature must succeed on a Constitution saving throw or drop the object if it can. If it doesn't drop the object, it has disadvantage on attack rolls and ability checks until the start of your next turn.");
			assertEquals(br.readLine(),
					"At Higher Levels. When you cast this spell using a spell slot of 3rd level or higher, the damage increases by 1d8 for each slot level above 2nd.");
			assertTrue(br.readLine().startsWith(" hits for "));
			assertEquals(br.readLine(), "Spell Save: 15");
			assertEquals(br.readLine(), "Durnt-reference hit for 50");
			String saveThrowMessage = br.readLine();
			assertTrue(saveThrowMessage.startsWith("Durnt-reference rolled a CON save of "));
			assertTrue(saveThrowMessage
					.endsWith(" against a target of 25 and failed. They are no longer concentrating on a spell."));
		} catch (IOException ex) {
			fail(ex.getMessage());
		}
		main.shutdown();
	}

	@Test
	void testPlayerCharacterRecurringFeature() {
		SinglePlayerEncounterRunner main = new SinglePlayerEncounterRunner();
		try {
			main.initialize();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		ByteArrayOutputStream cmdBuffer = new ByteArrayOutputStream();
		BufferedOutputStream bos = new BufferedOutputStream(cmdBuffer);
		PrintWriter builder = new PrintWriter(bos);
		builder.println("usefeature 14");
		builder.println("advturn");
		builder.println("quit");
		builder.flush();

		BufferedReader br = new BufferedReader(
				new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
		cmdBuffer.reset();
		bos = new BufferedOutputStream(cmdBuffer);
		builder = new PrintWriter(bos);
		try {
			main.singlePlayerMode(builder, br, "barbie_the_barbarian");
		} catch (IOException e) {
			fail(e.getMessage());
		}

		try {
			br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
			assertEquals(br.readLine(),
					"Beginning at 10th level, you can use your action to frighten someone with your menacing presence. When you do so, choose one creature that you can see within 30 feet of you. If the creature can see or hear you, it must succeed on a Wisdom saving throw (DC equal to 8 + your proficiency bonus + your Charisma modifier) or be frightened of you until the end of your next turn. On subsequent turns, you can use your action to extend the duration of this effect on the frightened creature until the end of your next turn. This effect ends if the creature ends its turn out of line of sight or more than 60 feet away from you. If the creature succeeds on its saving throw, you can’t use this feature on that creature again for 24 hours.");
			assertEquals(br.readLine(), "Active feature: Intimidating Presence");
		} catch (IOException ex) {
			fail(ex.getMessage());
		}
		main.shutdown();
	}

	@Test
	void testPlayerCharacterManageActiveFeatures() {
		SinglePlayerEncounterRunner main = new SinglePlayerEncounterRunner();
		try {
			main.initialize();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		ByteArrayOutputStream cmdBuffer = new ByteArrayOutputStream();
		BufferedOutputStream bos = new BufferedOutputStream(cmdBuffer);
		PrintWriter builder = new PrintWriter(bos);
		builder.println("usefeature 0");
		builder.println("lfa");
		builder.println("ef 0");
		builder.println("quit");
		builder.flush();

		BufferedReader br = new BufferedReader(
				new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
		cmdBuffer.reset();
		bos = new BufferedOutputStream(cmdBuffer);
		builder = new PrintWriter(bos);
		try {
			main.singlePlayerMode(builder, br, "barbie_the_barbarian");
		} catch (IOException e) {
			fail(e.getMessage());
		}

		try {
			br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
			assertEquals(br.readLine(),
					"In battle, you fight with primal ferocity. On your turn, you can enter a rage as a bonus action.");
			assertEquals(br.readLine(),
					"While raging, you gain the following benefits if you aren’t wearing heavy armor:");
			assertEquals(br.readLine(), "    You have advantage on Strength checks and Strength saving throws.");
			assertEquals(br.readLine(),
					"    When you make a melee weapon attack using Strength, you gain a bonus to the damage roll that increases as you gain levels as a barbarian, as shown in the Rage Damage column of the Barbarian table.");
			assertEquals(br.readLine(), "    You have resistance to bludgeoning, piercing, and slashing damage.");
			assertEquals(br.readLine(),
					"If you are able to cast spells, you can’t cast them or concentrate on them while raging.");
			assertEquals(br.readLine(),
					"Your rage lasts for 1 minute. It ends early if you are knocked unconscious or if your turn ends and you haven’t attacked a hostile creature since your last turn or taken damage since then. You can also end your rage on your turn as a bonus action.");
			assertEquals(br.readLine(),
					"Once you have raged the number of times shown for your barbarian level in the Rages column of the Barbarian table, you must finish a long rest before you can rage again.");
			assertEquals(br.readLine(), "0 : Rage");
			assertEquals(br.readLine(), "Rage cleared.");
		} catch (IOException ex) {
			fail(ex.getMessage());
		}
		main.shutdown();
	}

	@Test
	void testPlayerAttackOfOpportunity() {
		SinglePlayerEncounterRunner main = new SinglePlayerEncounterRunner();
		try {
			main.initialize();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		ByteArrayOutputStream cmdBuffer = new ByteArrayOutputStream();
		BufferedOutputStream bos = new BufferedOutputStream(cmdBuffer);
		PrintWriter builder = new PrintWriter(bos);
		builder.println("react oppAtt");
		builder.println("react narf wat");
		builder.println("react oppAtt warhammer");
		builder.println("react oppAtt warhammer");
		builder.println("quit");
		builder.flush();

		BufferedReader br = new BufferedReader(
				new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
		cmdBuffer.reset();
		bos = new BufferedOutputStream(cmdBuffer);
		builder = new PrintWriter(bos);
		try {
			main.singlePlayerMode(builder, br, "Durnt-reference");
		} catch (IOException e) {
			fail(e.getMessage());
		}

		try {
			br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
			assertEquals("Unknown react command: react oppAtt", br.readLine());
			assertEquals("Unknown react argument: narf", br.readLine());
			assertTrue(br.readLine().startsWith("Durnt-reference strikes with Warhammer with a to hit of "));
			assertEquals("Character has already used reaction", br.readLine());
		} catch (IOException ex) {
			fail(ex.getMessage());
		}
		main.shutdown();
	}

	@Test
	void testPlayerPrintProficiencies() {
		SinglePlayerEncounterRunner main = new SinglePlayerEncounterRunner();
		try {
			main.initialize();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		ByteArrayOutputStream cmdBuffer = new ByteArrayOutputStream();
		BufferedOutputStream bos = new BufferedOutputStream(cmdBuffer);
		PrintWriter builder = new PrintWriter(bos);
		builder.println("lp");
		builder.println("quit");
		builder.flush();

		BufferedReader br = new BufferedReader(
				new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
		cmdBuffer.reset();
		bos = new BufferedOutputStream(cmdBuffer);
		builder = new PrintWriter(bos);
		try {
			main.singlePlayerMode(builder, br, "Durnt-reference");
		} catch (IOException e) {
			fail(e.getMessage());
		}

		try {
			br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
			assertEquals(br.readLine(), "Skill Proficiencies");
			assertEquals(br.readLine(), "Acrobatics");
			assertEquals(br.readLine(), "Medicine");
			assertEquals(br.readLine(), "Persuasion");
			assertEquals(br.readLine(), "Survival");
			assertEquals(br.readLine(),
					"Armor Proficiencies");
			br.readLine();//For proficiencies listed, tested elsewhere
			br.readLine();//For proficiencies listed, tested elsewhere
			br.readLine();//For proficiencies listed, tested elsewhere
			br.readLine();//For proficiencies listed, tested elsewhere
			assertEquals(br.readLine(),
					"Tool Proficiencies");
			assertEquals(br.readLine(), "smith");
		} catch (IOException ex) {
			fail(ex.getMessage());
		}
		main.shutdown();
	}

	@Test
	void testPlayerCharacterConcentrationRecurringDamage() {
		SinglePlayerEncounterRunner main = new SinglePlayerEncounterRunner();
		try {
			main.initialize();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		ByteArrayOutputStream cmdBuffer = new ByteArrayOutputStream();
		BufferedOutputStream bos = new BufferedOutputStream(cmdBuffer);
		PrintWriter builder = new PrintWriter(bos);
		builder.println("cast heat_metal");
		builder.println("advturn");
		builder.println("quit");
		builder.flush();

		BufferedReader br = new BufferedReader(
				new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
		cmdBuffer.reset();
		bos = new BufferedOutputStream(cmdBuffer);
		builder = new PrintWriter(bos);
		try {
			main.singlePlayerMode(builder, br, "Durnt-reference");
		} catch (IOException e) {
			fail(e.getMessage());
		}

		try {
			br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
			assertEquals(br.readLine(),
					"Heat Metal: Choose a manufactured metal object, such as a metal weapon or a suit of heavy or medium metal armor, that you can see within range. You cause the object to glow red-hot. Any creature in physical contact with the object takes 2d8 fire damage when you cast the spell. Until the spell ends, you can use a bonus action on each of your subsequent turns to cause this damage again.");
			assertEquals(br.readLine(),
					"If a creature is holding or wearing the object and takes the damage from it, the creature must succeed on a Constitution saving throw or drop the object if it can. If it doesn't drop the object, it has disadvantage on attack rolls and ability checks until the start of your next turn.");
			assertEquals(br.readLine(),
					"At Higher Levels. When you cast this spell using a spell slot of 3rd level or higher, the damage increases by 1d8 for each slot level above 2nd.");
			assertTrue(br.readLine().startsWith(" hits for "));
			assertEquals(br.readLine(), "Spell Save: 15");
			assertEquals(br.readLine(),
					"On your turn, you may use the spell Heat Metal as a bonus action to inflict: ");
			assertEquals(br.readLine(),
					"Heat Metal: Choose a manufactured metal object, such as a metal weapon or a suit of heavy or medium metal armor, that you can see within range. You cause the object to glow red-hot. Any creature in physical contact with the object takes 2d8 fire damage when you cast the spell. Until the spell ends, you can use a bonus action on each of your subsequent turns to cause this damage again.");
			assertEquals(br.readLine(),
					"If a creature is holding or wearing the object and takes the damage from it, the creature must succeed on a Constitution saving throw or drop the object if it can. If it doesn't drop the object, it has disadvantage on attack rolls and ability checks until the start of your next turn.");
			assertEquals(br.readLine(),
					"At Higher Levels. When you cast this spell using a spell slot of 3rd level or higher, the damage increases by 1d8 for each slot level above 2nd.");
			assertTrue(br.readLine().startsWith(" hits for "));
			assertEquals(br.readLine(), "Spell Save: 15");
		} catch (IOException ex) {
			fail(ex.getMessage());
		}
		main.shutdown();
	}

	@Test
	void testPlayerCastSpell() {
		SinglePlayerEncounterRunner main = new SinglePlayerEncounterRunner();
		try {
			main.initialize();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		ByteArrayOutputStream cmdBuffer = new ByteArrayOutputStream();
		BufferedOutputStream bos = new BufferedOutputStream(cmdBuffer);
		PrintWriter builder = new PrintWriter(bos);
		builder.println("ls");
		builder.println("lss");
		builder.println("cast cure_wounds");
		builder.println("lss");
		builder.println("cast cure_wounds");
		builder.println("quit");
		builder.flush();

		BufferedReader br = new BufferedReader(
				new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
		cmdBuffer.reset();
		bos = new BufferedOutputStream(cmdBuffer);
		builder = new PrintWriter(bos);
		try {
			main.singlePlayerMode(builder, br, "Durnt-reference");
		} catch (IOException e) {
			fail(e.getMessage());
		}

		try {
			br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
			// Test Spell list
			assertEquals(br.readLine(), "Available Spells:");
			testDurntSpellListing(br);
			br.readLine();// Blank line
			assertEquals(br.readLine(), "Level 1: 4, Level 2: 3, Level 3: 3, Level 4: 1, ");
			br.readLine();// Blank line
			assertEquals(br.readLine(), "Cure Wounds: ");
			assertEquals(br.readLine(),
					"A creature you touch regains a number of hit points equal to 1d8 + your spellcasting ability modifier. This spell has no effect on undead or constructs.");
			assertEquals(br.readLine(),
					"At Higher Levels. When you cast this spell using a spell slot of 2nd level or higher, the healing increases by 1d8 for each slot level above 1st.");
			br.readLine();
			assertTrue(br.readLine().startsWith("Healed for : "));
			assertEquals(br.readLine(), "Level 1: 3, Level 2: 3, Level 3: 3, Level 4: 1, ");
			br.readLine();
			assertEquals(br.readLine(),
					"Can only cast cantrip after casting a spell on the same turn, and only if prior spell was a bonus action spell");
		} catch (IOException ex) {
			fail(ex.getMessage());
		}
		main.shutdown();
	}

	private void testDurntSpellListing(BufferedReader br) throws IOException {
		for (int idx = 0; idx < 5; idx++) {
			String nextLine = br.readLine();
			assertTrue(nextLine.equals("Level: 4 Guardian of Faith, Death Ward, Fabricate, Wall of Fire, ")
					|| nextLine.equals(
							"Level: 1 Identify, Searing Smite, Bane, Cure Wounds, Shield of Faith, Detect Good and Evil, Guiding Bolt, ")
					|| nextLine.equals("Level: 2 Heat Metal, Magic Weapon, Hold Person, Zone of Truth, ")
					|| nextLine.equals("Level: Cantrip Spare the Dying, Sacred Flame, Thaumaturgy, Toll the Dead, ")
					|| nextLine.equals(
							"Level: 3 Spirit Guardians, Magic Circle, Protection From Energy, Elemental Weapon, "));
		}
	}

	@Test
	void testFeatureIntegeration() {
		SinglePlayerEncounterRunner main = new SinglePlayerEncounterRunner();
		try {
			main.initialize();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		ByteArrayOutputStream cmdBuffer = new ByteArrayOutputStream();
		BufferedOutputStream bos = new BufferedOutputStream(cmdBuffer);
		PrintWriter builder = new PrintWriter(bos);
		builder.println("listclassresources");
		builder.println("listfeatures");
		builder.println("usefeature 0");
		builder.println("usefeature barf");
		builder.println("listclassresources");
		builder.println("quit");
		builder.flush();

		BufferedReader br = new BufferedReader(
				new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
		cmdBuffer.reset();
		bos = new BufferedOutputStream(cmdBuffer);
		builder = new PrintWriter(bos);
		try {
			main.singlePlayerMode(builder, br, "Durnt-reference");
		} catch (IOException e) {
			fail(e.getMessage());
		}

		try {
			br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
			assertEquals(br.readLine(), "Channel Divinity 2 available out of 2");
			assertEquals("0 : Channel Divinity - Turn Undead", br.readLine());
			assertEquals("1 : Bonus Proficiencies", br.readLine());
			assertEquals("2 : Channel Divinity - Artisan's Blessing", br.readLine());
			assertEquals("3 : Soul of the Forge", br.readLine());
			assertEquals("4 : Blessing of the Forge", br.readLine());
			assertEquals(br.readLine(),
					"As an action, you present your holy symbol and speak a prayer censuring the undead. Each undead that can see or hear you within 30 feet of you must make a Wisdom saving throw. If the creature fails its saving throw, it is turned for 1 minute or until it takes any damage.");
			assertEquals(br.readLine(),
					"A turned creature must spend its turns trying to move as far away from you as it can, and it can't willingly move to a space within 30 feet of you. It also can't take reactions. For its action, it can use only the Dash action or try to escape from an effect that prevents it from moving. If there's nowhere to move, the creature can use the Dodge action.");
			assertEquals(br.readLine(),
					"Starting at 5th level, when an undead fails its saving throw against your Turn Undead feature, the creature is instantly destroyed if its challenge rating is at or below a certain threshold. For a 5th level cleric, CR 1/2 or lower. For a 8th level cleric, CR 1 or lower, For a 11th level cleric, CR 2 or lower, For a 14th level cleric, CR 3 or lower, For a 17th level cleric, CR 4 or lower.");
			assertEquals(br.readLine(), "Unable to use feature at idx: barf For input string: \"barf\"");
			assertEquals(br.readLine(), "Channel Divinity 1 available out of 2");
		} catch (IOException ex) {
			fail(ex.getMessage());
		}
		main.shutdown();
	}

	@Test
	void testPlayerMakePlusWeapon() {
		SinglePlayerEncounterRunner main = new SinglePlayerEncounterRunner();
		try {
			main.initialize();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		ByteArrayOutputStream cmdBuffer = new ByteArrayOutputStream();
		BufferedOutputStream bos = new BufferedOutputStream(cmdBuffer);
		PrintWriter builder = new PrintWriter(bos);
		builder.println("makeplusweapon narf");
		builder.println("makeplusweapon barf barf");
		builder.println("makeplusweapon warhammer 2");
		builder.println("unmakeplusweapon");
		builder.println("unmakeplusweapon dingdong");
		builder.println("unmakeplusweapon warhammer");
		builder.println("quit");
		builder.flush();

		BufferedReader br = new BufferedReader(
				new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
		cmdBuffer.reset();
		bos = new BufferedOutputStream(cmdBuffer);
		builder = new PrintWriter(bos);
		try {
			main.singlePlayerMode(builder, br, "Durnt-reference");
		} catch (IOException e) {
			fail(e.getMessage());
		}

		try {
			br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
			assertEquals("Improper format: makeplusweapon narf", br.readLine());
			assertEquals("Must supply a number: barf", br.readLine());
			assertEquals("Character weapon modifier has been set", br.readLine());
			assertEquals("Improper format: unmakeplusweapon", br.readLine());
			assertEquals("Character does not have this weapon available", br.readLine());
			assertEquals("Character weapon modifier has been reset", br.readLine());
		} catch (IOException ex) {
			fail(ex.getMessage());
		}
		main.shutdown();
	}

	@Test
	void testPlayerHitAndHeal() {
		SinglePlayerEncounterRunner main = new SinglePlayerEncounterRunner();
		try {
			main.initialize();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		ByteArrayOutputStream cmdBuffer = new ByteArrayOutputStream();
		BufferedOutputStream bos = new BufferedOutputStream(cmdBuffer);
		PrintWriter builder = new PrintWriter(bos);
		builder.println("hit 12");
		builder.println("curhp");
		builder.println("heal 4");
		builder.println("curhp");
		builder.println("heal narf");
		builder.println("heal 14 wat");
		builder.println("quit");
		builder.flush();

		BufferedReader br = new BufferedReader(
				new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
		cmdBuffer.reset();
		bos = new BufferedOutputStream(cmdBuffer);
		builder = new PrintWriter(bos);
		try {
			main.singlePlayerMode(builder, br, "Durnt-reference");
		} catch (IOException e) {
			fail(e.getMessage());
		}

		try {
			br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
			assertEquals(br.readLine(), "Durnt-reference hit for 12");
			assertEquals(br.readLine(), "Currently at 39 out of 51");
			assertEquals(br.readLine(), "Durnt-reference healed for 4");
			assertEquals(br.readLine(), "Currently at 43 out of 51");
			assertEquals(br.readLine(), "HP value not an integer");
			assertEquals(br.readLine(), "hit | heal <number of hp>");
		} catch (IOException ex) {
			fail(ex.getMessage());
		}
		main.shutdown();
	}

	@Test
	void testListAvailableWeaponsAndAttacks() {
		SinglePlayerEncounterRunner main = new SinglePlayerEncounterRunner();
		try {
			main.initialize();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		ByteArrayOutputStream cmdBuffer = new ByteArrayOutputStream();
		BufferedOutputStream bos = new BufferedOutputStream(cmdBuffer);
		PrintWriter builder = new PrintWriter(bos);
		builder.println("lsa");
		builder.println("quit");
		builder.flush();

		BufferedReader br = new BufferedReader(
				new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
		cmdBuffer.reset();
		bos = new BufferedOutputStream(cmdBuffer);
		builder = new PrintWriter(bos);
		try {
			main.singlePlayerMode(builder, br, "Durnt-reference");
		} catch (IOException e) {
			fail(e.getMessage());
		}

		try {
			br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
			assertEquals(br.readLine(), "Available attacks: 1 out of a max per turn: 1");
			assertEquals(br.readLine(),
					"(proficient) Warhammer(warhammer) hits for 1D8. Attributes include: VERSATILE, ");
			assertEquals(br.readLine(),
					"(proficient) Light Crossbow(light_crossbow) hits for 1D8 with a range of 80/320. Attributes include: TWO_HANDED, LOADING, AMMUNITION, ");
			assertEquals(br.readLine(),
					"(proficient) Dagger(dagger) hits for 1D4 and can be thrown for a range of 20/60. Attributes include: FINESSE, LIGHT, THROWN, ");
		} catch (IOException ex) {
			fail(ex.getMessage());
		}
		main.shutdown();
	}

	@Test
	void testPlayerCharacterStrikeRanged() {
		SinglePlayerEncounterRunner main = new SinglePlayerEncounterRunner();
		try {
			main.initialize();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		ByteArrayOutputStream cmdBuffer = new ByteArrayOutputStream();
		BufferedOutputStream bos = new BufferedOutputStream(cmdBuffer);
		PrintWriter builder = new PrintWriter(bos);
		builder.println("attack light_crossbow");
		builder.println("lsa");
		builder.println("at light_crossbow");
		builder.println("quit");
		builder.flush();

		BufferedReader br = new BufferedReader(
				new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
		cmdBuffer.reset();
		bos = new BufferedOutputStream(cmdBuffer);
		builder = new PrintWriter(bos);
		try {
			main.singlePlayerMode(builder, br, "Durnt-reference");
		} catch (IOException e) {
			fail(e.getMessage());
		}

		try {
			br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
			assertTrue(br.readLine()
					.startsWith("Durnt-reference uses Light Crossbow to hit at range(80/320) with a to hit of "));
			assertEquals(br.readLine(), "Available attacks: 0 out of a max per turn: 1");
			assertEquals(br.readLine(),
					"(proficient) Warhammer(warhammer) hits for 1D8. Attributes include: VERSATILE, ");
			assertEquals(br.readLine(),
					"(proficient) Light Crossbow(light_crossbow) hits for 1D8 with a range of 80/320. Attributes include: TWO_HANDED, LOADING, AMMUNITION, ");
			assertEquals(br.readLine(),
					"(proficient) Dagger(dagger) hits for 1D4 and can be thrown for a range of 20/60. Attributes include: FINESSE, LIGHT, THROWN, ");
			assertEquals(br.readLine(), "No attacks remaining this turn");
		} catch (IOException ex) {
			fail(ex.getMessage());
		}
		main.shutdown();
	}

	@Test
	void testPlayerCharacterStrikeMelee() {
		SinglePlayerEncounterRunner main = new SinglePlayerEncounterRunner();
		try {
			main.initialize();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		ByteArrayOutputStream cmdBuffer = new ByteArrayOutputStream();
		BufferedOutputStream bos = new BufferedOutputStream(cmdBuffer);
		PrintWriter builder = new PrintWriter(bos);
		builder.println("attack warhammer");
		builder.println("quit");
		builder.flush();

		BufferedReader br = new BufferedReader(
				new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
		cmdBuffer.reset();
		bos = new BufferedOutputStream(cmdBuffer);
		builder = new PrintWriter(bos);
		try {
			main.singlePlayerMode(builder, br, "Durnt-reference");
		} catch (IOException e) {
			fail(e.getMessage());
		}

		try {
			br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
			assertTrue(br.readLine().startsWith("Durnt-reference strikes with Warhammer with a to hit of "));

		} catch (IOException ex) {
			fail(ex.getMessage());
		}
		main.shutdown();
	}

	@Test
	void testPlayerCharacterStrikeThrown() {
		SinglePlayerEncounterRunner main = new SinglePlayerEncounterRunner();
		try {
			main.initialize();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		ByteArrayOutputStream cmdBuffer = new ByteArrayOutputStream();
		BufferedOutputStream bos = new BufferedOutputStream(cmdBuffer);
		PrintWriter builder = new PrintWriter(bos);
		builder.println("attack dagger y");
		builder.println("quit");
		builder.flush();

		BufferedReader br = new BufferedReader(
				new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
		cmdBuffer.reset();
		bos = new BufferedOutputStream(cmdBuffer);
		builder = new PrintWriter(bos);
		try {
			main.singlePlayerMode(builder, br, "Durnt-reference");
		} catch (IOException e) {
			fail(e.getMessage());
		}

		try {
			br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
			assertTrue(br.readLine().startsWith("Durnt-reference throws Dagger at range(20/60) with a to hit of "));
		} catch (IOException ex) {
			fail(ex.getMessage());
		}
		main.shutdown();
	}

	@Test
	void testPlayerCharacterRollInitiative() {
		SinglePlayerEncounterRunner main = new SinglePlayerEncounterRunner();
		try {
			main.initialize();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		ByteArrayOutputStream cmdBuffer = new ByteArrayOutputStream();
		BufferedOutputStream bos = new BufferedOutputStream(cmdBuffer);
		PrintWriter builder = new PrintWriter(bos);
		builder.println("rollInit");
		builder.println("quit");
		builder.flush();

		BufferedReader br = new BufferedReader(
				new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
		cmdBuffer.reset();
		bos = new BufferedOutputStream(cmdBuffer);
		builder = new PrintWriter(bos);
		try {
			main.singlePlayerMode(builder, br, "Durnt-reference");
		} catch (IOException e) {
			fail(e.getMessage());
		}

		try {
			br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
			String initStr = br.readLine();
			assertTrue(initStr.startsWith("Player initiative is: "));
			int initiative = Integer.parseInt(initStr.substring("Player initiative is: ".length()));
			assertTrue(initiative >= 1 + 1 && initiative <= 20 + 1);
		} catch (IOException ex) {
			fail(ex.getMessage());
		}
		main.shutdown();
	}

	@Test
	void testPlayerCharacterRollInitiativeAdv() {
		SinglePlayerEncounterRunner main = new SinglePlayerEncounterRunner();
		try {
			main.initialize();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		ByteArrayOutputStream cmdBuffer = new ByteArrayOutputStream();
		BufferedOutputStream bos = new BufferedOutputStream(cmdBuffer);
		PrintWriter builder = new PrintWriter(bos);
		builder.println("rollInit");
		builder.println("quit");
		builder.flush();

		BufferedReader br = new BufferedReader(
				new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
		cmdBuffer.reset();
		bos = new BufferedOutputStream(cmdBuffer);
		builder = new PrintWriter(bos);
		try {
			main.singlePlayerMode(builder, br, "barbie_the_barbarian");
		} catch (IOException e) {
			fail(e.getMessage());
		}

		try {
			br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
			String initStr = br.readLine();
			assertTrue(initStr.startsWith("Player initiative is: "));
			int initiative = Integer.parseInt(initStr.substring("Player initiative is: ".length()));
			assertTrue(initiative >= 1 + 3 && initiative <= 20 + 3);
			initStr = br.readLine();
			assertEquals("Player rolled initiative with advantage.", initStr);
		} catch (IOException ex) {
			fail(ex.getMessage());
		}
		main.shutdown();
	}
}
