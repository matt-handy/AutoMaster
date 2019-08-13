package handy.rp.dnd.monsters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import handy.rp.dnd.attacks.Attack;
import handy.rp.dnd.spells.Spell;

public class MonsterBuilder {
	public final String humanReadableName;
	private final List<List<Attack>> attackLists = new ArrayList<>();
	private int maxHP = -1;
	
	private int str = -1;
	private int dex = -1;
	private int con = -1;
	private int inte = -1;
	private int wis = -1;
	private int cha = -1;
	
	private int casterLevel = -1;
	private int casterDc = -1;
	private int casterToHit = -1;
	
	private Map<Spell.SLOTLEVEL, List<Spell>> spells;
	private Map<Spell.SLOTLEVEL, Integer> slotMapping;
	
	public MonsterBuilder(String humanReadableName) {
		this.humanReadableName = humanReadableName;
	}
	
	public void addMaxHP(int maxHP) {
		this.maxHP = maxHP;
	}
	
	public void addStr(int str) {
		this.str = str;
	}
	
	public void addDex(int dex) {
		this.dex = dex;
	}
	
	public void addCon(int con) {
		this.con = con;
	}
	
	public void addInt(int inte) {
		this.inte = inte;
	}
	
	public void addWis(int wis) {
		this.wis = wis;
	}
	
	public void addCha(int cha) {
		this.cha = cha;
	}
	
	public void addAttack(Attack attack, int setIdx) {
		if(setIdx < attackLists.size()) {
			attackLists.get(setIdx).add(attack);
		}else if(setIdx == attackLists.size()) {
			List<Attack> attacks = new ArrayList<>();
			attacks.add(attack);
			attackLists.add(attacks);
		}else {
			throw new IllegalArgumentException("Supplied index too high");
		}
		
	}
	
	public void addSpell(Spell spell) {
		if(spells == null) {
			spells = new HashMap<>();
		}
		if(spells.get(spell.minimumLevel) == null) {
			List<Spell> slotSpells = new ArrayList<>();
			slotSpells.add(spell);
			spells.put(spell.minimumLevel, slotSpells);
		}else {
			spells.get(spell.minimumLevel).add(spell);
		}
	}
	
	public void addSpellSlots(Map<Spell.SLOTLEVEL, Integer> slotMapping) {
		this.slotMapping = slotMapping;
	}
	
	public void addCasterLevel(int casterLevel) {
		this.casterLevel = casterLevel;
	}
	
	public void addCasterDc(int casterDc) {
		this.casterDc = casterDc;
	}
	
	public void addCasterToHit(int casterToHit) {
		this.casterToHit = casterToHit;
	}
	
	public MonsterTemplate build() {
		if(maxHP == -1 ||
				str == -1 ||
				dex == -1 ||
				con == -1 ||
				inte == -1 ||
				wis == -1 ||
				cha == -1) {
			throw new IllegalArgumentException("All stats must be supplied");
		}
		if((casterDc == -1 || casterLevel == -1 || casterToHit == -1) && (casterDc != casterLevel || casterDc != casterToHit)) {
			throw new IllegalArgumentException("Must have all caster stats or none");
		}else if((spells == null && slotMapping != null) || (spells != null && slotMapping == null)){
			throw new IllegalArgumentException("Casters must have spells and slot mappings");
		}
		return new MonsterTemplate(humanReadableName, maxHP, attackLists, str, dex, con, inte, wis, cha, casterLevel, casterDc, casterToHit, spells, slotMapping);
	}
	
}
