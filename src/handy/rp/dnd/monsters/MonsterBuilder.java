package handy.rp.dnd.monsters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import handy.rp.dnd.Action;
import handy.rp.dnd.Helpers;
import handy.rp.dnd.LegendaryAction;
import handy.rp.dnd.attacks.Attack;
import handy.rp.dnd.spells.Spell;

public class MonsterBuilder {
	public static final int UNINITIALIZED_VALUE = -1;
	public final String humanReadableName;
	private final List<List<Attack>> attackLists = new ArrayList<>();
	private int maxHP = UNINITIALIZED_VALUE;
	
	private int str = UNINITIALIZED_VALUE;
	private int dex = UNINITIALIZED_VALUE;
	private int con = UNINITIALIZED_VALUE;
	private int inte = UNINITIALIZED_VALUE;
	private int wis = UNINITIALIZED_VALUE;
	private int cha = UNINITIALIZED_VALUE;
	
	private int strsave = UNINITIALIZED_VALUE;
	private int dexsave = UNINITIALIZED_VALUE;
	private int consave = UNINITIALIZED_VALUE;
	private int intsave = UNINITIALIZED_VALUE;
	private int wissave = UNINITIALIZED_VALUE;
	private int chasave = UNINITIALIZED_VALUE;
	
	private int casterLevel = UNINITIALIZED_VALUE;
	private int casterDc = UNINITIALIZED_VALUE;
	private int casterInnateDc = UNINITIALIZED_VALUE;
	private int casterToHit = UNINITIALIZED_VALUE;
	
	private Map<Spell.SLOTLEVEL, List<Spell>> spells;
	private Map<Spell.SLOTLEVEL, Integer> slotMapping;
	
	private Map<Spell, Integer> innateSpells;
	private Map<Action, Integer> actions;
	
	private int legendaryActionCharges = UNINITIALIZED_VALUE;
	private Map<LegendaryAction, Integer> legendaryActions;
	
	private int ac = UNINITIALIZED_VALUE;
	private int speed = UNINITIALIZED_VALUE;
	private String attrs = null;
	
	private Map<String, String> reactions = null;
	
	public void addAc(int ac) {
		this.ac = ac;
	}
	
	public void addSpeed(int speed) {
		this.speed = speed;
	}
	
	public void addAttrs(String attrs) {
		this.attrs = attrs;
	}
	
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
	
	public void addLegendaryActionsCharges(int charges) {
		legendaryActionCharges = charges;
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
	
	public void addLegendaryAction(LegendaryAction action, int charges) {
		if(legendaryActions == null) {
			legendaryActions = new HashMap<>();
		}
		legendaryActions.put(action, charges);
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
	
	public void addReaction(String name, String description) {
		if(reactions == null) {
			reactions = new HashMap<>();
		}
		reactions.put(name, description);
	}
	
	public MonsterTemplate build() {
		if(strsave == UNINITIALIZED_VALUE) {
			strsave = Helpers.getModifierFromAbility(str);
		}
		if(dexsave == UNINITIALIZED_VALUE) {
			dexsave = Helpers.getModifierFromAbility(dex);
		}
		if(consave == UNINITIALIZED_VALUE) {
			consave = Helpers.getModifierFromAbility(con);
		}
		if(intsave == UNINITIALIZED_VALUE) {
			intsave = Helpers.getModifierFromAbility(inte);
		}
		if(wissave == UNINITIALIZED_VALUE) {
			wissave = Helpers.getModifierFromAbility(wis);
		}
		if(chasave == UNINITIALIZED_VALUE) {
			chasave = Helpers.getModifierFromAbility(cha);
		}
		
		if(maxHP == UNINITIALIZED_VALUE ||
				str == UNINITIALIZED_VALUE ||
				dex == UNINITIALIZED_VALUE ||
				con == UNINITIALIZED_VALUE ||
				inte == UNINITIALIZED_VALUE ||
				wis == UNINITIALIZED_VALUE ||
				cha == UNINITIALIZED_VALUE) {
			throw new IllegalArgumentException("All stats must be supplied");
		}
		if((casterDc == UNINITIALIZED_VALUE || casterLevel == UNINITIALIZED_VALUE || casterToHit == UNINITIALIZED_VALUE) && (casterDc != casterLevel || casterDc != casterToHit)) {
			throw new IllegalArgumentException("Must have all caster stats or none");
		}else if((spells == null && slotMapping != null) || (spells != null && slotMapping == null)){
			throw new IllegalArgumentException("Casters must have spells and slot mappings");
		}
		
		if(casterInnateDc == UNINITIALIZED_VALUE && innateSpells != null) {
			throw new IllegalArgumentException("Innate casters must have spells and slot mappings");
		}
		
		if(legendaryActionCharges == UNINITIALIZED_VALUE && legendaryActions != null) {
			throw new IllegalArgumentException("Need legendary action charges if giving legendary actions");
		}
		return new MonsterTemplate(humanReadableName, maxHP, attackLists, str, dex, con, inte, wis, cha, casterLevel, casterDc, casterInnateDc, casterToHit, strsave, dexsave, consave, intsave, wissave, chasave, spells, slotMapping, innateSpells, actions, legendaryActionCharges, legendaryActions, ac, speed, attrs, reactions);
	}
	
}
