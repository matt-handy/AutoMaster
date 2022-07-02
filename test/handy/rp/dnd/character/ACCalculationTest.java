package handy.rp.dnd.character;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import handy.rp.xml.PlayerCharacterParser;

class ACCalculationTest {

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
			Files.copy(Paths.get("player_chars_backup", "armor_barbie.xml"), Paths.get("player_chars", "armor_barbie.xml"), StandardCopyOption.REPLACE_EXISTING);
			Files.copy(Paths.get("player_chars_backup", "armor_med_barbie.xml"), Paths.get("player_chars", "armor_med_barbie.xml"), StandardCopyOption.REPLACE_EXISTING);
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
			Files.copy(Paths.get("player_chars_backup", "armor_barbie.xml"), Paths.get("player_chars", "armor_barbie.xml"), StandardCopyOption.REPLACE_EXISTING);
			Files.copy(Paths.get("player_chars_backup", "armor_med_barbie.xml"), Paths.get("player_chars", "armor_med_barbie.xml"), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	void testShieldOfFaith() {

		try {
			List<PlayerCharacter> characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			PlayerCharacter durnt = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("Durnt-reference")) {
					durnt = pcs;
				}
			}
			assertTrue(durnt != null, "Durnt-reference should have been found");
			durnt.expendSpell("shield_of_faith");
			// 18 (Plate) + 2 (Shield) + 1 (Soul of the Forge) + 2 (Shield of Faith Cast)
			assertEquals(23, durnt.getCurrentAC());
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

	}

	@Test
	void testWizardShield() {
		try {
			List<PlayerCharacter> characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			PlayerCharacter durnt = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("lilwizzie_the_wizard")) {
					durnt = pcs;
				}
			}
			assertTrue(durnt != null, "lilwizzie_the_wizard should have been found");
			durnt.expendSpell("shield");
			// 2 dex + 5 shield
			assertEquals(17, durnt.getCurrentAC());
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	void testPhysicalShield() {
		try {
			List<PlayerCharacter> characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			PlayerCharacter durnt = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("Durnt-life-cleric")) {
					durnt = pcs;
				}
			}
			assertTrue(durnt != null, "Durnt-life-cleric should have been found");
			// 18 plate + 2 shield
			assertEquals(20,durnt.getCurrentAC());
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	
	@Test
	void testHeavyArmor() {
		try {
			List<PlayerCharacter> characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			PlayerCharacter durnt = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("Durnt Level 1")) {
					durnt = pcs;
				}
			}
			assertTrue(durnt != null, "Durnt Level 1 should have been found");
			// 16 Chain Mail
			assertEquals(16, durnt.getCurrentAC());
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	void testMediumArmor() {
		// Cap on Dex bonus
		try {
			List<PlayerCharacter> characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			PlayerCharacter durnt = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("medarmored_barbie_the_barbarian")) {
					durnt = pcs;
				}
			}
			assertTrue(durnt != null, "barbie_the_barbarian should have been found");
			// 3 dex (limited to 2) + 4 from armor bonus
			assertEquals(durnt.getCurrentAC(), 16);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	void testLightArmor() {
		try {
			List<PlayerCharacter> characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			PlayerCharacter durnt = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("armored_barbie_the_barbarian")) {
					durnt = pcs;
				}
			}
			assertTrue(durnt != null, "barbie_the_barbarian should have been found");
			// 3 dex + 2 from armor bonus
			assertEquals(durnt.getCurrentAC(), 15);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	void testNakedBaseACNonBarbarian() {
		try {
			List<PlayerCharacter> characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			PlayerCharacter durnt = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("lilwizzie_the_wizard")) {
					durnt = pcs;
				}
			}
			assertTrue(durnt != null, "lilwizzie_the_wizard should have been found");
			// 2 dex
			assertEquals(12, durnt.getCurrentAC());
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	@Test
	void testNakedBaseACBarbarian() {
		try {
			List<PlayerCharacter> characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			PlayerCharacter durnt = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("barbie_the_barbarian")) {
					durnt = pcs;
				}
			}
			assertTrue(durnt != null, "barbie_the_barbarian should have been found");
			// 2 shield + 3 dex + 4 con
			assertEquals(durnt.getCurrentAC(), 19);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	void testACIncreaseFeature() {
		try {
			List<PlayerCharacter> characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			PlayerCharacter durnt = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("Durnt-reference")) {
					durnt = pcs;
				}
			}
			assertTrue(durnt != null, "Durnt-reference should have been found");
			//18 (Plate) + 2 (Shield) + 1 (Soul of the Forge)
			assertEquals(21, durnt.getCurrentAC());
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

}
