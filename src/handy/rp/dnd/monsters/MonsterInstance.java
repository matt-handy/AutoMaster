package handy.rp.dnd.monsters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import handy.rp.Dice;
import handy.rp.dnd.Entity;
import handy.rp.dnd.Helpers;
import handy.rp.dnd.attacks.Action;
import handy.rp.dnd.attacks.Attack;
import handy.rp.dnd.attacks.Damage;
import handy.rp.dnd.spells.*;
import handy.rp.dnd.spells.Spell.SLOTLEVEL;

public class MonsterInstance extends Entity{

	public final String humanReadableName;
	List<List<Attack>> attackLists;
	public final int maxHP;
	
	public final int casterLevel;
	public final int casterDc;
	public final int casterToHit;
	public final int casterInnateDc;
	
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
	
	private int currentHp;
	
	private List<Attack> attacksThisTurn = null;
	
	private Map<Spell.SLOTLEVEL, List<Spell>> spells;
	private Map<Spell.SLOTLEVEL, Integer> slotsRemaining;
	
	//Innate spells possessed, plus number of charges per day
	private Map<Spell, Integer> innateSpells;
	public static final int AT_WILL = 9999;
	
	private Map<Action, Integer> actions;
	private Map<Action, Boolean> actionReadiness = new HashMap<>();
	
	private Spell concentratedSpell = null;
	
	MonsterInstance(String humanReadableName, int maxHP, List<List<Attack>> attackLists, String personalName, int currentHp,
			int str, int dex, int con, int inte, int wis, int cha, int casterLevel, int casterDc, int casterInnateDc, int casterToHit,
			int strsave, int dexsave, int consave, int intsave, int wissave, int chasave,
			Map<Spell.SLOTLEVEL, List<Spell>> spells, Map<Spell.SLOTLEVEL, Integer> slotMapping, 
			Map<Spell, Integer> innateSpells, Map<Action, Integer> actions){
		super(personalName);
		this.currentHp = currentHp;
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
		
		this.casterLevel = casterLevel;
		this.casterDc = casterDc;
		this.casterInnateDc = casterInnateDc;
		this.casterToHit = casterToHit;
		this.spells = spells;
		
		if(slotMapping != null) {
			slotsRemaining = new HashMap<>();
			for(Spell.SLOTLEVEL slot : slotMapping.keySet()) {
				slotsRemaining.put(slot, new Integer(slotMapping.get(slot)));
			}
		}
		
		this.innateSpells = innateSpells;
		
		this.actions = actions;
		if(actions != null) {
			for(Action action : actions.keySet()) {
				if(action.rechargeDice != null) {
					actionReadiness.put(action, true);
				}
			}
		}
	}
	
	MonsterInstance(String humanReadableName, int maxHP, List<List<Attack>> attackLists, String personalName,
			int str, int dex, int con, int inte, int wis, int cha, int casterLevel, 
			int casterDc, int casterInnateDc, int casterToHit,
			int strsave, int dexsave, int consave, int intsave, int wissave, int chasave,
			Map<Spell.SLOTLEVEL, List<Spell>> spells, Map<Spell.SLOTLEVEL, Integer> slotMapping, Map<Spell, Integer> innateSpells,
			Map<Action, Integer> actions){
		this(humanReadableName, maxHP, attackLists, personalName, maxHP,
				str, dex, con, inte, wis, cha, casterLevel, casterDc, casterInnateDc, casterToHit, 
				strsave, dexsave, consave, intsave, wissave, chasave,
				spells, slotMapping, innateSpells, actions);
	}
	
	public void resetTurn() {
		if(attackLists.size() == 1) {
			attacksThisTurn = new ArrayList<>();
			attacksThisTurn.addAll(attackLists.get(0));
		}else {
			attacksThisTurn = null;
		}
		for(Action action : actionReadiness.keySet()) {
			if(!actionReadiness.get(action)) {
				int roll = action.rechargeDice.roll();
				if(roll >= action.rechargeDiceMeets) {
					actionReadiness.put(action, true);
				}
			}
		}
	}
	
	public int getCurrentHp() {
		return currentHp;
	}
	
	public void heal(int hp) {
		currentHp += hp;
	}
	
	public void hit(int hp) {
		currentHp -= hp;
	}
	
	public int conSaveThrow() {
		int save = Dice.d20() + Helpers.getModifierFromAbility(con) + consave;
		return save;
	}
	
