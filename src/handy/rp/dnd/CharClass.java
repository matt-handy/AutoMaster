package handy.rp.dnd;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import handy.rp.Dice.DICE_TYPE;
import handy.rp.dnd.spells.Spell;

public class CharClass {
	public enum SPELLCASTING_MODIFIER {
		CHARISMA, WISDOM, INTELLIGENCE, NA
	};

	public enum SAVING_THROW_PROFICIENCY {
		STRENGTH, DEXTERITY, CONSTITUTION, INTELLIGENCE, WISDOM, CHARISMA
	};

	public final String name;
	public final Map<Integer, Map<Spell.SLOTLEVEL, Integer>> slotsPerLevel;
	public final SPELLCASTING_MODIFIER spellcastingModifier;
	public final List<SAVING_THROW_PROFICIENCY> savingThrowProficiencies;
	public final DICE_TYPE hitDice;
	private List<ClassFeature> features;
	public final ClassResource resource;
	
	public CharClass(String name, CharClass other) {
		this.name = name;
		this.slotsPerLevel = other.slotsPerLevel;
		this.spellcastingModifier = other.spellcastingModifier;
		this.savingThrowProficiencies = other.savingThrowProficiencies;
		this.hitDice = other.hitDice;
		this.features = other.features;
		this.resource = other.resource;
	}
	
	public CharClass(String name, Map<Integer, Map<Spell.SLOTLEVEL, Integer>> slotsPerLevel,
			SPELLCASTING_MODIFIER spellcastingModifier, List<SAVING_THROW_PROFICIENCY> savingThrowProficiencies,
			DICE_TYPE hitDice, List<ClassFeature> features, ClassResource resource) {
		this.name = name;
		this.slotsPerLevel = slotsPerLevel;
		this.spellcastingModifier = spellcastingModifier;
		this.savingThrowProficiencies = savingThrowProficiencies;
		this.hitDice = hitDice;
		this.features = features;
		this.resource = resource;
	}
	
	public List<ClassFeature> getFeatures(){
		return new ArrayList<>(features); 
	}
	
	public List<Spell> getAutomaticSpells(int level){
		return new ArrayList<>();
	}
	
	public CharClass getRootClass() {
		return this;
	}
}
