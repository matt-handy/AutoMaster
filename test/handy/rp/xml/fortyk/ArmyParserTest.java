package handy.rp.xml.fortyk;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import handy.rp.fortyk.datamodel.Unit;
import handy.rp.fortyk.datamodel.UnitInstance;
import handy.rp.fortyk.datamodel.Army;
import handy.rp.fortyk.datamodel.StatBlock.StatElement;

class ArmyParserTest {

	@Test
	void testLoadsTrukkBoyz() {
		Army trukkBoyz = ArmyParser.getArmyByName("Trukk Boyz Test");
		assertEquals(trukkBoyz.armyName, "Trukk Boyz Test");
		assertEquals(trukkBoyz.armyComposition.size(), 2);
		UnitInstance trukk = null;
		UnitInstance boyz = null;
		for(UnitInstance unit : trukkBoyz.armyComposition) {
			if(unit.mnemonic.equals("Trukk")) {
				trukk = unit;
			}else if(unit.mnemonic.equals("Boyz")) {
				boyz = unit;
			}
		}
		
		assertTrue(trukk != null);
		assertTrue(boyz != null);
		
		assertTrue(trukk.leader == null);
		assertEquals(trukk.getModels().size(), 1);
		assertEquals(trukk.getModels().get(0).baseStats.movement, 12);//Proxy that this is a trukk
		
		assertTrue(boyz.leader != null);
		assertEquals(boyz.leader.baseStats.wounds, 2);//Proxy that this is a nob
		assertEquals(boyz.getModels().size(), 10);
		assertEquals(boyz.getModels().get(0).baseStats.wounds, 1);//Proxy that this is a normal boy
	}


}
