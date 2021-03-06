package handy.rp.dnd;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import handy.rp.dnd.monsters.MonsterInstance;
import handy.rp.xml.MonsterParser;

class MainTest {

	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void test() {
		EncounterRunner main = new EncounterRunner();
		try {
			main.initialize();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		String args[] = {"amon", "Hill Giant", "Danny Boy"};
		String response = main.addMonster(args);
		assertTrue(response.startsWith("Added Hill Giant as Danny Boy with initiative "));
		String args2[] = {"amon", "1", "Other Guy"};
		response = main.addMonster(args2);
		assertTrue(response.startsWith("Added Bandit Captain as Other Guy with initiative "));
		String args3[] = {"amon", "1", "Dude"};
		response = main.addMonster(args3);
		assertTrue(response.startsWith("Added Bandit Captain as Dude with initiative "));
		
		main.startCombat();
		
		String starter = main.getCurrentEntity().personalName;
		
		String rmArgs[] = {"rm", starter};
		response = main.rmEntity(rmArgs);
		assertTrue(response.startsWith("Removed: " + starter));
		
		assertFalse(main.getCurrentEntity().personalName.equals(starter));
		assertTrue(main.getCurrentEntity().personalName.equals("Other Guy") ||
				main.getCurrentEntity().personalName.equals("Dude") ||
				main.getCurrentEntity().personalName.equals("Danny Boy"));
		main.shutdown();
	}
	
	@Test
	void savingThrowTest() {
		EncounterRunner main = new EncounterRunner();
		try {
			main.initialize();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		String args[] = {"amon", "Adult Red Dragon", "Dave"};
		main.addMonster(args);
		main.startCombat();
		
		String argsConSave[] = {"rollSave", "str"};
		String result = main.rollSave(argsConSave);
		String targetStr = "Dave rolls a strength saving throw of ";
		assertTrue(result.startsWith(targetStr));
		int sthrow = Integer.parseInt(result.substring(targetStr.length()));
		assertTrue(sthrow >= 7 && sthrow <= 27);
	}
	
	@Test
	void testReactionIntegrationTest() {
		EncounterRunner main = new EncounterRunner();
		try {
			main.initialize();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		String args[] = {"amon", "Adult Red Dragon", "Dave"};
		main.addMonster(args);
		String argsApc[] = {"apc", "Larry", "-1"};
		main.addPlayerCharacter(argsApc);
		main.startCombat();
		
		String badIdxArcs[] = {"react", "oppAtt", "2"};
		String response = main.takeReaction(badIdxArcs);
		assertEquals(response, "Invalid index");
		String badIdxArcs2[] = {"react", "oppAtt", "fart"};
		response = main.takeReaction(badIdxArcs2);
		assertEquals(response, "Need reactor index");
		
		String entityTest[] = {"react", "oppAtt", "1"};
		response = main.takeReaction(entityTest);
		assertEquals(response, "Only monsters can take reactions now");
		
		String oppAttTest[] = {"react", "oppAtt", "0"};
		response = main.takeReaction(oppAttTest);
		assertTrue(response.contains("Dave takes reaction"));
		assertTrue(response.contains("Available attacks for opportunity attack: Bite hits for "));
		assertTrue(response.contains("\r\nClaw hits for "));
		
		response = main.takeReaction(oppAttTest);
		assertEquals(response, "Dave takes reaction\r\n" + 
				"Dave cannot take reaction: oppAtt");
		
		response = main.advanceTurn();
		assertEquals(response, "Next in order: Larry\r\n" + 
				"Cannot list actions, entity is not managed by this tool\r\n" + 
				"Cannot list stats, entity is not managed by this tool\r\n" + 
				"[]");
		response = main.advanceTurn();
		assertTrue(response.contains("New round! Current round: 2\r\n" + 
				"Next in order: Dave\r\n" + 
				"Attack: 0 Bite hits for 2D6 + 0 Fire with 14 to hit and 2D10 + 8 Piercing with 14 to hit\r\n" + 
				"Attack: 1 Claw hits for 2D6 + 8 Slashing with 14 to hit\r\n" + 
				"Attack: 2 Claw hits for 2D6 + 8 Slashing with 14 to hit\r\n" + 
				"\r\n" + 
				"AC: 19\r\n" + 
				"Speed: 40\r\n") 
				);
		response = main.takeReaction(oppAttTest);
		assertTrue(response.contains("Dave takes reaction"));
		assertTrue(response.contains("Available attacks for opportunity attack: Bite hits for "));
		assertTrue(response.contains("\r\nClaw hits for "));
		
	}
	
	@Test
	void testLegendaryActions() {
		EncounterRunner main = new EncounterRunner();
		try {
			main.initialize();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		String args[] = {"amon", "Adult Red Dragon", "Dave"};
		main.addMonster(args);
		main.startCombat();
		
		//Guarentee Gary will have a lower initiative than the dragon
		String args2[] = {"apc", "Gary", "1"};
		String result = main.addPlayerCharacter(args2);
		assertEquals("Added Gary", result);
		
		String args3[] = {"listlact", "0"};
		result = main.listLegendaryActions(args3);
		assertTrue(result.contains("Charges for legendary actions: 3" + System.lineSeparator())); 
		assertTrue(result.contains("Name: Wing Attack (wing_attack) Daily charges: At will" + System.lineSeparator()));
		assertTrue(result.contains("Name: Tail Attack (tail_attack) Daily charges: At will" + System.lineSeparator()));
		assertTrue(result.contains("Name: Detect (detect) Daily charges: At will" + System.lineSeparator())); 
		assertTrue(result.contains("The creature makes a Wisdom (Perception) check." + System.lineSeparator()));
		
		String args4[] = {"lact", "0", "tail_attack"};
		result = main.doLegendaryAction(args4);
		assertTrue(result.contains("Tail Attack" + System.lineSeparator() + "Tail hits for "));
		assertTrue(result.contains(" Bludgeoning damage with a hit dice of "));
		assertFalse(result.contains("Against Gary"));
		
		result = main.listLegendaryActions(args3);
		assertTrue(result.contains("Charges for legendary actions: 2" + System.lineSeparator())); 
		assertTrue(result.contains("Name: Wing Attack (wing_attack) Daily charges: At will" + System.lineSeparator()));
		assertTrue(result.contains("Name: Tail Attack (tail_attack) Daily charges: At will" + System.lineSeparator()));
		assertTrue(result.contains("Name: Detect (detect) Daily charges: At will" + System.lineSeparator())); 
		assertTrue(result.contains("The creature makes a Wisdom (Perception) check." + System.lineSeparator()));
		
		String args5[] = {"lact", "0", "tail_attack", "1"};
		result = main.doLegendaryAction(args5);
		assertTrue(result.contains("Tail Attack" + System.lineSeparator() + "Tail hits for "));
		assertTrue(result.contains(" Bludgeoning damage with a hit dice of "));
		assertTrue(result.contains("against target Gary"));
		
		String args6[] = {"lact", "0", "wing_attack"};
		result = main.doLegendaryAction(args6);
		assertEquals("Insufficient charges for legendary action: wing_attack", result);
		
		
		main.shutdown();
	}
	
	@Test
	void testActions() {
		EncounterRunner main = new EncounterRunner();
		try {
			main.initialize();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		String args[] = {"amon", "Young Red Dragon", "Larry"};
		main.addMonster(args);
		main.startCombat();
		
		String stats = main.getAttrs(new String[] {"listAttr", "0"});
		assertEquals("AC: 18" + System.lineSeparator() + "Speed: 40" + System.lineSeparator() + "Attributes: N/A", stats);
		
		String listActResults = main.listActions();
		assertEquals(listActResults, "Name: Fire Breath (fire_breath) Daily charges: At will" + System.lineSeparator() + 
				"Rechargable spell, ready? true" + System.lineSeparator());
		
		String args3[] = {"act", "fire_breath"};
		String actResult = main.doAction(args3);
		assertTrue(actResult.contains("Fire Breath: The dragon exhales fire in a 30-foot cone. Each creature in that area must make a DC 17 Dexterity saving throw, taking 56 (16d6) fire damage on a failed save, or half as much damage on a successful one."));
	
		listActResults = main.listActions();
		assertEquals(listActResults, "Name: Fire Breath (fire_breath) Daily charges: At will" + System.lineSeparator() + 
				"Rechargable spell, ready? false" + System.lineSeparator());
		
		main.shutdown();
	}

	@Test
	void testLair() {
		EncounterRunner main = new EncounterRunner();
		try {
			main.initialize();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		String args[] = {"setlair", "100"};
		String message = main.setLair(args);
		assertEquals("Unable to use provided index: 100", message);
		
		String args2[] = {"setlair", "dave"};
		message = main.setLair(args2);
		assertEquals("Unable to use provided index: dave", message);
		
		String args3[] = {"setlair", "0"};
		message = main.setLair(args3);
		assertEquals("Added Lair: Demogorgon's Lair", message);
		
		main.startCombat();
		
		String args4[] = {"lairact", "0"};
		message = main.lairAct(args4);
		assertTrue(message.contains("Illusory Duplicate"));
		assertTrue(message.contains("The creature creates an illusory duplicate of himself, which appears in his own space and lasts until initiative count 20 of the next round. On his turn, the creature can move the illusory duplicate a distance equal to his walking speed (no action required). The first time a creature or object interacts physically with the creature (for example. hitting him with an attack). there is a 50 percent chance that it is the illusory duplicate that is being affected, not the creature himself, in which case the illusion disappears."));
		
		message = main.lairAct(args4);
		assertEquals("Cannot act this turn, already acted", message);
		main.shutdown();
	}
	
	@Test
	void testInnateSpellcast() {
		EncounterRunner main = new EncounterRunner();
		try {
			main.initialize();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		String args[] = {"amon", "Drow Mage", "Dave"};
		main.addMonster(args);
		
		main.startCombat();
		
		String args2[] = {"icast", "levitate"};
		String results = main.castInnateSpell(args2);
		assertTrue(results.contains("Levitate: One creature or loose object of your choice that you can see "));
		
		results = main.castInnateSpell(args2);
		assertEquals(results, "Cannot cast spell: No more charges for spell: levitate");
		
		String args3[] = {"icast", "darkness"};
		results = main.castInnateSpell(args3);
		assertEquals(results, "Cannot cast spell: Already concentrating on: Levitate");
		main.shutdown();
	}
	
	@Test
	void testLoadMonsters() {
		EncounterRunner main = new EncounterRunner();
		try {
			main.initialize();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		String args[] = {"bad command"};
		String response = main.loadMonsterSet(args);
		assertEquals(response, "lms <filename>");
		
		String args2[] = {"lms", "test_data" + File.separator + "monster_sets" + File.separator + "test_monsterset.csv"};
		response = main.loadMonsterSet(args2);
		assertEquals(response, "Loaded Successfully");
		
		assertTrue(main.currentInitiativeList.size() == 5);
		boolean foundDave = false;
		boolean foundLarry = false;
		boolean foundFred = false;
		boolean foundOlaf = false;
		boolean foundGreg = false;
		
		List<MonsterInstance> mis = new ArrayList<>();
		for(Entity ent : main.currentInitiativeList) {
			mis.add((MonsterInstance) ent);
		}
		
		for(MonsterInstance mi : mis) {
			if(mi.personalName.equals("Dave")) {
				assertTrue(mi.humanReadableName.equalsIgnoreCase("Fire Giant"));
				foundDave = true;
			}else if(mi.personalName.equals("Larry")) {
				assertTrue(mi.humanReadableName.equalsIgnoreCase("Hill Giant"));
				foundLarry = true;
			}else if(mi.personalName.equals("Fred")) {
				assertTrue(mi.humanReadableName.equalsIgnoreCase("Fire Giant"));
				foundFred = true;
			}else if(mi.personalName.equals("Olaf")) {
				assertTrue(mi.humanReadableName.equalsIgnoreCase("Mage"));
				foundOlaf = true;
			}else if(mi.personalName.equals("Greg")) {
				assertTrue(mi.humanReadableName.equalsIgnoreCase("Mage"));
				foundGreg = true;
			}else {
				fail("Unexpected entity: -" + mi.personalName+ "-");
			}
		}
		
		assertTrue(foundDave);
		assertTrue(foundLarry);
		assertTrue(foundFred);
		assertTrue(foundOlaf);
		assertTrue(foundGreg);
		main.shutdown();
	}
	
	@Test
	void testLogFile() {
		try {
			Files.deleteIfExists(Paths.get("log"));
		} catch (IOException e) {
			fail(e.getMessage());
		}
		
		EncounterRunner main = new EncounterRunner();
		try {
			main.initialize();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		String args00[] = {"amon", "Mage", "Danny Boy"};
		String response = main.addMonster(args00);
		assertTrue(response.startsWith("Added Mage as Danny Boy with initiative "));
		String args01[] = {"amon", "Mage", "Dave"};
		response = main.addMonster(args01);
		assertTrue(response.startsWith("Added Mage as Dave with initiative "));
		String args02[] = {"amon", "Mage", "Gary"};
		response = main.addMonster(args02);
		assertTrue(response.startsWith("Added Mage as Gary with initiative "));
		
		main.startCombat();
		
		String firstGuy = main.getCurrentEntity().personalName;
		
		String args[] = {"hit", "0", "34", "1"};
		response = main.hpMod(args);
		assertEquals(response, "Current HP: 6");
		
		String args3[] = {"heal", "0", "15", "1"};
		response = main.hpMod(args3);
		assertEquals(response, "Current HP: 21");
		
		String args4[] = {"at", "0", "1"};
		response = main.attack(args4);
		assertTrue(response.startsWith("Dagger (ranged 20/60 or melee) hits for "));
		
		String args5[] = {"cast", "cone_of_cold"};
		response = main.castSpell(args5);
		assertTrue(response.contains("Spell Save: 14"));
		
		main.shutdown();
		
		try {
			BufferedReader fr = new BufferedReader(new FileReader("log"));
			String next = fr.readLine();
			if(next == null) {
				fail("Expected another log line");
			}
			assertTrue(next.contains(firstGuy + " is hit by "));
			assertTrue(next.contains("for 34"));
			
			next = fr.readLine();
			if(next == null) {
				fail("Expected another log line");
			}
			assertEquals(next, firstGuy + " HP: 6");
			
			next = fr.readLine();
			if(next == null) {
				fail("Expected another log line");
			}
			assertTrue(next.contains(firstGuy + " is healed by "));
			assertTrue(next.contains("for 15"));
			
			next = fr.readLine();
			if(next == null) {
				fail("Expected another log line");
			}
			assertEquals(next, firstGuy + " HP: 21");
			
			next = fr.readLine();
			if(next == null) {
				fail("Expected another log line");
			}
			assertTrue(next.contains(firstGuy + " attacks "));
			
			next = fr.readLine();
			if(next == null) {
				fail("Expected another log line");
			}
			assertTrue(next.startsWith("Dagger (ranged 20/60 or melee) hits for "));
			
			next = fr.readLine();
			if(next == null) {
				fail("Expected another log line");
			}
			assertEquals(next, firstGuy + " cast Cone of Cold");
			
			next = fr.readLine();
			if(next == null) {
				fail("Expected another log line");
			}
			assertEquals(next, "Cone of Cold: A blast of cold air erupts from your hands. Each creature in a 60-foot cone must make a Constitution saving throw. A creature takes 8d8 cold damage on a failed save, or half as much damage on a successful one. A creature killed by this spell becomes a frozen statue until it thaws. At Higher Levels: When you cast this spell using a spell slot of 6th level or higher, the damage increases by 1d8 for each slot level above 5th.");
			
			next = fr.readLine();
			if(next == null) {
				fail("Expected another log line");
			}
			assertTrue(next.startsWith(" hits for "));
			assertTrue(next.contains("Cold damage"));
			
			next = fr.readLine();
			if(next == null) {
				fail("Expected another log line");
			}
			assertEquals(next, "Spell Save: 14");
			
			fr.close();
		} catch (FileNotFoundException e1) {
			fail(e1.getMessage());
		} catch (IOException e) {
			fail(e.getMessage());
		}
		
		
		
		try {
			Files.deleteIfExists(Paths.get("log"));
		} catch (IOException e) {
			fail(e.getMessage());
		}
		
	}

}
