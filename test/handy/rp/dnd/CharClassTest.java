package handy.rp.dnd;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import handy.rp.Dice.DICE_TYPE;
import handy.rp.dnd.CharClass.ESSENTIAL_ABILITY_SCORE;
import handy.rp.dnd.ClassFeature.DAMAGE_EFFECT;
import handy.rp.dnd.ClassFeature.RECHARGE_DURATION;
import handy.rp.dnd.ClassFeature.USE_TYPE;
import handy.rp.dnd.attacks.CoreDamageComponent;
import handy.rp.dnd.attacks.DamageComponent.DAMAGE_TYPE;
import handy.rp.dnd.spells.Spell;
import handy.rp.dnd.spells.Spell.SLOTLEVEL;
import handy.rp.xml.ClassParser;
import handy.rp.xml.MonsterParser;
import handy.rp.xml.SubClassParser;

class CharClassTest {

	@Test
	void testClericFeatureLoad() {
		boolean foundCleric = false;
		for(CharClass cClass : ClassParser.charClasses) {
			if(cClass.name.equals("Cleric")) {
				foundCleric = true;
			}else {
				continue;
			}
			
			boolean foundTurnUndead = false;
			boolean foundDivineIntervention = false;
			for(ClassFeature feature : cClass.getFeatures()) {
				if(feature.featureName.equals("Divine Intervention")) {
					foundDivineIntervention = true;
					
					assertEquals(feature.damageEffect, DAMAGE_EFFECT.NA);
					assertEquals(feature.minimumLevel, 10);
					assertEquals(feature.classResourceChargesUsed, 0);
					assertTrue(feature.effectString.contains("Beginning at 10th level, you can call on your deity to intervene on your behalf when your need is great."));
					assertTrue(feature.effectString.contains("Imploring your deity's aid requires you to use your action. Describe the assistance you seek, and roll percentile dice. If you roll a number equal to or lower than your cleric level, your deity intervenes. The DM chooses the nature of the intervention; the effect of any cleric spell or cleric domain spell would be appropriate."));
					assertTrue(feature.effectString.contains("If your deity intervenes, you can't use this feature again for 7 days. Otherwise, you can use it again after you finish a long rest."));
					assertTrue(feature.effectString.contains("At 20th level, your call for intervention succeeds automatically, no roll required."));
					assertEquals(feature.useType, USE_TYPE.ACTION);
				}else if(feature.featureName.equals("Channel Divinity - Turn Undead")) {
					foundTurnUndead = true;
					
					assertEquals(feature.damageEffect, DAMAGE_EFFECT.NA);
					assertEquals(feature.minimumLevel, 2);
					assertEquals(feature.classResourceChargesUsed, 1);
					assertTrue(feature.effectString.contains("As an action, you present your holy symbol and speak a prayer censuring the undead. Each undead that can see or hear you within 30 feet of you must make a Wisdom saving throw. If the creature fails its saving throw, it is turned for 1 minute or until it takes any damage."));
					assertTrue(feature.effectString.contains("A turned creature must spend its turns trying to move as far away from you as it can, and it can't willingly move to a space within 30 feet of you. It also can't take reactions. For its action, it can use only the Dash action or try to escape from an effect that prevents it from moving. If there's nowhere to move, the creature can use the Dodge action."));
					assertTrue(feature.effectString.contains("Starting at 5th level, when an undead fails its saving throw against your Turn Undead feature, the creature is instantly destroyed if its challenge rating is at or below a certain threshold. For a 5th level cleric, CR 1/2 or lower. For a 8th level cleric, CR 1 or lower, For a 11th level cleric, CR 2 or lower, For a 14th level cleric, CR 3 or lower, For a 17th level cleric, CR 4 or lower."));
					assertEquals(feature.useType, USE_TYPE.ACTION);
				}
			}
			assertTrue(foundTurnUndead);
			assertTrue(foundDivineIntervention);
			
		}
		assertTrue(foundCleric);
	}
	
