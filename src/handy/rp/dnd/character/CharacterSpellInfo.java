package handy.rp.dnd.character;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import handy.rp.dnd.spells.Spell;

public class CharacterSpellInfo {

	private List<Spell> knownCantrips;
	private List<Spell> preparedSpells;
	private List<Spell> knownSpells;

	private Map<Spell.SLOTLEVEL, Integer> spellSlotsRemaining;
	
	public CharacterSpellInfo(List<Spell> knownCantrips, List<Spell> preparedSpells, List<Spell> knownSpells) {
		this.knownCantrips = new ArrayList<>(knownCantrips);
		this.preparedSpells = new ArrayList<>(preparedSpells);
		if (knownSpells != null) {
			this.knownSpells = new ArrayList<>(knownSpells);
		}
	}
	
	public void update(Map<Spell.SLOTLEVEL, Integer> spellSlotsRemaining) {
		this.spellSlotsRemaining = new HashMap<>(spellSlotsRemaining);
	}
	
	public Map<Spell.SLOTLEVEL, Integer> getSpellSlotsRemaining(){
		return new HashMap<>(spellSlotsRemaining);
	}

	public void prepareMoreSpells(List<Spell> preparedSpells) {
		this.preparedSpells.addAll(preparedSpells);
	}

	public List<Spell> getKnownCantrips() {
		return new ArrayList<>(knownCantrips);
	}

	public List<Spell> getPreparedSpells() {
		return new ArrayList<>(preparedSpells);
	}

	public List<Spell> getKnownSpells() {
		return new ArrayList<>(knownSpells);
	}
}
