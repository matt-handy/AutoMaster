package handy.rp.dnd.spells;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import handy.rp.Dice;
import handy.rp.dnd.CharClass;
import handy.rp.dnd.spells.Spell;
import handy.rp.dnd.spells.Spell.SLOTLEVEL;
import handy.rp.xml.ClassParser;
import handy.rp.xml.SpellParser;

class SpellTest {

	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testFly() {
		try {
			Spell fly = SpellParser.load("spells\\fly.xml", false);
			assertEquals(fly.readableName, "Fly");
			assertEquals(fly.computerName, "fly");
			assertEquals(fly.minimumLevel, Spell.SLOTLEVEL.get(3));
			assertFalse(fly.saveDc);
			assertFalse(fly.toHit);
			assertTrue(fly.concentrate);

			assertTrue(fly.damagers == null);

		} catch (Exception ex) {
			ex.printStackTrace();
			fail(ex.getMessage());
		}
	}

	@Test
	void testFireball() {
		try {
			Spell fireball = SpellParser.load("spells\\fireball.xml", false);
			assertEquals(fireball.readableName, "Fireball");
			assertEquals(fireball.computerName, "fireball");
			assertEquals(fireball.minimumLevel, Spell.SLOTLEVEL.get(3));
			assertEquals(fireball.readableEffect,
					"A bright streak flashes from your pointing finger to a point you choose within range and then blossoms with a low roar into an explosion of flame. Each creature in a 20-foot-radius sphere centered on that point must make a Dexterity saving throw. A target takes 8d6 fire damage on a failed save, or half as much damage on a successful one. The fire spreads around corners. It ignites flammable objects in the area that aren't being worn or carried. When you cast this spell using a spell slot of 4th level or higher, the damage increases by 1d6 for each slot level above 3rd.");
			assertTrue(fireball.saveDc);
			assertFalse(fireball.toHit);
			assertFalse(fireball.concentrate);

			Map<SLOTLEVEL, List<SpellDamageComponent>> damagers = fireball.damagers;
			assertTrue(damagers.size() == 7);
			for (SLOTLEVEL lvl : damagers.keySet()) {
				List<SpellDamageComponent> sdcs = damagers.get(lvl);
				assertEquals(sdcs.size(), 1);
				SpellDamageComponent sdc = sdcs.get(0);
				assertTrue(sdc.damageType == SpellDamageComponent.DAMAGE_TYPE.FIRE);
				assertTrue(sdc.diceType == Dice.DICE_TYPE.D6);
				assertEquals(sdc.modifier, 0);
				assertEquals(sdc.toHit, -1);
				if (lvl.level < 3) {
					fail("Minimum spell level is 3");
				} else {
					assertEquals(sdc.ranges.size(), 1);
					assertEquals(sdc.ranges.get(0).numDice, 8 + lvl.level - 3);
				}
			}
		} catch (Exception ex) {
			fail(ex.getMessage());
		}
	}