	@Test
	void testForgeDomainFeatureLoad() {
		boolean foundCleric = false;
		for(CharClass cClass : SubClassParser.charClasses) {
			if(cClass.name.equals("Forge Domain Cleric")) {
				foundCleric = true;
			}else {
				continue;
			}
			
			boolean foundTurnUndead = false;
			boolean foundDivineIntervention = false;
			boolean foundBonusProficiencies = false;
			boolean foundArtisansBlessings = false;
			boolean foundSaintOfForgeAndFire = false;
			boolean foundSoulOfTheForge = false;
			boolean divineStrike = false;
			boolean foundBlessingsOfTheForge = false;
			for(ClassFeature feature : cClass.getFeatures()) {
				if(feature.featureName.equals("Divine Intervention")) {
					foundDivineIntervention = true;
				}else if(feature.featureName.equals("Channel Divinity - Turn Undead")) {
					foundTurnUndead = true;
				}else if(feature.featureName.equals("Bonus Proficiencies")) {
					foundBonusProficiencies = true;
					assertEquals(feature.damageEffect, DAMAGE_EFFECT.NA);
					assertEquals(feature.minimumLevel, 1);
					assertEquals(feature.classResourceChargesUsed, 0);
					assertEquals(feature.effectString, "When you choose this domain at 1st level, you gain proficiency with heavy armor and smith's tools.");
					assertEquals(feature.useType, USE_TYPE.NONCOMBAT);
					assertEquals(feature.recharge, RECHARGE_DURATION.NA);
					assertEquals(feature.maxCharges, 0);
				}
				else if(feature.featureName.equals("Channel Divinity - Artisan's Blessing")) {
					foundArtisansBlessings = true;
					assertEquals(feature.damageEffect, DAMAGE_EFFECT.NA);
					assertEquals(feature.minimumLevel, 2);
					assertEquals(feature.classResourceChargesUsed, 1);
					String elements[] = feature.effectString.split("\n");
					assertEquals(elements[0], "Starting at 2nd level, you can use your Channel Divinity to create simple items.");
					assertEquals(elements[1], "You conduct an hour-long ritual that crafts a nonmagical item that must include some metal: a simple or martial weapon, a suit of armor, ten pieces of ammunition, a set of tools, or another metal object. The creation is completed at the end of the hour, coalescing in an unoccupied space of your choice on a surface within 5 feet of you.");
					assertEquals(elements[2], "The thing you create can be something that is worth no more than 100 gp. As part of this ritual, you must lay out metal, which can include coins, with a value equal to the creation. The metal irretrievably coalesces and transforms into the creation at the ritual's end, magically forming even nonmetal parts of the creation.");
					assertEquals(elements[3], "The ritual can create a duplicate of a nonmagical item that contains metal, such as a key, if you possess the original during the ritual."); 
					assertEquals(feature.useType, USE_TYPE.NONCOMBAT);
					assertEquals(feature.recharge, RECHARGE_DURATION.NA);
					assertEquals(feature.maxCharges, 0);
				}
				
				else if(feature.featureName.equals("Soul of the Forge")) {
					foundSoulOfTheForge = true;
					assertEquals(feature.damageEffect, DAMAGE_EFFECT.NA);
					assertEquals(feature.minimumLevel, 6);
					assertEquals(feature.classResourceChargesUsed, 0);
					assertEquals(feature.effectString, "Starting at 6th level, your mastery of the forge grants you special abilities. You gain resistance to fire damage. While wearing heavy armor, you gain a +1 bonus to AC."); 
					assertEquals(feature.useType, USE_TYPE.NONCOMBAT);
					assertEquals(feature.recharge, RECHARGE_DURATION.NA);
					assertEquals(feature.maxCharges, 0);
				}
				else if(feature.featureName.equals("Saint of Forge and Fire")) {
					foundSaintOfForgeAndFire = true;
					assertEquals(feature.damageEffect, DAMAGE_EFFECT.NA);
					assertEquals(feature.minimumLevel, 17);
					assertEquals(feature.classResourceChargesUsed, 0);
					assertEquals(feature.effectString, "At 17th level, your blessed affinity with fire and metal becomes more powerful. You gain immunity to fire damage. While wearing heavy armor, you have resistance to bludgeoning, piercing, and slashing damage from nonmagical attacks.");
					assertEquals(feature.useType, USE_TYPE.NONCOMBAT);
					assertEquals(feature.recharge, RECHARGE_DURATION.NA);
					assertEquals(feature.maxCharges, 0);
				}
				else if(feature.featureName.equals("Blessing of the Forge")) {
					foundBlessingsOfTheForge = true;
					assertEquals(feature.damageEffect, DAMAGE_EFFECT.NA);
					assertEquals(feature.minimumLevel, 1);
					assertEquals(feature.classResourceChargesUsed, 0);
					assertEquals(feature.effectString, "At 1st level, you gain the ability to imbue magic into a weapon or armor. At the end of a long rest, you can touch one nonmagical object that is a suit of armor or a simple or martial weapon. Until the end of your next long rest or until you die, the object becomes a magic item, granting a +1 bonus to AC if it's armor or a +1 bonus to attack and damage rolls if it's a weapon. Once you use this feature, you can't use it again until you finish a long rest.");
					assertEquals(feature.useType, USE_TYPE.NONCOMBAT);
					assertEquals(feature.recharge, RECHARGE_DURATION.LONG_REST);
					assertEquals(feature.maxCharges, 1);
				}
				else if(feature.featureName.equals("Divine Strike")) {
					foundSaintOfForgeAndFire = true;
					assertEquals(feature.damageEffect, DAMAGE_EFFECT.MELEE);
					assertEquals(feature.minimumLevel, 8);
					assertEquals(feature.classResourceChargesUsed, 0);
					assertEquals(feature.effectString, "At 8th level, you gain the ability to infuse your weapon strikes with the fiery power of the forge. Once on each of your turns when you hit a creature with a weapon attack, you can cause the attack to deal an extra 1d8 fire damage to the target. When you reach 14th level, the extra damage increases to 2d8.");
					assertEquals(feature.useType, USE_TYPE.NONCOMBAT);
					assertEquals(feature.recharge, RECHARGE_DURATION.NA);
					assertEquals(feature.maxCharges, 0);
					CoreDamageComponent eighthDam = feature.getCoreDamageComponentForLevel(8);
					assertEquals(eighthDam.damageType, DAMAGE_TYPE.FIRE);
					assertEquals(eighthDam.diceCount, 1);
					assertEquals(eighthDam.diceType, DICE_TYPE.D8);
					CoreDamageComponent fourteenthDam = feature.getCoreDamageComponentForLevel(14);
					assertEquals(fourteenthDam.damageType, DAMAGE_TYPE.FIRE);
					assertEquals(fourteenthDam.diceCount, 2);
					assertEquals(fourteenthDam.diceType, DICE_TYPE.D8);
					CoreDamageComponent twentiethDam = feature.getCoreDamageComponentForLevel(20);
					assertEquals(twentiethDam.damageType, DAMAGE_TYPE.FIRE);
					assertEquals(twentiethDam.diceCount, 2);
					assertEquals(twentiethDam.diceType, DICE_TYPE.D8);
					try {
						feature.getCoreDamageComponentForLevel(1);
						fail("This should have failed");
					}catch (IllegalArgumentException ex){
						
					}
					divineStrike = true;
				}
			}
			assertTrue(foundTurnUndead);
			assertTrue(foundDivineIntervention);
			assertTrue(foundBonusProficiencies);
			assertTrue(foundArtisansBlessings);
			assertTrue(foundSoulOfTheForge);
			assertTrue(foundSaintOfForgeAndFire);
			assertTrue(divineStrike);
			assertTrue(foundBlessingsOfTheForge);
			
			List<Spell> spells = cClass.getAutomaticSpells(7);
			assertTrue(spells.contains(MonsterParser.getSpellByName("identify")));
			assertTrue(spells.contains(MonsterParser.getSpellByName("searing_smite")));
			assertTrue(spells.contains(MonsterParser.getSpellByName("heat_metal")));
			assertTrue(spells.contains(MonsterParser.getSpellByName("magic_weapon")));
			assertTrue(spells.contains(MonsterParser.getSpellByName("elemental_weapon")));
			assertTrue(spells.contains(MonsterParser.getSpellByName("protection_from_energy")));
			assertTrue(spells.contains(MonsterParser.getSpellByName("fabricate")));
			assertTrue(spells.contains(MonsterParser.getSpellByName("wall_of_fire")));
			assertFalse(spells.contains(MonsterParser.getSpellByName("animate_objects")));
			assertFalse(spells.contains(MonsterParser.getSpellByName("creation")));
			
			spells = cClass.getAutomaticSpells(10);
			assertTrue(spells.contains(MonsterParser.getSpellByName("identify")));
			assertTrue(spells.contains(MonsterParser.getSpellByName("searing_smite")));
			assertTrue(spells.contains(MonsterParser.getSpellByName("heat_metal")));
			assertTrue(spells.contains(MonsterParser.getSpellByName("magic_weapon")));
			assertTrue(spells.contains(MonsterParser.getSpellByName("elemental_weapon")));
			assertTrue(spells.contains(MonsterParser.getSpellByName("protection_from_energy")));
			assertTrue(spells.contains(MonsterParser.getSpellByName("fabricate")));
			assertTrue(spells.contains(MonsterParser.getSpellByName("wall_of_fire")));
			assertTrue(spells.contains(MonsterParser.getSpellByName("animate_objects")));
			assertTrue(spells.contains(MonsterParser.getSpellByName("creation")));
		}
		assertTrue(foundCleric);
	}
	
