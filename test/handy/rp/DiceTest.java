package handy.rp;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DiceTest {

	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void test() {
		for (int i = 0; i < 100; i++) {
			int d4 = Dice.d4();
			assertTrue(d4 > 0 && d4 < 5);
		}
		
		for (int i = 0; i < 100; i++) {
			int d6 = Dice.d6();
			assertTrue(d6 > 0 && d6 < 7);
		}
		
		for (int i = 0; i < 100; i++) {
			int d8 = Dice.d8();
			assertTrue(d8 > 0 && d8 < 9);
		}
		
		for (int i = 0; i < 100; i++) {
			int d10 = Dice.d10();
			assertTrue(d10 > 0 && d10 < 11);
		}
		
		for (int i = 0; i < 100; i++) {
			int d12 = Dice.d12();
			assertTrue(d12 > 0 && d12 < 13);
		}
		
		for (int i = 0; i < 100; i++) {
			int d20 = Dice.d20();
			assertTrue(d20 > 0 && d20 < 21);
		}
		
		for (int i = 0; i < 100; i++) {
			int d100 = Dice.d100();
			assertTrue(d100 > 0 && d100 < 101);
		}
		
		for (int i = 0; i < 100; i++) {
			int d4 = Dice.DICE_TYPE.D4.roll();
			assertTrue(d4 > 0 && d4 < 5);
		}
		
		for (int i = 0; i < 100; i++) {
			int d6 = Dice.DICE_TYPE.D6.roll();
			assertTrue(d6 > 0 && d6 < 7);
		}
		
		for (int i = 0; i < 100; i++) {
			int d8 = Dice.DICE_TYPE.D8.roll();
			assertTrue(d8 > 0 && d8 < 9);
		}
		
		for (int i = 0; i < 100; i++) {
			int d10 = Dice.DICE_TYPE.D10.roll();
			assertTrue(d10 > 0 && d10 < 11);
		}
		
		for (int i = 0; i < 100; i++) {
			int d12 = Dice.DICE_TYPE.D12.roll();
			assertTrue(d12 > 0 && d12 < 13);
		}
		
		for (int i = 0; i < 100; i++) {
			int d20 = Dice.DICE_TYPE.D20.roll();
			assertTrue(d20 > 0 && d20 < 21);
		}
		
		for (int i = 0; i < 100; i++) {
			int d100 = Dice.DICE_TYPE.D100.roll();
			assertTrue(d100 > 0 && d100 < 101);
		}
	}

}
