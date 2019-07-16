package handy.rp.dnd;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class HelpersTest {

	@Test
	void test() {
		assertEquals(Helpers.getModifierFromAbility(21), 5);
		assertEquals(Helpers.getModifierFromAbility(8), -1);
		assertEquals(Helpers.getModifierFromAbility(19), 4);
		assertEquals(Helpers.getModifierFromAbility(5), -3);
		assertEquals(Helpers.getModifierFromAbility(9), -1);
		assertEquals(Helpers.getModifierFromAbility(6), -2);
	}

}
