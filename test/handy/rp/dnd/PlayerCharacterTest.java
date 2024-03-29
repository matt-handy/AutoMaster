package handy.rp.dnd;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import handy.rp.OutcomeNotification;
import handy.rp.dnd.CharClass.SPELLCASTING_MODIFIER;
import handy.rp.dnd.SkillCheckInfo.SKILL_CHECK;
import handy.rp.dnd.character.PlayerCharacter;
import handy.rp.dnd.character.PlayerCharacterSaver;
import handy.rp.dnd.character.Proficiency;
import handy.rp.dnd.spells.Spell;
import handy.rp.dnd.spells.Spell.SLOTLEVEL;
import handy.rp.xml.PlayerCharacterParser;

class PlayerCharacterTest {

	@BeforeEach
	void setupDurnt() {
		try {
			Files.copy(Paths.get("player_chars_backup", "durnt_reference.xml"),
					Paths.get("player_chars", "durnt_reference.xml"), StandardCopyOption.REPLACE_EXISTING);
			Files.copy(Paths.get("player_chars_backup", "durnt_life.xml"), Paths.get("player_chars", "durnt_life.xml"),
					StandardCopyOption.REPLACE_EXISTING);
			Files.copy(Paths.get("player_chars_backup", "durnt_lvl1.xml"), Paths.get("player_chars", "durnt_lvl1.xml"),
					StandardCopyOption.REPLACE_EXISTING);
			Files.copy(Paths.get("player_chars_backup", "barbie.xml"), Paths.get("player_chars", "barbie.xml"),
					StandardCopyOption.REPLACE_EXISTING);
			Files.copy(Paths.get("player_chars_backup", "wizzie.xml"), Paths.get("player_chars", "wizzie.xml"), StandardCopyOption.REPLACE_EXISTING);
			Files.copy(Paths.get("player_chars_backup", "lil_wizzie.xml"), Paths.get("player_chars", "lil_wizzie.xml"), StandardCopyOption.REPLACE_EXISTING);
			Files.copy(Paths.get("player_chars_backup", "unprep_lil_wizzie.xml"), Paths.get("player_chars", "unprep_lil_wizzie.xml"), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@AfterEach
	void resetDurnt() {
		try {
			Files.copy(Paths.get("player_chars_backup", "durnt_reference.xml"),
					Paths.get("player_chars", "durnt_reference.xml"), StandardCopyOption.REPLACE_EXISTING);
			Files.copy(Paths.get("player_chars_backup", "durnt_life.xml"), Paths.get("player_chars", "durnt_life.xml"),
					StandardCopyOption.REPLACE_EXISTING);
			Files.copy(Paths.get("player_chars_backup", "barbie.xml"), Paths.get("player_chars", "barbie.xml"),
					StandardCopyOption.REPLACE_EXISTING);
			Files.copy(Paths.get("player_chars_backup", "durnt_lvl1.xml"), Paths.get("player_chars", "durnt_lvl1.xml"),
					StandardCopyOption.REPLACE_EXISTING);
			Files.copy(Paths.get("player_chars_backup", "wizzie.xml"), Paths.get("player_chars", "wizzie.xml"), StandardCopyOption.REPLACE_EXISTING);
			Files.copy(Paths.get("player_chars_backup", "lil_wizzie.xml"), Paths.get("player_chars", "lil_wizzie.xml"), StandardCopyOption.REPLACE_EXISTING);
			Files.copy(Paths.get("player_chars_backup", "unprep_lil_wizzie.xml"), Paths.get("player_chars", "unprep_lil_wizzie.xml"), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	void testEquipmentSaved() {
		//Tests that the equipment class is saved to XML
		try {
			List<PlayerCharacter> characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			PlayerCharacter durnt = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("Durnt-reference")) {
					durnt = pcs;
				}
			}
			assertTrue(durnt != null, "Didn't load Durnt reference");

			assertEquals(21, durnt.getCurrentAC());

			//We're going to take a long rest to force a save-out of the character and a full XML reload.
			//For this reference, both armor and shield are represented in the AC calculation
			//If the AC calculation is the same, then all armor assets were saved and reloaded positively.
			durnt.takeLongRest();

			characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			durnt = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("Durnt-reference")) {
					durnt = pcs;
				}
			}
			assertTrue(durnt != null, "Didn't load Durnt reference");

			assertEquals(21, durnt.getCurrentAC());
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	@Test
	void testLongRest() {
		try {
			List<PlayerCharacter> characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			PlayerCharacter durnt = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("Durnt-reference")) {
					durnt = pcs;
				}
			}
			assertTrue(durnt != null, "Didn't load Durnt reference");

			assertTrue(durnt.listSpellSlotsRemaining().contains("Level 1: 4, Level 2: 3, Level 3: 3, Level 4: 1"));
			durnt.expendSpell("cure_wounds");
			assertTrue(durnt.listSpellSlotsRemaining().contains("Level 1: 3, Level 2: 3, Level 3: 3, Level 4: 1"));
			durnt.hit(10);
			assertEquals(durnt.getCurrentHp(), 41);
			durnt.takeLongRest();
			assertTrue(durnt.listSpellSlotsRemaining().contains("Level 1: 4, Level 2: 3, Level 3: 3, Level 4: 1"));
			assertEquals(durnt.getCurrentHp(), 51);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	void testSpendHitDice() {
		try {
			List<PlayerCharacter> characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			PlayerCharacter durnt = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("Durnt-reference")) {
					durnt = pcs;
				}
			}
			assertTrue(durnt != null, "Didn't load Durnt reference");

			durnt.hit(50);
			assertEquals("D8: 7, ", durnt.printRemainingHitDice());
			assertTrue(durnt.spendHitDice());
			assertTrue(durnt.getCurrentHp() >= 2 && durnt.getCurrentHp() <= 9);
			assertEquals("D8: 6, ", durnt.printRemainingHitDice());
			assertTrue(durnt.spendHitDice());
			assertTrue(durnt.spendHitDice());
			assertTrue(durnt.spendHitDice());
			assertTrue(durnt.spendHitDice());
			assertTrue(durnt.spendHitDice());
			assertTrue(durnt.spendHitDice());
			assertFalse(durnt.spendHitDice());
			durnt.takeLongRest();
			assertEquals("D8: 3, ", durnt.printRemainingHitDice());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	void testSavingThrows() {
		try {
			List<PlayerCharacter> characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			PlayerCharacter durnt = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("Durnt-reference")) {
					durnt = pcs;
				}
			}
			assertTrue(durnt != null, "Didn't load Durnt reference");

			for (int idx = 0; idx < 100; idx++) {
				int strsave = durnt.strSaveThrow();
				assertTrue(strsave >= 3 && strsave <= 22);
			}
			for (int idx = 0; idx < 100; idx++) {
				int save = durnt.dexSaveThrow();
				assertTrue(save >= 2 && save <= 21);
			}
			for (int idx = 0; idx < 100; idx++) {
				int save = durnt.conSaveThrow();
				assertTrue(save >= 3 && save <= 22);
			}
			for (int idx = 0; idx < 100; idx++) {
				int save = durnt.intSaveThrow();
				assertTrue(save >= 0 && save <= 19);
			}
			for (int idx = 0; idx < 100; idx++) {
				int save = durnt.wisSaveThrow();
				assertTrue(save >= 8 && save <= 27);
			}
			for (int idx = 0; idx < 100; idx++) {
				int save = durnt.chaSaveThrow();
				assertTrue(save >= 4 && save <= 23);
			}
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	void testActiveFeatureSaved() {
		try {
			List<PlayerCharacter> characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			PlayerCharacter durnt = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("barbie_the_barbarian")) {
					durnt = pcs;
				}
			}
			assertTrue(durnt != null, "Didn't load Barbie reference");

			durnt.expendFeature(0);

			List<ClassFeature> features = durnt.getActiveFeatures();
			assertEquals(1, features.size());
			assertEquals(features.get(0).featureName, "Rage");

			characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			durnt = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("barbie_the_barbarian")) {
					durnt = pcs;
				}
			}
			assertTrue(durnt != null, "Didn't load Barbie reference");

			features = durnt.getActiveFeatures();
			assertEquals(1, features.size());
			assertEquals(features.get(0).featureName, "Rage");

			durnt.clearFeature(0);
			features = durnt.getActiveFeatures();
			assertEquals(0, features.size());

			characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			durnt = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("barbie_the_barbarian")) {
					durnt = pcs;
				}
			}
			assertTrue(durnt != null, "Didn't load Barbie reference");

