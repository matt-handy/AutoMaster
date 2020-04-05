package handy.rp.dnd;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import handy.rp.dnd.attacks.Attack;
import handy.rp.dnd.attacks.Damage;
import handy.rp.dnd.attacks.DamageComponent.DAMAGE_TYPE;
import handy.rp.dnd.spells.Spell;
import handy.rp.xml.ActionParser;
import handy.rp.xml.SpellParser;

class ActionTest {

	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testLoadAll() {
		try {
			List<Action> allLoaded = ActionParser.loadAll("actions");
			boolean foundSummonDemon = false;
			boolean foundDKMultiattack = false;
			boolean foundHellfireOrb = false;
			for(Action action : allLoaded) {
				if(action.cname.equals("summon_demon")) {
					foundSummonDemon = true;
					assertEquals("Summon Demon", action.name);
					assertEquals(0, action.attack.size());
					assertTrue(action.rechargeDice == null);
					assertEquals("The drow magically summons a quasit, or attempts to summon a shadow demon with a 50 percent chance of success. The summoned demon appears in an unoccupied space within 60 feet of its summoner, acts as an ally of its summoner, and can't summon other demons. It remains for 10 minutes, until it or its summoner dies, or until its summoner dismisses it as an action.", action.text);
					assertTrue(action.spell == null);
				}else if(action.cname.equals("dk_multiattack")) {
					foundDKMultiattack = true;
					assertEquals("Multiattack", action.name);
					assertTrue(action.rechargeDice == null);
					assertTrue(action.text == null);
					assertTrue(action.spell == null);
					assertEquals(3, action.attack.size());
					for(Attack attack : action.attack) {
						assertEquals(11, attack.toHit);
						assertEquals("Longsword", attack.readableAttackName);
						Set<Damage> damages = attack.rollDamage();
						assertEquals(1, damages.size());
						Damage damage = damages.iterator().next();
						assertEquals(DAMAGE_TYPE.SLASHING, damage.damageType);
					}
				}else if(action.cname.equals("hellfire_orb")) {
					foundHellfireOrb = true;
					assertEquals("Hellfire Orb", action.name);
					assertTrue(action.rechargeDice == null);
					assertTrue(action.text == null);
					assertFalse(action.spell == null);
					assertEquals("Hellfire Orb", action.spell.readableName);
				}
			}
			
			assertTrue(foundDKMultiattack);
			assertTrue(foundSummonDemon);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