	@Test
	void testIceStorm() {
		try {
			Spell ice_storm = SpellParser.load("spells\\ice_storm.xml", false);
			assertEquals(ice_storm.readableName, "Ice Storm");
			assertEquals(ice_storm.computerName, "ice_storm");
			assertEquals(ice_storm.minimumLevel, Spell.SLOTLEVEL.get(4));
			assertEquals(ice_storm.readableEffect,
					"A hail of rock-hard ice pounds to the ground in a 20-foot-radius, 40-foot-high cylinder centered on a point within range. Each creature in the cylinder must make a Dexterity saving throw. A creature takes 2d8 bludgeoning damage and 4d6 cold damage on a failed save, or half as much damage on a successful one. Hailstones turn the storm's area of effect into difficult terrain until the end of your next turn. At Higher Levels: When you cast this spell using a spell slot of 5th level or higher, the bludgeoning damage increases by 1d8 for each slot level above 4th.");
			assertTrue(ice_storm.saveDc);
			assertFalse(ice_storm.toHit);
			assertFalse(ice_storm.concentrate);

			Map<SLOTLEVEL, List<SpellDamageComponent>> damagers = ice_storm.damagers;
			assertTrue(damagers.size() == 6);
			for (SLOTLEVEL lvl : damagers.keySet()) {
				List<SpellDamageComponent> sdcs = damagers.get(lvl);
				assertEquals(sdcs.size(), 2);
				SpellDamageComponent sdc = sdcs.get(0);
				if (sdc.damageType == SpellDamageComponent.DAMAGE_TYPE.COLD) {
					assertTrue(sdc.diceType == Dice.DICE_TYPE.D6);
					assertEquals(sdc.modifier, 0);
					assertEquals(sdc.toHit, -1);
					if (lvl.level < 4) {
						fail("Minimum spell level is 4");
					} else {
						assertEquals(sdc.ranges.size(), 1);
						assertEquals(sdc.ranges.get(0).numDice, 4);
					}
				} else if (sdc.damageType == SpellDamageComponent.DAMAGE_TYPE.BLUDGEONING) {
					assertTrue(sdc.diceType == Dice.DICE_TYPE.D8);
					assertEquals(sdc.modifier, 0);
					assertEquals(sdc.toHit, -1);
					if (lvl.level < 4) {
						fail("Minimum spell level is 4");
					} else {
						assertEquals(sdc.ranges.size(), 1);
						assertEquals(sdc.ranges.get(0).numDice, 2 + lvl.level - 4);
					}
				} else {
					fail("Needs to be either cold or bludgeoning damage");
				}

			}
		} catch (Exception ex) {
			ex.printStackTrace();
			fail(ex.getMessage());
		}
	}

	@Test
	void testFireBolt() {
		try {
			Spell firebolt = SpellParser.load("spells\\fire_bolt.xml", false);
			assertEquals(firebolt.readableName, "Fire Bolt");
			assertEquals(firebolt.computerName, "fire_bolt");
			assertEquals(firebolt.minimumLevel, Spell.SLOTLEVEL.CANTRIP);
			assertEquals(firebolt.readableEffect,
					"You hurl a mote of fire at a creature or object within range. Make a ranged spell attack against the target. On a hit, the target takes 1d10 fire damage. A flammable object hit by this spell ignites if it isn't being worn or carried. This spell's damage increases by 1d10 when you reach 5th level (2d10), 11th level (3d10), and 17th level (4d10).");
			assertFalse(firebolt.saveDc);
			assertTrue(firebolt.toHit);
			assertFalse(firebolt.concentrate);

			Map<SLOTLEVEL, List<SpellDamageComponent>> damagers = firebolt.damagers;
			assertTrue(damagers.size() == 10);
			for (SLOTLEVEL lvl : damagers.keySet()) {
				List<SpellDamageComponent> sdcs = damagers.get(lvl);
				assertEquals(sdcs.size(), 1);
				SpellDamageComponent sdc = sdcs.get(0);
				assertTrue(sdc.damageType == SpellDamageComponent.DAMAGE_TYPE.FIRE);
				assertTrue(sdc.diceType == Dice.DICE_TYPE.D10);
				assertEquals(sdc.modifier, 0);
				assertEquals(sdc.toHit, -1);

				assertEquals(sdc.ranges.size(), 4);

				boolean found04 = false;
				boolean found510 = false;
				boolean found1116 = false;
				boolean found17Max = false;
				for (DiceToLevelRange dtr : sdc.ranges) {
					if (dtr.lowerLimitInclusive == 0 && dtr.upperLimitInclusive == 4) {
						assertEquals(dtr.numDice, 1);
						found04 = true;
					} else if (dtr.lowerLimitInclusive == 5 && dtr.upperLimitInclusive == 10) {
						assertEquals(dtr.numDice, 2);
						found510 = true;
					} else if (dtr.lowerLimitInclusive == 11 && dtr.upperLimitInclusive == 16) {
						assertEquals(dtr.numDice, 3);
						found1116 = true;
					} else if (dtr.lowerLimitInclusive == 17 && dtr.upperLimitInclusive == Integer.MAX_VALUE) {
						assertEquals(dtr.numDice, 4);
						found17Max = true;
					}
				}
				assertTrue(found04);
				assertTrue(found510);
				assertTrue(found1116);
				assertTrue(found17Max);
			}
		} catch (Exception ex) {
			fail(ex.getMessage());
		}
	}

