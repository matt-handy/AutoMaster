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
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map.Entry;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import handy.rp.dnd.character.LevelUpWizard;
import handy.rp.dnd.character.PlayerCharacter;
import handy.rp.xml.PlayerCharacterParser;

class CharacterLevelUpTest {

	@BeforeEach
	void setupDurnt() {
		try {
			Files.copy(Paths.get("player_chars_backup", "durnt_reference.xml"),
					Paths.get("player_chars", "durnt_reference.xml"), StandardCopyOption.REPLACE_EXISTING);
			Files.copy(Paths.get("player_chars_backup", "durnt_lvl1.xml"),
					Paths.get("player_chars", "durnt_lvl1.xml"), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@AfterEach
	void resetDurnt() {
		try {
			Files.copy(Paths.get("player_chars_backup", "durnt_reference.xml"),
					Paths.get("player_chars", "durnt_reference.xml"), StandardCopyOption.REPLACE_EXISTING);
			Files.copy(Paths.get("player_chars_backup", "durnt_lvl1.xml"),
					Paths.get("player_chars", "durnt_lvl1.xml"), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	void testAutomaticAbilityScoreIncreaseErrorHandling() {
		// Tests HP increase & ASI at lvl 7->8
		ByteArrayOutputStream cmdBuffer = new ByteArrayOutputStream();
		BufferedOutputStream bos = new BufferedOutputStream(cmdBuffer);
		PrintWriter builder = new PrintWriter(bos);
		builder.println("0");
		builder.println("barf");
		builder.println("WIS");
		builder.println("barf");
		builder.println("WIS");
		builder.println("WIS");
		builder.flush();

		BufferedReader br = new BufferedReader(
				new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
		cmdBuffer.reset();
		bos = new BufferedOutputStream(cmdBuffer);
		builder = new PrintWriter(bos);
		PlayerCharacter pc = null;
		try {
			List<PlayerCharacter> characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("Durnt-reference")) {
					pc = pcs;
				}
			}
		} catch (Exception ex) {
			fail("Unable to load characters");
		}

		try {
			LevelUpWizard.levelUpCharacter(pc, builder, br);
			br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
			assertEquals(br.readLine(), "0: Forge Domain Cleric at level 7");
			assertEquals(br.readLine(), "Select 1 for a new multiclass option");

			assertEquals(br.readLine(), "Select first ability score area (str, dex, con, int, wis, cha): ");
			assertEquals(br.readLine(), "That wasn't a valid ability score, try again");
			assertEquals(br.readLine(), "Select first ability score area (str, dex, con, int, wis, cha): ");
			assertEquals(br.readLine(), "Select second ability score area (str, dex, con, int, wis, cha): ");
			assertEquals(br.readLine(), "That wasn't a valid ability score, try again");
			assertEquals(br.readLine(), "Select first ability score area (str, dex, con, int, wis, cha): ");
			assertEquals(br.readLine(), "Select second ability score area (str, dex, con, int, wis, cha): ");
			testDurntLevelUpBaseline(pc);

		} catch (IOException ex) {
			fail(ex.getMessage());
		}
	}

	@Test
	void testLevelUpAddNewMulticlass() {
		// Tests HP increase & ASI at lvl 7->8
		ByteArrayOutputStream cmdBuffer = new ByteArrayOutputStream();
		BufferedOutputStream bos = new BufferedOutputStream(cmdBuffer);
		PrintWriter builder = new PrintWriter(bos);
		builder.println("1");
		builder.println("0");
		builder.println("wis");
		builder.println("WIS");
		builder.flush();

		BufferedReader br = new BufferedReader(
				new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
		cmdBuffer.reset();
		bos = new BufferedOutputStream(cmdBuffer);
		builder = new PrintWriter(bos);
		PlayerCharacter pc = null;
		try {
			List<PlayerCharacter> characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("Durnt-reference")) {
					pc = pcs;
				}
			}
		} catch (Exception ex) {
			fail("Unable to load characters");
		}

		try {
			LevelUpWizard.levelUpCharacter(pc, builder, br);
			br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
			assertEquals(br.readLine(), "0: Forge Domain Cleric at level 7");
			assertEquals(br.readLine(), "Select 1 for a new multiclass option");

			assertEquals(br.readLine(), "Select your new base class: ");
			assertEquals(br.readLine(), "0: Barbarian");
			assertEquals(br.readLine(), "1: Cleric");

			assertEquals(2, pc.getClassInfo().size());
			boolean foundCleric = false;
			boolean foundBarbarian = false;
			for (Entry<CharClass, Integer> entry : pc.getClassInfo().entrySet()) {
				if (entry.getKey().name.equals("Forge Domain Cleric")) {
					assertEquals(7, entry.getValue());
					foundCleric = true;
				} else if (entry.getKey().name.equals("Barbarian")) {
					assertEquals(1, entry.getValue());
					foundBarbarian = true;
				}
			}
			assertTrue(foundCleric);
			assertTrue(foundBarbarian);

			assertTrue(pc.getMaxHp() >= 51 + 1 + Helpers.getModifierFromAbility(pc.getCon()));
			assertTrue(pc.getMaxHp() <= 51 + 12 + Helpers.getModifierFromAbility(pc.getCon()));

			assertEquals(14, pc.getStr());
			assertEquals(12, pc.getDex());
			assertEquals(15, pc.getCon());
			assertEquals(8, pc.getInte());
			assertEquals(18, pc.getWis());
			assertEquals(10, pc.getCha());

			pc.takeLongRest();
			// Test new spell slot levels
			assertEquals("Level 1: 4, Level 2: 3, Level 3: 3, Level 4: 1, " + System.lineSeparator(),
					pc.listSpellSlotsRemaining());

		} catch (IOException ex) {
			fail(ex.getMessage());
		}
	}

	private void testDurntLevelUpBaseline(PlayerCharacter pc) {
		assertEquals(1, pc.getClassInfo().size());
		for (Entry<CharClass, Integer> entry : pc.getClassInfo().entrySet()) {
			assertEquals(8, entry.getValue());
			assertEquals("Forge Domain Cleric", entry.getKey().name);
		}

		assertTrue(pc.getMaxHp() >= 51 + 1 + Helpers.getModifierFromAbility(pc.getCon()));
		assertTrue(pc.getMaxHp() <= 51 + 8 + Helpers.getModifierFromAbility(pc.getCon()));

		assertEquals(14, pc.getStr());
		assertEquals(12, pc.getDex());
		assertEquals(15, pc.getCon());
		assertEquals(8, pc.getInte());
		assertEquals(20, pc.getWis());
		assertEquals(10, pc.getCha());

		pc.takeLongRest();
		// Test new spell slot levels
		assertEquals("Level 1: 4, Level 2: 3, Level 3: 3, Level 4: 2, " + System.lineSeparator(),
				pc.listSpellSlotsRemaining());

	}

	private void executeSimpleDurntLevelUp() {
		// Tests HP increase & ASI at lvl 7->8
		ByteArrayOutputStream cmdBuffer = new ByteArrayOutputStream();
		BufferedOutputStream bos = new BufferedOutputStream(cmdBuffer);
		PrintWriter builder = new PrintWriter(bos);
		builder.println("0");
		builder.println("wis");
		builder.println("WIS");
		builder.flush();

		BufferedReader br = new BufferedReader(
				new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
		cmdBuffer.reset();
		bos = new BufferedOutputStream(cmdBuffer);
		builder = new PrintWriter(bos);
		PlayerCharacter pc = null;
		try {
			List<PlayerCharacter> characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("Durnt-reference")) {
					pc = pcs;
				}
			}
		} catch (Exception ex) {
			fail("Unable to load characters");
		}

		try {
			LevelUpWizard.levelUpCharacter(pc, builder, br);
			br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
			assertEquals(br.readLine(), "0: Forge Domain Cleric at level 7");
			assertEquals(br.readLine(), "Select 1 for a new multiclass option");

			assertEquals(br.readLine(), "Select first ability score area (str, dex, con, int, wis, cha): ");
			assertEquals(br.readLine(), "Select second ability score area (str, dex, con, int, wis, cha): ");

			testDurntLevelUpBaseline(pc);

		} catch (IOException ex) {
			fail(ex.getMessage());
		}
	}

	@Test
	void testSimpleLinearLevelUp() {
		executeSimpleDurntLevelUp();
	}

	@Test
	void testSavesAfterLevelUp() {
		executeSimpleDurntLevelUp();

		PlayerCharacter pc = null;
		try {
			List<PlayerCharacter> characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("Durnt-reference")) {
					pc = pcs;
				}
			}
		} catch (Exception ex) {
			fail("Unable to load characters");
		}

		testDurntLevelUpBaseline(pc);
	}

	@Test
	void testMakesTransitionFromBaseClassToSpecialtyClass() {
		// Tests HP increase & ASI at lvl 7->8
		ByteArrayOutputStream cmdBuffer = new ByteArrayOutputStream();
		BufferedOutputStream bos = new BufferedOutputStream(cmdBuffer);
		PrintWriter builder = new PrintWriter(bos);
		builder.println("0");
		builder.println("barf");//Test non-numerical answer to subclass choice wizard
		builder.println("65");//Test invalid index number to subclass choice wizard
		builder.println("1");
		builder.flush();

		BufferedReader br = new BufferedReader(
				new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
		cmdBuffer.reset();
		bos = new BufferedOutputStream(cmdBuffer);
		builder = new PrintWriter(bos);
		PlayerCharacter pc = null;
		try {
			List<PlayerCharacter> characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("Durnt Level 1")) {
					pc = pcs;
				}
			}
		} catch (Exception ex) {
			fail("Unable to load characters");
		}

		assertTrue(pc != null);
		
		try {
			LevelUpWizard.levelUpCharacter(pc, builder, br);
			br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
			assertEquals(br.readLine(), "0: Cleric at level 1");
			assertEquals(br.readLine(), "Select 1 for a new multiclass option");
			assertEquals(br.readLine(), "Select a subclass: ");
			assertEquals(br.readLine(), "0: Forge Domain Cleric");
			assertEquals(br.readLine(), "1: Life Domain Cleric");

			assertEquals(br.readLine(), "Invalid entry");
			assertEquals(br.readLine(), "Select a subclass: ");
			assertEquals(br.readLine(), "0: Forge Domain Cleric");
			assertEquals(br.readLine(), "1: Life Domain Cleric");
			assertEquals(br.readLine(), "Invalid entry");

			assertEquals(1, pc.getClassInfo().size());
			for (Entry<CharClass, Integer> entry : pc.getClassInfo().entrySet()) {
				assertEquals(2, entry.getValue());
				assertEquals("Life Domain Cleric", entry.getKey().name);
			}

			assertTrue(pc.getMaxHp() >= 10 + 1 + Helpers.getModifierFromAbility(pc.getCon()));
			assertTrue(pc.getMaxHp() <= 10 + 8 + Helpers.getModifierFromAbility(pc.getCon()));

			assertEquals(14, pc.getStr());
			assertEquals(12, pc.getDex());
			assertEquals(15, pc.getCon());
			assertEquals(8, pc.getInte());
			assertEquals(18, pc.getWis());
			assertEquals(10, pc.getCha());

			pc.takeLongRest();
			// Test new spell slot levels
			assertEquals("Level 1: 3, " + System.lineSeparator(),
					pc.listSpellSlotsRemaining());

		} catch (IOException ex) {
			fail(ex.getMessage());
		}
	}
}
