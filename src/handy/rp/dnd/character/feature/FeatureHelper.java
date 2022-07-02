package handy.rp.dnd.character.feature;

import java.util.List;

import handy.rp.dnd.ClassFeature;

public class FeatureHelper {
	public static int assembleTotalACBonus(List<ClassFeature> features) {
		int totalACBonus = 0;
		for(ClassFeature cClassFeature : features) {
			totalACBonus += cClassFeature.acBonus;
		}
		return totalACBonus;
	}
}
