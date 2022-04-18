package handy.rp.fortyk.datamodel;

import java.util.Map;

public class Model {

	public final StatBlock baseStats;
	Map<Integer, StatBlock> degradedStats;
	private int currentWounds;
	
	public Model(StatBlock baseStats, Map<Integer, StatBlock> degradedStats) {
		this.baseStats = baseStats;
		//No copy needed as XML parser can pass ownership
		this.degradedStats = degradedStats;
	}
	
	public int getCurrentWounds() {
		return currentWounds;
	}
	
	public void wound(int wounds) {
		if(currentWounds - wounds > 0) {
			currentWounds = currentWounds - wounds;
		}else {
			currentWounds = 0;
		}
	}
	
	public StatBlock getCurrentStats() {
		if(degradedStats == null) {
			return baseStats;
		}
		int currentLowestThreshold = baseStats.wounds;
		boolean metAThreshold = false;
		for(Integer threshold : degradedStats.keySet()) {
			if(threshold < currentLowestThreshold && threshold <= currentWounds) {
				metAThreshold = true;	
				currentLowestThreshold = threshold;
			}
		}
		if(metAThreshold) {
			return degradedStats.get(currentLowestThreshold);
		}else {
			return baseStats;
		}
	}
	
	public Model clone() {
		return new Model(baseStats, degradedStats);
	}
}
