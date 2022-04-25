package handy.rp.fortyk;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import org.junit.jupiter.api.Test;

class BattleRunnerTest {

	@Test
	void testLoadNonexistentArmy() {
		BattleRunner battleRunner = new BattleRunner();

		ByteArrayOutputStream cmdBuffer = new ByteArrayOutputStream();
		BufferedOutputStream bos = new BufferedOutputStream(cmdBuffer);
		PrintWriter builder = new PrintWriter(bos);
		builder.println("loadarmy Not An Army");
		builder.println("quit");
		builder.flush();

		BufferedReader br = new BufferedReader(
				new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
		cmdBuffer.reset();
		bos = new BufferedOutputStream(cmdBuffer);
		builder = new PrintWriter(bos);

		battleRunner.mainGameLoop(builder, br);

		try {
			br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
			// Test Spell list
			assertEquals(br.readLine(), "Unknown army: Not An Army");
		} catch (IOException ex) {
			fail(ex.getMessage());
		}
	}

	@Test
	void testWoundsAppliedToBoyz() {
		BattleRunner battleRunner = new BattleRunner();

		ByteArrayOutputStream cmdBuffer = new ByteArrayOutputStream();
		BufferedOutputStream bos = new BufferedOutputStream(cmdBuffer);
		PrintWriter builder = new PrintWriter(bos);
		builder.println("loadarmy Trukk Boyz Test");
		builder.println("takewounds Boyz 5 1 -1");
		builder.println("takewounds Boyz 6 1 -1");
		builder.println("takewounds Boyz 1 1 -1");
		builder.println("takewounds Boyz 1 1 -1");
		builder.println("takewounds Boyz 1 1 -1");
		builder.println("quit");
		builder.flush();

		BufferedReader br = new BufferedReader(
				new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
		cmdBuffer.reset();
		bos = new BufferedOutputStream(cmdBuffer);
		builder = new PrintWriter(bos);

		battleRunner.mainGameLoop(builder, br);

		try {
			br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
			assertEquals(br.readLine(), "Army loaded!");
			assertEquals(br.readLine(), "Unit takes the wound and a model dies.");
			assertEquals(br.readLine(), "Unit takes the wound and a model dies.");
			assertEquals(br.readLine(), "Unit takes the wound and a model dies.");
			assertEquals(br.readLine(), "Unit takes the wound and a model dies.");
			assertEquals(br.readLine(), "Unit takes the wound and a model dies.");
			// Second Wounding
			assertEquals(br.readLine(), "Unit takes the wound and a model dies.");
			assertEquals(br.readLine(), "Unit takes the wound and a model dies.");
			assertEquals(br.readLine(), "Unit takes the wound and a model dies.");
			assertEquals(br.readLine(), "Unit takes the wound and a model dies.");
			assertEquals(br.readLine(), "Unit takes the wound and a model dies.");
			assertEquals(br.readLine(), "Unit leader takes the wound.");
			assertEquals(br.readLine(), "Unit leader takes the wound and dies.");
			assertEquals(br.readLine(), "Boyz is no longer combat effective after wound.");
			// Test response to additional wound attempt;
			assertEquals(br.readLine(), "Unit no longer combat effective");
		} catch (IOException ex) {
			fail(ex.getMessage());
		}
	}
	
	@Test
	void testTrukkStatsDegradedWithTakingWounds() {
		BattleRunner battleRunner = new BattleRunner();

		ByteArrayOutputStream cmdBuffer = new ByteArrayOutputStream();
		BufferedOutputStream bos = new BufferedOutputStream(cmdBuffer);
		PrintWriter builder = new PrintWriter(bos);
		builder.println("loadarmy Trukk Boyz Test");
		builder.println("getmovement Trukk");
		builder.println("takewounds Trukk 1 6 -4");
		builder.println("getmovement Trukk");
		builder.println("takewounds Trukk 1 3 -4");
		builder.println("getmovement Trukk");
		builder.println("takewounds Trukk 1 1 -4");
		builder.println("quit");
		builder.flush();

		BufferedReader br = new BufferedReader(
				new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
		cmdBuffer.reset();
		bos = new BufferedOutputStream(cmdBuffer);
		builder = new PrintWriter(bos);

		battleRunner.mainGameLoop(builder, br);

		try {
			br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
			assertEquals(br.readLine(), "Army loaded!");
			assertEquals(br.readLine(), "Trukk: 12");
			assertEquals(br.readLine(), "Unit takes the wound.");
			assertEquals(br.readLine(), "Trukk: 8");
			assertEquals(br.readLine(), "Unit takes the wound.");
			assertEquals(br.readLine(), "Trukk: 6");
			assertEquals(br.readLine(), "Unit takes the wound and a model dies.");
			assertEquals(br.readLine(), "Trukk is no longer combat effective after wound.");
		} catch (IOException ex) {
			fail(ex.getMessage());
		}
	}

}