	public void breakConcentration() {
		concentratedSpell = null;
	}
	
	public Spell concentratedSpell() {
		return concentratedSpell;
	}
	
	public int rollInitiative() {
		currentInitiative = Dice.d20() + Helpers.getModifierFromAbility(dex);
		return currentInitiative;
	}
	
	public List<Attack> getCurrentAttacks(){
		return attacksThisTurn;
	}
	
	public String expendAction(String cName) {
		try {
			Action action = returnAction(cName);
			
			if(actionReadiness.containsKey(action)) {
				if(actionReadiness.get(action)) {
					actionReadiness.put(action, false);
				}else {
					return "Rechargable Action - Need " + action.rechargeDiceMeets + " or better from " + action.rechargeDice;
				}
			}
			
			StringBuilder sb = new StringBuilder();
			sb.append(action.name);
			sb.append(System.lineSeparator());
			if(action.spell != null) {
				sb.append(action.spell.cast());
				sb.append(System.lineSeparator());
			}
			if(action.attack != null) {
				//TODO support attack
			}
			if(action.text != null) {
				sb.append(action.text);
				sb.append(System.lineSeparator());
			}
			return sb.toString();
		}catch(IllegalArgumentException ex) {
			return ex.getMessage();
		}
	}
	
	public Action returnAction(String cName) {
		for(Action action : actions.keySet()) {
			if(action.cname.contentEquals(cName)) {
				int charges = actions.get(action);
				if(charges == AT_WILL) {
					return action;
				}else {
					if(charges > 0) {
						actions.put(action, charges - 1);
						return action;
					}else {
						throw new IllegalArgumentException("No remaining charges for action");
					}
				}
			}
		}
		throw new IllegalArgumentException("No such action: " + cName);
	}
	
	public String listActions() {
		if(actions == null) {
			return "";
		}
		
		StringBuilder sb = new StringBuilder();
		for(Action action : actions.keySet()) {
			int charges = actions.get(action);
			String chargeMsg = charges + "";
			if(charges == AT_WILL) {
				chargeMsg = "At will";
			}
			sb.append("Name: " + action.name + " Daily charges: " + chargeMsg);
			sb.append(System.lineSeparator());
			if(actionReadiness.containsKey(action)) {
				sb.append("Rechargable spell, ready? " + actionReadiness.get(action));
				sb.append(System.lineSeparator());
			}
			sb.append("Cname: " + action.cname);
			if(action.text != null) {
				sb.append(System.lineSeparator());
				sb.append(action.text);
			}
		}
		return sb.toString();
	}
	
	public Attack expendAttack(int number) {
		if(attacksThisTurn == null) {
			int totalIdx = 0;
			for(List<Attack> set : attackLists) {
				for(Attack attack : set) {
					if(totalIdx == number) {
						attacksThisTurn = new ArrayList<>();
						attacksThisTurn.addAll(set);
						attacksThisTurn.remove(attack);
						return attack;
					}
					totalIdx++;
				}
			}
			throw new IllegalArgumentException("Picked too high an attack number");
		}else {
			if(number >= attacksThisTurn.size()) {
				throw new IllegalArgumentException("Picked too high an attack number");
			}else {
				return attacksThisTurn.remove(number);
			}
		}
	}
	
	public boolean isMultioptionAttack() {
		return attackLists.size() > 1;
	}
	
	public String listAttacksReadable() {
		StringBuilder sb = new StringBuilder();
		int setIdx = 0;
		int aIdx = 0;
		for(List<Attack> set : attackLists) {
			if(attackLists.size() != 1) {
				sb.append("Set: " + setIdx++ + System.lineSeparator());
			}
			
			for(Attack attack : set) {
				sb.append("Attack: " + aIdx++ + " " + attack.toString() + System.lineSeparator());
			}
		}
		return sb.toString();
	}
	
	public String listRemainingAttacksReadable() {
		if(attacksThisTurn == null) {
			return listAttacksReadable();
		}
		StringBuilder sb = new StringBuilder();
		int aIdx = 0;
		for(Attack attack : attacksThisTurn) {
			sb.append("Attack: " + aIdx++ + " " + attack.toString() + System.lineSeparator());
		}
		return sb.toString();
	}

