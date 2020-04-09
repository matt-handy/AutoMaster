package handy.rp.dnd;

public class LegendaryAction extends Action {

	public final int charges;
	
	public LegendaryAction(Action action, int charges) {
		super(action.name, action.cname, action.text, action.spell, action.attack,
				action.rechargeDice, action.rechargeDiceMeets);
		this.charges = charges;
	}

}
