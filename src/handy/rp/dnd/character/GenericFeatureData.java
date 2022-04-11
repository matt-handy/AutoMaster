package handy.rp.dnd.character;

import java.util.ArrayList;
import java.util.List;

public class GenericFeatureData {

	public final String featureName;
	private List<String> featureData = new ArrayList<>();
	
	public GenericFeatureData(String featureName) {
		this.featureName = featureName;
	}
	
	public void addFeatureData(String featureData) {
		this.featureData.add(featureData);
	}
	
	public boolean hasFeatureDataString(String featureData) {
		return this.featureData.contains(featureData);
	}
	
	public List<String> getFeatureData(){
		return new ArrayList<>(featureData);
	}
}