	@Test
	void testBonusActionSetting() {
		try {
			Spell firebolt = SpellParser.load("spells\\eldritch_blast.xml", false);
			assertFalse(firebolt.bonusAction);

			Spell mistyStep = SpellParser.load("spells\\misty_step.xml", false);
			assertTrue(mistyStep.bonusAction);
		} catch (Exception ex) {
			fail(ex.getMessage());
		}
	}

	@Test
	void testTollTheDead() {
		try {
			Spell cureWounds = SpellParser.load("spells\\toll_the_dead.xml", false);
			assertEquals(cureWounds.readableName, "Toll the Dead");
			assertEquals(cureWounds.computerName, "toll_the_dead");
			assertEquals(cureWounds.minimumLevel, Spell.SLOTLEVEL.CANTRIP);
			assertTrue(cureWounds.saveDc);
			assertFalse(cureWounds.toHit);
			assertFalse(cureWounds.concentrate);
			String recurringMessage = cureWounds.getRecurringEffectOnTurn(SLOTLEVEL.CANTRIP, 9, 16, 7, 4);
			assertTrue(recurringMessage == null);

			recurringMessage = cureWounds.cast(SLOTLEVEL.CANTRIP, 9, 16, 7, 4);
			String elements[] = recurringMessage.split(System.lineSeparator());
			assertEquals(4, elements.length);
			assertEquals(elements[0], "Toll the Dead: You point at one creature you can see within range, and the sound of a dolorous bell fills the air around it for a moment. The target must succeed on a Wisdom saving throw or take 1d8 necrotic damage. If the target is missing any of its hit points, it instead takes 1d12 necrotic damage. AutoMaster will roll the d12 as primary damage and d8 as the alternative option.");
			String hitsFor = " hits for ";
			String necroticDamage = " Necrotic damage";
			assertTrue(elements[1].startsWith(hitsFor));
			assertTrue(elements[1].endsWith(necroticDamage));
			int hitValue = Integer.parseInt(elements[1].substring(hitsFor.length(), elements[1].length() - (necroticDamage.length())));
			assertTrue(hitValue >= 2 && hitValue <= 24);
			
			String altHitsFor = "Alternatively:  hits for ";
			assertTrue(elements[2].startsWith(altHitsFor));
			assertTrue(elements[2].endsWith(necroticDamage));
			hitValue = Integer.parseInt(elements[2].substring(altHitsFor.length(), elements[2].length() - (necroticDamage.length())));
			assertTrue(hitValue >= 2 && hitValue <= 16);
			
			assertEquals(elements[3], "Spell Save: 16");
		} catch (Exception ex) {
			ex.printStackTrace();
			fail(ex.getMessage());
		}
	}