	@Test
	void testExtraCritDice() {
		CharClass cClass = SubClassParser.getCharClass("Barbarian");
		ClassFeature extraCrit = null;
		for(ClassFeature feature : cClass.getFeatures()) {
			if(feature.featureName.equals("Brutal Critical")) {
				extraCrit = feature;
			}
		}
		assertFalse(extraCrit == null, "Didn't find the feature");
		assertEquals(0, extraCrit.getExtraCritDice(5));
		assertEquals(1, extraCrit.getExtraCritDice(9));
		assertEquals(2, extraCrit.getExtraCritDice(13));
		assertEquals(3, extraCrit.getExtraCritDice(17));
		assertEquals(3, extraCrit.getExtraCritDice(20));
	}
	
	@Test
	void testLifeDomainFeatureLoad() {
		CharClass cClass = SubClassParser.getCharClass("Life Domain Cleric");
		boolean foundDiscipleOfLife = false;
		boolean foundPreserveLife = false;
		boolean foundBonusProficiency = false;
		boolean foundBlessedHealer = false;
		boolean foundDivineStrike = false;
		boolean foundSupremeHealing = false;
		for(ClassFeature feature : cClass.getFeatures()) {
			if(feature.featureName.equals("Bonus Proficiency")) {
				foundBonusProficiency = true;
				assertEquals(feature.damageEffect, DAMAGE_EFFECT.NA);
				assertEquals(feature.minimumLevel, 1);
				assertEquals(feature.classResourceChargesUsed, 0);
				assertEquals(feature.effectString, "When you choose this domain at 1st level, you gain proficiency with heavy armor.");
				assertEquals(feature.useType, USE_TYPE.NONCOMBAT);
				assertEquals(feature.recharge, RECHARGE_DURATION.NA);
				assertEquals(feature.maxCharges, 0);
			}else if(feature.featureName.equals("Disciple of Life")) {
				foundDiscipleOfLife = true;
				assertEquals(feature.damageEffect, DAMAGE_EFFECT.NA);
				assertEquals(feature.minimumLevel, 1);
				assertEquals(feature.classResourceChargesUsed, 0);
				assertEquals(feature.effectString, "Also starting at 1st level, your healing spells are more effective. Whenever you use a spell of 1st level or higher to restore hit points to a creature, the creature regains additional hit points equal to 2 + the spell's level.");
				assertEquals(feature.useType, USE_TYPE.NONCOMBAT);
				assertEquals(feature.recharge, RECHARGE_DURATION.NA);
				assertEquals(feature.maxCharges, 0);
				assertEquals(feature.otherHealingMod.staticModifier, 2);
				assertTrue(feature.otherHealingMod.increaseHealBySpellLevel);
				assertFalse(feature.otherHealingMod.maximizeHealDice);
				assertEquals(feature.selfHealingMod, null);
			}else if(feature.featureName.equals("Channel Divinity - Preserve Life")) {
				foundPreserveLife = true;
				assertEquals(feature.damageEffect, DAMAGE_EFFECT.NA);
				assertEquals(feature.minimumLevel, 2);
				assertEquals(feature.classResourceChargesUsed, 1);
				assertEquals(feature.effectString, "Starting at 2nd level, you can use your Channel Divinity to heal the badly injured. As an action, you present your holy symbol and evoke healing energy that can restore a number of hit points equal to five times your cleric level. Choose any creatures within 30 feet of you, and divide those hit points among them. This feature can restore a creature to no more than half of its hit point maximum. You can't use this feature on an undead or a construct.");
				assertEquals(feature.useType, USE_TYPE.ACTION);
				assertEquals(feature.recharge, RECHARGE_DURATION.NA);
				assertEquals(feature.maxCharges, 0);
			}else if(feature.featureName.equals("Blessed Healer")) {
				foundBlessedHealer = true;
				assertEquals(feature.damageEffect, DAMAGE_EFFECT.NA);
				assertEquals(feature.minimumLevel, 6);
				assertEquals(feature.classResourceChargesUsed, 0);
				assertEquals(feature.effectString, "Beginning at 6th level, the healing spells you cast on others heal you as well. When you cast a spell of 1st level or higher that restores hit points to a creature other than you, you regain hit points equal to 2 + the spell's level.");
				assertEquals(feature.useType, USE_TYPE.NONCOMBAT);
				assertEquals(feature.recharge, RECHARGE_DURATION.NA);
				assertEquals(feature.maxCharges, 0);
				assertEquals(feature.selfHealingMod.staticModifier, 2);
				assertTrue(feature.selfHealingMod.increaseHealBySpellLevel);
				assertFalse(feature.selfHealingMod.maximizeHealDice);
				assertEquals(feature.otherHealingMod, null);
			}else if(feature.featureName.equals("Divine Strike")) {
				foundDivineStrike = true;
				assertEquals(feature.damageEffect, DAMAGE_EFFECT.MELEE);
				assertEquals(feature.minimumLevel, 8);
				assertEquals(feature.classResourceChargesUsed, 0);
				assertEquals(feature.effectString, "At 8th level, you gain the ability to infuse your weapon strikes with divine energy. Once on each of your turns when you hit a creature with a weapon attack, you can cause the attack to deal an extra 1d8 radiant damage to the target. When you reach 14th level, the extra damage increases to 2d8.");
				assertEquals(feature.useType, USE_TYPE.NONCOMBAT);
				assertEquals(feature.recharge, RECHARGE_DURATION.NA);
				assertEquals(feature.maxCharges, 0);
				CoreDamageComponent eighthDam = feature.getCoreDamageComponentForLevel(8);
				assertEquals(eighthDam.damageType, DAMAGE_TYPE.RADIANT);
				assertEquals(eighthDam.diceCount, 1);
				assertEquals(eighthDam.diceType, DICE_TYPE.D8);
				CoreDamageComponent fourteenthDam = feature.getCoreDamageComponentForLevel(14);
				assertEquals(fourteenthDam.damageType, DAMAGE_TYPE.RADIANT);
				assertEquals(fourteenthDam.diceCount, 2);
				assertEquals(fourteenthDam.diceType, DICE_TYPE.D8);
				CoreDamageComponent twentiethDam = feature.getCoreDamageComponentForLevel(20);
				assertEquals(twentiethDam.damageType, DAMAGE_TYPE.RADIANT);
				assertEquals(twentiethDam.diceCount, 2);
				assertEquals(twentiethDam.diceType, DICE_TYPE.D8);
				try {
					feature.getCoreDamageComponentForLevel(1);
					fail("This should have failed");
				}catch (IllegalArgumentException ex){
					
				}
			}else if(feature.featureName.equals("Supreme Healing")) {
				foundSupremeHealing = true;
				assertEquals(feature.damageEffect, DAMAGE_EFFECT.NA);
				assertEquals(feature.minimumLevel, 17);
				assertEquals(feature.classResourceChargesUsed, 0);
				assertEquals(feature.effectString, "Starting at 17th level, when you would normally roll one or more dice to restore hit points with a spell, you instead use the highest number possible for each die. For example, instead of restoring 2d6 hit points to a creature, you restore 12.");
				assertEquals(feature.useType, USE_TYPE.NONCOMBAT);
				assertEquals(feature.recharge, RECHARGE_DURATION.NA);
				assertEquals(feature.maxCharges, 0);
				assertEquals(feature.otherHealingMod.staticModifier, 0);
				assertFalse(feature.otherHealingMod.increaseHealBySpellLevel);
				assertTrue(feature.otherHealingMod.maximizeHealDice);
				assertEquals(feature.selfHealingMod, null);
			}
			
		}
		assertTrue(foundDiscipleOfLife);
		assertTrue(foundPreserveLife);
		assertTrue(foundBonusProficiency);
		assertTrue(foundBlessedHealer);
		assertTrue(foundDivineStrike);
		assertTrue(foundSupremeHealing);
		
		List<Spell> spells = cClass.getAutomaticSpells(7);
		assertTrue(spells.contains(MonsterParser.getSpellByName("bless")));
		assertTrue(spells.contains(MonsterParser.getSpellByName("cure_wounds")));
		assertTrue(spells.contains(MonsterParser.getSpellByName("lesser_restoration")));
		assertTrue(spells.contains(MonsterParser.getSpellByName("spiritual_weapon")));
		assertTrue(spells.contains(MonsterParser.getSpellByName("beacon_of_hope")));
		assertTrue(spells.contains(MonsterParser.getSpellByName("revivify")));
		assertTrue(spells.contains(MonsterParser.getSpellByName("death_ward")));
		assertTrue(spells.contains(MonsterParser.getSpellByName("guardian_of_faith")));
		assertFalse(spells.contains(MonsterParser.getSpellByName("mass_cure_wounds")));
		assertFalse(spells.contains(MonsterParser.getSpellByName("raise_dead")));
		
		spells = cClass.getAutomaticSpells(10);
		assertTrue(spells.contains(MonsterParser.getSpellByName("bless")));
		assertTrue(spells.contains(MonsterParser.getSpellByName("cure_wounds")));
		assertTrue(spells.contains(MonsterParser.getSpellByName("lesser_restoration")));
		assertTrue(spells.contains(MonsterParser.getSpellByName("spiritual_weapon")));
		assertTrue(spells.contains(MonsterParser.getSpellByName("beacon_of_hope")));
		assertTrue(spells.contains(MonsterParser.getSpellByName("revivify")));
		assertTrue(spells.contains(MonsterParser.getSpellByName("death_ward")));
		assertTrue(spells.contains(MonsterParser.getSpellByName("guardian_of_faith")));
		assertTrue(spells.contains(MonsterParser.getSpellByName("mass_cure_wounds")));
		assertTrue(spells.contains(MonsterParser.getSpellByName("raise_dead")));
	}
	
