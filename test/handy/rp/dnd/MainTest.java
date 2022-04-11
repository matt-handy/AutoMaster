package handy.rp.dnd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import handy.rp.OutcomeNotification;
import handy.rp.dnd.monsters.MonsterInstance;

class MainTest {

	@AfterEach
	void cleanupLog() {
		try {
			Files.deleteIfExists(Paths.get("log"));
		} catch (IOException e) {
			//e.printStackTrace();
		}
	}
	
	@Test
	void test() {
		EncounterRunner main = new EncounterRunner();
		try {
			main.initialize();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		String args[] = {"amon", "Hill Giant", "Danny Boy"};
		String response = main.addMonster(args).humanMessage;
		assertTrue(response.startsWith("Added Hill Giant as Danny Boy with initiative "));
		String args2[] = {"amon", "1", "Other Guy"};
		response = main.addMonster(args2).humanMessage;
		assertTrue(response.startsWith("Added Bandit Captain as Other Guy with initiative "));
		String args3[] = {"amon", "1", "Dude"};
		response = main.addMonster(args3).humanMessage;
		assertTrue(response.startsWith("Added Bandit Captain as Dude with initiative "));
		
		main.startCombat();
		
		String starter = main.getCurrentEntity().personalName;
		
		String rmArgs[] = {"rm", starter};
		response = main.rmEntity(rmArgs).humanMessage;
		assertTrue(response.startsWith("Removed: " + starter));
		
		assertFalse(main.getCurrentEntity().personalName.equals(starter));
		assertTrue(main.getCurrentEntity().personalName.equals("Other Guy") ||
				main.getCurrentEntity().personalName.equals("Dude") ||
				main.getCurrentEntity().personalName.equals("Danny Boy"));
		main.shutdown();
	}
	
	@Test
	//Originally all testing of the runEncounter loop was done by direct test of child methods
	//The main loop itself was not tested. This test was created to address some gaps
	//where validation logic and invocation of those child methods was not performed
	void testMainLoopCommandIntegration() {
		EncounterRunner main = new EncounterRunner();
		try {
			main.initialize();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		ByteArrayOutputStream cmdBuffer = new ByteArrayOutputStream();
		BufferedOutputStream bos = new BufferedOutputStream(cmdBuffer);
		PrintWriter builder = new PrintWriter(bos);
		builder.println("amon 0 Dave");
		builder.println("apc Gary -1");
		builder.println("apc Sven 99");
		builder.println("sc");
		//Test error handling on breaking concentration on spell - breakSpell
		builder.println("breakSpell");
		//Test listing of initiative - li
		builder.println("li");
		//Test naming of current entity - cur
		builder.println("cur");
		//Test stating of current HP - curHp
		builder.println("curhp");//Non-managed character
		builder.println("advturn");
		builder.println("breakSpell");//Test other error handling now that monster is in play
		builder.println("curhp");//Managed monster
		//gr - get round
		builder.println("gr");
		//lss - no spells
		builder.println("lss");
		//rollSave
		builder.println("rollSave");//Invalid command
		builder.println("rollSave str");//Assumes current monster, correct roll
		builder.println("rollSave str 2");//Directs at player, no roll possible
		//default - unknown command
		builder.println("Narf");
		builder.println("quit");
		builder.flush();

		BufferedReader br = new BufferedReader(
				new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
		cmdBuffer.reset();
		bos = new BufferedOutputStream(cmdBuffer);
		builder = new PrintWriter(bos);
		try {
			main.runEncounter(builder, br);
		} catch (IOException e) {
			fail(e.getMessage());
		}

		try {
			br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
			String line = br.readLine();
			assertTrue(line.startsWith("Added Adult Red Dragon as Dave with initiative "));
			line = br.readLine();
			assertEquals("Added Gary", line);
			line = br.readLine();
			assertEquals("Added Sven", line);
			assertEquals(br.readLine(),
					"First in order: Sven");
			
			line = br.readLine();
			assertEquals("Cannot list actions, entity is not managed by this tool", line);
			line = br.readLine();
			assertEquals("Can only break concentration for monsters", line);
			line = br.readLine();
			assertEquals("0 Sven 99", line);
			line = br.readLine();
			assertTrue(line.startsWith("1 Dave"));
			line = br.readLine();
			assertEquals("2 Gary -1", line);
			line = br.readLine();
			assertEquals("Current actor: Sven", line);
			line = br.readLine();
			assertEquals("Current actor does not have managed HP", line);
			//We advanced turn 
			
			line = br.readLine();
			assertEquals("Next in order: Dave", line);
			line = br.readLine();
			assertTrue(line.equals("Attack: 0 Bite hits for 2D6 + 0 Fire with 14 to hit and 2D10 + 8 Piercing with 14 to hit") ||
					line.equals("Attack: 0 Bite hits for 2D10 + 8 Piercing with 14 to hit and 2D6 + 0 Fire with 14 to hit"));
			line = br.readLine();
			assertEquals("Attack: 1 Claw hits for 2D6 + 8 Slashing with 14 to hit", line);
			line = br.readLine();
			assertEquals("Attack: 2 Claw hits for 2D6 + 8 Slashing with 14 to hit", line);
			line = br.readLine();
			assertEquals("", line);
			line = br.readLine();
			assertEquals("", line);
			line = br.readLine();
			assertEquals("AC: 19", line);
			line = br.readLine();
			assertEquals("Speed: 40", line);
			line = br.readLine();
			assertEquals("Attributes: Legendary Resistance (3/Day): If the Dragon fails a saving throw, it can choose to succeed instead.", line);
			line = br.readLine();
			assertEquals("Frightful Presence: Each creature of the dragon's choice that is within 120 ft. of the Dragon and aware of it must succeed on a DC 19 Wisdom saving throw or become Frightened for 1 minute. A creature can repeat the saving throw at the end of each of its turns, ending the Effect on itself on a success. If a creature's saving throw is successful or the Effect ends for it, the creature is immune to the dragon's Frightful Presence for the next 24 hours.", line);
			line = br.readLine();
			assertEquals("", line);
			line = br.readLine();
			assertEquals("", line);
			
			line = br.readLine();
			assertEquals("Monster was not concetrating on anything", line);
			line = br.readLine();
			assertEquals("Current HP: 256", line);
			line = br.readLine();
			assertEquals("Current round is: 1", line);
			line = br.readLine();
			assertEquals("", line);//Blank line for lss, monster has no slots remaining
			line = br.readLine();
			assertEquals("", line);//Blank line for lss, monster has no slots remaining
			line = br.readLine();
			assertEquals("rollSave <str|dex|con|int|wis|cha> <optional - entity index>", line);
			line = br.readLine();
			assertTrue(line.startsWith("Dave rolls a strength saving throw of "));
			line = br.readLine();
			assertEquals("Only monsters and managed players can roll saving throws for now", line);
			line = br.readLine();
			assertEquals("Unknown command: Narf", line);
		} catch (IOException ex) {
			fail(ex.getMessage());
		}
		
		//No log file validation performed here, log validation performed in child method tests
		
		main.shutdown();
	}
	
	@Test
	void testHpModification() {
		EncounterRunner main = new EncounterRunner();
		try {
			main.initialize();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		ByteArrayOutputStream cmdBuffer = new ByteArrayOutputStream();
		BufferedOutputStream bos = new BufferedOutputStream(cmdBuffer);
		PrintWriter builder = new PrintWriter(bos);
		builder.println("amon 0 Dave");
		builder.println("apc Gary -1");
		builder.println("sc");
		builder.println("hit 4");
		builder.println("hit 0 25");
		builder.println("heal 0 24 1");
		builder.println("quit");
		builder.flush();

		BufferedReader br = new BufferedReader(
				new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
		cmdBuffer.reset();
		bos = new BufferedOutputStream(cmdBuffer);
		builder = new PrintWriter(bos);
		try {
			main.runEncounter(builder, br);
		} catch (IOException e) {
			fail(e.getMessage());
		}

		try {
			br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
			String line = br.readLine();
			assertTrue(line.startsWith("Added Adult Red Dragon as Dave with initiative "));
			line = br.readLine();
			assertEquals("Added Gary", line);
			assertEquals(br.readLine(),
					"First in order: Dave");
			br.readLine();//Attack info
			br.readLine();//Attack info
			br.readLine();//Attack info
			br.readLine();//Attack info
			br.readLine();//Attack info
			line = br.readLine();
			assertEquals("hit <monster> <hp> <option - action taker index>", line);
			line = br.readLine();
			assertEquals("Current HP: 231", line);
			line = br.readLine();
			assertEquals("Current HP: 255", line);
		} catch (IOException ex) {
			fail(ex.getMessage());
		}
		
		try {
			BufferedReader fr = new BufferedReader(new FileReader("log"));
			String next = fr.readLine();
			assertEquals("Dave is hit for 25", next);
			next = fr.readLine();
			assertEquals("Dave HP: 231", next);
			next = fr.readLine();
			assertEquals("Dave is healed by Gary for 24", next);
			next = fr.readLine();
			assertEquals("Dave HP: 255", next);
			fr.close();
		} catch (FileNotFoundException e1) {
			fail(e1.getMessage());
		} catch (IOException e) {
			fail(e.getMessage());
		}
		main.shutdown();
	}
	
	@Test
	void testSavingThrowSinglePlayerCommonCodeTestWithLogging() {
		EncounterRunner main = new EncounterRunner();
		try {
			main.initialize();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		ByteArrayOutputStream cmdBuffer = new ByteArrayOutputStream();
		BufferedOutputStream bos = new BufferedOutputStream(cmdBuffer);
		PrintWriter builder = new PrintWriter(bos);
		builder.println("amon 0 Dave");
		builder.println("sc");
		for(int idx = 0; idx < 100; idx++) {
		builder.println("rollSave str");
		builder.println("rollSave dex");
		}
		builder.println("quit");
		builder.flush();

		BufferedReader br = new BufferedReader(
				new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
		cmdBuffer.reset();
		bos = new BufferedOutputStream(cmdBuffer);
		builder = new PrintWriter(bos);
		try {
			main.runEncounter(builder, br);
		} catch (IOException e) {
			fail(e.getMessage());
		}

		try {
			br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
			assertTrue(br.readLine().startsWith("Added Adult Red Dragon as Dave with initiative "));
			assertEquals(br.readLine(),
					"First in order: Dave");
			br.readLine();//Attack info
			br.readLine();//Attack info
			br.readLine();//Attack info
			br.readLine();//Attack info
			br.readLine();//Attack info
			//br.readLine();//Attack info
			for(int idx = 0; idx < 100; idx++) {
			String strThrow = br.readLine();
			String strStart = "Dave rolls a strength saving throw of ";
			assertTrue(strThrow.startsWith(strStart));
			int throwVal = Integer.parseInt(strThrow.substring(strStart.length()));
			assertTrue(throwVal >= 1 + 7 && throwVal <= 20+7 );
			
			String dexThrow = br.readLine();
			String dexStart = "Dave rolls a dexterity saving throw of ";
			assertTrue(dexThrow.startsWith(dexStart));
			throwVal = Integer.parseInt(dexThrow.substring(dexStart.length()));
			assertTrue(throwVal >= 1 + 6 && throwVal <= 20+6 );
			}
		} catch (IOException ex) {
			fail(ex.getMessage());
		}
		
		try {
			BufferedReader fr = new BufferedReader(new FileReader("log"));
			String next = fr.readLine();
			assertTrue(next.startsWith("Dave rolls a strength saving throw of "));
			next = fr.readLine();
			assertTrue(next.startsWith("Dave rolls a dexterity saving throw of "));
			fr.close();
		} catch (FileNotFoundException e1) {
			fail(e1.getMessage());
		} catch (IOException e) {
			fail(e.getMessage());
		}
		main.shutdown();
	}
	
	@Test
	void testConSaveOnTakingDamageConcentrationTest() {
		EncounterRunner main = new EncounterRunner();
		try {
			main.initialize();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		ByteArrayOutputStream cmdBuffer = new ByteArrayOutputStream();
		BufferedOutputStream bos = new BufferedOutputStream(cmdBuffer);
		PrintWriter builder = new PrintWriter(bos);
		builder.println("amon mage Dave");
		builder.println("sc");
		builder.println("cast greater_invisibility");
		builder.println("hit 0 45");
		builder.println("quit");
		builder.flush();

		BufferedReader br = new BufferedReader(
				new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
		cmdBuffer.reset();
		bos = new BufferedOutputStream(cmdBuffer);
		builder = new PrintWriter(bos);
		try {
			main.runEncounter(builder, br);
		} catch (IOException e) {
			fail(e.getMessage());
		}

		try {
			br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
			assertTrue(br.readLine().startsWith("Added Mage as Dave with initiative "));
			assertEquals(br.readLine(),
					"First in order: Dave");
			br.readLine();//Attack info
			br.readLine();//Attack info
			br.readLine();//Attack info
			br.readLine();//Attack info
			br.readLine();//Attack info
			br.readLine();//Attack info
			br.readLine();//Attack info
			br.readLine();//Attack info
			br.readLine();//Attack info
			assertEquals("Greater Invisibility:     You or a creature you touch becomes invisible until the spell ends. Anything the target is wearing or carrying is invisible as long as it is on the target’s person.", br.readLine());
			br.readLine();//Blank after spell
			br.readLine();//Blank after spell
			String failConMessage = br.readLine();
			assertTrue(failConMessage.startsWith("Dave rolled a CON save of "));
			assertTrue(failConMessage.endsWith(" against a target of 22 and failed. They are no longer concentrating on a spell."));
			assertEquals("Current HP: -5", br.readLine());
			
		} catch (IOException ex) {
			fail(ex.getMessage());
		}
		main.shutdown();
	}
	
	
	@Test
	void savingThrowTest() {
		EncounterRunner main = new EncounterRunner();
		try {
			main.initialize();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		String args[] = {"amon", "Adult Red Dragon", "Dave"};
		main.addMonster(args);
		main.startCombat();
		
		String argsConSave[] = {"rollSave", "str"};
		OutcomeNotification notice = main.rollSave(argsConSave);
		String result = notice.humanMessage;
		assertTrue(notice.outcome);
		String targetStr = "Dave rolls a strength saving throw of ";
		assertTrue(result.startsWith(targetStr));
		int sthrow = Integer.parseInt(result.substring(targetStr.length()));
		assertTrue(sthrow >= 7 && sthrow <= 27);
		main.shutdown();
	}
	
	@Test
	void testReactionIntegrationTest() {
		EncounterRunner main = new EncounterRunner();
		try {
			main.initialize();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		String args[] = {"amon", "Adult Red Dragon", "Dave"};
		main.addMonster(args);
		String argsApc[] = {"apc", "Larry", "-1"};
		main.addPlayerCharacter(argsApc);
		main.startCombat();
		
		String badIdxArcs[] = {"react", "oppAtt", "2"};
		OutcomeNotification response = main.takeReaction(badIdxArcs);
		assertEquals(response.humanMessage, "Invalid index");
		assertFalse(response.outcome);
		
		String badIdxArcs2[] = {"react", "oppAtt", "fart"};
		response = main.takeReaction(badIdxArcs2);
		assertEquals(response.humanMessage, "Need reactor index");
		assertFalse(response.outcome);
		
		String entityTest[] = {"react", "oppAtt", "1"};
		response = main.takeReaction(entityTest);
		assertEquals(response.humanMessage, "Only monsters can take reactions now");
		assertFalse(response.outcome);
		
		String oppAttTest[] = {"react", "oppAtt", "0"};
		response = main.takeReaction(oppAttTest);
		assertTrue(response.outcome);
		assertTrue(response.humanMessage.contains("Dave takes reaction"));
		assertTrue(response.humanMessage.contains("Available attacks for opportunity attack: Bite hits for "));
		assertTrue(response.humanMessage.contains("\r\nClaw hits for "));
		
		response = main.takeReaction(oppAttTest);
		assertEquals(response.humanMessage, "Dave takes reaction\r\n" + 
				"Dave cannot take reaction: oppAtt");
		
		String responseStr = main.advanceTurn();
		assertEquals(responseStr, "Next in order: Larry\r\n" + 
				"Cannot list actions, entity is not managed by this tool\r\n" + 
				"Cannot list stats, entity is not managed by this tool\r\n");
		responseStr = main.advanceTurn();
		String elements[] = responseStr.split(System.lineSeparator());
		assertEquals("New round! Current round: 2", elements[0]);
		assertEquals("Next in order: Dave", elements[1]);
		assertTrue(elements[2].contains("2D6 + 0 Fire with 14 to hit") &&
				elements[2].contains("2D10 + 8 Piercing with 14 to hit"));
		assertEquals("Attack: 1 Claw hits for 2D6 + 8 Slashing with 14 to hit", elements[3]);
		assertEquals("Attack: 2 Claw hits for 2D6 + 8 Slashing with 14 to hit", elements[4]);
		assertEquals("AC: 19", elements[7]);
		assertEquals("Speed: 40", elements[8]);
		response = main.takeReaction(oppAttTest);
		assertTrue(response.humanMessage.contains("Dave takes reaction"));
		assertTrue(response.humanMessage.contains("Available attacks for opportunity attack: Bite hits for "));
		assertTrue(response.humanMessage.contains("\r\nClaw hits for "));
		main.shutdown();
	}
	
	@Test
	void testLegendaryActions() {
		EncounterRunner main = new EncounterRunner();
		try {
			main.initialize();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		String args[] = {"amon", "Adult Red Dragon", "Dave"};
		main.addMonster(args);
		main.startCombat();
		
		//Guarentee Gary will have a lower initiative than the dragon
		String args2[] = {"apc", "Gary", "1"};
		String result = main.addPlayerCharacter(args2).humanMessage;
		assertEquals("Added Gary", result);
		
		String args3[] = {"listlact", "0"};
		result = main.listLegendaryActions(args3);
		assertTrue(result.contains("Charges for legendary actions: 3" + System.lineSeparator())); 
		assertTrue(result.contains("Name: Wing Attack (wing_attack) Daily charges: At will" + System.lineSeparator()));
		assertTrue(result.contains("Name: Tail Attack (tail_attack) Daily charges: At will" + System.lineSeparator()));
		assertTrue(result.contains("Name: Detect (detect) Daily charges: At will" + System.lineSeparator())); 
		assertTrue(result.contains("The creature makes a Wisdom (Perception) check." + System.lineSeparator()));
		
		String args4[] = {"lact", "0", "tail_attack"};
		result = main.doLegendaryAction(args4).humanMessage;
		assertTrue(result.contains("Tail Attack" + System.lineSeparator() + "Tail hits for "));
		assertTrue(result.contains(" Bludgeoning damage with a hit dice of "));
		assertFalse(result.contains("Against Gary"));
		
		result = main.listLegendaryActions(args3);
		assertTrue(result.contains("Charges for legendary actions: 2" + System.lineSeparator())); 
		assertTrue(result.contains("Name: Wing Attack (wing_attack) Daily charges: At will" + System.lineSeparator()));
		assertTrue(result.contains("Name: Tail Attack (tail_attack) Daily charges: At will" + System.lineSeparator()));
		assertTrue(result.contains("Name: Detect (detect) Daily charges: At will" + System.lineSeparator())); 
		assertTrue(result.contains("The creature makes a Wisdom (Perception) check." + System.lineSeparator()));
		
		String args5[] = {"lact", "0", "tail_attack", "1"};
		result = main.doLegendaryAction(args5).humanMessage;
		assertTrue(result.contains("Tail Attack" + System.lineSeparator() + "Tail hits for "));
		assertTrue(result.contains(" Bludgeoning damage with a hit dice of "));
		assertTrue(result.contains("against target Gary"));
		
		String args6[] = {"lact", "0", "wing_attack"};
		OutcomeNotification resultNotice = main.doLegendaryAction(args6);
		assertEquals("Insufficient charges for legendary action: wing_attack", resultNotice.humanMessage);
		assertFalse(resultNotice.outcome);
		
		
		main.shutdown();
	}
	
	@Test
	void testActions() {
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
		
		String stats = main.getAttrs(new String[] {"listAttr", "0"});
		assertEquals("AC: 18" + System.lineSeparator() + "Speed: 40" + System.lineSeparator() + "Attributes: N/A", stats);
		
		String listActResults = main.listActions();
		assertEquals(listActResults, "Name: Fire Breath (fire_breath) Daily charges: At will" + System.lineSeparator() + 
				"Rechargable spell, ready? true" + System.lineSeparator());
		
		String args3[] = {"act", "fire_breath"};
		OutcomeNotification notification = main.doAction(args3);
		String actResult = notification.humanMessage;
		assertTrue(actResult.contains("Fire Breath: The dragon exhales fire in a 30-foot cone. Each creature in that area must make a DC 17 Dexterity saving throw, taking 56 (16d6) fire damage on a failed save, or half as much damage on a successful one."));
	
		listActResults = main.listActions();
		assertEquals(listActResults, "Name: Fire Breath (fire_breath) Daily charges: At will" + System.lineSeparator() + 
				"Rechargable spell, ready? false" + System.lineSeparator());
		
		main.shutdown();
	}

	@Test
	void testLair() {
		EncounterRunner main = new EncounterRunner();
		try {
			main.initialize();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		String args[] = {"setlair", "100"};
		OutcomeNotification message = main.setLair(args);
		assertEquals("Unable to use provided index: 100", message.humanMessage);
		assertFalse(message.outcome);
		
		String args2[] = {"setlair", "dave"};
		message = main.setLair(args2);
		assertEquals("Unable to use provided index: dave", message.humanMessage);
		assertFalse(message.outcome);
		
		String args3[] = {"setlair", "0"};
		message = main.setLair(args3);
		assertEquals("Added Lair: Demogorgon's Lair", message.humanMessage);
		assertTrue(message.outcome);
		
		main.startCombat();
		
		String args4[] = {"lairact", "0"};
		message = main.lairAct(args4);
		assertTrue(message.humanMessage.contains("Illusory Duplicate"));
		assertTrue(message.humanMessage.contains("The creature creates an illusory duplicate of himself, which appears in his own space and lasts until initiative count 20 of the next round. On his turn, the creature can move the illusory duplicate a distance equal to his walking speed (no action required). The first time a creature or object interacts physically with the creature (for example. hitting him with an attack). there is a 50 percent chance that it is the illusory duplicate that is being affected, not the creature himself, in which case the illusion disappears."));
		assertTrue(message.outcome);
		
		message = main.lairAct(args4);
		assertEquals("Cannot act this turn, already acted", message.humanMessage);
		assertFalse(message.outcome);
		main.shutdown();
	}
	
	@Test
	void testInnateSpellcast() {
		EncounterRunner main = new EncounterRunner();
		try {
			main.initialize();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		String args[] = {"amon", "Drow Mage", "Dave"};
		main.addMonster(args);
		
		main.startCombat();
		
		String args2[] = {"icast", "levitate"};
		OutcomeNotification results = main.castInnateSpell(args2);
		assertTrue(results.humanMessage.contains("Levitate: One creature or loose object of your choice that you can see "));
		assertTrue(results.outcome);
		
		results = main.castInnateSpell(args2);
		assertEquals(results.humanMessage, "Cannot cast spell: No more charges for spell: levitate");
		assertFalse(results.outcome);
		
		String args3[] = {"icast", "darkness"};
		results = main.castInnateSpell(args3);
		assertEquals(results.humanMessage, "Cannot cast spell: Already concentrating on: Levitate");
		assertFalse(results.outcome);
		main.shutdown();
	}
	
	@Test
	void testMonsterCastHealSpell() {
		EncounterRunner main = new EncounterRunner();
		try {
			main.initialize();
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		ByteArrayOutputStream cmdBuffer = new ByteArrayOutputStream();
		BufferedOutputStream bos = new BufferedOutputStream(cmdBuffer);
		PrintWriter builder = new PrintWriter(bos);
		builder.println("amon priest Dave");
		builder.println("sc");
		builder.println("cast cure_wounds");
		builder.println("quit");
		builder.flush();

		BufferedReader br = new BufferedReader(
				new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
		cmdBuffer.reset();
		bos = new BufferedOutputStream(cmdBuffer);
		builder = new PrintWriter(bos);
		try {
			main.runEncounter(builder, br);
		} catch (IOException e) {
			fail(e.getMessage());
		}

		try {
			br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cmdBuffer.toByteArray())));
			assertTrue(br.readLine().startsWith("Added Priest as Dave with initiative "));
			assertEquals(br.readLine(),
					"First in order: Dave");
			br.readLine();//Attack info
			br.readLine();//Attack info
			br.readLine();//Attack info
			br.readLine();//Attack info
			br.readLine();//Attack info
			br.readLine();//Attack info
			br.readLine();//Attack info
			assertEquals("Cure Wounds: ", br.readLine());
			assertEquals("A creature you touch regains a number of hit points equal to 1d8 + your spellcasting ability modifier. This spell has no effect on undead or constructs.", br.readLine());
			assertEquals("At Higher Levels. When you cast this spell using a spell slot of 2nd level or higher, the healing increases by 1d8 for each slot level above 1st.", br.readLine());
			assertEquals("", br.readLine());
			String healedForMessage = "Healed for : ";
			String message = br.readLine();
			assertTrue(message.startsWith(healedForMessage));
			int healVal = Integer.parseInt(message.substring(healedForMessage.length()));
			assertTrue(healVal <= 8 + 3 && healVal >= 1 + 3);
		} catch (IOException ex) {
			fail(ex.getMessage());
		}
		main.shutdown();
	}
	
	@Test
	void testLoadMonsters() {
		EncounterRunner main = new EncounterRunner();
		try {
			main.initialize();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		String args[] = {"bad command"};
		OutcomeNotification responseNotice = main.loadMonsterSet(args);
		assertEquals(responseNotice.humanMessage, "lms <filename>");
		assertFalse(responseNotice.outcome);
		
		String args2[] = {"lms", "test_data" + File.separator + "monster_sets" + File.separator + "test_monsterset.csv"};
		responseNotice = main.loadMonsterSet(args2);
		assertEquals(responseNotice.humanMessage, "Loaded Successfully");
		assertTrue(responseNotice.outcome);
		
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
		main.shutdown();
	}
	
	@Test
	void testLogFile() {
		//TODO: Replace this test with a better comprehensive test of the logging system that actually works
		/*
		EncounterRunner main = new EncounterRunner();
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
			assertTrue(next.startsWith(" hits for "));
			assertTrue(next.contains("Cold damage"));
			
			next = fr.readLine();
			if(next == null) {
				fail("Expected another log line");
			}
			assertEquals(next, "Spell Save: 14");
			
			fr.close();
		} catch (FileNotFoundException e1) {
			fail(e1.getMessage());
		} catch (IOException e) {
			fail(e.getMessage());
		}
		
		*/
	}

}
