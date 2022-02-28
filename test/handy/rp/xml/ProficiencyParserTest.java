package handy.rp.xml;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import handy.rp.dnd.character.Proficiency;

class ProficiencyParserTest {

	@Test
	void testArmor() {
		boolean foundLight = false;
		boolean foundMedium = false;
		boolean foundHeavy = false;
		boolean foundShield = false;
		
		for(Proficiency prof : ProficiencyParser.armorProficiencies) {
			if(prof.name.equals("light")) {
				foundLight = true;
			}else if(prof.name.equals("medium")) {
				foundMedium = true;
			}else if(prof.name.equals("heavy")) {
				foundHeavy = true;
			}else if(prof.name.equals("shield")) {
				foundShield = true;
			}
		}
		
		assertTrue(foundLight);
		assertTrue(foundMedium);
		assertTrue(foundHeavy);
		assertTrue(foundShield);
	}
	
	@Test 
	void testTools() {
		boolean foundThief = false;
		boolean foundSmith = false;
		
		for(Proficiency prof : ProficiencyParser.toolProficiencies) {
			if(prof.name.equals("thief")) {
				foundThief = true;
			}else if(prof.name.equals("smith")) {
				foundSmith = true;
			}
		}
		
		assertTrue(foundThief);
		assertTrue(foundSmith);
	}

}
