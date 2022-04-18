package handy.rp.xml.fortyk;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import handy.rp.fortyk.datamodel.Unit;
import handy.rp.fortyk.datamodel.StatBlock.StatElement;

class UnitParserTest {
	@Test
	void testLoadsBoyz() {
		Unit boyz = UnitParser.getUnitByName("Ork Boyz");
		assertEquals(boyz.leadModel.getCurrentStats().attacks, StatElement.THREE);
		assertEquals(boyz.leadModel.getCurrentStats().ballisticSkill, 5);
		assertEquals(boyz.leadModel.getCurrentStats().leadership, 7);
		assertEquals(boyz.leadModel.getCurrentStats().movement, 5);
		assertEquals(boyz.leadModel.getCurrentStats().points, 9);
		assertEquals(boyz.leadModel.getCurrentStats().save, 6);
		assertEquals(boyz.leadModel.getCurrentStats().strength, 5);
		assertEquals(boyz.leadModel.getCurrentStats().toughness, 5);
		assertEquals(boyz.leadModel.getCurrentStats().weaponSkill, 3);
		assertEquals(boyz.leadModel.getCurrentStats().wounds, 2);
		
		assertEquals(boyz.commonModel.getCurrentStats().attacks, StatElement.TWO);
		assertEquals(boyz.commonModel.getCurrentStats().ballisticSkill, 5);
		assertEquals(boyz.commonModel.getCurrentStats().leadership, 6);
		assertEquals(boyz.commonModel.getCurrentStats().movement, 5);
		assertEquals(boyz.commonModel.getCurrentStats().points, 9);
		assertEquals(boyz.commonModel.getCurrentStats().save, 6);
		assertEquals(boyz.commonModel.getCurrentStats().strength, 4);
		assertEquals(boyz.commonModel.getCurrentStats().toughness, 5);
		assertEquals(boyz.commonModel.getCurrentStats().weaponSkill, 3);
		assertEquals(boyz.commonModel.getCurrentStats().wounds, 1);
	}
	
	@Test
	void testLoadsTrukk() {
		Unit trukk = UnitParser.getUnitByName("Trukk");
		assertTrue(trukk.leadModel == null);
		
		assertEquals(trukk.commonModel.getCurrentStats().attacks, StatElement.THREE);
		assertEquals(trukk.commonModel.getCurrentStats().ballisticSkill, 5);
		assertEquals(trukk.commonModel.getCurrentStats().leadership, 6);
		assertEquals(trukk.commonModel.getCurrentStats().movement, 12);
		assertEquals(trukk.commonModel.getCurrentStats().points, 70);
		assertEquals(trukk.commonModel.getCurrentStats().save, 4);
		assertEquals(trukk.commonModel.getCurrentStats().strength, 6);
		assertEquals(trukk.commonModel.getCurrentStats().toughness, 6);
		assertEquals(trukk.commonModel.getCurrentStats().weaponSkill, 5);
		assertEquals(trukk.commonModel.getCurrentStats().wounds, 10);
	}
		
}
