package handy.rp.fortyk;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import handy.rp.fortyk.datamodel.Army;
import handy.rp.fortyk.datamodel.UnitInstance;
import handy.rp.xml.fortyk.ArmyParser;

class TestBoyz {

	@Test
	void testTrukkBoyz1PointsCalc() {
		Army trukkBoyz = ArmyParser.getArmyByName("Trukk Boyz Test");
		UnitInstance boyz = trukkBoyz.getUnitByMnemonic("Boyz");
		assertEquals(114, boyz.getUnitPointValue());
	}
	
	@Test
	void testTrukkBoyz2PointsCalc() {
		Army trukkBoyz = ArmyParser.getArmyByName("Trukk Boyz Test Set 2");
		UnitInstance boyz = trukkBoyz.getUnitByMnemonic("Boyz");
		assertEquals(109, boyz.getUnitPointValue());
	}
	
	@Test
	void testStickgrenades() {
		// Can we only throw one grenade and shoot?
		Army trukkBoyz = ArmyParser.getArmyByName("Trukk Boyz Test Set 2");
		UnitInstance boyz = trukkBoyz.getUnitByMnemonic("Boyz");
		boolean foundStikkbombz = false;
		for (int idx = 0; idx < 100; idx++) {
			String attack = boyz.rollAndFormatRangedAttack();
			//System.out.println(attack);
			String elements[] = attack.split(System.lineSeparator());
			
			if (!attack.equals("No hits connected with the enemy")) {
				assertTrue(elements.length == 6 || elements.length == 12 || elements.length == 18|| elements.length == 24);
				// Skip stikkbombz
				testDamageAppliesToLowerRollsToo(elements, 1, 1);
				if (elements.length > 12 && elements[6].equals("Weapon name: Stikkbombz with strength 3 and ap 0")) {
					foundStikkbombz = true;
					assertTrue(elements[7].startsWith("Weapon hits unit toughness : 6 or more: "));
					assertTrue(elements[8].startsWith("Weapon hits unit toughness : 4 or more: "));
					assertTrue(elements[9].startsWith("Weapon hits unit toughness is 3 : "));
					assertTrue(elements[10].startsWith("Weapon hits unit toughness is less than 3 : "));
					// We need to have at least one hit for this to appear
					assertTrue(elements[11].startsWith("Weapon hits unit toughness is 1 or less: "));
					assertTrue(elements[11].endsWith("1 "));
					testDamageAppliesToLowerRollsToo(elements, 1, 7);
					if (elements[11].contains("1 1 1 1 1 1 1")) {
						fail("Too many grenades, only can have 1d6 grenades per unit");
					}
				}
			}
		}

		assertTrue(foundStikkbombz);
	}

	public void testDamageAppliesToLowerRollsToo(String[] elements, int expectedDamage, int startIdx) {
		if (elements[startIdx].endsWith(expectedDamage + " ")) {
			assertTrue(elements[startIdx + 1].endsWith(expectedDamage + " "));
			assertTrue(elements[startIdx + 2].endsWith(expectedDamage + " "));
			assertTrue(elements[startIdx + 3].endsWith(expectedDamage + " "));
		} else if (elements[startIdx + 1].endsWith(expectedDamage + " ")) {
			assertTrue(elements[startIdx + 2].endsWith(expectedDamage + " "));
			assertTrue(elements[startIdx + 3].endsWith(expectedDamage + " "));
		} else if (elements[startIdx + 2].endsWith(expectedDamage + " ")) {
			assertTrue(elements[startIdx + 3].endsWith(expectedDamage + " "));
		}
	}

