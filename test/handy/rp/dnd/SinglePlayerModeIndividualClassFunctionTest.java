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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SinglePlayerModeIndividualClassFunctionTest {

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
	void testAddPreparedSpellUI() {
		EncounterRunner main = new EncounterRunner();
		try {
			main.initialize();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		ByteArrayOutputStream cmdBuffer = new ByteArrayOutputStream();
		BufferedOutputStream bos = new BufferedOutputStream(cmdBuffer);
		PrintWriter builder = new PrintWriter(bos);
		builder.println("prepareSpell grease");
		builder.println("prepareSpell fart fart");
		builder.println("quit");
		builder.flush();

		BufferedReader br = new BufferedReader(
				new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
		cmdBuffer.reset();
		bos = new BufferedOutputStream(cmdBuffer);
		builder = new PrintWriter(bos);
		try {
			main.singlePlayerMode(builder, br, "unprep_lilwizzie_the_wizard");
		} catch (IOException e) {
			fail(e.getMessage());
		}

		try {
			br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
			assertEquals("Spell prepared: Grease", br.readLine());
			assertEquals("prepareSpell <spell name>", br.readLine());
		} catch (IOException ex) {
			fail(ex.getMessage());
		}
		
	}
	
	@Test
	void testNonwizardsCantPrepareSpell() {
		//TODO Refactor so that other classes can prep too from their class lists
		EncounterRunner main = new EncounterRunner();
		try {
			main.initialize();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		ByteArrayOutputStream cmdBuffer = new ByteArrayOutputStream();
		BufferedOutputStream bos = new BufferedOutputStream(cmdBuffer);
		PrintWriter builder = new PrintWriter(bos);
		builder.println("prepareSpell cloudkill");
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
			assertEquals("Only wizards can prepare spells for now", br.readLine());
		} catch (IOException ex) {
			fail(ex.getMessage());
		}
	}
	
	@Test
	void testWizardPreparedKnownSpellSwapUI() {
		EncounterRunner main = new EncounterRunner();
		try {
			main.initialize();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		ByteArrayOutputStream cmdBuffer = new ByteArrayOutputStream();
		BufferedOutputStream bos = new BufferedOutputStream(cmdBuffer);
		PrintWriter builder = new PrintWriter(bos);
		builder.println("swapSpell bane magic_missile");
		builder.println("swapSpell fart");
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
			assertEquals("Spell prepared: Magic Missile", br.readLine());
			assertEquals("swapSpell <old spell name> <new spell name>", br.readLine());
		} catch (IOException ex) {
			fail(ex.getMessage());
		}
	}

	@Test
	void testNonWizardsCantSwapsSpells() {
		//TODO: Refactor so that other classes that prepare against class lists can do so instead of relying on individual known spell
		EncounterRunner main = new EncounterRunner();
		try {
			main.initialize();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		ByteArrayOutputStream cmdBuffer = new ByteArrayOutputStream();
		BufferedOutputStream bos = new BufferedOutputStream(cmdBuffer);
		PrintWriter builder = new PrintWriter(bos);
		builder.println("swapSpell cloudkill barf");
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
			assertEquals("Only wizards can swap spells for now", br.readLine());
		} catch (IOException ex) {
			fail(ex.getMessage());
		}
	}
	
	@Test
	void testWizardLearnNewSpellUI() {
		EncounterRunner main = new EncounterRunner();
		try {
			main.initialize();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		ByteArrayOutputStream cmdBuffer = new ByteArrayOutputStream();
		BufferedOutputStream bos = new BufferedOutputStream(cmdBuffer);
		PrintWriter builder = new PrintWriter(bos);
		builder.println("learnSpell cloudkill");
		builder.println("learnSpell fart");
		builder.println("learnSpell fart fart");
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
			assertEquals("Spell learned: Cloudkill", br.readLine());
			assertEquals("Spell not found", br.readLine());
			assertEquals("learnSpell <spell name>", br.readLine());
		} catch (IOException ex) {
			fail(ex.getMessage());
		}
		
	}
	
	@Test
	void testNonWizardsCantLearnNewSpell() {
		EncounterRunner main = new EncounterRunner();
		try {
			main.initialize();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		ByteArrayOutputStream cmdBuffer = new ByteArrayOutputStream();
		BufferedOutputStream bos = new BufferedOutputStream(cmdBuffer);
		PrintWriter builder = new PrintWriter(bos);
		builder.println("learnSpell cloudkill");
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
			assertEquals("Only wizards can learn spells", br.readLine());
		} catch (IOException ex) {
			fail(ex.getMessage());
		}
	}
	
	@Test
	void testArcaneRecoveryIntegratedWithShortRest() {
		EncounterRunner main = new EncounterRunner();
		try {
			main.initialize();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		ByteArrayOutputStream cmdBuffer = new ByteArrayOutputStream();
		BufferedOutputStream bos = new BufferedOutputStream(cmdBuffer);
		PrintWriter builder = new PrintWriter(bos);
		builder.println("cast catapult");
		builder.println("advturn");
		builder.println("cast catapult");
		builder.println("advturn");
		builder.println("cast catapult");
		builder.println("advturn");
		builder.println("cast catapult 2");
		builder.println("advturn");
		builder.println("usefeature 0");
		builder.println("slot 1 2");
		builder.println("slot 2");
		builder.println("slot 1");
		builder.println("lss");
		builder.println("lss");
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
			assertEquals("Catapult: Choose one object weighing 1 to 5 pounds within range that isn’t being worn or carried. The object flies in a straight line up to 90 feet in a direction you choose before falling to the ground, stopping early if it impacts against a solid surface. If the object would strike a creature, that creature must make a Dexterity saving throw. On a failed save, the object strikes the target and stops moving. When the object strikes something, the object and what it strikes each take 3d8 bludgeoning damage.", br.readLine());
			assertEquals("At Higher Levels. When you cast this spell using a spell slot of 2nd level or higher, the maximum weight of objects that you can target with this spell increases by 5 pounds, and the damage increases by 1d8, for each slot level above 1st.", br.readLine());
			br.readLine();//Discard catapult damage
			assertEquals("Spell Save: 13, no damage on save.", br.readLine());
			//Discard second & third & fourth catapult output
			for(int idx = 0; idx < 12; idx++) {
				br.readLine();
			}
			assertEquals("You have learned to regain some of your magical energy by studying your spellbook. Once per day when you finish a short rest, you can choose expended spell slots to recover. The spell slots can have a combined level that is equal to or less than half your wizard level (rounded up), and none of the slots can be 6th level or higher.", br.readLine());
			assertEquals("For example, if you're a 4th-level wizard, you can recover up to two levels worth of spell slots. You can recover either a 2nd-level spell slot or two 1st-level spell slots.", br.readLine());
			assertEquals("Welcome to the Spell Slot Recovery Wizard", br.readLine());
			assertEquals("Enter the slots you would like to recover, such as: 'slot 1 2' to recover two #1 slots, or 'slot 2' to recover a single #2 slot", br.readLine());
			assertEquals("Enter 'quit' to return to play", br.readLine());
			assertEquals("Insufficient recovery slots available", br.readLine());
			assertEquals("Insufficient recovery slots available", br.readLine());
			assertEquals("Slots recovered", br.readLine());
			assertEquals("Your slot work is complete", br.readLine());
			assertEquals("Level 1: 2, Level 2: 1, ", br.readLine());
		} catch (IOException ex) {
			fail(ex.getMessage());
		}
	}
}

