package handy.rp.dnd.monsters;

import java.util.List;
import java.util.Map;

import handy.rp.dnd.attacks.Attack;
import handy.rp.dnd.spells.Spell;

public class MonsterTemplate {

	public final String humanReadableName;
	List<List<Attack>> attackLists;
	public final int maxHP;
	
	public final int str;
	public final int dex;
	public final int con;
	public final int inte;
	public final int wis;
	public final int cha;
	
	public final int casterLevel;
	public final int casterDc;
	public final int casterToHit;
	
	private Map<Spell.SLOTLEVEL, List<Spell>> spells;
	private Map<Spell.SLOTLEVEL, Integer> slotMapping;
	
	MonsterTemplate(String humanReadableName, int maxHP, List<List<Attack>> attackLists,
			int str, int dex, int con, int inte, int wis, int cha, int casterLevel, int casterDc, int casterToHit,
			Map<Spell.SLOTLEVEL, List<Spell>> spells, Map<Spell.SLOTLEVEL, Integer> slotMapping){
		this.humanReadableName = humanReadableName;
		this.maxHP = maxHP;
		this.attackLists = attackLists;
		
		this.str = str;
		this.dex = dex;
		this.con = con;
		this.inte = inte;
		this.wis = wis;
		this.cha = cha;
		
		this.casterLevel = casterLevel;
		this.casterDc = casterDc;
		this.casterToHit = casterToHit;
		
		this.spells = spells;
		this.slotMapping = slotMapping;
	}
		
	public List<List<Attack>> getAttacks(){
		return attackLists;
	}
	
	public MonsterInstance getInstance(String personalName) {
		return new MonsterInstance(humanReadableName, maxHP, attackLists, personalName, str, dex, con, inte, wis, cha, casterLevel, casterDc, casterToHit, spells, slotMapping);
	}
	
}
