package handy.rp.dnd.monsters;

import java.util.ArrayList;
import java.util.List;

import handy.rp.dnd.attacks.Attack;

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
		return new MonsterTemplate(humanReadableName, maxHP, attackLists, str, dex, con, inte, wis, cha);
	}
	
}