	@Test
	void testCureWounds() {
		try {
			Spell cureWounds = SpellParser.load("spells\\cure_wounds.xml", false);
			assertEquals(cureWounds.readableName, "Cure Wounds");
			assertEquals(cureWounds.computerName, "cure_wounds");
			assertEquals(cureWounds.minimumLevel, Spell.SLOTLEVEL.ONE);
			assertFalse(cureWounds.saveDc);
			assertFalse(cureWounds.toHit);
			assertFalse(cureWounds.concentrate);
			String recurringMessage = cureWounds.getRecurringEffectOnTurn(SLOTLEVEL.ONE, 9, 16, 7, 4);
			assertTrue(recurringMessage == null);

			recurringMessage = cureWounds.cast(SLOTLEVEL.ONE, 9, 16, 7, 4);
			String elements[] = recurringMessage.split(System.lineSeparator());
			assertEquals(2, elements.length);
			assertTrue(elements[0].startsWith("Cure Wounds: "));
			assertTrue(elements[1].startsWith("Healed for : "));
			int healingValue = Integer.parseInt(elements[1].substring("Healed for : ".length()));
			assertTrue(healingValue >= 5 && healingValue <= 12);
		} catch (Exception ex) {
			ex.printStackTrace();
			fail(ex.getMessage());
		}
	}

	@Test
	void testHeatMetal() {
		try {
			Spell heatMetal = SpellParser.load("spells\\heat_metal.xml", false);
			assertEquals(heatMetal.readableName, "Heat Metal");
			assertEquals(heatMetal.computerName, "heat_metal");
			assertEquals(heatMetal.minimumLevel, Spell.SLOTLEVEL.TWO);
			assertTrue(heatMetal.saveDc);
			assertFalse(heatMetal.toHit);
			assertTrue(heatMetal.concentrate);
			String recurringMessage = heatMetal.getRecurringEffectOnTurn(SLOTLEVEL.TWO, 9, 16, 7, -1);
			String elements[] = recurringMessage.split(System.lineSeparator());
			assertEquals(4, elements.length);
			assertEquals("On your turn, you may use the spell Heat Metal as a bonus action to inflict: ", elements[0]);
			assertTrue(elements[2].startsWith(" hits for "));
			assertTrue(elements[2].endsWith(" Fire damage"));
			assertEquals("Spell Save: 16", elements[3]);
		} catch (Exception ex) {
			fail(ex.getMessage());
		}
	}

	@Test
	void testEldritchBlast() {
		try {
			Spell firebolt = SpellParser.load("spells\\eldritch_blast.xml", false);
			assertEquals(firebolt.readableName, "Eldritch Blast");
			assertEquals(firebolt.computerName, "eldritch_blast");
			assertEquals(firebolt.minimumLevel, Spell.SLOTLEVEL.CANTRIP);
			assertEquals(firebolt.readableEffect,
					"A beam of crackling energy streaks toward a creature within range. Make a ranged spell attack against the target. On a hit, the target takes 1d10 force damage. The spell creates more than one beam when you reach higher levels: two beams at 5th level, three beams at 11th level, and four beams at 17th level. You can direct the beams at the same target or at different ones. Make a separate attack roll for each beam.");
			assertFalse(firebolt.saveDc);
			assertTrue(firebolt.toHit);
			assertFalse(firebolt.concentrate);

			Map<SLOTLEVEL, List<SpellDamageComponent>> damagers = firebolt.damagers;
			assertEquals(damagers.size(), 10);
			for (SLOTLEVEL lvl : damagers.keySet()) {
				List<SpellDamageComponent> sdcs = damagers.get(lvl);
				assertEquals(sdcs.size(), 1);
				SpellDamageComponent sdc = sdcs.get(0);
				assertTrue(sdc.damageType == SpellDamageComponent.DAMAGE_TYPE.FORCE);
				assertTrue(sdc.diceType == Dice.DICE_TYPE.D10);
				assertEquals(sdc.modifier, 0);
				assertEquals(sdc.toHit, -1);

				assertEquals(sdc.ranges.size(), 4);

				boolean found04 = false;
				boolean found510 = false;
				boolean found1116 = false;
				boolean found17Max = false;
				for (DiceToLevelRange dtr : sdc.ranges) {
					if (dtr.lowerLimitInclusive == 0 && dtr.upperLimitInclusive == 4) {
						assertEquals(dtr.numDice, 1);
						found04 = true;
					} else if (dtr.lowerLimitInclusive == 5 && dtr.upperLimitInclusive == 10) {
						assertEquals(dtr.numDice, 2);
						found510 = true;
					} else if (dtr.lowerLimitInclusive == 11 && dtr.upperLimitInclusive == 16) {
						assertEquals(dtr.numDice, 3);
						found1116 = true;
					} else if (dtr.lowerLimitInclusive == 17 && dtr.upperLimitInclusive == Integer.MAX_VALUE) {
						assertEquals(dtr.numDice, 4);
						found17Max = true;
					}
				}
				assertTrue(found04);
				assertTrue(found510);
				assertTrue(found1116);
				assertTrue(found17Max);
			}
		} catch (Exception ex) {
			fail(ex.getMessage());
		}
	}