			features = durnt.getActiveFeatures();
			assertEquals(0, features.size());
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	void testTestFeatureChargeSaves() {
		try {
			List<PlayerCharacter> characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			PlayerCharacter durnt = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("Durnt-reference")) {
					durnt = pcs;
				}
			}
			assertTrue(durnt != null, "Didn't load Durnt reference");

			ClassFeature feature = durnt.expendFeature(4);
			assertEquals("Blessing of the Forge", feature.featureName);

			try {
				feature = durnt.expendFeature(4);
				fail("There should be an error saying we need charges for the feature");
			} catch (Exception ex) {
				assertEquals("Insufficient charges", ex.getMessage());
			}

			characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			durnt = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("Durnt-reference")) {
					durnt = pcs;
				}
			}
			assertTrue(durnt != null, "Didn't load Durnt reference");

			try {
				feature = durnt.expendFeature(4);
				fail("There should be an error saying we need charges for the feature");
			} catch (Exception ex) {
				assertEquals("Insufficient charges", ex.getMessage());
			}

			durnt.takeLongRest();

			characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			durnt = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("Durnt-reference")) {
					durnt = pcs;
				}
			}
			assertTrue(durnt != null, "Didn't load Durnt reference");

			feature = durnt.expendFeature(4);
			assertEquals("Blessing of the Forge", feature.featureName);

		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	void testDurntSpellSlotSaves() {
		try {
			List<PlayerCharacter> characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			PlayerCharacter durnt = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("Durnt-reference")) {
					durnt = pcs;
				}
			}
			durnt.expendSpell("cure_wounds");
			durnt.notifyNewTurn();
			durnt.expendSpell("spirit_guardians");
			String spellSlotsSaved = durnt.listSpellSlotsRemaining();

			characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			durnt = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("Durnt-reference")) {
					durnt = pcs;
				}
			}
			assertEquals(spellSlotsSaved, durnt.listSpellSlotsRemaining());
			durnt.takeLongRest();
			spellSlotsSaved = durnt.listSpellSlotsRemaining();

			characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			durnt = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("Durnt-reference")) {
					durnt = pcs;
				}
			}

			assertEquals(spellSlotsSaved, durnt.listSpellSlotsRemaining());

		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	void testSaveHitDice() {
		try {
			List<PlayerCharacter> characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			PlayerCharacter durnt = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("Durnt-reference")) {
					durnt = pcs;
				}
			}
			assertTrue(durnt != null, "Didn't load Durnt reference");

			durnt.hit(50);
			assertEquals("D8: 7, ", durnt.printRemainingHitDice());
			assertTrue(durnt.spendHitDice());

			characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			durnt = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("Durnt-reference")) {
					durnt = pcs;
				}
			}
			assertTrue(durnt != null, "Didn't load Durnt reference");

			assertEquals("D8: 6, ", durnt.printRemainingHitDice());
			assertTrue(durnt.spendHitDice());
			assertTrue(durnt.spendHitDice());
			assertTrue(durnt.spendHitDice());
			assertTrue(durnt.spendHitDice());
			assertTrue(durnt.spendHitDice());
			assertTrue(durnt.spendHitDice());
			assertFalse(durnt.spendHitDice());
			durnt.takeLongRest();

			characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			durnt = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("Durnt-reference")) {
					durnt = pcs;
				}
			}
			assertTrue(durnt != null, "Didn't load Durnt reference");

			assertEquals("D8: 3, ", durnt.printRemainingHitDice());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	
	@Test 
	void testGenericFeatureDataSaves(){
		try {
			List<PlayerCharacter> characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			PlayerCharacter durnt = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("wizzie_the_wizard")) {
					durnt = pcs;
				}
			}
			assertTrue(durnt != null, "Didn't load Wizzie reference");

			//This will test that feature data is saved and acted on.
			assertTrue(durnt.hasFeatureToIgnoreSpellCast("shield"));
			assertTrue(durnt.hasFeatureToIgnoreSpellCast("detect_magic"));
			assertTrue(durnt.hasFeatureToIgnoreSpellCast("fireball"));
			assertTrue(durnt.hasFeatureToIgnoreSpellCast("fly"));
			assertFalse(durnt.hasFeatureToIgnoreSpellCast("command"));
			
			//Trigger Save
			durnt.takeLongRest();

			durnt = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("wizzie_the_wizard")) {
					durnt = pcs;
				}
			}
			assertTrue(durnt != null, "Didn't load Wizzie reference");

			assertTrue(durnt.hasFeatureToIgnoreSpellCast("shield"));
			assertTrue(durnt.hasFeatureToIgnoreSpellCast("detect_magic"));
			assertTrue(durnt.hasFeatureToIgnoreSpellCast("fireball"));
			assertTrue(durnt.hasFeatureToIgnoreSpellCast("fly"));
			assertFalse(durnt.hasFeatureToIgnoreSpellCast("command"));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	@Test
	void testLearnsCantripAndNewSpellsOnLevelup() {
		//Validate the UI on level up to learn a cantrip
		//Try selecting a fake cantrip
		//Select correct cantrip
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
		builder.println("int");
		builder.println("int");
		builder.println("fart");
		builder.println("fire_bolt");
		builder.println("light");
		builder.println("fly");
		builder.println("levitate");
		builder.println("quit");
		builder.flush();

		BufferedReader br = new BufferedReader(
				new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
		cmdBuffer.reset();
		bos = new BufferedOutputStream(cmdBuffer);
		builder = new PrintWriter(bos);
		try {
			main.singlePlayerMode(builder, br, "lilwizzie_the_wizard");
		} catch (IOException e) {
			fail(e.getMessage());
		}

		try {
			br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
			assertEquals(br.readLine(), "0: Evocation Wizard at level 3");
			assertEquals(br.readLine(), "Select 1 for a new multiclass option");
			assertEquals(br.readLine(), "Select first ability score area (str, dex, con, int, wis, cha): ");
			assertEquals(br.readLine(), "Select second ability score area (str, dex, con, int, wis, cha): ");
			assertEquals(br.readLine(), "You learned a new cantrip! Enter the name below (format: toll_the_dead)");
			assertEquals(br.readLine(), "Error, try again: ");
			assertEquals(br.readLine(), "Spell not found");
			assertEquals(br.readLine(), "Error, try again: ");
			assertEquals(br.readLine(), "Spell already known");
			assertEquals(br.readLine(), "Spell learned: Light");
			assertEquals(br.readLine(), "You learned a new spell! Enter the name below (format: fireball)");
			assertEquals(br.readLine(), "Spell learned: Fly");
			assertEquals(br.readLine(), "You learned a new spell! Enter the name below (format: fireball)");
			assertEquals(br.readLine(), "Spell learned: Levitate");
		} catch (IOException ex) {
			fail(ex.getMessage());
		}
		main.shutdown();
	}
	
	@Test
	void testClassResourceSave() {
		try {
			List<PlayerCharacter> characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			PlayerCharacter durnt = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("barbie_the_barbarian")) {
					durnt = pcs;
				}
			}
			assertTrue(durnt != null, "Didn't load Barbie reference");

			durnt.expendFeature(0);

			List<ClassFeature> features = durnt.getActiveFeatures();
			assertEquals(1, features.size());
			assertEquals(features.get(0).featureName, "Rage");
			String preSaveResources = durnt.printResourceCounters();

			characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			durnt = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("barbie_the_barbarian")) {
					durnt = pcs;
				}
			}
			assertTrue(durnt != null, "Didn't load Barbie reference");

			assertEquals(preSaveResources, durnt.printResourceCounters());

			durnt.takeLongRest();

			preSaveResources = durnt.printResourceCounters();

			characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			durnt = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("barbie_the_barbarian")) {
					durnt = pcs;
				}
			}
			assertTrue(durnt != null, "Didn't load Barbie reference");

			assertEquals(preSaveResources, durnt.printResourceCounters());
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	void testHPSaves() {
		try {
			List<PlayerCharacter> characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			PlayerCharacter durnt = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("Durnt-reference")) {
					durnt = pcs;
				}
			}
			assertTrue(durnt != null, "Didn't load Durnt reference");
			assertEquals(durnt.maxHP, 51);
			assertEquals(durnt.currentHp, 51);

			durnt.hit(10);
			assertEquals(durnt.maxHP, 51);
			assertEquals(durnt.currentHp, 41);

			characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			durnt = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("Durnt-reference")) {
					durnt = pcs;
				}
			}
			assertTrue(durnt != null, "Didn't load Durnt reference");

			assertEquals(durnt.maxHP, 51);
			assertEquals(durnt.currentHp, 41);
			durnt.heal(4);
			assertEquals(durnt.maxHP, 51);
			assertEquals(durnt.currentHp, 45);

			characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			durnt = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("Durnt-reference")) {
					durnt = pcs;
				}
			}
			assertTrue(durnt != null, "Didn't load Durnt reference");
			assertEquals(durnt.maxHP, 51);
			assertEquals(durnt.currentHp, 45);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	void testCharacterAtWillSave() {
		try {
			List<PlayerCharacter> characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			PlayerCharacter durnt = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("Durnt-reference")) {
					durnt = pcs;
				}
			}
			assertTrue(durnt != null, "Didn't load Durnt reference");
			Path copyDurnt = Paths.get("player_chars", "durnt-copy.xml");
			PlayerCharacterSaver.saveCharacter(durnt, copyDurnt);

			PlayerCharacter durntCopy = PlayerCharacterParser.load(copyDurnt.toString());
			assertEquals(durnt.personalName, durntCopy.personalName);
			assertEquals(durnt.maxHP, durntCopy.maxHP);
			assertEquals(durnt.currentHp, durntCopy.currentHp);

			assertEquals(durnt.str, durntCopy.str);
			assertEquals(durnt.dex, durntCopy.dex);
			assertEquals(durnt.con, durntCopy.con);
			assertEquals(durnt.inte, durntCopy.inte);
			assertEquals(durnt.wis, durntCopy.wis);
			assertEquals(durnt.cha, durntCopy.cha);

			assertEquals(durnt.getCharacterLevel(), durnt.getCharacterLevel());

			assertEquals(durnt.listSpells(), durntCopy.listSpells());

			Files.delete(copyDurnt);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	void testDurntBasicsAndSpells() {
		try {
			List<PlayerCharacter> characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			PlayerCharacter durnt = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("Durnt-reference")) {
					durnt = pcs;
				}
			}
			assertTrue(durnt != null, "Didn't load Durnt reference");

			assertEquals(durnt.maxHP, 51);
			assertEquals(durnt.currentHp, 51);
			assertEquals(durnt.str, 14);
			assertEquals(durnt.dex, 12);
			assertEquals(durnt.con, 15);
			assertEquals(durnt.inte, 8);
			assertEquals(durnt.wis, 18);
			assertEquals(durnt.cha, 10);

			assertEquals(durnt.getCharacterLevel(), 7);
			assertEquals(durnt.getProficiencyBonus(), 3);

			assertEquals(durnt.getSpellcastingModifier(), SPELLCASTING_MODIFIER.WISDOM);
			assertEquals(durnt.getSpellSaveDC(), 15);
			assertEquals(durnt.getSpellToHit(), 7);

			assertTrue(durnt.listSpellSlotsRemaining().contains("Level 1: 4, Level 2: 3, Level 3: 3, Level 4: 1"));
			durnt.expendSpell("cure_wounds");
			assertTrue(durnt.listSpellSlotsRemaining().contains("Level 1: 3, Level 2: 3, Level 3: 3, Level 4: 1"));
			try {
				durnt.expendSpell("cure_wounds");
			} catch (IllegalArgumentException ex) {
				assertEquals(ex.getMessage(),
						"Can only cast cantrip after casting a spell on the same turn, and only if prior spell was a bonus action spell");
			}
			durnt.notifyNewTurn();
			durnt.expendSpell("cure_wounds");
			assertTrue(durnt.listSpellSlotsRemaining().contains("Level 1: 2, Level 2: 3, Level 3: 3, Level 4: 1"));

			String spells = durnt.listSpells();
			String spellsArr[] = spells.split(System.lineSeparator());
			assertEquals(spellsArr.length, 5);
			assertTrue(spells.contains(
					"Level: 1 Identify, Searing Smite, Bane, Cure Wounds, Shield of Faith, Detect Good and Evil, Guiding Bolt, "));
			assertTrue(spells.contains("Level: 4 Guardian of Faith, Death Ward, "));
			assertTrue(spells.contains("Level: 2 Heat Metal, Magic Weapon, Hold Person, Zone of Truth, "));
			assertTrue(spells.contains("Level: 3 Spirit Guardians, Magic Circle, Protection From Energy, "));
			assertTrue(spells.contains("Level: Cantrip Spare the Dying, Sacred Flame, Thaumaturgy, Toll the Dead,"));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	void testPlusTwoWeaponWarhammer() {
		try {
			List<PlayerCharacter> characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			PlayerCharacter durnt = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("Durnt-life-cleric")) {
					durnt = pcs;
				}
			}
			assertTrue(durnt != null, "Didn't load Durnt reference");
			for (int i = 0; i < 100; i++) {
				OutcomeNotification failedAttack = durnt.attack("warhammer", true, false);
				assertEquals("Warhammer cannot be thrown", failedAttack.humanMessage);
				assertFalse(failedAttack.outcome);
				OutcomeNotification successAttack = durnt.attack("warhammer", false, false);
				assertTrue(successAttack.outcome);
				String hitStartString = "Durnt-life-cleric strikes with Warhammer with a to hit of ";
				assertTrue(successAttack.humanMessage.startsWith(hitStartString));
				String[] remainingElements = successAttack.humanMessage.substring(hitStartString.length()).split(" ");
				int toHit = Integer.parseInt(remainingElements[0]);
				assertTrue(toHit <= 27 & toHit >= 8);
				toHit = Integer.parseInt(remainingElements[3]);
				assertTrue(toHit <= 27 & toHit >= 8);
				int oneHandDamage = Integer.parseInt(remainingElements[6]);
				assertTrue(oneHandDamage <= 12 & oneHandDamage >= 5);
				int twoHandDamage = Integer.parseInt(remainingElements[18]);
				assertTrue(twoHandDamage <= 14 & twoHandDamage >= 5);
				assertTrue(successAttack.humanMessage.contains(" Bludgeoning damage if used one-handed, or for "));
				assertTrue(successAttack.humanMessage.contains(" if used two-handed. Divine Strike hits for an extra "));
				String failedSecondAttack = durnt.attack("warhammer", false, false).humanMessage;
				assertEquals("No attacks remaining this turn", failedSecondAttack);
				durnt.notifyNewTurn();
			}
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	void testWarhammer() {
		try {
			List<PlayerCharacter> characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			PlayerCharacter durnt = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("Durnt-reference")) {
					durnt = pcs;
				}
			}
			assertTrue(durnt != null, "Didn't load Durnt reference");
			for (int i = 0; i < 100; i++) {
				OutcomeNotification failedAttack = durnt.attack("warhammer", true, false);
				assertEquals("Warhammer cannot be thrown", failedAttack.humanMessage);
				assertFalse(failedAttack.outcome);
				String successAttack = durnt.attack("warhammer", false, false).humanMessage;
				String hitStartString = "Durnt-reference strikes with Warhammer with a to hit of ";
				assertTrue(successAttack.startsWith(hitStartString));
				String[] remainingElements = successAttack.substring(hitStartString.length()).split(" ");
				int toHit = Integer.parseInt(remainingElements[0]);
				assertTrue(toHit <= 25 & toHit >= 6);
				toHit = Integer.parseInt(remainingElements[3]);
				assertTrue(toHit <= 25 & toHit >= 6);
				int oneHandDamage = Integer.parseInt(remainingElements[6]);
				assertTrue(oneHandDamage <= 10 & oneHandDamage >= 3);
				int twoHandDamage = Integer.parseInt(remainingElements[18]);
				assertTrue(twoHandDamage <= 12 & twoHandDamage >= 3);
				assertTrue(successAttack.contains(" Bludgeoning damage if used one-handed, or for "));
				assertTrue(successAttack.endsWith(" if used two-handed."));
				String failedSecondAttack = durnt.attack("warhammer", false, false).humanMessage;
				assertEquals("No attacks remaining this turn", failedSecondAttack);
				durnt.notifyNewTurn();
			}
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	void testWarhammerTempPlusOne() {
		try {
			List<PlayerCharacter> characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			PlayerCharacter durnt = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("Durnt-reference")) {
					durnt = pcs;
				}
			}
			assertTrue(durnt != null, "Didn't load Durnt reference");
			for (int i = 0; i < 100; i++) {
				String failedAttack = durnt.attack("warhammer", true, false).humanMessage;
				assertEquals("Warhammer cannot be thrown", failedAttack);
				String successAttack = durnt.attack("warhammer", false, false).humanMessage;
				String hitStartString = "Durnt-reference strikes with Warhammer with a to hit of ";
				assertTrue(successAttack.startsWith(hitStartString));
				String[] remainingElements = successAttack.substring(hitStartString.length()).split(" ");
				int toHit = Integer.parseInt(remainingElements[0]);
				assertTrue(toHit <= 25 & toHit >= 6);
				toHit = Integer.parseInt(remainingElements[3]);
				assertTrue(toHit <= 25 & toHit >= 6);
				int oneHandDamage = Integer.parseInt(remainingElements[6]);
				assertTrue(oneHandDamage <= 10 & oneHandDamage >= 3);
				int twoHandDamage = Integer.parseInt(remainingElements[18]);
				assertTrue(twoHandDamage <= 12 & twoHandDamage >= 3);
				assertTrue(successAttack.contains(" Bludgeoning damage if used one-handed, or for "));
				assertTrue(successAttack.endsWith(" if used two-handed."));
				String failedSecondAttack = durnt.attack("warhammer", false, false).humanMessage;
				assertEquals("No attacks remaining this turn", failedSecondAttack);
				durnt.notifyNewTurn();
			}
			durnt.makeTempPlusWeapon("warhammer", 2);
			for (int i = 0; i < 100; i++) {
				String failedAttack = durnt.attack("warhammer", true, false).humanMessage;
				assertEquals("Warhammer cannot be thrown", failedAttack);
				String successAttack = durnt.attack("warhammer", false, false).humanMessage;
				String hitStartString = "Durnt-reference strikes with Warhammer with a to hit of ";
				assertTrue(successAttack.startsWith(hitStartString));
				String[] remainingElements = successAttack.substring(hitStartString.length()).split(" ");
				int toHit = Integer.parseInt(remainingElements[0]);
				assertTrue(toHit <= 27 & toHit >= 8);
				toHit = Integer.parseInt(remainingElements[3]);
				assertTrue(toHit <= 27 & toHit >= 8);
				int oneHandDamage = Integer.parseInt(remainingElements[6]);
				assertTrue(oneHandDamage <= 12 & oneHandDamage >= 5);
				int twoHandDamage = Integer.parseInt(remainingElements[18]);
				assertTrue(twoHandDamage <= 14 & twoHandDamage >= 5);
				assertTrue(successAttack.contains(" Bludgeoning damage if used one-handed, or for "));
				assertTrue(successAttack.endsWith(" if used two-handed."));
				String failedSecondAttack = durnt.attack("warhammer", false, false).humanMessage;
				assertEquals("No attacks remaining this turn", failedSecondAttack);
				durnt.notifyNewTurn();
			}
			durnt.resetTempPlusWeapon("warhammer");
			for (int i = 0; i < 100; i++) {
				String failedAttack = durnt.attack("warhammer", true, false).humanMessage;
				assertEquals("Warhammer cannot be thrown", failedAttack);
				String successAttack = durnt.attack("warhammer", false, false).humanMessage;
				String hitStartString = "Durnt-reference strikes with Warhammer with a to hit of ";
				assertTrue(successAttack.startsWith(hitStartString));
				String[] remainingElements = successAttack.substring(hitStartString.length()).split(" ");
				int toHit = Integer.parseInt(remainingElements[0]);
				assertTrue(toHit <= 25 & toHit >= 6);
				toHit = Integer.parseInt(remainingElements[3]);
				assertTrue(toHit <= 25 & toHit >= 6);
				int oneHandDamage = Integer.parseInt(remainingElements[6]);
				assertTrue(oneHandDamage <= 10 & oneHandDamage >= 3);
				int twoHandDamage = Integer.parseInt(remainingElements[18]);
				assertTrue(twoHandDamage <= 12 & twoHandDamage >= 3);
				assertTrue(successAttack.contains(" Bludgeoning damage if used one-handed, or for "));
				assertTrue(successAttack.endsWith(" if used two-handed."));
				String failedSecondAttack = durnt.attack("warhammer", false, false).humanMessage;
				assertEquals("No attacks remaining this turn", failedSecondAttack);
				durnt.notifyNewTurn();
			}
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	void testDagger() {
		try {
			List<PlayerCharacter> characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			PlayerCharacter durnt = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("Durnt-reference")) {
					durnt = pcs;
				}
			}
			assertTrue(durnt != null, "Didn't load Durnt reference");
			for (int i = 0; i < 100; i++) {
				String successAttack = durnt.attack("dagger", false, false).humanMessage;
				String hitStartString = "Durnt-reference strikes with Dagger with a to hit of ";
				assertTrue(successAttack.startsWith(hitStartString));
				String[] remainingElements = successAttack.substring(hitStartString.length()).split(" ");
				int toHit = Integer.parseInt(remainingElements[0]);
				assertTrue(toHit <= 25 & toHit >= 6);
				toHit = Integer.parseInt(remainingElements[3]);
				assertTrue(toHit <= 25 & toHit >= 6);
				int oneHandDamage = Integer.parseInt(remainingElements[6]);
				assertTrue(oneHandDamage <= 6 & oneHandDamage >= 3);
				assertTrue(successAttack.endsWith(" Piercing damage."));
				String failedSecondAttack = durnt.attack("dagger", false, false).humanMessage;
				assertEquals("No attacks remaining this turn", failedSecondAttack);
				durnt.notifyNewTurn();
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	void testThrowDagger() {
		try {
			List<PlayerCharacter> characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			PlayerCharacter durnt = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("Durnt-reference")) {
					durnt = pcs;
				}
			}
			assertTrue(durnt != null, "Didn't load Durnt reference");
			for (int i = 0; i < 100; i++) {
				String successAttack = durnt.attack("dagger", true, false).humanMessage;
				String hitStartString = "Durnt-reference throws Dagger at range(20/60) with a to hit of ";
				assertTrue(successAttack.startsWith(hitStartString));
				String[] remainingElements = successAttack.substring(hitStartString.length()).split(" ");
				int toHit = Integer.parseInt(remainingElements[0]);
				assertTrue(toHit <= 25 & toHit >= 6);
				int oneHandDamage = Integer.parseInt(remainingElements[2]);
				assertTrue(oneHandDamage <= 6 & oneHandDamage >= 3);
				assertTrue(successAttack.endsWith(" Piercing damage."));
				String failedSecondAttack = durnt.attack("dagger", false, false).humanMessage;
				assertEquals("No attacks remaining this turn", failedSecondAttack);
				durnt.notifyNewTurn();
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	void testArmorAndToolProficiencies() {
		try {
			List<PlayerCharacter> characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			PlayerCharacter durnt = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("Durnt-reference")) {
					durnt = pcs;
				}
			}
			assertTrue(durnt != null, "Didn't load Durnt reference");
			
			assertEquals(1, durnt.getToolProficiencies().size());
			assertEquals("smith", durnt.getToolProficiencies().iterator().next().name);
			
			assertEquals(4, durnt.getArmorProficiencies().size());
			boolean foundLight = false;
			boolean foundMedium = false;
			boolean foundHeavy = false;
			boolean foundShield = false;
			
			for(Proficiency prof : durnt.getArmorProficiencies()) {
				if(prof.name.equals("light")) {
					foundLight = true;
				}else if(prof.name.equals("medium")) {
					foundMedium = true;
				}else if(prof.name.equals("heavy")) {
					foundHeavy = true;
				}else if(prof.name.equals("shield")) {
					foundShield = true;
				} 
			}
			
			assertTrue(foundLight);
			assertTrue(foundMedium);
			assertTrue(foundHeavy);
			assertTrue(foundShield);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	@Test
	void testClericSpellPreparationLimitCalculation() {
		try {
			List<PlayerCharacter> characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			PlayerCharacter durnt = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("Durnt-reference")) {
					durnt = pcs;
				}
			}
			assertTrue(durnt != null, "Didn't load Durnt reference");
			assertEquals(11, durnt.maxSpellsToPrepare());
		}catch(Exception ex) {
			fail(ex.getMessage());
		}
	}
	
	@Test
	void testClericAddPreparedSpell() {
		try {
			List<PlayerCharacter> characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			PlayerCharacter durnt = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("Durnt-life-cleric")) {
					durnt = pcs;
				}
			}
			assertTrue(durnt != null, "Didn't load Durnt reference");
			
			assertEquals(9, durnt.getCharacterLevel());
			assertEquals(18, durnt.getWis());
			assertEquals(13, durnt.maxSpellsToPrepare());
			assertEquals(12, durnt.currentSpellsPrepared());
			
			OutcomeNotification outcome = durnt.addPreparedSpell("magic_circle");
			assertEquals("Spell prepared: Magic Circle", outcome.humanMessage);
			assertTrue(outcome.outcome);
			
			outcome = durnt.addPreparedSpell("fireball");
			assertFalse(outcome.outcome);
			assertEquals("Unknown spell: fireball", outcome.humanMessage);
			
			outcome = durnt.addPreparedSpell("spirit_guardians");
			assertFalse(outcome.outcome);
			assertEquals("Player already has maximum spell number prepared", outcome.humanMessage);
		}catch(Exception ex) {
			fail(ex.getMessage());
		}
	}
	
	@Test
	void testClericSwapPreparedSpell() {
		try {
			List<PlayerCharacter> characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			PlayerCharacter durnt = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("Durnt-life-cleric")) {
					durnt = pcs;
				}
			}
			assertTrue(durnt != null, "Didn't load Durnt reference");
			
			OutcomeNotification outcome = durnt.swapPreparedSpell("cure_wounds", "magic_circle");
			assertEquals("Spell prepared: Magic Circle", outcome.humanMessage);
			assertTrue(outcome.outcome);
			
			outcome = durnt.swapPreparedSpell("magic_circle", "fireball");
			assertEquals("Unknown spell: fireball", outcome.humanMessage);
			assertFalse(outcome.outcome);
		}catch(Exception ex) {
			fail(ex.getMessage());
		}
	}

	@Test
	void testCrossbow() {
		try {
			List<PlayerCharacter> characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			PlayerCharacter durnt = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("Durnt-reference")) {
					durnt = pcs;
				}
			}
			assertTrue(durnt != null, "Didn't load Durnt reference");
			for (int i = 0; i < 100; i++) {
				String successAttack = durnt.attack("light_crossbow", false, false).humanMessage;
				String hitStartString = "Durnt-reference uses Light Crossbow to hit at range(80/320) with a to hit of ";
				assertTrue(successAttack.startsWith(hitStartString));
				String[] remainingElements = successAttack.substring(hitStartString.length()).split(" ");
				int toHit = Integer.parseInt(remainingElements[0]);
				assertTrue(toHit <= 24 & toHit >= 5);
				toHit = Integer.parseInt(remainingElements[3]);
				assertTrue(toHit <= 24 & toHit >= 5);
				int oneHandDamage = Integer.parseInt(remainingElements[6]);
				assertTrue(oneHandDamage <= 9 & oneHandDamage >= 2);
				assertTrue(successAttack.endsWith(" Piercing damage."));
				String failedSecondAttack = durnt.attack("light_crossbow", false, false).humanMessage;
				assertEquals("No attacks remaining this turn", failedSecondAttack);
				durnt.notifyNewTurn();
			}
		} catch (Exception e) {
			e.printStackTrace();
			//System.out.println("-" + MonsterParser.readFile(file) + "-");
			fail(e.getMessage());
		}
	}

	@Test
	void testNonExistantWeapon() {
		try {
			List<PlayerCharacter> characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			PlayerCharacter durnt = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("Durnt-reference")) {
					durnt = pcs;
				}
			}
			assertTrue(durnt != null, "Didn't load Durnt reference");

			String failAttack = durnt.attack("fakeweapon", true, false).humanMessage;
			assertEquals("Character does not have this weapon available", failAttack);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	void testSkillChecks() {
		try {
			List<PlayerCharacter> characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			PlayerCharacter durnt = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("Durnt-reference")) {
					durnt = pcs;
				}
			}
			assertTrue(durnt != null, "Didn't load Durnt reference");
			for (int idx = 0; idx < 100; idx++) {
				int val = durnt.rollSkillCheck(SKILL_CHECK.Persuasion);
				assertTrue(val >= 4 && val <= 23);

				val = durnt.rollSkillCheck(SKILL_CHECK.Acrobatics);
				assertTrue(val >= 5 && val <= 24);

				val = durnt.rollSkillCheck(SKILL_CHECK.Intimidation);
				assertTrue(val >= 1 && val <= 20);
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	void testHeals() {
		try {
			List<PlayerCharacter> characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			PlayerCharacter durnt = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("Durnt-reference")) {
					durnt = pcs;
				}
			}
			assertTrue(durnt != null, "Didn't load Durnt reference");
			assertEquals(durnt.maxHP, 51);
			assertEquals(durnt.currentHp, 51);

			durnt.hit(10);
			assertEquals(durnt.maxHP, 51);
			assertEquals(durnt.currentHp, 41);

			durnt.heal(4);
			assertEquals(durnt.maxHP, 51);
			assertEquals(durnt.currentHp, 45);

			durnt.hit(3);
			assertEquals(durnt.maxHP, 51);
			assertEquals(durnt.currentHp, 42);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	void testListFeaturesAndSelectOption() {
		try {
			List<PlayerCharacter> characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			PlayerCharacter durnt = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("Durnt-reference")) {
					durnt = pcs;
				}
			}
			assertTrue(durnt != null, "Didn't load Durnt reference");

			String features = durnt.printFeatures();
			String featureElems[] = features.split(System.lineSeparator());
			assertEquals("0 : Channel Divinity - Turn Undead", featureElems[0]);
			assertEquals("1 : Bonus Proficiencies", featureElems[1]);
			assertEquals("2 : Channel Divinity - Artisan's Blessing", featureElems[2]);
			assertEquals("3 : Soul of the Forge", featureElems[3]);
			assertEquals("4 : Blessing of the Forge", featureElems[4]);

			ClassFeature feature = durnt.expendFeature(3);
			assertEquals("Soul of the Forge", feature.featureName);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	void testFeatureClassResourceExpend() {
		try {
			List<PlayerCharacter> characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			PlayerCharacter durnt = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("Durnt-reference")) {
					durnt = pcs;
				}
			}
			assertTrue(durnt != null, "Didn't load Durnt reference");

			String features = durnt.printFeatures();
			String featureElems[] = features.split(System.lineSeparator());
			assertEquals("0 : Channel Divinity - Turn Undead", featureElems[0]);
			assertEquals("1 : Bonus Proficiencies", featureElems[1]);
			assertEquals("2 : Channel Divinity - Artisan's Blessing", featureElems[2]);
			assertEquals("3 : Soul of the Forge", featureElems[3]);
			assertEquals("4 : Blessing of the Forge", featureElems[4]);

			String pointsAvailable = durnt.printResourceCounters();
			assertEquals("Channel Divinity 2 available out of 2", pointsAvailable);

			ClassFeature feature = durnt.expendFeature(0);
			assertEquals("Channel Divinity - Turn Undead", feature.featureName);

			feature = durnt.expendFeature(2);
			assertEquals("Channel Divinity - Artisan's Blessing", feature.featureName);

			pointsAvailable = durnt.printResourceCounters();
			assertEquals("Channel Divinity 0 available out of 2", pointsAvailable);

			try {
				feature = durnt.expendFeature(2);
				fail("There should be an error saying we need charges for the feature");
			} catch (Exception ex) {
				assertEquals("Insufficient charges for class: Cleric", ex.getMessage());
			}

			durnt.takeLongRest();
			feature = durnt.expendFeature(2);
			assertEquals("Channel Divinity - Artisan's Blessing", feature.featureName);

		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	void testFeatureChargesExpend() {
		try {
			List<PlayerCharacter> characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			PlayerCharacter durnt = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("Durnt-reference")) {
					durnt = pcs;
				}
			}
			assertTrue(durnt != null, "Didn't load Durnt reference");

			String features = durnt.printFeatures();
			String featureElems[] = features.split(System.lineSeparator());
			assertEquals("0 : Channel Divinity - Turn Undead", featureElems[0]);
			assertEquals("1 : Bonus Proficiencies", featureElems[1]);
			assertEquals("2 : Channel Divinity - Artisan's Blessing", featureElems[2]);
			assertEquals("3 : Soul of the Forge", featureElems[3]);
			assertEquals("4 : Blessing of the Forge", featureElems[4]);

			ClassFeature feature = durnt.expendFeature(4);
			assertEquals("Blessing of the Forge", feature.featureName);

			try {
				feature = durnt.expendFeature(4);
				fail("There should be an error saying we need charges for the feature");
			} catch (Exception ex) {
				assertEquals("Insufficient charges", ex.getMessage());
			}

			durnt.takeLongRest();
			feature = durnt.expendFeature(4);
			assertEquals("Blessing of the Forge", feature.featureName);

		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	void testFeatureExtraMeleeDamage() {
		try {
			List<PlayerCharacter> characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			PlayerCharacter durnt = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("Durnt-life-cleric")) {
					durnt = pcs;
				}
			}
			assertTrue(durnt != null, "Didn't load Durnt reference");

			assertEquals(durnt.getFeatureMeleeBonus().size(), 1);
			assertEquals(durnt.getFeatureMeleeBonus().get(0).featureName, "Divine Strike");

			String successAttack = durnt.attack("warhammer", false, false).humanMessage;
			String hitStartString = "Durnt-life-cleric strikes with Warhammer with a to hit of ";
			assertTrue(successAttack.startsWith(hitStartString));
			String[] remainingElements = successAttack.substring(hitStartString.length()).split(" ");
			int toHit = Integer.parseInt(remainingElements[0]);
			assertTrue(toHit <= 27 & toHit >= 8);
			toHit = Integer.parseInt(remainingElements[3]);
			assertTrue(toHit <= 27 & toHit >= 8);
			int oneHandDamage = Integer.parseInt(remainingElements[6]);
			assertTrue(oneHandDamage <= 12 & oneHandDamage >= 5);
			int twoHandDamage = Integer.parseInt(remainingElements[18]);
			assertTrue(twoHandDamage <= 14 & twoHandDamage >= 5);
			assertTrue(successAttack.contains(" Bludgeoning damage if used one-handed, or for "));
			assertTrue(successAttack.contains(" if used two-handed. Divine Strike hits for an extra "));
			assertTrue(successAttack.endsWith(" Radiant damage"));
			int radDamage = Integer.parseInt(remainingElements[32]);
			assertTrue(radDamage <= 8 & radDamage >= 1);
		} catch (Exception ex) {
			ex.printStackTrace();
			fail();
		}
	}

	@Test
	void testDurntNoExtraMeleeDamage() {
		try {
			List<PlayerCharacter> characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			PlayerCharacter durnt = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("Durnt-reference")) {
					durnt = pcs;
				}
			}
			assertTrue(durnt != null, "Didn't load Durnt reference");

			assertEquals(durnt.getFeatureMeleeBonus().size(), 0);

		} catch (Exception ex) {
			fail();
		}
	}

	@Test
	void testFeatureExtraHealing() {
		try {
			List<PlayerCharacter> characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			PlayerCharacter durnt = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("Durnt-life-cleric")) {
					durnt = pcs;
				}
			}
			assertTrue(durnt != null, "Didn't load Durnt reference");
			Spell spell = durnt.expendSpell("cure_wounds");
			String effect = spell.cast(SLOTLEVEL.ONE, durnt.getCasterLevel(), durnt.getSpellSaveDC(),
					durnt.getSpellToHit(), durnt.getSpellcastingModifierValue(), durnt);
			String effects[] = effect.split("\n");
			assertEquals(5, effects.length);
			assertTrue(effects[4]
					.endsWith(" Class feature, extra heals: 3 Class feature, heals self: 3 (applied automatically)"));
		} catch (Exception ex) {
			ex.printStackTrace();
			fail();
		}
	}

	@Test
	void testSubclassSpellAddition() {
		try {
			List<PlayerCharacter> characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			PlayerCharacter durnt = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("Durnt-life-cleric")) {
					durnt = pcs;
				}
			}
			assertTrue(durnt != null, "Didn't load Durnt reference");
			String spells = durnt.listSpells();
			String spellsArr[] = spells.split(System.lineSeparator());
			assertEquals(spellsArr.length, 6);
			assertTrue(spells.contains(
					"Level: 1 Identify, Searing Smite, Bane, Shield of Faith, Detect Good and Evil, Guiding Bolt, Guiding Bolt, Bless, Cure Wounds, "));
			assertTrue(spells.contains("Level: 4 Death Ward, Guardian of Faith"));
			assertTrue(spells.contains(
					"Level: 2 Heat Metal, Magic Weapon, Hold Person, Zone of Truth, Lesser Restoration, Spiritual Weapon, "));
			assertTrue(spells.contains(
					"Level: 3 Spirit Guardians, Beacon of Hope, Revivify, "));
			assertTrue(spells.contains("Level: Cantrip Spare the Dying, Sacred Flame, Thaumaturgy, Toll the Dead, "));
			assertTrue(spells.contains("Level: 5 Mass Cure Wounds, Raise Dead, "));
		} catch (Exception ex) {
			fail();
		}
	}
}
