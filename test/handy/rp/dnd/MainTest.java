package handy.rp.dnd;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
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
		System.out.println(response);
		assertTrue(response.startsWith("Added Bandit Captain as Other Guy with initiative "));
		
		String rmArgs[] = {"rm", "Other Guy"};
		response = main.rmEntity(rmArgs);
		assertTrue(response.startsWith("Removed: Other Guy"));
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

}
