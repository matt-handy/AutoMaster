package handy.rp.dnd;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
		assertTrue(response.startsWith("Added Fire Giant as Other Guy with initiative "));
		
		String rmArgs[] = {"rm", "Other Guy"};
		response = main.rmMonster(rmArgs);
		assertTrue(response.startsWith("Removed: Other Guy"));
	}

}
