package handy.rp.dnd;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import handy.rp.dnd.character.Proficiency;
import handy.rp.dnd.spells.Spell;

public class CharSubClass extends CharClass {

	private List<ClassFeature> features = null;
	private Map<Integer, List<Spell>> automaticSpells;
	private CharClass parent;
	
	private List<Proficiency> armorProficiencies;
	private List<Proficiency> toolProficiencies;
	
	public CharSubClass(String name, CharClass parent, List<ClassFeature> features,
			Map<Integer, List<Spell>> automaticSpells, List<Proficiency> armorProficiencies, List<Proficiency> toolProficiencies) {
		super(name, parent);
		this.features = new ArrayList<>(parent.getFeatures());
		this.features.addAll(features);
		this.automaticSpells = automaticSpells;
		this.parent = parent;
		this.toolProficiencies = toolProficiencies;
		this.armorProficiencies = armorProficiencies;
	}
	
	public List<Proficiency> getArmorProficiencies(){
		return new ArrayList<>(armorProficiencies); 
	}
	
	public List<Proficiency> getToolProficiencies(){
		return new ArrayList<>(toolProficiencies); 
	}
	
	@Override
	public List<ClassFeature> getFeatures(){
		return new ArrayList<>(features); 
	}
	
	@Override
	public List<Spell> getAutomaticSpells(int level){
		List<Spell> newList = new ArrayList<>();
		for(int counter = 1; counter <= level; counter++) {
			if(automaticSpells.containsKey(counter)) {
				newList.addAll(automaticSpells.get(counter));
			}
		}
		return newList;
	}
	
	@Override
	public CharClass getRootClass() {
		return parent;
	}
}
