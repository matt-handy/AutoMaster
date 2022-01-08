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
		String response = main.addMonster(args);
		assertTrue(response.startsWith("Added Hill Giant as Danny Boy with initiative "));
		String args2[] = {"amon", "1", "Other Guy"};
		response = main.addMonster(args2);
		assertTrue(response.startsWith("Added Bandit Captain as Other Guy with initiative "));
		String args3[] = {"amon", "1", "Dude"};
		response = main.addMonster(args3);
		assertTrue(response.startsWith("Added Bandit Captain as Dude with initiative "));
		
		main.startCombat();
		
		String starter = main.getCurrentEntity().personalName;
		
		String rmArgs[] = {"rm", starter};
		response = main.rmEntity(rmArgs);
		assertTrue(response.startsWith("Removed: " + starter));
		
		assertFalse(main.getCurrentEntity().personalName.equals(starter));
		assertTrue(main.getCurrentEntity().personalName.equals("Other Guy") ||
				main.getCurrentEntity().personalName.equals("Dude") ||
				main.getCurrentEntity().personalName.equals("Danny Boy"));
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
		String result = main.rollSave(argsConSave);
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
		String response = main.takeReaction(badIdxArcs);
		assertEquals(response, "Invalid index");
		String badIdxArcs2[] = {"react", "oppAtt", "fart"};
		response = main.takeReaction(badIdxArcs2);
		assertEquals(response, "Need reactor index");
		
		String entityTest[] = {"react", "oppAtt", "1"};
		response = main.takeReaction(entityTest);
		assertEquals(response, "Only monsters can take reactions now");
		
		String oppAttTest[] = {"react", "oppAtt", "0"};
		response = main.takeReaction(oppAttTest);
		assertTrue(response.contains("Dave takes reaction"));
		assertTrue(response.contains("Available attacks for opportunity attack: Bite hits for "));
		assertTrue(response.contains("\r\nClaw hits for "));
		
		response = main.takeReaction(oppAttTest);
		assertEquals(response, "Dave takes reaction\r\n" + 
				"Dave cannot take reaction: oppAtt");
		
		response = main.advanceTurn();
		assertEquals(response, "Next in order: Larry\r\n" + 
				"Cannot list actions, entity is not managed by this tool\r\n" + 
				"Cannot list stats, entity is not managed by this tool\r\n" + 
				"[]");
		response = main.advanceTurn();
		String elements[] = response.split(System.lineSeparator());
		assertEquals("New round! Current round: 2", elements[0]);
		assertEquals("Next in order: Dave", elements[1]);
		assertTrue(elements[2].contains("2D6 + 0 Fire with 14 to hit") &&
				elements[2].contains("2D10 + 8 Piercing with 14 to hit"));
		assertEquals("Attack: 1 Claw hits for 2D6 + 8 Slashing with 14 to hit", elements[3]);
		assertEquals("Attack: 2 Claw hits for 2D6 + 8 Slashing with 14 to hit", elements[4]);
		assertEquals("AC: 19", elements[7]);
		assertEquals("Speed: 40", elements[8]);
		response = main.takeReaction(oppAttTest);
		assertTrue(response.contains("Dave takes reaction"));
		assertTrue(response.contains("Available attacks for opportunity attack: Bite hits for "));
		assertTrue(response.contains("\r\nClaw hits for "));
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
		String result = main.addPlayerCharacter(args2);
		assertEquals("Added Gary", result);
		
		String args3[] = {"listlact", "0"};
		result = main.listLegendaryActions(args3);
		assertTrue(result.contains("Charges for legendary actions: 3" + System.lineSeparator())); 
		assertTrue(result.contains("Name: Wing Attack (wing_attack) Daily charges: At will" + System.lineSeparator()));
		assertTrue(result.contains("Name: Tail Attack (tail_attack) Daily charges: At will" + System.lineSeparator()));
		assertTrue(result.contains("Name: Detect (detect) Daily charges: At will" + System.lineSeparator())); 
		assertTrue(result.contains("The creature makes a Wisdom (Perception) check." + System.lineSeparator()));
		
		String args4[] = {"lact", "0", "tail_attack"};
		result = main.doLegendaryAction(args4);
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
		result = main.doLegendaryAction(args5);
		assertTrue(result.contains("Tail Attack" + System.lineSeparator() + "Tail hits for "));
		assertTrue(result.contains(" Bludgeoning damage with a hit dice of "));
		assertTrue(result.contains("against target Gary"));
		
		String args6[] = {"lact", "0", "wing_attack"};
		result = main.doLegendaryAction(args6);
		assertEquals("Insufficient charges for legendary action: wing_attack", result);
		
		
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
		String actResult = main.doAction(args3);
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
		String message = main.setLair(args);
		assertEquals("Unable to use provided index: 100", message);
		
		String args2[] = {"setlair", "dave"};
		message = main.setLair(args2);
		assertEquals("Unable to use provided index: dave", message);
		
		String args3[] = {"setlair", "0"};
		message = main.setLair(args3);
		assertEquals("Added Lair: Demogorgon's Lair", message);
		
		main.startCombat();
		
		String args4[] = {"lairact", "0"};
		message = main.lairAct(args4);
		assertTrue(message.contains("Illusory Duplicate"));
		assertTrue(message.contains("The creature creates an illusory duplicate of himself, which appears in his own space and lasts until initiative count 20 of the next round. On his turn, the creature can move the illusory duplicate a distance equal to his walking speed (no action required). The first time a creature or object interacts physically with the creature (for example. hitting him with an attack). there is a 50 percent chance that it is the illusory duplicate that is being affected, not the creature himself, in which case the illusion disappears."));
		
		message = main.lairAct(args4);
		assertEquals("Cannot act this turn, already acted", message);
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
		String results = main.castInnateSpell(args2);
		assertTrue(results.contains("Levitate: One creature or loose object of your choice that you can see "));
		
		results = main.castInnateSpell(args2);
		assertEquals(results, "Cannot cast spell: No more charges for spell: levitate");
		
		String args3[] = {"icast", "darkness"};
		results = main.castInnateSpell(args3);
		assertEquals(results, "Cannot cast spell: Already concentrating on: Levitate");
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
