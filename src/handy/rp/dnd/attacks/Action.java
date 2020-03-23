package handy.rp.dnd.attacks;

import handy.rp.Dice;
import handy.rp.Dice.DICE_TYPE;
import handy.rp.dnd.spells.ActionSpell;

public class Action {
	public final String name;
	public final String cname;
	public final String text;
	public final ActionSpell spell;
	public final Attack attack;
	public final DICE_TYPE rechargeDice;
	public final int rechargeDiceMeets;
	
	public Action(String name, String cname, String text, ActionSpell spell, Attack attack,
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
