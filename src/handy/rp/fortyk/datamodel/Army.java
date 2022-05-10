package handy.rp.fortyk.datamodel;

import java.util.List;

public class Army {

	public final String armyName;
	public List<UnitInstance> armyComposition;
	
	public Army(String armyName, List<UnitInstance> armyComposition) {
		this.armyName = armyName;
		this.armyComposition = armyComposition;
	}
	
	public UnitInstance getUnitByMnemonic(String mnemonic) {
		for(UnitInstance unit : armyComposition) {
			if(unit.mnemonic.equalsIgnoreCase(mnemonic)) {
				return unit;
			}
		}
		throw new IllegalArgumentException("Unknown unit: " + mnemonic);
	}
}
