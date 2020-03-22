package handy.rp.dnd.attacks;

import handy.rp.dnd.spells.ActionSpell;

public class Action {
	public final String name;
	public final String cname;
	public final String text;
	public final ActionSpell spell;
	public final Attack attack;
	
	public Action(String name, String cname, String text, ActionSpell spell, Attack attack) {
		this.name = name;
		this.cname = cname;
		this.text = text;
		this.spell = spell;
		this.attack = attack;
	}
	
	
}
