package handy.rp.dnd.monsters;

import java.util.ArrayList;
import java.util.List;

import handy.rp.Dice;
import handy.rp.dnd.Entity;
import handy.rp.dnd.Helpers;
import handy.rp.dnd.attacks.Attack;

public class MonsterInstance extends Entity{

	public final String humanReadableName;
	List<List<Attack>> attackLists;
	public final int maxHP;
	
	public final int str;
	public final int dex;
	public final int con;
	public final int inte;
	public final int wis;
	public final int cha;
	
	private int currentHp;
	
	private List<Attack> attacksThisTurn = null;
	
	MonsterInstance(String humanReadableName, int maxHP, List<List<Attack>> attackLists, String personalName, int currentHp,
			int str, int dex, int con, int inte, int wis, int cha){
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
		
	}
	
	MonsterInstance(String humanReadableName, int maxHP, List<List<Attack>> attackLists, String personalName,
			int str, int dex, int con, int inte, int wis, int cha){
		this(humanReadableName, maxHP, attackLists, personalName, maxHP,
				str, dex, con, inte, wis, cha);
	}
	
	public void resetTurn() {
		if(attackLists.size() == 1) {
			attacksThisTurn = attackLists.get(0);
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

}
