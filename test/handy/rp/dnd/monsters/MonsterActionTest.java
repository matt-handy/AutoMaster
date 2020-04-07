package handy.rp.dnd.monsters;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import handy.rp.xml.MonsterParser;

class MonsterActionTest {

	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void test() {
		try {
			List<MonsterTemplate> allLoaded = MonsterParser.loadAll("monsters");
			boolean foundDeathKnight = false;
			boolean foundDrow = false;
			boolean foundYRD = false;
			
			for(MonsterTemplate mt : allLoaded) {
				if(mt.humanReadableName.contentEquals("Death Knight")) {
					foundDeathKnight = true;
					MonsterInstance dk = mt.getInstance("Arthas");
					String result = dk.expendAction("nonaction");
					assertEquals("No such action: nonaction", result);
					result = dk.expendAction("hellfire_orb");
					assertTrue(result.contains("Spell Save: 18"));
					assertTrue(result.contains("Fire damage"));
					assertTrue(result.contains("Hellfire Orb: The death knight hurls a magical ball of fire that explodes at a point it can see within 120 feet of it. Each creature in a 20-foot-radius sphere centered on that point must make a DC 18 Dexterity saving throw. The sphere spreads around corners. A creature takes 35 (10d6) fire damage and 35 (10d6) necrotic damage on a failed save, or half as much damage on a successful one."));
					result = dk.expendAction("hellfire_orb");
					assertEquals("Cannot take action, already acted this turn", result);
					dk.notifyNewTurn();
					result = dk.expendAction("hellfire_orb");
					assertEquals("No remaining charges for action", result);
				}else if(mt.humanReadableName.contentEquals("Drow Mage")) {
					foundDrow = true;
					MonsterInstance dk = mt.getInstance("Magic Brian");
					String result = dk.expendAction("nonaction");
					assertEquals("No such action: nonaction", result);
					result = dk.expendAction("summon_demon");
					assertTrue(result.contains("The drow magically summons a quasit, or attempts to summon a shadow demon with a 50 percent chance of success. The summoned demon appears in an unoccupied space within 60 feet of its summoner, acts as an ally of its summoner, and can't summon other demons. It remains for 10 minutes, until it or its summoner dies, or until its summoner dismisses it as an action."));
					dk.notifyNewTurn();
					result = dk.expendAction("summon_demon");
					assertEquals("No remaining charges for action", result);
				}else if(mt.humanReadableName.contentEquals("Young Red Dragon")) {
					foundYRD = true;
					MonsterInstance dk = mt.getInstance("Young Red Dragon");String result = dk.expendAction("nonaction");
					assertEquals("No such action: nonaction", result);
					result = dk.expendAction("fire_breath");
					assertTrue(result.contains("Fire Breath: The dragon exhales fire in a 30-foot cone. Each creature in that area must make a DC 17 Dexterity saving throw, taking 56 (16d6) fire damage on a failed save, or half as much damage on a successful one."));
					dk.notifyNewTurn();
					result = dk.expendAction("fire_breath");
					assertEquals("Rechargable Action - Need 5 or better from D6", result);
				}
			}
			
			assertTrue(foundDeathKnight);
			assertTrue(foundDrow);
		} catch (Exception ex) {
			fail(ex.getMessage());
		}
	}

}