	@Test
	void testCatapultDoesNoDamageOnSave() {
		try {
			Spell catapult = SpellParser.load("spells\\catapult.xml", false);
			String result = catapult.cast(SLOTLEVEL.ONE, 1, 14, 7, 4);
			assertTrue(result.endsWith("Spell Save: 14, no damage on save."));
		}catch(Exception ex) {
			fail(ex.getMessage());
		}
	}
	
	@Test
	void testLoadAllActionSpells() {
		try {
			List<ActionSpell> allLoaded = SpellParser.loadAllActionSpells("action_spells");
			assertTrue(allLoaded.size() >= 1);

			boolean foundDKHellfireOrb = false;
			for (ActionSpell spell : allLoaded) {
				if (spell.readableName.equals("Hellfire Orb")) {
					foundDKHellfireOrb = true;
					assertEquals(spell.staticDC, 18);
				}
			}

			assertTrue(foundDKHellfireOrb);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	void testGetAllSpellsForClass() {
		CharClass cClass = ClassParser.getCharClass("Cleric");
		assertEquals("Cleric", cClass.name);
		List<Spell> spells = Spell.getAllSpellsForCharClass(cClass);
		
		boolean foundFireball = false;
		boolean foundCurewounds = false;
		boolean foundDetectMagic = false;
		
		for(Spell spell : spells) {
			if(spell.computerName.equals("fireball")) {
				foundFireball = true;
			}else if(spell.computerName.equals("cure_wounds")) {
				foundCurewounds = true;
			}else if(spell.computerName.equals("detect_magic")) {
				foundDetectMagic = true;
			}
		}
		
		assertTrue(foundCurewounds);
		assertFalse(foundFireball);
		assertTrue(foundDetectMagic);
		
		cClass = ClassParser.getCharClass("Wizard");
		assertEquals("Wizard", cClass.name);
		spells = Spell.getAllSpellsForCharClass(cClass);
		
		foundFireball = false;
		foundCurewounds = false;
		foundDetectMagic = false;
		
		for(Spell spell : spells) {
			if(spell.computerName.equals("fireball")) {
				foundFireball = true;
			}else if(spell.computerName.equals("cure_wounds")) {
				foundCurewounds = true;
			}else if(spell.computerName.equals("detect_magic")) {
				foundDetectMagic = true;
			}
		}
		
		assertFalse(foundCurewounds);
		assertTrue(foundFireball);
		assertTrue(foundDetectMagic);
	}
	
	@Test
	void testLoadAll() {
		try {
			List<Spell> allLoaded = SpellParser.loadAll("spells", false);
			assertTrue(allLoaded.size() >= 3);
			boolean foundFireball = false;
			boolean foundFirebolt = false;
			boolean foundEldritchBlast = false;

			for (Spell spell : allLoaded) {
				if (spell.readableName.equals("Fireball")) {
					foundFireball = true;
				} else if (spell.readableName.equals("Fire Bolt")) {
					foundFirebolt = true;
				} else if (spell.readableName.equals("Eldritch Blast")) {
					foundEldritchBlast = true;
				}
			}

			assertTrue(foundFireball);
			assertTrue(foundFirebolt);
			assertTrue(foundEldritchBlast);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

}
