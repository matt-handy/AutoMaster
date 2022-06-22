package handy.rp.fortyk;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import handy.rp.fortyk.datamodel.Army;
import handy.rp.fortyk.datamodel.UnitInstance;
import handy.rp.xml.fortyk.ArmyParser;

class TrukkTest {

	@Test
	void testPointCalculation() {
		Army trukkBoyz = ArmyParser.getArmyByName("Trukk Boyz Test");
		UnitInstance trukk = trukkBoyz.getUnitByMnemonic("Trukk");
		assertEquals(75, trukk.getUnitPointValue());
	}
	
	@Test
	void testBigShoota() {
		Army trukkBoyz = ArmyParser.getArmyByName("Trukk Boyz Test");
		UnitInstance trukk = trukkBoyz.getUnitByMnemonic("Trukk");
		boolean sawADamageRoll = false;
		for(int idx = 0; idx < 100; idx++) {
			String ranged = trukk.rollAndFormatRangedAttack();
			if(!ranged.equalsIgnoreCase("No hits connected with the enemy")) {
				sawADamageRoll = true;
				String elements[] = ranged.split(System.lineSeparator());
				assertEquals("Weapon name: Big shoota with strength 5 and ap 0", elements[0]);
				assertTrue(elements[1].startsWith("Weapon hits unit toughness : 10 or more: "));
				assertTrue(elements[2].startsWith("Weapon hits unit toughness : 6 or more: "));
				assertTrue(elements[3].startsWith("Weapon hits unit toughness is 5 : "));
				assertTrue(elements[4].startsWith("Weapon hits unit toughness is less than 5 : "));
				//We need to have at least one hit for this to appear
				assertTrue(elements[5].startsWith("Weapon hits unit toughness is 2 or less: "));
				assertTrue(elements[5].endsWith("1 "));
				
				//Test that attacks that hit at a given toughness go down to the other layers
				if(elements[1].endsWith("1 ")) {
					assertTrue(elements[2].endsWith("1 "));
					assertTrue(elements[3].endsWith("1 "));
					assertTrue(elements[4].endsWith("1 "));
				}else if(elements[2].endsWith("1 ")) {
					assertTrue(elements[3].endsWith("1 "));
					assertTrue(elements[4].endsWith("1 "));
				}else if(elements[3].endsWith("1 ")) {
					assertTrue(elements[4].endsWith("1 "));
				}
			}
			
		}
		assertTrue(sawADamageRoll);
	}
	
	@Test
	void testGrabbinKlaw() {
		Army trukkBoyz = ArmyParser.getArmyByName("Trukk Boyz Test");
		UnitInstance trukk = trukkBoyz.getUnitByMnemonic("Trukk");
		boolean foundKlaw = false;
		boolean foundDefaultMelee = false;
		for(int idx = 0; idx <100; idx++) {
			String meleeDamage =trukk.rollAndFormatMeleeAttack(); 
			if(!meleeDamage.equals("No hits connected with the enemy")) {
				String elements[] = meleeDamage.split(System.lineSeparator());
				//We want only default attacks and grabbin klaw, and they must be aggregated into two entries
				assertTrue(elements.length == 6 || elements.length == 12);
				if(elements[0].equals("Weapon name: Grabbin' Klaw with strength 6 and ap -3")) {
					foundKlaw = true;
					assertTrue(elements[1].startsWith("Weapon hits unit toughness : 12 or more: "));
					assertTrue(elements[2].startsWith("Weapon hits unit toughness : 7 or more: "));
					assertTrue(elements[3].startsWith("Weapon hits unit toughness is 6 : "));
					assertTrue(elements[4].startsWith("Weapon hits unit toughness is less than 6 : "));
					//We need to have at least one hit for this to appear
					assertTrue(elements[5].startsWith("Weapon hits unit toughness is 3 or less: 2"));
					
					//Test that attacks that hit at a given toughness go down to the other layers
					if(elements[1].endsWith("2 ")) {
						assertTrue(elements[2].endsWith("2 "));
						assertTrue(elements[3].endsWith("2 "));
						assertTrue(elements[4].endsWith("2 "));
					}else if(elements[2].endsWith("2 ")) {
						assertTrue(elements[3].endsWith("2 "));
						assertTrue(elements[4].endsWith("2 "));
					}else if(elements[3].endsWith("2 ")) {
						assertTrue(elements[4].endsWith("2 "));
					}
				}else if(elements[0].equals("Weapon name: Default Melee with strength 6 and ap 0")) {
					foundDefaultMelee = true;
					
					assertTrue(elements[1].startsWith("Weapon hits unit toughness : 12 or more: "));
					assertTrue(elements[2].startsWith("Weapon hits unit toughness : 7 or more: "));
					assertTrue(elements[3].startsWith("Weapon hits unit toughness is 6 : "));
					assertTrue(elements[4].startsWith("Weapon hits unit toughness is less than 6 : "));
					//We need to have at least one hit for this to appear
					assertTrue(elements[5].startsWith("Weapon hits unit toughness is 3 or less: 1"));
					
					//Test that attacks that hit at a given toughness go down to the other layers
					if(elements[1].endsWith("1 ")) {
						assertTrue(elements[2].endsWith("1 "));
						assertTrue(elements[3].endsWith("1 "));
						assertTrue(elements[4].endsWith("1 "));
					}else if(elements[2].endsWith("1 ")) {
						assertTrue(elements[3].endsWith("1 "));
						assertTrue(elements[4].endsWith("1 "));
					}else if(elements[3].endsWith("1 ")) {
						assertTrue(elements[4].endsWith("1 "));
					}
				}
				if(elements.length > 6) {
					assertEquals(elements[6], "Weapon name: Default Melee with strength 6 and ap 0");
					assertTrue(elements[7].startsWith("Weapon hits unit toughness : 12 or more: "));
					assertTrue(elements[8].startsWith("Weapon hits unit toughness : 7 or more: "));
					assertTrue(elements[9].startsWith("Weapon hits unit toughness is 6 : "));
					assertTrue(elements[10].startsWith("Weapon hits unit toughness is less than 6 : "));
					//We need to have at least one hit for this to appear
					assertTrue(elements[11].startsWith("Weapon hits unit toughness is 3 or less: 1"));
				}
			}
			
		}
		assertTrue(foundDefaultMelee);
		assertTrue(foundKlaw);
	}

}
