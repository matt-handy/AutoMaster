package handy.rp.dnd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import handy.rp.OutcomeNotification;
import handy.rp.dnd.character.PlayerCharacter;
import handy.rp.dnd.spells.Spell;
import handy.rp.dnd.spells.Spell.SLOTLEVEL;
import handy.rp.xml.PlayerCharacterParser;

class WizardIntegrationTest {
	
	@BeforeEach
	void setup() {
		try {
			Files.copy(Paths.get("player_chars_backup", "wizzie.xml"), Paths.get("player_chars", "wizzie.xml"), StandardCopyOption.REPLACE_EXISTING);
			Files.copy(Paths.get("player_chars_backup", "lil_wizzie.xml"), Paths.get("player_chars", "lil_wizzie.xml"), StandardCopyOption.REPLACE_EXISTING);
			Files.copy(Paths.get("player_chars_backup", "unprep_lil_wizzie.xml"), Paths.get("player_chars", "unprep_lil_wizzie.xml"), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@AfterEach
	void reset() {
		try {
			Files.copy(Paths.get("player_chars_backup", "wizzie.xml"), Paths.get("player_chars", "wizzie.xml"), StandardCopyOption.REPLACE_EXISTING);
			Files.copy(Paths.get("player_chars_backup", "lil_wizzie.xml"), Paths.get("player_chars", "lil_wizzie.xml"), StandardCopyOption.REPLACE_EXISTING);
			Files.copy(Paths.get("player_chars_backup", "unprep_lil_wizzie.xml"), Paths.get("player_chars", "unprep_lil_wizzie.xml"), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	void testLearnNewSpell() {
		try {
			List<PlayerCharacter> characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			PlayerCharacter wizzie = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("lilwizzie_the_wizard")) {
					wizzie = pcs;
				}
			}
			assertTrue(wizzie != null, "Didn't load Wizzie reference");
			
			
			//Test that we can add it
			OutcomeNotification outcome = wizzie.learnSpell("cloudkill");
			assertTrue(outcome.outcome);
			assertEquals("Spell learned: Cloudkill", outcome.humanMessage);
			
			//Test we can't add a bogus spell
			outcome = wizzie.learnSpell("fart");
			assertFalse(outcome.outcome);
			assertEquals("Spell not found", outcome.humanMessage);
			
			//Test we can't add a spell we know
			outcome = wizzie.learnSpell("bane");
			assertFalse(outcome.outcome);
			assertEquals("Spell already known", outcome.humanMessage);
			
			//Test that it is saved
			characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			wizzie = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("lilwizzie_the_wizard")) {
					wizzie = pcs;
				}
			}
			
			assertTrue(wizzie != null, "Didn't load Wizzie reference");
			outcome = wizzie.learnSpell("cloudkill");
			assertFalse(outcome.outcome);
			assertEquals("Spell already known", outcome.humanMessage);
		}catch(Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
	}
	
	@Test
	void testPrepareNewSpellFunctionality() {
		try {
			List<PlayerCharacter> characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			PlayerCharacter wizzie = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("unprep_lilwizzie_the_wizard")) {
					wizzie = pcs;
				}
			}
			assertTrue(wizzie != null, "Didn't load Wizzie reference");
		
			//Test list prepared spells
			String preparedSpells = wizzie.listSpells();
			assertTrue(preparedSpells.contains("Level: Cantrip Mage Hand, Fire Bolt, Toll the Dead,"));
			assertTrue(preparedSpells.contains("Level: 1 Bane, Shield, Catapult, Detect Magic, Fog Cloud,"));
			
			//Test add prepared spells
			OutcomeNotification notification = wizzie.addPreparedSpell("grease");
			assertEquals("Spell prepared: Grease",notification.humanMessage);
			assertTrue(notification.outcome);
			preparedSpells = wizzie.listSpells();
			assertTrue(preparedSpells.contains("Level: Cantrip Mage Hand, Fire Bolt, Toll the Dead,"));
			assertTrue(preparedSpells.contains("Level: 1 Bane, Shield, Catapult, Detect Magic, Fog Cloud, Grease,"));
			
			notification = wizzie.addPreparedSpell("magic_missile");
			assertFalse(notification.outcome);
			assertEquals("Player already has maximum spell number prepared", notification.humanMessage);
			
			//Test add unknown spell
			notification = wizzie.addPreparedSpell("barf");
			assertFalse(notification.outcome);
			assertEquals("Unknown spell: barf", notification.humanMessage);
			
			//Test save of prepared spells
			characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			wizzie = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("unprep_lilwizzie_the_wizard")) {
					wizzie = pcs;
				}
			}
			
			assertTrue(wizzie != null, "Didn't load Wizzie reference");
			preparedSpells = wizzie.listSpells();
			assertTrue(preparedSpells.contains("Level: Cantrip Mage Hand, Fire Bolt, Toll the Dead,"));
			assertTrue(preparedSpells.contains("Level: 1 Bane, Shield, Catapult, Detect Magic, Fog Cloud, Grease,"));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	@Test
	void testSwapPreparedSpellsFunctionality() {
		try {
			List<PlayerCharacter> characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			PlayerCharacter wizzie = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("lilwizzie_the_wizard")) {
					wizzie = pcs;
				}
			}
			assertTrue(wizzie != null, "Didn't load Wizzie reference");
		
			//Test list prepared spells
			String preparedSpells = wizzie.listSpells();
			assertTrue(preparedSpells.contains("Level: Cantrip Mage Hand, Fire Bolt, Toll the Dead,"));
			assertTrue(preparedSpells.contains("Level: 1 Bane, Shield, Catapult, Detect Magic, Grease, Fog Cloud,"));
			
			//Test list known spells
			String knownSpells = wizzie.printSpellsKnown();
			assertTrue(knownSpells.contains("Bane (Level 1)"));
			assertTrue(knownSpells.contains("Shield (Level 1)"));
			assertTrue(knownSpells.contains("Catapult (Level 1)"));
			assertTrue(knownSpells.contains("Detect Magic (Level 1)"));
			assertTrue(knownSpells.contains("Grease (Level 1)"));
			assertTrue(knownSpells.contains("Fog Cloud (Level 1)"));
			assertTrue(knownSpells.contains("Jump (Level 1)"));
			assertTrue(knownSpells.contains("Magic Missile (Level 1)"));
			
			//Test swap prepared spells
			OutcomeNotification notification = wizzie.swapPreparedSpell("bane", "jump");
			assertTrue(notification.outcome);
			preparedSpells = wizzie.listSpells();
			assertTrue(preparedSpells.contains("Level: Cantrip Mage Hand, Fire Bolt, Toll the Dead,"));
			assertTrue(preparedSpells.contains("Level: 1 Shield, Catapult, Detect Magic, Grease, Fog Cloud, Jump,"));
			knownSpells = wizzie.printSpellsKnown();
			assertTrue(knownSpells.contains("Bane (Level 1)"));
			assertTrue(knownSpells.contains("Shield (Level 1)"));
			assertTrue(knownSpells.contains("Catapult (Level 1)"));
			assertTrue(knownSpells.contains("Detect Magic (Level 1)"));
			assertTrue(knownSpells.contains("Grease (Level 1)"));
			assertTrue(knownSpells.contains("Fog Cloud (Level 1)"));
			assertTrue(knownSpells.contains("Jump (Level 1)"));
			assertTrue(knownSpells.contains("Magic Missile (Level 1)"));
			
			//Test swap unknown spell
			notification = wizzie.swapPreparedSpell("barf", "jump");
			assertFalse(notification.outcome);
			assertEquals("Spell not prepared: barf", notification.humanMessage);
			
			notification = wizzie.swapPreparedSpell("bane", "barf");
			assertFalse(notification.outcome);
			assertEquals("Unknown spell: barf", notification.humanMessage);
			
			//Test can't swap a cantrip
			notification = wizzie.swapPreparedSpell("mage_hand", "jump");
			assertFalse(notification.outcome);
			assertEquals("Cannot swap cantrip: mage_hand", notification.humanMessage);
			
			//Test save of prepared spells
			characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			wizzie = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("lilwizzie_the_wizard")) {
					wizzie = pcs;
				}
			}
			
			assertTrue(wizzie != null, "Didn't load Wizzie reference");
			preparedSpells = wizzie.listSpells();
			assertTrue(preparedSpells.contains("Level: Cantrip Mage Hand, Fire Bolt, Toll the Dead,"));
			assertTrue(preparedSpells.contains("Level: 1 Shield, Catapult, Detect Magic, Grease, Fog Cloud, Jump,"));
			knownSpells = wizzie.printSpellsKnown();
			assertTrue(knownSpells.contains("Bane (Level 1)"));
			assertTrue(knownSpells.contains("Shield (Level 1)"));
			assertTrue(knownSpells.contains("Catapult (Level 1)"));
			assertTrue(knownSpells.contains("Detect Magic (Level 1)"));
			assertTrue(knownSpells.contains("Grease (Level 1)"));
			assertTrue(knownSpells.contains("Fog Cloud (Level 1)"));
			assertTrue(knownSpells.contains("Jump (Level 1)"));
			assertTrue(knownSpells.contains("Magic Missile (Level 1)"));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	@Test
	void testNoArmorProficiency() {
		try {
			List<PlayerCharacter> characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			PlayerCharacter wizzie = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("wizzie_the_wizard")) {
					wizzie = pcs;
				}
			}
			assertTrue(wizzie != null, "Didn't load Wizzie reference");
			assertEquals(0, wizzie.getArmorProficiencies().size());
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	@Test
	void validateNumberOfCantripsLoadedFromConfiguration() {
		//Validate that we will fail to load a character who has an incorrect number of cantrips known
		try {
			PlayerCharacterParser.load(Paths.get("player_chars_malformed", "lil_wizzie.xml").toString());
			fail("Should not get here");
		}catch(Exception ex) {
			assertEquals("Character lilwizzie_the_wizard created with too many cantrips for level: 4 vs allowed 3", ex.getMessage());
		}
	}
	
	@Test
	void testArcaneRecovery() {
		//Test that feature has flag is implemented for the feature
		//The actual integration with the short rest system is tested with the 
		//SinglePlayerModeIndividualClassFunctionTest
		try {
			List<PlayerCharacter> characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			PlayerCharacter wizzie = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("wizzie_the_wizard")) {
					wizzie = pcs;
				}
			}
			assertTrue(wizzie != null, "Didn't load Wizzie reference");
			//List features 
			String features = wizzie.printFeatures();
			String featureElements[] = features.split(System.lineSeparator());
			assertEquals("0 : Arcane Recovery", featureElements[0]);
			//Expend feature
			ClassFeature feature = wizzie.expendFeature(0);
			assertEquals("Arcane Recovery", feature.featureName);
			assertTrue(feature.recoverSpellSlotsOnShortRest);
			
			//Try expending again, and see failure
			try {
				wizzie.expendFeature(0);
				fail("Should not have been able to use feature");
			}catch(Exception ex) {
				assertEquals("Insufficient charges", ex.getMessage());
			}
			
			//Recover and test feature is available again
			wizzie.takeLongRest();
			
			feature = wizzie.expendFeature(0);
			assertEquals("Arcane Recovery", feature.featureName);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	@Test
	void testSpellMastery() {
		
		try {
			List<PlayerCharacter> characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			PlayerCharacter wizzie = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("wizzie_the_wizard")) {
					wizzie = pcs;
				}
			}
			assertTrue(wizzie != null, "Didn't load Wizzie reference");
			//Test that Spell Mastery has the appropriate flag set
			//List features 
			String features = wizzie.printFeatures();
			String featureElements[] = features.split(System.lineSeparator());
			assertEquals("1 : Spell Mastery", featureElements[1]);
			ClassFeature feature = wizzie.expendFeature(1);
			assertTrue(feature.allowsFreeSpells);
			
			String baselineSpellSlots = wizzie.listSpellSlotsRemaining();
			
			//Test that wizzie can cast shield without burning a slot
			wizzie.expendSpell("shield");
			assertEquals(baselineSpellSlots, wizzie.listSpellSlotsRemaining());
			wizzie.notifyNewTurn();
		
			//Test that wizzie can cast detect magic without burning a slot
			wizzie.expendSpell("detect_magic");
			assertEquals(baselineSpellSlots, wizzie.listSpellSlotsRemaining());
			wizzie.notifyNewTurn();
		
			//test that wizzie casts bane and burns slot
			wizzie.expendSpell("bane");
			assertTrue(wizzie.listSpellSlotsRemaining().contains("Level 1: 3"));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	@Test
	void testSignatureSpells() {
		
		
		
		try {
			List<PlayerCharacter> characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			PlayerCharacter wizzie = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("wizzie_the_wizard")) {
					wizzie = pcs;
				}
			}
			assertTrue(wizzie != null, "Didn't load Wizzie reference");
			//Test that Signature Spells has both flags
			//List features 
			String features = wizzie.printFeatures();
			String featureElements[] = features.split(System.lineSeparator());
			assertEquals("2 : Signature Spells", featureElements[2]);
			ClassFeature feature = wizzie.expendFeature(2);
			assertTrue(feature.allowsFreeSpells);
			assertTrue(feature.allowsNoPrepSpells);
		
			//Test that fireball and fly are available and total prepared spell count is normal + signature spell limit
			String spellList = wizzie.listSpells();
			int expectSpellCount = wizzie.getCasterLevel() + Helpers.getModifierFromAbility(wizzie.inte) + 2;
			int actualSpellCount = 0;
			for(String line : spellList.split(System.lineSeparator())) {
				if(!line.contains("Cantrip")) {
				actualSpellCount += line.split(",").length - 1;
				}
			}
			assertEquals(expectSpellCount, actualSpellCount);
			
			String baselineSpellSlots = wizzie.listSpellSlotsRemaining();
			
			//Test that fireball casts without a spell slot
			wizzie.expendSpell("fireball");
			assertEquals(baselineSpellSlots, wizzie.listSpellSlotsRemaining());
			wizzie.notifyNewTurn();
		
			//Test that fly casts without a spell slot
			wizzie.expendSpell("fly");
			assertEquals(baselineSpellSlots, wizzie.listSpellSlotsRemaining());
			wizzie.notifyNewTurn();
		
			//test that wizzie casts catapult and burns slot
			wizzie.expendSpell("catapult");
			assertTrue(wizzie.listSpellSlotsRemaining().contains("Level 1: 3"));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	@Test
	void testPotentCantrip() {
		//Test that potent cantrip has the flag set. 
		try {
			List<PlayerCharacter> characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			PlayerCharacter wizzie = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("wizzie_the_wizard")) {
					wizzie = pcs;
				}
			}
			assertTrue(wizzie != null, "Didn't load Wizzie reference");
			//List features 
			String features = wizzie.printFeatures();
			//System.out.println(features);
			String featureElements[] = features.split(System.lineSeparator());
			assertEquals("5 : Potent Cantrip", featureElements[5]);
			ClassFeature feature = wizzie.expendFeature(5);
			assertTrue(feature.halfDamageCantrip);
			
			//Try to cast Toll the Dead with wizard and see if caveat printed
			Spell tollTheDead = wizzie.expendSpell("toll_the_dead");
			String value = tollTheDead.cast(SLOTLEVEL.CANTRIP, wizzie.getCasterLevel(), wizzie.getSpellSaveDC(), wizzie.getSpellToHit(), wizzie.getSpellcastingModifierValue(), wizzie);
			assertTrue(value.endsWith("Spell Save: 15, player feature causes half damage to cantrips on save. Only affects damage, other effects are still saved against"));
		
			//Cast toll the dead and with Durnt and make sure it is not printed
			PlayerCharacter durnt = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("Durnt-reference")) {
					durnt = pcs;
				}
			}
			assertTrue(durnt != null, "Didn't load Durnt reference");
			tollTheDead = durnt.expendSpell("toll_the_dead");
			value = tollTheDead.cast(SLOTLEVEL.CANTRIP, durnt.getCasterLevel(), durnt.getSpellSaveDC(), durnt.getSpellToHit(), durnt.getSpellcastingModifierValue(), durnt);
			assertTrue(value.endsWith("Spell Save: 15"));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	
}
