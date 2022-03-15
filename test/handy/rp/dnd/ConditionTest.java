package handy.rp.dnd;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import handy.rp.OutcomeNotification;
import handy.rp.dnd.EntityCondition.CONDITIONS;

class ConditionTest {

	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void baseFunctionTest() {
		CONDITIONS blinded = EntityCondition.getCondition("BLI");
		assertEquals(CONDITIONS.BLINDED, blinded);
		
		Entity ent = new Entity("Testy McTesterton");
		ent.addCondition(blinded);
		
		assertFalse(EntityCondition.hasAdvantageOnAttack(ent.getConditions()));
		assertTrue(EntityCondition.hasDisadvantageOnAttack(ent.getConditions()));
		assertTrue(EntityCondition.canAttack(ent.getConditions()));
		
		ent.removeConditions(CONDITIONS.BLINDED);
		
		assertFalse(EntityCondition.hasAdvantageOnAttack(ent.getConditions()));
		assertFalse(EntityCondition.hasDisadvantageOnAttack(ent.getConditions()));
		assertTrue(EntityCondition.canAttack(ent.getConditions()));
		
		CONDITIONS stunned = EntityCondition.getCondition("STUN");
		assertEquals(CONDITIONS.STUNNED, stunned);
		ent.addCondition(stunned);
		
		assertFalse(EntityCondition.hasAdvantageOnAttack(ent.getConditions()));
		assertFalse(EntityCondition.hasDisadvantageOnAttack(ent.getConditions()));
		assertFalse(EntityCondition.canAttack(ent.getConditions()));
	}

	@Test
	void mainLoopIntegrationTestDisadv() {
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
		
		String argsCon[] = {"addCon", "UNC"};
		main.addOrRemoveCondition(argsCon);
		
		String argsAt[] = {"at", "0"};
		OutcomeNotification outcome = main.attack(argsAt);
		assertEquals("Monster cannot attack in its current condition", outcome.humanMessage);
		assertFalse(outcome.outcome);
		
		String argsRm[] = {"rmCon", "UNC"};
		String result = main.addOrRemoveCondition(argsRm).humanMessage;
		assertEquals("Removing condition from Larry: UNCONSCIOUS", result);
		String argBli[] = {"addCon", "BLI"};
		result = main.addOrRemoveCondition(argBli).humanMessage;
		assertEquals("Adding condition to Larry: BLINDED", result);
		
		outcome = main.attack(argsAt);
		assertTrue(outcome.humanMessage.contains("disadvantage"));
		assertTrue(outcome.outcome);
	}
	
	@Test
	void mainLoopIntegrationTestAdv() {
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
		
		String argsAt[] = {"at", "0"};
		
		String argInv[] = {"addCon", "INV"};
		String result = main.addOrRemoveCondition(argInv).humanMessage;
		assertEquals("Adding condition to Larry: INVISIBLE", result);
		
		result = main.attack(argsAt).humanMessage;
		assertTrue(result.contains(" advantage"));
	}
}
