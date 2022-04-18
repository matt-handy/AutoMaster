package handy.rp.fortyk.datamodel;

import java.util.ArrayList;
import java.util.List;

public class UnitInstance {

	public final Model leader;
	private List<Model> models;
	public final String mnemonic;
	
	protected UnitInstance(Unit base, int count, String mnemonic) {
		if(base.leadModel == null) {
			leader = null;
		}else {
			leader = base.leadModel.clone();
		}
		models = new ArrayList<>();
		for(int idx = 0; idx < count; idx++) {
			models.add(base.commonModel.clone());
		}
		this.mnemonic = mnemonic;
	}
	
	public List<Model> getModels(){
		return new ArrayList<>(models);
	}

	public void applyWounds(List<Integer> wounds) {
		throw new IllegalArgumentException("Not implemented");
	}
}