	@Test 
	void testFindSubclasses() {
		CharClass cleric = null;
		for(CharClass cClass : ClassParser.charClasses) {
			if(cClass.name.equals("Cleric")) {
				cleric = cClass;
			}else {
				continue;
			}
		}
		assertTrue(cleric != null);
		
		List<CharClass> subclasses = SubClassParser.getAllSubclassesForParent(cleric);
		boolean foundLifeCleric = false;
		boolean foundForgeCleric = false;
		boolean foundBerserker = false;
		boolean foundBarbarian = false;
		boolean foundCleric = false;
		for(CharClass cClass : subclasses) {
			if(cClass.name.equals("Cleric")) {
				foundCleric = true;
			}else if(cClass.name.equals("Barbarian")) {
				foundBarbarian = true;
			}else if(cClass.name.equals("Forge Domain Cleric")) {
				foundForgeCleric = true;
			}else if(cClass.name.equals("Life Domain Cleric")) {
				foundLifeCleric = true;
			}else if(cClass.name.equals("Path of the Berserker Barbarian")) {
				foundBerserker = true;
			}
		}
		
		assertTrue(foundLifeCleric);
		assertTrue(foundForgeCleric);
		assertFalse(foundBerserker);
		assertFalse(foundBarbarian);
		assertFalse(foundCleric);//Cleric is not a subclass of cleric
			
	}
	