	@Test
	void testChoppasAndDefaultMelee() {
		// This army configuration has 1 boy with no melee and the boss nob with no
		// melee, which might show up.
		// Choppa is virtually certain.
		Army trukkBoyz = ArmyParser.getArmyByName("Trukk Boyz Test");
		UnitInstance boyz = trukkBoyz.getUnitByMnemonic("Boyz");
		boolean foundChoppas = false;
		boolean foundNobDefault = false;
		boolean foundBoyDefault = false;
		for (int idx = 0; idx < 100; idx++) {
			String attack = boyz.rollAndFormatMeleeAttack();
			String elements[] = attack.split(System.lineSeparator());
			if (!attack.equals("No hits connected with the enemy")) {
				foundChoppas = true;
				assertTrue(elements.length == 6 || elements.length == 12 || elements.length == 18
						|| elements.length == 24);
				if (elements[0].contains("Power Stabba")) {
					// Power stabba is tested elsewhere, keep going
					continue;
				}
				assertEquals("Weapon name: Choppa with strength 4 and ap -1", elements[0]);
				assertTrue(elements[1].startsWith("Weapon hits unit toughness : 8 or more: "));
				assertTrue(elements[2].startsWith("Weapon hits unit toughness : 5 or more: "));
				assertTrue(elements[3].startsWith("Weapon hits unit toughness is 4 : "));
				assertTrue(elements[4].startsWith("Weapon hits unit toughness is less than 4 : "));
				// We need to have at least one hit for this to appear
				assertTrue(elements[5].startsWith("Weapon hits unit toughness is 2 or less: 1"));
				testDamageAppliesToLowerRollsToo(elements, 1, 1);
				if (elements.length > 6 && elements[6].equals("Weapon name: Default Melee with strength 5 and ap 0")) {
					foundNobDefault = true;

					assertTrue(elements[7].startsWith("Weapon hits unit toughness : 10 or more: "));
					assertTrue(elements[8].startsWith("Weapon hits unit toughness : 6 or more: "));
					assertTrue(elements[9].startsWith("Weapon hits unit toughness is 5 : "));
					assertTrue(elements[10].startsWith("Weapon hits unit toughness is less than 5 : "));
					// We need to have at least one hit for this to appear
					assertTrue(elements[11].startsWith("Weapon hits unit toughness is 2 or less: 1"));
					testDamageAppliesToLowerRollsToo(elements, 1, 7);
				}
				if (elements.length == 18) {
					foundBoyDefault = true;
					assertEquals("Weapon name: Default Melee with strength 4 and ap 0", elements[12]);
					assertTrue(elements[13].startsWith("Weapon hits unit toughness : 8 or more: "));
					assertTrue(elements[14].startsWith("Weapon hits unit toughness : 5 or more: "));
					assertTrue(elements[15].startsWith("Weapon hits unit toughness is 4 : "));
					assertTrue(elements[16].startsWith("Weapon hits unit toughness is less than 4 : "));
					// We need to have at least one hit for this to appear
					assertTrue(elements[17].startsWith("Weapon hits unit toughness is 2 or less: 1"));
					testDamageAppliesToLowerRollsToo(elements, 1, 13);
				}
			}
		}

		assertTrue(foundChoppas);
		assertTrue(foundNobDefault);
		assertTrue(foundBoyDefault);
	}

	@Test
	void testBigShoota() {
		Army trukkBoyz = ArmyParser.getArmyByName("Trukk Boyz Test Set 2");
		UnitInstance boyz = trukkBoyz.getUnitByMnemonic("Boyz");
		boolean foundBigShoota = false;
		boolean foundOptionalDakkaBigShoota = false;
		for (int idx = 0; idx < 100; idx++) {
			String attack = boyz.rollAndFormatRangedAttack();
			String elements[] = attack.split(System.lineSeparator());
			
			if (!attack.equals("No hits connected with the enemy")) {
				assertTrue(elements.length == 6 || elements.length == 12 || elements.length == 18|| elements.length == 24);
				// Skip stikkbombz
				testDamageAppliesToLowerRollsToo(elements, 1, 1);
				if (elements.length > 12 && elements[12].equals("Weapon name: Big Shoota with strength 5 and ap 0")) {
					foundBigShoota = true;
					assertTrue(elements[13].startsWith("Weapon hits unit toughness : 10 or more: "));
					assertTrue(elements[14].startsWith("Weapon hits unit toughness : 6 or more: "));
					assertTrue(elements[15].startsWith("Weapon hits unit toughness is 5 : "));
					assertTrue(elements[16].startsWith("Weapon hits unit toughness is less than 5 : "));
					// We need to have at least one hit for this to appear
					assertTrue(elements[17].startsWith("Weapon hits unit toughness is 2 or less: "));
					assertTrue(elements[17].endsWith("1 "));
					testDamageAppliesToLowerRollsToo(elements, 1, 13);
					if (elements[17].contains("1 1 1 1 ")) {
						fail("Too much dakka - only one Big Shoota should be present, only three regular shots with 2 extra");
					}
					if(elements[17].contains("Optional attacks: 1")) {
						foundOptionalDakkaBigShoota = true;
					}
				}
				
			}

		}

		assertTrue(foundBigShoota);
		assertTrue(foundOptionalDakkaBigShoota);
	}

