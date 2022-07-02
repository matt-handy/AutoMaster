package handy.rp.dnd.spells;

import java.util.List;
import java.util.Map;

import handy.rp.dnd.CharClass;


public class ActionSpell extends Spell {

	public final int staticDC;
	public final int staticToHit;
	public ActionSpell(String computerName, String readableName, SLOTLEVEL minimumLevel, boolean saveDc, boolean toHit,
			Map<SLOTLEVEL, List<SpellDamageComponent>> damagers, String readableEffect, boolean concentrate,
			int staticDC, int staticToHit, SpellHealingComponent healingComponent, Map<SLOTLEVEL, List<SpellDamageComponent>> altDamagers, boolean noDamageOnSave, List<CharClass> applicableClasses, int activeACBonus, int temporaryACBonus) {
		super(computerName, readableName, minimumLevel, saveDc, toHit, damagers, readableEffect, concentrate, false, null, healingComponent, altDamagers, noDamageOnSave, applicableClasses, activeACBonus, temporaryACBonus);
		this.staticDC = staticDC;
		this.staticToHit = staticToHit;
	}
	
	public String cast() {
		//All static action spells are labeled as max for level and caster level, they are not used
		return super.cast(SLOTLEVEL.NINE, 20, staticDC, staticToHit, -1);
	}

}
