package handy.rp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import handy.rp.dnd.attacks.Attack;
import handy.rp.dnd.attacks.Damage;
import handy.rp.dnd.attacks.DamageComponent.DAMAGE_TYPE;
import handy.rp.dnd.monsters.MonsterInstance;
import handy.rp.dnd.monsters.MonsterTemplate;
import handy.rp.dnd.spells.Spell.SLOTLEVEL;
import handy.rp.xml.MonsterParser;

class MagicMonsterTest {

	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testMage() {

		try {
			MonsterTemplate mage = MonsterParser.load("monsters\\mage.xml");

			assertEquals(mage.humanReadableName, "Mage");
			assertEquals(mage.maxHP, 40);
			assertEquals(mage.str, 9);
			assertEquals(mage.con, 11);
			assertEquals(mage.dex, 14);
			assertEquals(mage.inte, 17);
			assertEquals(mage.wis, 12);
			assertEquals(mage.cha, 11);

			assertTrue(mage.getAttacks().size() == 1);
			assertTrue(mage.getAttacks().get(0).size() == 1);

			assertTrue(mage.casterDc == 14);
			assertTrue(mage.casterLevel == 9);
			assertTrue(mage.casterToHit == 6);

			Attack s1a1 = mage.getAttacks().get(0).get(0);

			Set<Damage> d1 = s1a1.rollDamage();
			assertTrue(d1.size() == 1);
			for (Damage damage : d1) {
				assertTrue(damage.toHit >= 4 && damage.toHit <= 24);
				assertTrue(damage.damageType == DAMAGE_TYPE.PIERCING);
				assertTrue(damage.damage >= 3 && damage.damage <= 6);
			}

			MonsterInstance mageInst = mage.getInstance("Tim");
			assertEquals(mageInst.personalName, "Tim");
			assertEquals(mageInst.humanReadableName, "Mage");
			assertEquals(mageInst.maxHP, 40);
			assertEquals(mageInst.str, 9);
			assertEquals(mageInst.con, 11);
			assertEquals(mageInst.dex, 14);
			assertEquals(mageInst.inte, 17);
			assertEquals(mageInst.wis, 12);
			assertEquals(mageInst.cha, 11);
			assertEquals(mageInst.getCurrentHp(), 40);

			assertTrue(mageInst.listSpells().contains("Level: 1 Detect Magic, Mage Armor, Magic Missile, Shield, "));
			assertTrue(mageInst.listSpells().contains("Level: 2 Misty Step, Suggestion, "));
			assertTrue(mageInst.listSpells().contains("Level: 3 Counterspell, Fireball, Fly, "));
			assertTrue(mageInst.listSpells().contains("Level: 4 Greater Invisibility, Ice Storm, "));
			assertTrue(mageInst.listSpells().contains("Level: 5 Cone of Cold, "));
			assertTrue(
					mageInst.listSpells().contains("Level: Cantrip Fire Bolt, Light, Mage Hand, Prestidigitation, "));

			assertTrue(mageInst.listSpellSlotsRemaining().contains("Level 5: 1,"));
			assertTrue(mageInst.listSpellSlotsRemaining().contains("Level 4: 3,"));
			assertTrue(mageInst.listSpellSlotsRemaining().contains("Level 3: 3,"));
			assertTrue(mageInst.listSpellSlotsRemaining().contains("Level 2: 3,"));
			assertTrue(mageInst.listSpellSlotsRemaining().contains("Level 1: 4"));

			mageInst.expendSpell("fireball");

			assertTrue(mageInst.listSpellSlotsRemaining().contains("Level 5: 1,"));
			assertTrue(mageInst.listSpellSlotsRemaining().contains("Level 4: 3,"));
			assertTrue(mageInst.listSpellSlotsRemaining().contains("Level 3: 2,"));
			assertTrue(mageInst.listSpellSlotsRemaining().contains("Level 2: 3,"));
			assertTrue(mageInst.listSpellSlotsRemaining().contains("Level 1: 4"));

			mageInst.expendSpell("fireball", SLOTLEVEL.FIVE);

			assertTrue(mageInst.listSpellSlotsRemaining().contains("Level 5: 0,"));
			assertTrue(mageInst.listSpellSlotsRemaining().contains("Level 4: 3,"));
			assertTrue(mageInst.listSpellSlotsRemaining().contains("Level 3: 2,"));
			assertTrue(mageInst.listSpellSlotsRemaining().contains("Level 2: 3,"));
			assertTrue(mageInst.listSpellSlotsRemaining().contains("Level 1: 4"));

			assertTrue(mageInst.expendSpell("suggestion")
					.cast(SLOTLEVEL.TWO, mageInst.casterLevel, mageInst.casterDc, mageInst.casterToHit)
					.contains("Spell Save: 14"));

		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

	}
	
	@Test
	void testConcentration() {
		try {
			MonsterTemplate mage = MonsterParser.load("monsters\\mage.xml");
			MonsterInstance mageInst = mage.getInstance("Tim");
			
			assertTrue(mageInst.listSpellSlotsRemaining().contains("Level 5: 1,"));
			assertTrue(mageInst.listSpellSlotsRemaining().contains("Level 4: 3,"));
			assertTrue(mageInst.listSpellSlotsRemaining().contains("Level 3: 3,"));
			assertTrue(mageInst.listSpellSlotsRemaining().contains("Level 2: 3,"));
			assertTrue(mageInst.listSpellSlotsRemaining().contains("Level 1: 4"));
			
			mageInst.expendSpell("fly");
			
			assertTrue(mageInst.listSpellSlotsRemaining().contains("Level 5: 1,"));
			assertTrue(mageInst.listSpellSlotsRemaining().contains("Level 4: 3,"));
			assertTrue(mageInst.listSpellSlotsRemaining().contains("Level 3: 2,"));
			assertTrue(mageInst.listSpellSlotsRemaining().contains("Level 2: 3,"));
			assertTrue(mageInst.listSpellSlotsRemaining().contains("Level 1: 4"));
			
			assertThrows(IllegalArgumentException.class, () -> {mageInst.expendSpell("fly");});
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

}
