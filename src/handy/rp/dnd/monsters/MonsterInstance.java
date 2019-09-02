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
import handy.rp.dnd.attacks.Attack;
import handy.rp.dnd.attacks.Damage;
import handy.rp.dnd.spells.*;

public class MonsterInstance extends Entity{

	public final String humanReadableName;
	List<List<Attack>> attackLists;
	public final int maxHP;
	
	public final int casterLevel;
	public final int casterDc;
	public final int casterToHit;
	
	public final int str;
	public final int dex;
	public final int con;
	public final int inte;
	public final int wis;
	public final int cha;
	
	private int currentHp;
	
	private List<Attack> attacksThisTurn = null;
	
	private Map<Spell.SLOTLEVEL, List<Spell>> spells;
	private Map<Spell.SLOTLEVEL, Integer> slotsRemaining;
	
	MonsterInstance(String humanReadableName, int maxHP, List<List<Attack>> attackLists, String personalName, int currentHp,
			int str, int dex, int con, int inte, int wis, int cha, int casterLevel, int casterDc, int casterToHit,
			Map<Spell.SLOTLEVEL, List<Spell>> spells, Map<Spell.SLOTLEVEL, Integer> slotMapping){
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
		
		this.casterLevel = casterLevel;
		this.casterDc = casterDc;
		this.casterToHit = casterToHit;
		this.spells = spells;
		
		if(slotMapping != null) {
			slotsRemaining = new HashMap<>();
			for(Spell.SLOTLEVEL slot : slotMapping.keySet()) {
				slotsRemaining.put(slot, new Integer(slotMapping.get(slot)));
			}
		}
	}
	
	MonsterInstance(String humanReadableName, int maxHP, List<List<Attack>> attackLists, String personalName,
			int str, int dex, int con, int inte, int wis, int cha, int casterLevel, int casterDc, int casterToHit, 
			Map<Spell.SLOTLEVEL, List<Spell>> spells, Map<Spell.SLOTLEVEL, Integer> slotMapping){
		this(humanReadableName, maxHP, attackLists, personalName, maxHP,
				str, dex, con, inte, wis, cha, casterLevel, casterDc, casterToHit, spells, slotMapping);
	}
	
	public void resetTurn() {
		if(attackLists.size() == 1) {
			attacksThisTurn = new ArrayList<>();
			attacksThisTurn.addAll(attackLists.get(0));
		}else {
			attacksThisTurn = null;
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
	
	public int rollInitiative() {
		currentInitiative = Dice.d20() + Helpers.getModifierFromAbility(dex);
		return currentInitiative;
	}
	/*
	public void chooseAttackSet(int number) {
		if(number >= attackLists.size()) {
			throw new IllegalArgumentException("Picked too high an attack set number");
		}else {
			attacksThisTurn = attackLists.get(number - 1);
		}
	}
	*/
	public List<Attack> getCurrentAttacks(){
		return attacksThisTurn;
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
	
	public Spell expendSpell(String spellName, Spell.SLOTLEVEL slotLvl) {
		
			int minSpellLevel = getMinSpellLevel(spellName);
			if(minSpellLevel > slotLvl.level) {
				throw new IllegalArgumentException("Need a spell slot of at least minimum level: " + minSpellLevel);
			}
			Spell.SLOTLEVEL minSpellLevelSlot = Spell.SLOTLEVEL.get(minSpellLevel);
			if(minSpellLevelSlot == Spell.SLOTLEVEL.CANTRIP) {
				return getSpellByCompName(spellName);
			}
			if(slotsRemaining.get(slotLvl) < 1) {
				throw new IllegalArgumentException("Insufficient slots at level: " + slotLvl.level);
			}else {
				slotsRemaining.put(slotLvl, slotsRemaining.get(slotLvl) - 1);
				return getSpellByCompName(spellName);
			}
		
	}
	
	public Spell expendSpell(String spellName) {
		
		int minSpellLevel = getMinSpellLevel(spellName);
		Spell.SLOTLEVEL spellLevel = Spell.SLOTLEVEL.get(minSpellLevel);
		if(spellLevel == Spell.SLOTLEVEL.CANTRIP) {
			return getSpellByCompName(spellName);
		}
		if(slotsRemaining.get(spellLevel) < 1) {
			throw new IllegalArgumentException("Insufficient slots at level: " + spellLevel.level);
		}else {
			slotsRemaining.put(spellLevel, slotsRemaining.get(spellLevel) - 1);
			return getSpellByCompName(spellName);
		}
	
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