	public String listSpellSlotsRemaining() {
		if(slotsRemaining == null) {
			return "";
		}
		
		StringBuilder sb = new StringBuilder();
		List<Spell.SLOTLEVEL> slots = new ArrayList<>();
		slots.addAll(slotsRemaining.keySet());
		Collections.sort(slots);
		for(Spell.SLOTLEVEL slot : slots) {
			sb.append("Level " + slot.level + ": " + slotsRemaining.get(slot));
			sb.append(", ");
		}
		return sb.toString();
	}
	
	public String listInnateSpells() {
		if(innateSpells == null) {
			return "";
		}
		
		StringBuilder sb = new StringBuilder();
		
		for(Spell spell : innateSpells.keySet()) {
			sb.append(spell.readableName + " - ");
			if(innateSpells.get(spell) != AT_WILL) {
				sb.append(innateSpells.get(spell) + "/day, ");
			}else {
				sb.append("at will, ");
			}
		}
		
		return sb.toString();
	}
	
	public Spell expendInnateSpell(String spellName) {
		Spell spell = getInnateSpell(spellName);
		Integer remainingCharges = innateSpells.get(spell);
		if(remainingCharges > 0) {
			if(spell.concentrate && concentratedSpell != null) {
				throw new IllegalArgumentException("Already concentrating on: " + concentratedSpell.readableName);
			}else if(spell.concentrate) {
				concentratedSpell = spell;
			}
			
			if(innateSpells.get(spell) != AT_WILL) {
				innateSpells.put(spell, remainingCharges - 1);
			}
		}else {
			throw new IllegalArgumentException("No more charges for spell: " + spellName);
		}
		
		return spell;
	}
	
	private Spell getInnateSpell(String spellName) {
		for(Spell spell : innateSpells.keySet()) {
			if(spell.computerName.equals(spellName)) {
				return spell;
			}
		}
		throw new IllegalArgumentException("Unknown spell: " + spellName);
	}
	
	public String listSpells() {
		if(spells == null) {
			return "";
		}
		
		StringBuilder sb = new StringBuilder();
		
		for(Spell.SLOTLEVEL slot : spells.keySet()) {
			List<Spell> spells = this.spells.get(slot);
			sb.append("Level: " + slot.toString() + " ");
			for(Spell spell : spells) {
				sb.append(spell.readableName + ", ");
			}
			sb.append(System.lineSeparator());
		}
		
		return sb.toString();
	}
	
	private Spell getSpellWithConcentrationCheck(String spellName) {
		Spell targetSpell = getSpellByCompName(spellName);
		if(targetSpell.concentrate && concentratedSpell != null) {
			throw new IllegalArgumentException("Already concentrating on: " + concentratedSpell.readableName);
		}else if(targetSpell.concentrate) {
			concentratedSpell = targetSpell;
		}
		return targetSpell;
	}
	
	public Spell expendSpell(String spellName, Spell.SLOTLEVEL slotLvl) {	
			int minSpellLevel = getMinSpellLevel(spellName);
			if(minSpellLevel > slotLvl.level) {
				throw new IllegalArgumentException("Need a spell slot of at least minimum level: " + minSpellLevel);
			}
			return expendSpellWorker(spellName, slotLvl);
	}
	
	public Spell expendSpellWorker(String spellName, Spell.SLOTLEVEL slotLvl) {
		if(slotLvl == Spell.SLOTLEVEL.CANTRIP) {
			return getSpellWithConcentrationCheck(spellName);
		}
		if(slotsRemaining.get(slotLvl) < 1) {
			throw new IllegalArgumentException("Insufficient slots at level: " + slotLvl.level);
		}else {
			slotsRemaining.put(slotLvl, slotsRemaining.get(slotLvl) - 1);
			return getSpellWithConcentrationCheck(spellName);
		}
	}
	
	public Spell expendSpell(String spellName) {
		
		int minSpellLevel = getMinSpellLevel(spellName);
		Spell.SLOTLEVEL spellLevel = Spell.SLOTLEVEL.get(minSpellLevel);
		return expendSpellWorker(spellName, spellLevel);
	
	}
	
	private int getMinSpellLevel(String spellName) {
		return getSpellByCompName(spellName).minimumLevel.level;
	}
	
	private Spell getSpellByCompName(String spellName) {
		for(Spell.SLOTLEVEL slot : spells.keySet()) {
			List<Spell> spells = this.spells.get(slot);
			for(Spell spell : spells) {
				if(spell.computerName.equals(spellName)) {
					return spell;
				}
			}
		}
		throw new IllegalArgumentException("I have no such spell: " + spellName);
	}
}