	@Test
	void testRokkit() {
		Army trukkBoyz = ArmyParser.getArmyByName("Trukk Boyz Test");
		UnitInstance boyz = trukkBoyz.getUnitByMnemonic("Boyz");
		boolean foundShoota = false;
		boolean foundRokkit = false;
		for (int idx = 0; idx < 100; idx++) {
			
			String attack = boyz.rollAndFormatRangedAttack();
			String elements[] = attack.split(System.lineSeparator());
			if (!attack.equals("No hits connected with the enemy")) {
				assertTrue(elements.length == 6 || elements.length == 12 || elements.length == 18 || elements.length == 24);
				
				if (elements[0].equals("Weapon name: Rokkit Launcha with strength 8 and ap -2")) {
					foundRokkit = true;
					assertTrue(elements[1].startsWith("Weapon hits unit toughness : 16 or more: "));
					assertTrue(elements[2].startsWith("Weapon hits unit toughness : 9 or more: "));
					assertTrue(elements[3].startsWith("Weapon hits unit toughness is 8 : "));
					assertTrue(elements[4].startsWith("Weapon hits unit toughness is less than 8 : "));
					// We need to have at least one hit for this to appear
					assertTrue(elements[5].startsWith("Weapon hits unit toughness is 4 or less: 3"));
					if(elements[5].contains("3 3 3 3")) {
						fail("No more than 3 rokkits can come from the single rocket launcher");
					}
					testDamageAppliesToLowerRollsToo(elements, 3, 1);
				}
				if (elements.length >= 18 && elements[6].equals("Weapon name: Slugga with strength 4 and ap 0")) {
					foundShoota = true;
					assertTrue(elements[7].startsWith("Weapon hits unit toughness : 8 or more: "));
					assertTrue(elements[8].startsWith("Weapon hits unit toughness : 5 or more: "));
					assertTrue(elements[9].startsWith("Weapon hits unit toughness is 4 : "));
					assertTrue(elements[10].startsWith("Weapon hits unit toughness is less than 4 : "));
					// We need to have at least one hit for this to appear
					assertTrue(elements[11].startsWith("Weapon hits unit toughness is 2 or less: 1"));
					testDamageAppliesToLowerRollsToo(elements, 1, 13);
				}
				// Skip stikkbombz and boss shoota
			}

		}

		assertTrue(foundRokkit);
		assertTrue(foundShoota);
	}

	@Test
	void testBossKombiScorcha() {
		Army trukkBoyz = ArmyParser.getArmyByName("Trukk Boyz Test Set 4");
		UnitInstance boyz = trukkBoyz.getUnitByMnemonic("Boyz");
		boolean foundShoota = false;
		boolean foundOptionalDakkaShoota = false;
		boolean foundSkorcha = false;
		for (int idx = 0; idx < 100; idx++) {
			String attack = boyz.rollAndFormatRangedAttack();
			String elements[] = attack.split(System.lineSeparator());
			//System.out.println(attack);
			
			if (!attack.equals("No hits connected with the enemy")) {
				assertTrue(elements.length == 6 || elements.length == 12 || elements.length == 18|| elements.length == 24 || elements.length == 30);
				if (elements.length == 30 && elements[24].equals("Weapon name: Skorcha with strength 5 and ap -1")) {
					foundSkorcha = true;
					assertTrue(elements[25].startsWith("Weapon hits unit toughness : 10 or more: "));
					assertTrue(elements[26].startsWith("Weapon hits unit toughness : 6 or more: "));
					assertTrue(elements[27].startsWith("Weapon hits unit toughness is 5 : "));
					assertTrue(elements[28].startsWith("Weapon hits unit toughness is less than 5 : "));
					// We need to have at least one hit for this to appear
					assertTrue(elements[29].startsWith("Weapon hits unit toughness is 2 or less: "));
					assertTrue(elements[29].endsWith("1 "));
					testDamageAppliesToLowerRollsToo(elements, 1, 24);
				}
				if (elements.length == 24 && elements[18].equals("Weapon name: Shoota with strength 4 and ap 0")) {
					foundShoota = true;
					assertTrue(elements[19].startsWith("Weapon hits unit toughness : 8 or more: "));
					assertTrue(elements[20].startsWith("Weapon hits unit toughness : 5 or more: "));
					assertTrue(elements[21].startsWith("Weapon hits unit toughness is 4 : "));
					assertTrue(elements[22].startsWith("Weapon hits unit toughness is less than 4 : "));
					// We need to have at least one hit for this to appear
					assertTrue(elements[23].startsWith("Weapon hits unit toughness is 2 or less: "));
					assertTrue(elements[23].endsWith("1 "));
					testDamageAppliesToLowerRollsToo(elements, 1, 18);
					if(elements[23].contains("Optional attacks: 1")) {
						foundOptionalDakkaShoota = true;
					}
				}
			}
			

		}

		assertTrue(foundSkorcha);
		assertTrue(foundOptionalDakkaShoota);
		assertTrue(foundShoota);
	}

