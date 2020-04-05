package handy.rp.dnd;

import java.util.List;

import handy.rp.Dice;
import handy.rp.Dice.DICE_TYPE;
import handy.rp.dnd.attacks.Attack;
import handy.rp.dnd.spells.ActionSpell;

public class Action {
	public final String name;
	public final String cname;
	public final String text;
	public final ActionSpell spell;
	public final List<Attack> attack; //TODO: don't expose this list raw
	public final DICE_TYPE rechargeDice;
	public final int rechargeDiceMeets;
	
	public Action(String name, String cname, String text, ActionSpell spell, List<Attack> attack,
			DICE_TYPE rechargeDice, int rechargeDiceMeets) {
		this.name = name;
		this.cname = cname;
		this.text = text;
		this.spell = spell;
		this.attack = attack;
		this.rechargeDice = rechargeDice;
		this.rechargeDiceMeets = rechargeDiceMeets;
	}
	
	
}
