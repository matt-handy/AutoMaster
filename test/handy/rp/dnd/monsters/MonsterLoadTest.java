package handy.rp.dnd.monsters;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import handy.rp.xml.MonsterParser;

class MonsterLoadTest {

	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testBadMonster() {
		try {
			List<MonsterTemplate> allLoaded = MonsterParser.loadAll("monsters");
			List<MonsterInstance> mis = MonsterSetLoader.getMonsterSet(allLoaded,
					"test_data" + File.separator + "monster_sets" + File.separator + "test_monsterset_bad.csv");
			fail("Why are we here? Should have errored out");
		}catch(IllegalArgumentException ex) {
			assertEquals(ex.getMessage(), "Non-existant monster given: Fake Giant");
		}catch(Exception ex) {
			fail(ex.getMessage());
		}
	}
	
	
	@Test
	void test() {
		try {
			List<MonsterTemplate> allLoaded = MonsterParser.loadAll("monsters");
			List<MonsterInstance> mis = MonsterSetLoader.getMonsterSet(allLoaded,
					"test_data" + File.separator + "monster_sets" + File.separator + "test_monsterset.csv");
			assertTrue(mis.size() == 5);
			boolean foundDave = false;
			boolean foundLarry = false;
			boolean foundFred = false;
			boolean foundOlaf = false;
			boolean foundGreg = false;
			
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
		} catch (Exception ex) {
			fail(ex.getMessage());
		}
	}

}
