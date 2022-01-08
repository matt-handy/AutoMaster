package handy.rp.dnd.attacks;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import handy.rp.Dice.DICE_TYPE;
import handy.rp.dnd.attacks.DamageComponent.DAMAGE_TYPE;
import handy.rp.dnd.attacks.Weapon.WEAPON_ATTRIBUTES;
import handy.rp.xml.WeaponParser;

class WeaponTest {

	@Test
	void testLoadHeavyCrossbow() {
		try {
			Weapon axe = null;
			for(Weapon weapon : WeaponParser.weapons) {
				if(weapon.cname.equalsIgnoreCase("heavy_crossbow")) {
					axe = weapon;
				}
			}
			assertTrue(axe != null);
			
			assertEquals("Heavy Crossbow", axe.name);
			assertEquals(DAMAGE_TYPE.PIERCING, axe.damageType);
			assertEquals(1, axe.diceCount);
			assertEquals(true, axe.isRanged);
			assertEquals(DICE_TYPE.D10, axe.diceType);
			assertEquals(false, axe.hasThrownOption);
			assertEquals(0, axe.modifier);
			assertEquals("100/400", axe.range);
			
			assertEquals(4, axe.getAttributes().size());
			assertTrue(axe.getAttributes().contains(WEAPON_ATTRIBUTES.TWO_HANDED));
			assertTrue(axe.getAttributes().contains(WEAPON_ATTRIBUTES.HEAVY));
			assertTrue(axe.getAttributes().contains(WEAPON_ATTRIBUTES.LOADING));
			assertTrue(axe.getAttributes().contains(WEAPON_ATTRIBUTES.AMMUNITION));
		}catch(Exception ex) {
			fail(ex.getLocalizedMessage());
		}
	}
	
	@Test
	void testLoadDagger() {
		try {
			Weapon axe = null;
			for(Weapon weapon : WeaponParser.weapons) {
				if(weapon.cname.equalsIgnoreCase("dagger")) {
					axe = weapon;
				}
			}
			assertTrue(axe != null);
			
			assertEquals("Dagger", axe.name);
			assertEquals(DAMAGE_TYPE.PIERCING, axe.damageType);
			assertEquals(1, axe.diceCount);
			assertEquals(false, axe.isRanged);
			assertEquals(DICE_TYPE.D4, axe.diceType);
			assertEquals(true, axe.hasThrownOption);
			assertEquals(0, axe.modifier);
			assertEquals("20/60", axe.thrownRange);
			
			assertEquals(3, axe.getAttributes().size());
			assertTrue(axe.getAttributes().contains(WEAPON_ATTRIBUTES.FINESSE));
			assertTrue(axe.getAttributes().contains(WEAPON_ATTRIBUTES.LIGHT));
			assertTrue(axe.getAttributes().contains(WEAPON_ATTRIBUTES.THROWN));
		}catch(Exception ex) {
			fail(ex.getLocalizedMessage());
		}
	}
	
	@Test
	void testLoadGreataxe() {
		try {
			Weapon axe = null;
			for(Weapon weapon : WeaponParser.weapons) {
				if(weapon.cname.equalsIgnoreCase("greataxe")) {
					axe = weapon;
				}
			}
			assertTrue(axe != null);
			
			assertEquals("Greataxe", axe.name);
			assertEquals(DAMAGE_TYPE.SLASHING, axe.damageType);
			assertEquals(1, axe.diceCount);
			assertEquals(false, axe.isRanged);
			assertEquals(DICE_TYPE.D12, axe.diceType);
			assertEquals(false, axe.hasThrownOption);
			assertEquals(0, axe.modifier);
			assertEquals("melee", axe.range);
			
			assertEquals(2, axe.getAttributes().size());
			assertTrue(axe.getAttributes().contains(WEAPON_ATTRIBUTES.HEAVY));
			assertTrue(axe.getAttributes().contains(WEAPON_ATTRIBUTES.TWO_HANDED));
		}catch(Exception ex) {
			fail(ex.getLocalizedMessage());
		}
	}
	
	@Test
	void testLoadWarhammer() {
		try {
			Weapon axe = null;
			for(Weapon weapon : WeaponParser.weapons) {
				if(weapon.cname.equalsIgnoreCase("warhammer")) {
					axe = weapon;
				}
			}
			assertTrue(axe != null);
			
			assertEquals("Warhammer", axe.name);
			assertEquals(DAMAGE_TYPE.BLUDGEONING, axe.damageType);
			assertEquals(1, axe.diceCount);
			assertEquals(false, axe.isRanged);
			assertEquals(DICE_TYPE.D8, axe.diceType);
			assertEquals(DICE_TYPE.D10, axe.versatileDiceType);
			assertEquals(false, axe.hasThrownOption);
			assertEquals(0, axe.modifier);
			assertEquals("melee", axe.range);
			
			assertEquals(1, axe.getAttributes().size());
			assertTrue(axe.getAttributes().contains(WEAPON_ATTRIBUTES.VERSATILE));
		}catch(Exception ex) {
			fail(ex.getLocalizedMessage());
		}
	}
	
	@Test
	void testLoadLightCrossbow() {
		try {
			Weapon axe = null;
			for(Weapon weapon : WeaponParser.weapons) {
				if(weapon.cname.equalsIgnoreCase("light_crossbow")) {
					axe = weapon;
				}
			}
			assertTrue(axe != null);
			
			assertEquals("Light Crossbow", axe.name);
			assertEquals(DAMAGE_TYPE.PIERCING, axe.damageType);
			assertEquals(1, axe.diceCount);
			assertEquals(true, axe.isRanged);
			assertEquals(DICE_TYPE.D8, axe.diceType);
			assertEquals(false, axe.hasThrownOption);
			assertEquals(0, axe.modifier);
			assertEquals("80/320", axe.range);
			
			assertEquals(3, axe.getAttributes().size());
			assertTrue(axe.getAttributes().contains(WEAPON_ATTRIBUTES.TWO_HANDED));
			assertTrue(axe.getAttributes().contains(WEAPON_ATTRIBUTES.LOADING));
			assertTrue(axe.getAttributes().contains(WEAPON_ATTRIBUTES.AMMUNITION));
		}catch(Exception ex) {
			fail(ex.getLocalizedMessage());
		}
	}

}
