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
		Main main = new Main();
		try {
			main.initialize();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		String args[] = {"amon", "Hill Giant", "Danny Boy"};
		String response = main.addMonster(args);
		assertTrue(response.startsWith("Added Hill Giant as Danny Boy with initiative "));
		String args2[] = {"amon", "0", "Other Guy"};
		response = main.addMonster(args2);
		assertTrue(response.startsWith("Added Bandit Captain as Other Guy with initiative "));
		String args3[] = {"amon", "0", "Dude"};
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
	}
	
	@Test
	void testLoadMonsters() {
		Main main = new Main();
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
	}
	
	@Test
	void testLogFile() {
		try {
			Files.deleteIfExists(Paths.get("log"));
		} catch (IOException e) {
			fail(e.getMessage());
		}
		
		Main main = new Main();
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
			System.out.println(next);
			assertTrue(next.startsWith(" hits for "));
			assertTrue(next.contains("Cold damage"));
			
			next = fr.readLine();
			if(next == null) {
				fail("Expected another log line");
			}
			assertEquals(next, "Spell Save: 14");
			/*
			String next = fr.readLine();
			while(next != null) {
				System.out.println(next);
				next = fr.readLine();
			}
			*/
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
