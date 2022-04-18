package handy.rp.fortyk.datamodel;

import java.util.List;

public class Unit {

	public final String name;
	public final Model leadModel;
	public final Model commonModel;
	
	public Unit(String name, Model leadModel, Model commonModel) {
		this.leadModel = leadModel;
		this.commonModel = commonModel;
		this.name = name;
	}
	
	public UnitInstance getInstance(int count, String mnemonic) {
		return new UnitInstance(this, count, mnemonic);
	}
}