	@Test
	void testBossKombiRokkit() {
		Army trukkBoyz = ArmyParser.getArmyByName("Trukk Boyz Test Set 3");
		UnitInstance boyz = trukkBoyz.getUnitByMnemonic("Boyz");
		boolean foundShoota = false;
		boolean foundOptionalDakkaShoota = false;
		boolean foundRokkit = false;
		for (int idx = 0; idx < 100; idx++) {
			String attack = boyz.rollAndFormatRangedAttack();
			String elements[] = attack.split(System.lineSeparator());
			
			if (!attack.equals("No hits connected with the enemy")) {
				assertTrue(elements.length == 6 || elements.length == 12 || elements.length == 18|| elements.length == 24 || elements.length == 30);
				// Skip stikkbombz
				if (elements.length == 30 && elements[24].equals("Weapon name: Rokkit with strength 8 and ap -2")) {
					foundRokkit = true;
					assertTrue(elements[25].startsWith("Weapon hits unit toughness : 16 or more: "));
					assertTrue(elements[26].startsWith("Weapon hits unit toughness : 9 or more: "));
					assertTrue(elements[27].startsWith("Weapon hits unit toughness is 8 : "));
					assertTrue(elements[28].startsWith("Weapon hits unit toughness is less than 8 : "));
					// We need to have at least one hit for this to appear
					assertTrue(elements[29].startsWith("Weapon hits unit toughness is 4 or less: "));
					assertTrue(elements[29].endsWith("3 "));
					testDamageAppliesToLowerRollsToo(elements, 1, 13);
					if (elements[29].contains("3 3 3 3 ")) {
						fail("Too much rokkit - only one Rokkit should be present, only D3 shots");
					}
				}
				if (elements.length == 24 && elements[18].equals("Weapon name: Shoota with strength 4 and ap 0")) {
					foundShoota = true;
					assertTrue(elements[19].startsWith("Weapon hits unit toughness : 8 or more: "));
					assertTrue(elements[20].startsWith("Weapon hits unit toughness : 5 or more: "));
					assertTrue(elements[21].startsWith("Weapon hits unit toughness is 4 : "));
					assertTrue(elements[22].startsWith("Weapon hits unit toughness is less than 4 : "));
					// We need to have at least one hit for this to appear
					assertTrue(elements[23].startsWith("Weapon hits unit toughness is 2 or less: "));
					assertTrue(elements[23].endsWith("1 "));
					testDamageAppliesToLowerRollsToo(elements, 1, 18);
					if(elements[23].contains("Optional attacks: 1")) {
						foundOptionalDakkaShoota = true;
					}
				}
			}
			

		}

		assertTrue(foundRokkit);
		assertTrue(foundOptionalDakkaShoota);
		assertTrue(foundShoota);
	}

	@Test
	void testPowerKlawAndStabbaAndKillsaw() {
		Army trukkBoyz = ArmyParser.getArmyByName("Trukk Boyz Test");
		UnitInstance boyz = trukkBoyz.getUnitByMnemonic("Boyz");
		boolean foundPowerStabba = false;
		for (int idx = 0; idx < 100; idx++) {
			String attack = boyz.rollAndFormatMeleeAttack();
			String elements[] = attack.split(System.lineSeparator());
			if (!attack.equals("No hits connected with the enemy")) {
				foundPowerStabba = true;
				
				if (elements[0].contains("Power Stabba")) {
					assertEquals("Weapon name: Power Stabba with strength 5 and ap -2", elements[0]);
					assertTrue(elements[1].startsWith("Weapon hits unit toughness : 10 or more: "));
					assertTrue(elements[2].startsWith("Weapon hits unit toughness : 6 or more: "));
					assertTrue(elements[3].startsWith("Weapon hits unit toughness is 5 : "));
					assertTrue(elements[4].startsWith("Weapon hits unit toughness is less than 5 : "));
					// We need to have at least one hit for this to appear
					assertTrue(elements[5].startsWith("Weapon hits unit toughness is 2 or less: 1"));
					testDamageAppliesToLowerRollsToo(elements, 1, 1);
				}
			}
		}

		assertTrue(foundPowerStabba);
	
	}
}
