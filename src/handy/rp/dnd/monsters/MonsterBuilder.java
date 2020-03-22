package handy.rp.dnd.monsters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import handy.rp.dnd.Helpers;
import handy.rp.dnd.attacks.Action;
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
	
	private int strsave = -1;
	private int dexsave = -1;
	private int consave = -1;
	private int intsave = -1;
	private int wissave = -1;
	private int chasave = -1;
	
	private int casterLevel = -1;
	private int casterDc = -1;
	private int casterInnateDc = -1;
	private int casterToHit = -1;
	
	private Map<Spell.SLOTLEVEL, List<Spell>> spells;
	private Map<Spell.SLOTLEVEL, Integer> slotMapping;
	
	private Map<Spell, Integer> innateSpells;
	private Map<Action, Integer> actions;
	
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
	
	public void addStrsave(int str) {
		this.strsave = str;
	}
	
	public void addDexsave(int dex) {
		this.dexsave = dex;
	}
	
	public void addConsave(int con) {
		this.consave = con;
	}
	
	public void addIntsave(int inte) {
		this.intsave = inte;
	}
	
	public void addWissave(int wis) {
		this.wissave = wis;
	}
	
	public void addChasave(int cha) {
		this.chasave = cha;
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
	
	public void addInnateSpell(Spell spell, int charges) {
		if(innateSpells == null) {
			innateSpells = new HashMap<>();
		}
		innateSpells.put(spell, charges);
	}
	
	public void addAction(Action action, int charges) {
		if(actions == null) {
			actions = new HashMap<>();
		}
		actions.put(action, charges);
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
	
	public void addCasterInnateDc(int casterInnateDc) {
		this.casterInnateDc = casterInnateDc;
	}
	
	public void addCasterToHit(int casterToHit) {
		this.casterToHit = casterToHit;
	}
	
	public MonsterTemplate build() {
		if(strsave == -1) {
			strsave = Helpers.getModifierFromAbility(str);
		}
		if(dexsave == -1) {
			dexsave = Helpers.getModifierFromAbility(dex);
		}
		if(consave == -1) {
			consave = Helpers.getModifierFromAbility(con);
		}
		if(intsave == -1) {
			intsave = Helpers.getModifierFromAbility(inte);
		}
		if(wissave == -1) {
			wissave = Helpers.getModifierFromAbility(wis);
		}
		if(chasave == -1) {
			chasave = Helpers.getModifierFromAbility(cha);
		}
		
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
		
		if(casterInnateDc == -1 && innateSpells != null) {
			throw new IllegalArgumentException("Innate casters must have spells and slot mappings");
		}
		return new MonsterTemplate(humanReadableName, maxHP, attackLists, str, dex, con, inte, wis, cha, casterLevel, casterDc, casterInnateDc, casterToHit, strsave, dexsave, consave, intsave, wissave, chasave, spells, slotMapping, innateSpells, actions);
	}
	
}
