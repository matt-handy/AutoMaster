package handy.rp.dnd.attacks;

import java.util.ArrayList;
import java.util.List;

public class AttackBuilder {

	private List<DamageComponent> damagers = new ArrayList<DamageComponent>();
	private final String readableAttackName;
	
	public AttackBuilder(String readableAttackName) {
		this.readableAttackName = readableAttackName;
	}
	
	public void addDamageComponent(DamageComponent dc) {
		damagers.add(dc);
	}
	
	public Attack build() {
		if(damagers.size() < 1) {
			throw new IllegalArgumentException("Trying to build without attacks");
		}
		return new Attack(damagers, readableAttackName);
	}
}