	@Test
	void testLoadForgeDomainCleric() {
		boolean foundCleric = false;
		for(CharClass cClass : SubClassParser.charClasses) {
			if(cClass.name.equals("Forge Domain Cleric")) {
				foundCleric = true;
			}else {
				continue;
			}
		}
		assertTrue(foundCleric);
	}
	
	@Test
	void testLoadCleric() {
		boolean foundCleric = false;
		for(CharClass cClass : ClassParser.charClasses) {
			if(cClass.name.equals("Cleric")) {
				foundCleric = true;
			}else {
				continue;
			}
			
			assertEquals(2, cClass.subClassLevel);
			
			Map<Spell.SLOTLEVEL, Integer> l1 = cClass.slotsPerLevel.get(1);
			assertEquals(l1.get(SLOTLEVEL.ONE), 2);
			assertEquals(l1.get(SLOTLEVEL.TWO), null);
			assertEquals(l1.get(SLOTLEVEL.THREE), null);
			assertEquals(l1.get(SLOTLEVEL.FOUR), null);
			assertEquals(l1.get(SLOTLEVEL.FIVE), null);
			assertEquals(l1.get(SLOTLEVEL.SIX), null);
			assertEquals(l1.get(SLOTLEVEL.SEVEN), null);
			assertEquals(l1.get(SLOTLEVEL.EIGHT), null);
			assertEquals(l1.get(SLOTLEVEL.NINE), null);
			
			Map<Spell.SLOTLEVEL, Integer> l2 = cClass.slotsPerLevel.get(2);
			assertEquals(l2.get(SLOTLEVEL.ONE), 3);
			assertEquals(l2.get(SLOTLEVEL.TWO), null);
			assertEquals(l2.get(SLOTLEVEL.THREE), null);
			assertEquals(l2.get(SLOTLEVEL.FOUR), null);
			assertEquals(l2.get(SLOTLEVEL.FIVE), null);
			assertEquals(l2.get(SLOTLEVEL.SIX), null);
			assertEquals(l2.get(SLOTLEVEL.SEVEN), null);
			assertEquals(l2.get(SLOTLEVEL.EIGHT), null);
			assertEquals(l2.get(SLOTLEVEL.NINE), null);
			
			Map<Spell.SLOTLEVEL, Integer> testLevel = cClass.slotsPerLevel.get(3);
			assertEquals(testLevel.get(SLOTLEVEL.ONE), 4);
			assertEquals(testLevel.get(SLOTLEVEL.TWO), 2);
			assertEquals(testLevel.get(SLOTLEVEL.THREE), null);
			assertEquals(testLevel.get(SLOTLEVEL.FOUR), null);
			assertEquals(testLevel.get(SLOTLEVEL.FIVE), null);
			assertEquals(testLevel.get(SLOTLEVEL.SIX), null);
			assertEquals(testLevel.get(SLOTLEVEL.SEVEN), null);
			assertEquals(testLevel.get(SLOTLEVEL.EIGHT), null);
			assertEquals(testLevel.get(SLOTLEVEL.NINE), null);
			
			testLevel = cClass.slotsPerLevel.get(4);
			assertEquals(testLevel.get(SLOTLEVEL.ONE), 4);
			assertEquals(testLevel.get(SLOTLEVEL.TWO), 3);
			assertEquals(testLevel.get(SLOTLEVEL.THREE), null);
			assertEquals(testLevel.get(SLOTLEVEL.FOUR), null);
			assertEquals(testLevel.get(SLOTLEVEL.FIVE), null);
			assertEquals(testLevel.get(SLOTLEVEL.SIX), null);
			assertEquals(testLevel.get(SLOTLEVEL.SEVEN), null);
			assertEquals(testLevel.get(SLOTLEVEL.EIGHT), null);
			assertEquals(testLevel.get(SLOTLEVEL.NINE), null);
			
			testLevel = cClass.slotsPerLevel.get(5);
			assertEquals(testLevel.get(SLOTLEVEL.ONE), 4);
			assertEquals(testLevel.get(SLOTLEVEL.TWO), 3);
			assertEquals(testLevel.get(SLOTLEVEL.THREE), 2);
			assertEquals(testLevel.get(SLOTLEVEL.FOUR), null);
			assertEquals(testLevel.get(SLOTLEVEL.FIVE), null);
			assertEquals(testLevel.get(SLOTLEVEL.SIX), null);
			assertEquals(testLevel.get(SLOTLEVEL.SEVEN), null);
			assertEquals(testLevel.get(SLOTLEVEL.EIGHT), null);
			assertEquals(testLevel.get(SLOTLEVEL.NINE), null);
			
			testLevel = cClass.slotsPerLevel.get(6);
			assertEquals(testLevel.get(SLOTLEVEL.ONE), 4);
			assertEquals(testLevel.get(SLOTLEVEL.TWO), 3);
			assertEquals(testLevel.get(SLOTLEVEL.THREE), 3);
			assertEquals(testLevel.get(SLOTLEVEL.FOUR), null);
			assertEquals(testLevel.get(SLOTLEVEL.FIVE), null);
			assertEquals(testLevel.get(SLOTLEVEL.SIX), null);
			assertEquals(testLevel.get(SLOTLEVEL.SEVEN), null);
			assertEquals(testLevel.get(SLOTLEVEL.EIGHT), null);
			assertEquals(testLevel.get(SLOTLEVEL.NINE), null);
			
			testLevel = cClass.slotsPerLevel.get(7);
			assertEquals(testLevel.get(SLOTLEVEL.ONE), 4);
			assertEquals(testLevel.get(SLOTLEVEL.TWO), 3);
			assertEquals(testLevel.get(SLOTLEVEL.THREE), 3);
			assertEquals(testLevel.get(SLOTLEVEL.FOUR), 1);
			assertEquals(testLevel.get(SLOTLEVEL.FIVE), null);
			assertEquals(testLevel.get(SLOTLEVEL.SIX), null);
			assertEquals(testLevel.get(SLOTLEVEL.SEVEN), null);
			assertEquals(testLevel.get(SLOTLEVEL.EIGHT), null);
			assertEquals(testLevel.get(SLOTLEVEL.NINE), null);
			
			testLevel = cClass.slotsPerLevel.get(8);
			assertEquals(testLevel.get(SLOTLEVEL.ONE), 4);
			assertEquals(testLevel.get(SLOTLEVEL.TWO), 3);
			assertEquals(testLevel.get(SLOTLEVEL.THREE), 3);
			assertEquals(testLevel.get(SLOTLEVEL.FOUR), 2);
			assertEquals(testLevel.get(SLOTLEVEL.FIVE), null);
			assertEquals(testLevel.get(SLOTLEVEL.SIX), null);
			assertEquals(testLevel.get(SLOTLEVEL.SEVEN), null);
			assertEquals(testLevel.get(SLOTLEVEL.EIGHT), null);
			assertEquals(testLevel.get(SLOTLEVEL.NINE), null);
			
			testLevel = cClass.slotsPerLevel.get(9);
			assertEquals(testLevel.get(SLOTLEVEL.ONE), 4);
			assertEquals(testLevel.get(SLOTLEVEL.TWO), 3);
			assertEquals(testLevel.get(SLOTLEVEL.THREE), 3);
			assertEquals(testLevel.get(SLOTLEVEL.FOUR), 3);
			assertEquals(testLevel.get(SLOTLEVEL.FIVE), 1);
			assertEquals(testLevel.get(SLOTLEVEL.SIX), null);
			assertEquals(testLevel.get(SLOTLEVEL.SEVEN), null);
			assertEquals(testLevel.get(SLOTLEVEL.EIGHT), null);
			assertEquals(testLevel.get(SLOTLEVEL.NINE), null);
			
			testLevel = cClass.slotsPerLevel.get(10);
			assertEquals(testLevel.get(SLOTLEVEL.ONE), 4);
			assertEquals(testLevel.get(SLOTLEVEL.TWO), 3);
			assertEquals(testLevel.get(SLOTLEVEL.THREE), 3);
			assertEquals(testLevel.get(SLOTLEVEL.FOUR), 3);
			assertEquals(testLevel.get(SLOTLEVEL.FIVE), 2);
			assertEquals(testLevel.get(SLOTLEVEL.SIX), null);
			assertEquals(testLevel.get(SLOTLEVEL.SEVEN), null);
			assertEquals(testLevel.get(SLOTLEVEL.EIGHT), null);
			assertEquals(testLevel.get(SLOTLEVEL.NINE), null);
			
			testLevel = cClass.slotsPerLevel.get(11);
			assertEquals(testLevel.get(SLOTLEVEL.ONE), 4);
			assertEquals(testLevel.get(SLOTLEVEL.TWO), 3);
			assertEquals(testLevel.get(SLOTLEVEL.THREE), 3);
			assertEquals(testLevel.get(SLOTLEVEL.FOUR), 3);
			assertEquals(testLevel.get(SLOTLEVEL.FIVE), 2);
			assertEquals(testLevel.get(SLOTLEVEL.SIX), 1);
			assertEquals(testLevel.get(SLOTLEVEL.SEVEN), null);
			assertEquals(testLevel.get(SLOTLEVEL.EIGHT), null);
			assertEquals(testLevel.get(SLOTLEVEL.NINE), null);
			
			testLevel = cClass.slotsPerLevel.get(12);
			assertEquals(testLevel.get(SLOTLEVEL.ONE), 4);
			assertEquals(testLevel.get(SLOTLEVEL.TWO), 3);
			assertEquals(testLevel.get(SLOTLEVEL.THREE), 3);
			assertEquals(testLevel.get(SLOTLEVEL.FOUR), 3);
			assertEquals(testLevel.get(SLOTLEVEL.FIVE), 2);
			assertEquals(testLevel.get(SLOTLEVEL.SIX), 1);
			assertEquals(testLevel.get(SLOTLEVEL.SEVEN), null);
			assertEquals(testLevel.get(SLOTLEVEL.EIGHT), null);
			assertEquals(testLevel.get(SLOTLEVEL.NINE), null);
			
			testLevel = cClass.slotsPerLevel.get(13);
			assertEquals(testLevel.get(SLOTLEVEL.ONE), 4);
			assertEquals(testLevel.get(SLOTLEVEL.TWO), 3);
			assertEquals(testLevel.get(SLOTLEVEL.THREE), 3);
			assertEquals(testLevel.get(SLOTLEVEL.FOUR), 3);
			assertEquals(testLevel.get(SLOTLEVEL.FIVE), 2);
			assertEquals(testLevel.get(SLOTLEVEL.SIX), 1);
			assertEquals(testLevel.get(SLOTLEVEL.SEVEN), 1);
			assertEquals(testLevel.get(SLOTLEVEL.EIGHT), null);
			assertEquals(testLevel.get(SLOTLEVEL.NINE), null);
			
			testLevel = cClass.slotsPerLevel.get(14);
			assertEquals(testLevel.get(SLOTLEVEL.ONE), 4);
			assertEquals(testLevel.get(SLOTLEVEL.TWO), 3);
			assertEquals(testLevel.get(SLOTLEVEL.THREE), 3);
			assertEquals(testLevel.get(SLOTLEVEL.FOUR), 3);
			assertEquals(testLevel.get(SLOTLEVEL.FIVE), 2);
			assertEquals(testLevel.get(SLOTLEVEL.SIX), 1);
			assertEquals(testLevel.get(SLOTLEVEL.SEVEN), 1);
			assertEquals(testLevel.get(SLOTLEVEL.EIGHT), null);
			assertEquals(testLevel.get(SLOTLEVEL.NINE), null);
			
			testLevel = cClass.slotsPerLevel.get(15);
			assertEquals(testLevel.get(SLOTLEVEL.ONE), 4);
			assertEquals(testLevel.get(SLOTLEVEL.TWO), 3);
			assertEquals(testLevel.get(SLOTLEVEL.THREE), 3);
			assertEquals(testLevel.get(SLOTLEVEL.FOUR), 3);
			assertEquals(testLevel.get(SLOTLEVEL.FIVE), 2);
			assertEquals(testLevel.get(SLOTLEVEL.SIX), 1);
			assertEquals(testLevel.get(SLOTLEVEL.SEVEN), 1);
			assertEquals(testLevel.get(SLOTLEVEL.EIGHT), 1);
			assertEquals(testLevel.get(SLOTLEVEL.NINE), null);
			
			testLevel = cClass.slotsPerLevel.get(16);
			assertEquals(testLevel.get(SLOTLEVEL.ONE), 4);
			assertEquals(testLevel.get(SLOTLEVEL.TWO), 3);
			assertEquals(testLevel.get(SLOTLEVEL.THREE), 3);
			assertEquals(testLevel.get(SLOTLEVEL.FOUR), 3);
			assertEquals(testLevel.get(SLOTLEVEL.FIVE), 2);
			assertEquals(testLevel.get(SLOTLEVEL.SIX), 1);
			assertEquals(testLevel.get(SLOTLEVEL.SEVEN), 1);
			assertEquals(testLevel.get(SLOTLEVEL.EIGHT), 1);
			assertEquals(testLevel.get(SLOTLEVEL.NINE), null);
			
			testLevel = cClass.slotsPerLevel.get(17);
			assertEquals(testLevel.get(SLOTLEVEL.ONE), 4);
			assertEquals(testLevel.get(SLOTLEVEL.TWO), 3);
			assertEquals(testLevel.get(SLOTLEVEL.THREE), 3);
			assertEquals(testLevel.get(SLOTLEVEL.FOUR), 3);
			assertEquals(testLevel.get(SLOTLEVEL.FIVE), 2);
			assertEquals(testLevel.get(SLOTLEVEL.SIX), 1);
			assertEquals(testLevel.get(SLOTLEVEL.SEVEN), 1);
			assertEquals(testLevel.get(SLOTLEVEL.EIGHT), 1);
			assertEquals(testLevel.get(SLOTLEVEL.NINE), 1);
			
			testLevel = cClass.slotsPerLevel.get(18);
			assertEquals(testLevel.get(SLOTLEVEL.ONE), 4);
			assertEquals(testLevel.get(SLOTLEVEL.TWO), 3);
			assertEquals(testLevel.get(SLOTLEVEL.THREE), 3);
			assertEquals(testLevel.get(SLOTLEVEL.FOUR), 3);
			assertEquals(testLevel.get(SLOTLEVEL.FIVE), 3);
			assertEquals(testLevel.get(SLOTLEVEL.SIX), 1);
			assertEquals(testLevel.get(SLOTLEVEL.SEVEN), 1);
			assertEquals(testLevel.get(SLOTLEVEL.EIGHT), 1);
			assertEquals(testLevel.get(SLOTLEVEL.NINE), 1);
			
			testLevel = cClass.slotsPerLevel.get(19);
			assertEquals(testLevel.get(SLOTLEVEL.ONE), 4);
			assertEquals(testLevel.get(SLOTLEVEL.TWO), 3);
			assertEquals(testLevel.get(SLOTLEVEL.THREE), 3);
			assertEquals(testLevel.get(SLOTLEVEL.FOUR), 3);
			assertEquals(testLevel.get(SLOTLEVEL.FIVE), 3);
			assertEquals(testLevel.get(SLOTLEVEL.SIX), 2);
			assertEquals(testLevel.get(SLOTLEVEL.SEVEN), 1);
			assertEquals(testLevel.get(SLOTLEVEL.EIGHT), 1);
			assertEquals(testLevel.get(SLOTLEVEL.NINE), 1);
			
			testLevel = cClass.slotsPerLevel.get(20);
			assertEquals(testLevel.get(SLOTLEVEL.ONE), 4);
			assertEquals(testLevel.get(SLOTLEVEL.TWO), 3);
			assertEquals(testLevel.get(SLOTLEVEL.THREE), 3);
			assertEquals(testLevel.get(SLOTLEVEL.FOUR), 3);
			assertEquals(testLevel.get(SLOTLEVEL.FIVE), 3);
			assertEquals(testLevel.get(SLOTLEVEL.SIX), 2);
			assertEquals(testLevel.get(SLOTLEVEL.SEVEN), 2);
			assertEquals(testLevel.get(SLOTLEVEL.EIGHT), 1);
			assertEquals(testLevel.get(SLOTLEVEL.NINE), 1);
			
			assertTrue(cClass.savingThrowProficiencies.contains(ESSENTIAL_ABILITY_SCORE.WISDOM));
			assertTrue(cClass.savingThrowProficiencies.contains(ESSENTIAL_ABILITY_SCORE.CHARISMA));
		}
		assertTrue(foundCleric);
	}

}
