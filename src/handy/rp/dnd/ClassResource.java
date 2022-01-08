package handy.rp.dnd;

import java.util.HashMap;
import java.util.Map;

public class ClassResource {

	public enum RECHARGE_INTERVAL {SHORT_REST, LONG_REST}
	
	public final String name;
	public final RECHARGE_INTERVAL rechargeInterval;
	
	private Map<Integer, Integer> chargesMap;
	
	public ClassResource(String name, RECHARGE_INTERVAL rechargeInterval, Map<Integer, Integer> levelsToCharges) {
		this.name = name;
		this.rechargeInterval = rechargeInterval;
		chargesMap = new HashMap<>();
		int currentCharges = 0;
		for(int levelIter = 1; levelIter <= 20; levelIter++) {
			Integer testCharge = levelsToCharges.get(levelIter); 
			if(testCharge != null) {
				currentCharges = testCharge;
			}
			chargesMap.put(levelIter, currentCharges);
		}
	}
	
	public int getCharges(int classLevel) {
		if(chargesMap.containsKey(classLevel)) {
			return chargesMap.get(classLevel);
		}else {
			throw new IllegalArgumentException("Invalid level supplied");
		}
	}
}
