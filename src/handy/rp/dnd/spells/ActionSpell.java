package handy.rp.dnd.spells;

import java.util.List;
import java.util.Map;


public class ActionSpell extends Spell {

	public final int staticDC;
	public final int staticToHit;
	public ActionSpell(String computerName, String readableName, SLOTLEVEL minimumLevel, boolean saveDc, boolean toHit,
			Map<SLOTLEVEL, List<SpellDamageComponent>> damagers, String readableEffect, boolean concentrate,
			int staticDC, int staticToHit) {
		super(computerName, readableName, minimumLevel, saveDc, toHit, damagers, readableEffect, concentrate, false);
		this.staticDC = staticDC;
		this.staticToHit = staticToHit;
	}
	
	public String cast() {
		//All static action spells are labeled as max for level and caster level, they are not used
		return super.cast(SLOTLEVEL.NINE, 20, staticDC, staticToHit);
	}

}
