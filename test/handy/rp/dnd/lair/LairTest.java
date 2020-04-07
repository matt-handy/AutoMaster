package handy.rp.dnd.lair;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import handy.rp.dnd.monsters.MonsterInstance;
import handy.rp.xml.LairParser;

class LairTest {

	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
	}
	
	@Test
	void testLair() {
		try {
			Lair demogorgonLair = LairParser.load(Paths.get("lairs", "demogorgon.xml").toString());
			String listActions = demogorgonLair.listAvailableActionsAttackSpells();
			assertTrue(listActions.contains("Actions Available: "));
			assertTrue(listActions.contains("Action: 0: Illusory Duplicate"));
			assertTrue(listActions.contains("Action: 1: Darkness"));
			
			String duplicateResult = demogorgonLair.expendAction(demogorgonLair.actions.get(0));
			assertTrue(duplicateResult.contains("Illusory Duplicate"));
			assertTrue(duplicateResult.contains("The creature creates an illusory duplicate of himself, which appears in his own space and lasts until initiative count 20 of the next round. On his turn, the creature can move the illusory duplicate a distance equal to his walking speed (no action required). The first time a creature or object interacts physically with the creature (for example. hitting him with an attack). there is a 50 percent chance that it is the illusory duplicate that is being affected, not the creature himself, in which case the illusion disappears."));
			
			listActions = demogorgonLair.listAvailableActionsAttackSpells();
			assertTrue(listActions.contains("Actions Available: "));
			assertFalse(listActions.contains("Action: 0: Illusory Duplicate"));
			assertTrue(listActions.contains("Action: 1: Darkness"));
			
			demogorgonLair.notifyNewTurn();
			
			listActions = demogorgonLair.listAvailableActionsAttackSpells();
			assertTrue(listActions.contains("Actions Available: "));
			assertFalse(listActions.contains("Action: 0: Illusory Duplicate"));
			assertTrue(listActions.contains("Action: 1: Darkness"));
			
			demogorgonLair.notifyNewTurn();
			
			listActions = demogorgonLair.listAvailableActionsAttackSpells();
			assertTrue(listActions.contains("Actions Available: "));
			assertTrue(listActions.contains("Action: 0: Illusory Duplicate"));
			assertTrue(listActions.contains("Action: 1: Darkness"));
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	void testLoadAll() {
		try {
			List<Lair> allLoaded = LairParser.loadAll("lairs");
			boolean foundDemogorgon = false;
			for(Lair lair : allLoaded) {
				if(lair.personalName.equals("Demogorgon's Lair")) {
					foundDemogorgon = true;
					assertEquals(2, lair.actions.size());
					boolean foundDarkness = false;
					boolean foundIllusion = false;
					for(LairAction la : lair.actions) {
						if(la.action.cname.equals("lair_darkness")) {
							foundDarkness = true;
							assertEquals(MonsterInstance.AT_WILL, la.chargeTotal);
							assertEquals(2, la.rechargeInterval);
						}else if(la.action.cname.equals("illusory_duplicate")) {
							foundIllusion = true;
							assertEquals(MonsterInstance.AT_WILL, la.chargeTotal);
							assertEquals(2, la.rechargeInterval);
						}
					}
					assertTrue(foundDarkness);
					assertTrue(foundIllusion);
				}
			}
			assertTrue(foundDemogorgon);
		}catch(Exception ex) {
			ex.printStackTrace();
			fail(ex.getMessage());
		}
	}

}
