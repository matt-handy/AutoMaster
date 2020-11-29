package handy.rp.dnd.monsters;

import java.util.List;
import java.util.Map;

import handy.rp.dnd.Action;
import handy.rp.dnd.LegendaryAction;
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
	
	public final int strsave;
	public final int dexsave;
	public final int consave;
	public final int intsave;
	public final int wissave;
	public final int chasave;
	
	public final int casterLevel;
	public final int casterDc;
	public final int casterInnateDc;
	public final int casterToHit;
	
	private Map<Spell.SLOTLEVEL, List<Spell>> spells;
	private Map<Spell.SLOTLEVEL, Integer> slotMapping;
	
	private Map<Spell, Integer> innateSpells;
	private Map<Action, Integer> actions;
	
	private Map<LegendaryAction, Integer> legendaryActions;
	private int legendaryCharges;
	
	public final int ac;
	public final int speed;
	public final String attrs;
	
	MonsterTemplate(String humanReadableName, int maxHP, List<List<Attack>> attackLists,
			int str, int dex, int con, int inte, int wis, int cha, int casterLevel, int casterDc, int casterInnateDc, int casterToHit,
			int strsave, int dexsave, int consave, int intsave, int wissave, int chasave,
			Map<Spell.SLOTLEVEL, List<Spell>> spells, Map<Spell.SLOTLEVEL, Integer> slotMapping, Map<Spell, Integer> innateSpells,
			Map<Action, Integer> actions, int legendaryCharges, Map<LegendaryAction, Integer> legendaryActions,
			int ac, int speed, String attrs){
		this.humanReadableName = humanReadableName;
		this.maxHP = maxHP;
		this.attackLists = attackLists;
		
		this.str = str;
		this.dex = dex;
		this.con = con;
		this.inte = inte;
		this.wis = wis;
		this.cha = cha;
		
		this.strsave = strsave;
		this.dexsave = dexsave;
		this.consave = consave;
		this.intsave = intsave;
		this.wissave = wissave;
		this.chasave = chasave;
		
		this.ac = ac;
		this.attrs = attrs;
		this.speed = speed;
		
		this.casterLevel = casterLevel;
		this.casterDc = casterDc;
		this.casterInnateDc = casterInnateDc;
		this.casterToHit = casterToHit;
		
		this.spells = spells;
		this.slotMapping = slotMapping;
		
		this.innateSpells = innateSpells;
		this.actions = actions;
		
		this.legendaryActions = legendaryActions;
		this.legendaryCharges = legendaryCharges;
	}
		
	public List<List<Attack>> getAttacks(){
		return attackLists;
	}
	
	public MonsterInstance getInstance(String personalName) {
		return new MonsterInstance(humanReadableName, maxHP, attackLists, personalName, str, dex, con, inte, wis, cha, casterLevel, casterDc, casterInnateDc, casterToHit, strsave, dexsave, consave, intsave, wissave, chasave, spells, slotMapping, innateSpells, actions, legendaryCharges, legendaryActions, ac, speed, attrs);
	}
	
}
