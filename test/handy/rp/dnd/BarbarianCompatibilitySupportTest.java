package handy.rp.dnd;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import handy.rp.OutcomeNotification;
import handy.rp.dnd.CharClass.SPELLCASTING_MODIFIER;
import handy.rp.dnd.ClassFeature.USE_TYPE;
import handy.rp.dnd.character.PlayerCharacter;
import handy.rp.xml.PlayerCharacterParser;

class BarbarianCompatibilitySupportTest {

	@BeforeEach
	void setupDurnt() {
		try {
			Files.copy(Paths.get("player_chars_backup", "durnt_reference.xml"), Paths.get("player_chars", "durnt_reference.xml"), StandardCopyOption.REPLACE_EXISTING);
			Files.copy(Paths.get("player_chars_backup", "durnt_life.xml"), Paths.get("player_chars", "durnt_life.xml"), StandardCopyOption.REPLACE_EXISTING);
			Files.copy(Paths.get("player_chars_backup", "barbie.xml"), Paths.get("player_chars", "barbie.xml"), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@AfterEach
	void resetDurnt() {
		try {
			Files.copy(Paths.get("player_chars_backup", "durnt_reference.xml"), Paths.get("player_chars", "durnt_reference.xml"), StandardCopyOption.REPLACE_EXISTING);
			Files.copy(Paths.get("player_chars_backup", "durnt_life.xml"), Paths.get("player_chars", "durnt_life.xml"), StandardCopyOption.REPLACE_EXISTING);
			Files.copy(Paths.get("player_chars_backup", "barbie.xml"), Paths.get("player_chars", "barbie.xml"), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	void testBasicRage() {
		try {
			List<PlayerCharacter> characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			PlayerCharacter durnt = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("barbie_the_barbarian")) {
					durnt = pcs;
				}
			}
			assertTrue(durnt != null, "Didn't load Barbie reference");

			assertEquals(durnt.maxHP, 200);
			assertEquals(durnt.currentHp, 200);
			assertEquals(durnt.getCharacterLevel(), 19);
			assertEquals(durnt.getProficiencyBonus(), 6);

			assertEquals(durnt.getSpellcastingModifier(), SPELLCASTING_MODIFIER.NA);
			assertEquals(durnt.getSpellSaveDC(), null);
			assertEquals(durnt.getSpellToHit(), null);

			assertEquals("Rage 6 available out of 6", durnt.printResourceCounters());

			durnt.expendFeature(0);
			
			List<ClassFeature> features = durnt.getActiveFeatures();
			assertEquals(1, features.size());
			assertEquals(features.get(0).featureName, "Rage");
			durnt.clearFeature(0);
			features = durnt.getActiveFeatures();
			assertEquals(0, features.size());

			assertEquals("Rage 5 available out of 6", durnt.printResourceCounters());

			durnt.takeShortRest();

			assertEquals("Rage 5 available out of 6", durnt.printResourceCounters());

			durnt.takeLongRest();

			assertEquals("Rage 6 available out of 6", durnt.printResourceCounters());
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	void testPersistentFeatureRage() {
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
			String successAttack = durnt.attack("warhammer", false, false).humanMessage;
			assertTrue(successAttack.endsWith("Rage hits for an extra 4 damage"));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	void testNominalFeatures() {
		try {
			List<PlayerCharacter> characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			PlayerCharacter durnt = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("barbie_the_barbarian")) {
					durnt = pcs;
				}
			}
			assertTrue(durnt != null, "Didn't load Barbie reference");

			assertEquals(durnt.maxHP, 200);
			assertEquals(durnt.currentHp, 200);
			assertEquals(durnt.getCharacterLevel(), 19);
			assertEquals(durnt.getProficiencyBonus(), 6);

			assertEquals(durnt.getSpellcastingModifier(), SPELLCASTING_MODIFIER.NA);
			assertEquals(durnt.getSpellSaveDC(), null);
			assertEquals(durnt.getSpellToHit(), null);

			String features = durnt.printFeatures();
			String featureElems[] = features.split(System.lineSeparator());
			assertEquals("0 : Rage", featureElems[0]);
			assertEquals("1 : Unarmored Defense", featureElems[1]);
			assertEquals("2 : Danger Sense", featureElems[2]);
			assertEquals("3 : Reckless Attack", featureElems[3]);
			assertEquals("4 : Fast Movement", featureElems[4]);
			assertEquals("5 : Extra Attack", featureElems[5]);
			assertEquals("6 : Persistent Rage", featureElems[6]);
			assertEquals("7 : Indomitable Might", featureElems[7]);
			assertEquals("8 : Brutal Critical", featureElems[8]);
			assertEquals("9 : Feral Instinct", featureElems[9]);
			assertEquals("10 : Mindless Rage", featureElems[10]);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	void testExtraAttack() {
		try {
			List<PlayerCharacter> characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			PlayerCharacter durnt = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("barbie_the_barbarian")) {
					durnt = pcs;
				}
			}
			//3 attacks, extra attack + reaction
			assertTrue(durnt != null, "Didn't load Barbie reference");
			String successAttack = durnt.attack("warhammer", false, false).humanMessage;
			String hitStartString = "barbie_the_barbarian strikes with Warhammer with a to hit of ";
			assertTrue(successAttack.startsWith(hitStartString));
			successAttack = durnt.attack("warhammer", false, false).humanMessage;
			assertTrue(successAttack.startsWith(hitStartString));
			successAttack = durnt.attack("warhammer", false, false).humanMessage;
			assertTrue(successAttack.startsWith(hitStartString));
			OutcomeNotification failedThirdAttack= durnt.attack("warhammer", false, false);
			assertEquals("No attacks remaining this turn", failedThirdAttack.humanMessage);
			assertFalse(failedThirdAttack.outcome);
			durnt.notifyNewTurn();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	void testFeralInstinct() {
		try {
			List<PlayerCharacter> characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			PlayerCharacter durnt = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("barbie_the_barbarian")) {
					durnt = pcs;
				}
			}
			assertTrue(durnt != null, "Didn't load Barbie reference");
			assertTrue(durnt.hasInitiativeAdvantage());
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		try {
			List<PlayerCharacter> characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			PlayerCharacter durnt = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("Durnt-reference")) {
					durnt = pcs;
				}
			}
			assertTrue(durnt != null, "Didn't load Durnt reference");
			assertFalse(durnt.hasInitiativeAdvantage());
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	void testFrenzyAndRetaliation() {
		//Player will have 2 regular attacks, 1 reaction attack from retaliation, and 1 additional if frenzy is enabled
		//Check to make sure first that 3 attacks are available, then turn on Frenzy, then check for 4 attacks
		try {
			List<PlayerCharacter> characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			PlayerCharacter durnt = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("barbie_the_barbarian")) {
					durnt = pcs;
				}
			}
			assertTrue(durnt != null, "Didn't load Barbie reference");
			String attackOptions = durnt.listAttackOptions();
			String optionElements[] = attackOptions.split(System.lineSeparator());
			assertEquals("Reaction attack still available and will be automatically used on attack", optionElements[1]);
			String successAttack = durnt.attack("warhammer", false, false).humanMessage;
			String hitStartString = "barbie_the_barbarian strikes with Warhammer with a to hit of ";
			assertTrue(successAttack.startsWith(hitStartString));
			successAttack = durnt.attack("warhammer", false, false).humanMessage;
			assertTrue(successAttack.startsWith(hitStartString));
			successAttack = durnt.attack("warhammer", false, false).humanMessage;
			assertTrue(successAttack.startsWith(hitStartString));
			OutcomeNotification failedFourthAttack = durnt.attack("warhammer", false, false);
			assertEquals("No attacks remaining this turn", failedFourthAttack.humanMessage);
			assertFalse(failedFourthAttack.outcome);
			durnt.notifyNewTurn();
			
			ClassFeature feature = durnt.expendFeature(11);//Frenzy
			assertEquals("Frenzy", feature.featureName);
			attackOptions = durnt.listAttackOptions();
			optionElements = attackOptions.split(System.lineSeparator());
			assertEquals("Bonus action attack still available and will be automatically used on attack", optionElements[1]);
			assertEquals("Reaction attack still available and will be automatically used on attack", optionElements[2]);
			assertTrue(feature.allowBonusActionAttack(true));
			successAttack = durnt.attack("warhammer", false, false).humanMessage;
			assertTrue(successAttack.startsWith(hitStartString));
			successAttack = durnt.attack("warhammer", false, false).humanMessage;
			assertTrue(successAttack.startsWith(hitStartString));
			successAttack = durnt.attack("warhammer", false, false).humanMessage;
			assertTrue(successAttack.startsWith(hitStartString));
			successAttack = durnt.attack("warhammer", false, false).humanMessage;
			assertTrue(successAttack.startsWith(hitStartString));
			failedFourthAttack = durnt.attack("warhammer", false, false);
			assertEquals("No attacks remaining this turn", failedFourthAttack.humanMessage);
			assertFalse(failedFourthAttack.outcome);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	@Test
	void testIntimidatingPresense() {
		try {
			List<PlayerCharacter> characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			PlayerCharacter durnt = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("barbie_the_barbarian")) {
					durnt = pcs;
				}
			}
			assertTrue(durnt != null, "Didn't load Barbie reference");
			ClassFeature feature = durnt.expendFeature(14);//Intimidating Presence
			assertEquals("Intimidating Presence", feature.featureName);
			assertEquals(USE_TYPE.ACTION, feature.useType);
			List<ClassFeature> features = durnt.getActiveFeatures();
			assertEquals(1, features.size());
			assertEquals("Intimidating Presence", features.get(0).featureName);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	@Test
	void testBrutalCritical() {
		try {
			List<PlayerCharacter> characters = PlayerCharacterParser.loadAllPlayerCharacters("player_chars");
			PlayerCharacter durnt = null;
			for (PlayerCharacter pcs : characters) {
				if (pcs.personalName.equals("barbie_the_barbarian")) {
					durnt = pcs;
				}
			}
			assertTrue(durnt != null, "Didn't load Barbie reference");

			assertEquals(3, durnt.extraCritDice());
			assertTrue(durnt != null, "Didn't load Durnt reference");
			for (int i = 0; i < 100; i++) {
				String successAttack = durnt.attack("warhammer", false, false).humanMessage;
				String hitStartString = "barbie_the_barbarian strikes with Warhammer with a to hit of ";
				assertTrue(successAttack.startsWith(hitStartString));
				String[] remainingElements = successAttack.substring(hitStartString.length()).split(" ");
				int toHit = Integer.parseInt(remainingElements[0]);
				assertTrue(toHit <= 20 + 6 + 7 & toHit >= 13);
				toHit = Integer.parseInt(remainingElements[3]);
				assertTrue(toHit <= 20 + 6 + 7 & toHit >= 13);
				int oneHandDamage = Integer.parseInt(remainingElements[6]);
				assertTrue(oneHandDamage <= 8 + 7 & oneHandDamage >= 1 + 7);
				int critOneHandDamage = Integer.parseInt(remainingElements[9]);
				assertTrue(critOneHandDamage <= 40 + 7 & critOneHandDamage >= 5 + 7);
				int twoHandDamage = Integer.parseInt(remainingElements[18]);
				assertTrue(twoHandDamage <= 10 + 7 & twoHandDamage >= 1 + 7);
				int critTwoHandDamage = Integer.parseInt(remainingElements[21]);
				assertTrue(critTwoHandDamage <= 50 + 7 & critTwoHandDamage >= 5 + 7);
				assertTrue(successAttack.contains(" Bludgeoning damage if used one-handed, or for "));
				assertTrue(successAttack.endsWith(" if used two-handed."));
				durnt.notifyNewTurn();
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		// Remember extra updates
	}

}
