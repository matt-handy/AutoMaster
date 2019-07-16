package handy.rp.dnd.monsters;

import java.util.List;

import handy.rp.dnd.attacks.Attack;

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
	
	MonsterTemplate(String humanReadableName, int maxHP, List<List<Attack>> attackLists,
			int str, int dex, int con, int inte, int wis, int cha){
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
		
	public List<List<Attack>> getAttacks(){
		return attackLists;
	}
	
	public MonsterInstance getInstance(String personalName) {
		return new MonsterInstance(humanReadableName, maxHP, attackLists, personalName, str, dex, con, inte, wis, cha);
	}
	
}
