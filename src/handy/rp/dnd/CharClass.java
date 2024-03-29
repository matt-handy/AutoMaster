package handy.rp.dnd;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import handy.rp.Dice.DICE_TYPE;
import handy.rp.dnd.character.Proficiency;
import handy.rp.dnd.spells.Spell;

public class CharClass {
	
	@Override
	public boolean equals(Object other) {
		if(other instanceof CharClass) {
			return name.equals(((CharClass) other).name);
		}else {
			return false;
		}
	}
	
	@Override 
	public int hashCode() {
		return name.hashCode();
	}
	
	public enum SPELLCASTING_MODIFIER {
		CHARISMA, WISDOM, INTELLIGENCE, NA;
		
		public static SPELLCASTING_MODIFIER getFromName(String name) {
			if(name.equalsIgnoreCase("INT") || name.equalsIgnoreCase("INTELLIGENCE")) {
				return INTELLIGENCE;
			}else if(name.equalsIgnoreCase("WIS") || name.equalsIgnoreCase("WISDOM")) {
				return WISDOM;
			}else if(name.equalsIgnoreCase("CHA") || name.equalsIgnoreCase("CHARISMA")) {
				return CHARISMA;
			}else {
				throw new IllegalArgumentException("Unknown modifier: " + name);
			}
		}
	};

	public enum ESSENTIAL_ABILITY_SCORE {
		STRENGTH, DEXTERITY, CONSTITUTION, INTELLIGENCE, WISDOM, CHARISMA;
		
		public static ESSENTIAL_ABILITY_SCORE getFromName(String name) {
			if(name.equalsIgnoreCase("STR") || name.equalsIgnoreCase("STRENGTH")) {
				return STRENGTH;
			}else if(name.equalsIgnoreCase("DEX") || name.equalsIgnoreCase("DEXTERITY")) {
				return DEXTERITY;
			}else if(name.equalsIgnoreCase("CON") || name.equalsIgnoreCase("CONSTITUTION")) {
				return CONSTITUTION;
			}else if(name.equalsIgnoreCase("INT") || name.equalsIgnoreCase("INTELLIGENCE")) {
				return INTELLIGENCE;
			}else if(name.equalsIgnoreCase("WIS") || name.equalsIgnoreCase("WISDOM")) {
				return WISDOM;
			}else if(name.equalsIgnoreCase("CHA") || name.equalsIgnoreCase("CHARISMA")) {
				return CHARISMA;
			}else {
				throw new IllegalArgumentException();
			}
		}
	};

	public final String name;
	public final Map<Integer, Map<Spell.SLOTLEVEL, Integer>> slotsPerLevel;
	public final SPELLCASTING_MODIFIER spellcastingModifier;
	public final List<ESSENTIAL_ABILITY_SCORE> savingThrowProficiencies;
	public final DICE_TYPE hitDice;
	private List<ClassFeature> features;
	public final ClassResource resource;
	public final int subClassLevel;
	
	private List<Proficiency> armorProficiencies;
	private List<Proficiency> toolProficiencies;
	
	public CharClass(String name, CharClass other) {
		this.name = name;
		this.slotsPerLevel = other.slotsPerLevel;
		this.spellcastingModifier = other.spellcastingModifier;
		this.savingThrowProficiencies = other.savingThrowProficiencies;
		this.hitDice = other.hitDice;
		this.features = other.features;
		this.resource = other.resource;
		this.subClassLevel = other.subClassLevel;
		this.armorProficiencies = new ArrayList<>(other.armorProficiencies);
		this.toolProficiencies = new ArrayList<>(other.toolProficiencies);
	}
	
	public CharClass(String name, Map<Integer, Map<Spell.SLOTLEVEL, Integer>> slotsPerLevel,
			SPELLCASTING_MODIFIER spellcastingModifier, List<ESSENTIAL_ABILITY_SCORE> savingThrowProficiencies,
			DICE_TYPE hitDice, List<ClassFeature> features, ClassResource resource, int subClassLevel, List<Proficiency> armorProficiencies, List<Proficiency> toolProficiencies) {
		this.name = name;
		this.slotsPerLevel = slotsPerLevel;
		this.spellcastingModifier = spellcastingModifier;
		this.savingThrowProficiencies = savingThrowProficiencies;
		this.hitDice = hitDice;
		this.features = features;
		this.resource = resource;
		this.subClassLevel = subClassLevel;
		this.toolProficiencies = toolProficiencies;
		this.armorProficiencies = armorProficiencies;
	}
	
	public List<Proficiency> getArmorProficiencies(){
		return new ArrayList<>(armorProficiencies); 
	}
	
	public List<Proficiency> getToolProficiencies(){
		return new ArrayList<>(toolProficiencies); 
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
