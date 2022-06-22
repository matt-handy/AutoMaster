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
	
	public String getTotalPointCount() {
		StringBuilder sb = new StringBuilder();
		int currentWorkingCount = 0;
		
		for(UnitInstance unit : armyComposition) {
			sb.append(unit.mnemonic + " has point value: " + unit.getUnitPointValue());
			sb.append(System.lineSeparator());
			currentWorkingCount += unit.getUnitPointValue();
		}
		
		sb.append("Total Army Points: " + currentWorkingCount);
		return sb.toString();
	}
}
