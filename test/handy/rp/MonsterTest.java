package handy.rp;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import handy.rp.dnd.CharClass.SPELLCASTING_MODIFIER;
import handy.rp.dnd.attacks.Attack;
import handy.rp.dnd.attacks.Damage;
import handy.rp.dnd.attacks.DamageComponent.DAMAGE_TYPE;
import handy.rp.dnd.monsters.MonsterInstance;
import handy.rp.dnd.monsters.MonsterTemplate;
import handy.rp.xml.MonsterParser;

class MonsterTest {

	@Test
	void testFireGiant() {
		try {
			MonsterTemplate fireGiant = MonsterParser.load("monsters\\FireGiant.xml");
			assertEquals(fireGiant.str, 25);
			assertEquals(fireGiant.con, 31);
			assertEquals(fireGiant.dex, 9);
			assertEquals(fireGiant.inte, 10);
			assertEquals(fireGiant.wis, 14);
			assertEquals(fireGiant.cha, 13);
			
			assertEquals(fireGiant.strsave, 7);
			assertEquals(fireGiant.consave, 10);
			assertEquals(fireGiant.dexsave, 3);
			assertEquals(fireGiant.intsave, 0);
			assertEquals(fireGiant.wissave, 2);
			assertEquals(fireGiant.chasave, 5);
			
			assertEquals(fireGiant.ac, 18);
			assertEquals(fireGiant.speed, 30);
			assertEquals(fireGiant.attrs, "N/A");
			
			assertEquals(fireGiant.spellcastingMod, SPELLCASTING_MODIFIER.NA);
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	@Test
	void test() {
		try {
			MonsterTemplate hillGiant = MonsterParser.load("monsters\\HillGiant.xml");
			
			assertEquals(hillGiant.humanReadableName, "Hill Giant");
			assertEquals(hillGiant.maxHP, 105);
			assertEquals(hillGiant.str, 21);
			assertEquals(hillGiant.con, 19);
			assertEquals(hillGiant.dex, 8);
			assertEquals(hillGiant.inte, 5);
			assertEquals(hillGiant.wis, 9);
			assertEquals(hillGiant.cha, 6);
			
			assertTrue(hillGiant.getAttacks().size() == 2);
			assertTrue(hillGiant.getAttacks().get(0).size() == 2);
			assertTrue(hillGiant.getAttacks().get(1).size() == 1);
			
			assertTrue(hillGiant.casterDc == -1);
			assertTrue(hillGiant.casterLevel == -1);
			assertTrue(hillGiant.casterToHit == -1);
			
			Attack s1a1 = hillGiant.getAttacks().get(0).get(0);
			Attack s1a2 = hillGiant.getAttacks().get(0).get(1);
			Attack s2a1 = hillGiant.getAttacks().get(1).get(0);
			
			Set<Damage> d1 = s1a1.rollDamage();
			assertTrue(d1.size() == 1);
			for(Damage damage : d1) {
				assertTrue(damage.toHit >= 9 && damage.toHit <= 28);
				assertTrue(damage.damageType == DAMAGE_TYPE.BLUDGEONING);
				assertTrue(damage.damage >= 8 && damage.damage <= 29);
			}
			
			Set<Damage> d2 = s1a2.rollDamage();
			assertTrue(d2.size() == 1);
			for(Damage damage : d2) {
				assertTrue(damage.toHit >= 9 && damage.toHit <= 28);
				assertTrue(damage.damageType == DAMAGE_TYPE.BLUDGEONING);
				assertTrue(damage.damage >= 8 && damage.damage <= 29);
			}
			
			Set<Damage> d3 = s2a1.rollDamage();
			assertTrue(d3.size() == 1);
			for(Damage damage : d3) {
				assertTrue(damage.toHit >= 9 && damage.toHit <= 28);
				assertTrue(damage.damageType == DAMAGE_TYPE.BLUDGEONING);
				assertTrue(damage.damage >= 8 && damage.damage <= 35);
			}
			
			MonsterInstance giant1 = hillGiant.getInstance("Trogdor");
			assertEquals(giant1.personalName, "Trogdor");
			assertEquals(giant1.humanReadableName, "Hill Giant");
			assertEquals(giant1.getMaxHp(), 105);
			assertEquals(giant1.getStr(), 21);
			assertEquals(giant1.getCon(), 19);
			assertEquals(giant1.getDex(), 8);
			assertEquals(giant1.getInte(), 5);
			assertEquals(giant1.getWis(), 9);
			assertEquals(giant1.getCha(), 6);
			assertEquals(giant1.getCurrentHp(), 105);
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
	
	@Test 
	void testPrintAttributes() {
		try {
			MonsterTemplate drowMage = MonsterParser.load("monsters\\drow_mage.xml");
			MonsterInstance trogdor = drowMage.getInstance("Trogdor");
			
			assertTrue(trogdor.listStats().startsWith("AC: 12" + System.lineSeparator() + "Speed: 30" + System.lineSeparator() +
					"Attributes: Fey Ancestry. The drow has advantage on saving throws against being charmed, and magic can't put the drow to sleep."));
			assertTrue(trogdor.listStats().contains("Sunlight Sensitivity. While in sunlight, the drow has disadvantage on attack rolls, as well as on Wisdom (Perception) checks that rely on sight. ")); 
			
		}catch(Exception ex) {
			fail(ex.getMessage());
		}
	}
	
	@Test
	void testInnateLoad() {
		try {
			MonsterTemplate drowMage = MonsterParser.load("monsters\\drow_mage.xml");
			MonsterInstance trogdor = drowMage.getInstance("Trogdor");
			assertEquals(trogdor.casterInnateDc, 12);
			
			assertTrue(trogdor.expendInnateSpell("levitate").readableName.contentEquals("Levitate"));
			assertTrue(trogdor.expendInnateSpell("faerie_fire").readableName.contentEquals("Faerie Fire"));
		}catch(Exception ex) {
			fail(ex.getMessage());
		}
		
		try {
			MonsterTemplate drowMage = MonsterParser.load("monsters\\drow_mage.xml");
			MonsterInstance trogdor = drowMage.getInstance("Trogdor");
			assertEquals(trogdor.casterInnateDc, 12);
			
			assertTrue(trogdor.expendInnateSpell("levitate").readableName.contentEquals("Levitate"));
			Assertions.assertThrows(IllegalArgumentException.class, () -> {
				trogdor.expendInnateSpell("darkness").readableName.contentEquals("Darkness");
			  });
		}catch(Exception ex) {
			fail(ex.getMessage());
		}
	}
	
	@Test
	void testResetSingleAttack() {
		try {
			MonsterTemplate mage = MonsterParser.load("monsters\\mage.xml");
			MonsterInstance trogdor = mage.getInstance("Trogdor");
			Attack at = trogdor.expendAttack(0);
			assertEquals(at.readableAttackName, "Dagger (ranged 20/60 or melee)");
			assertEquals(trogdor.listRemainingAttacksReadable(), "");
			trogdor.resetTurn();
			at = trogdor.expendAttack(0);
			assertEquals(at.readableAttackName, "Dagger (ranged 20/60 or melee)");
		}catch(Exception ex) {
			fail(ex.getMessage());
		}
	}
	
	@Test
	void testReaction() {
		try {
			MonsterTemplate dk = MonsterParser.load("monsters\\DeathKnight.xml");
			MonsterInstance arthas = dk.getInstance("Arthas");
			String response = arthas.expendReaction("parry");
			assertEquals(response, "Arthas takes reaction: parry" + System.lineSeparator() + "The death knight adds 6 to its AC against one melee attack that would hit it. To do so, the death knight must see the attacker and be wielding a melee weapon.");
			response = arthas.expendReaction("oppAtt");
			assertEquals(response, "Arthas cannot take reaction: oppAtt");
			arthas.notifyNewTurn();
			response = arthas.expendReaction("oppAtt");
			assertTrue(response.contains("Available attacks for opportunity attack: Longsword hits for "));
			assertTrue(response.contains("\r\nLongsword hits for "));
			assertTrue(response.contains("\r\nHeavy Crossbow hits for "));
		}catch(Exception ex) {
			fail(ex.getMessage());
		}
	}
	
	@Test
	void testLoadAll() {
		try {
			List<MonsterTemplate> allLoaded = MonsterParser.loadAll("monsters");
			assertTrue(allLoaded.size() >= 2);
			boolean foundHillGiant = false;
			boolean foundFireGiant = false;
			
			for(MonsterTemplate mt : allLoaded) {
				if(mt.humanReadableName.equals("Hill Giant")) {
					foundHillGiant = true;
				}else if(mt.humanReadableName.equals("Fire Giant")){
					foundFireGiant = true;
				}
			}
			
			assertTrue(foundHillGiant);
			assertTrue(foundFireGiant);
		}catch(Exception e) {
			e.printStackTrace();
			fail();
		}
	}

}
