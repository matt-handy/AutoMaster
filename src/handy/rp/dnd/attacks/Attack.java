package handy.rp.dnd.attacks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Attack {
	
	private List<DamageComponent> damagers = new ArrayList<DamageComponent>();
	private final String readableAttackName;
	
	//Keep at package, folks should use Builder
	Attack(List<DamageComponent> damagers, String readableAttackName){
		this.damagers = damagers;
		this.readableAttackName = readableAttackName;
	}
	
	private Set<Damage> lastDamage;
	
	public Set<Damage> rollDamage(){
		lastDamage = new HashSet<Damage>();
		
		for(DamageComponent dc : damagers) {
			lastDamage.add(dc.rollDamage());
		}
		
		return lastDamage;
	}
	
	public String readDamage() {
		StringBuilder sBuilder = new StringBuilder(readableAttackName);
		sBuilder.append(" hits for ");
		
		boolean firstDamage = true;
		
		for(Damage damage : lastDamage) {
			if(firstDamage) {
				firstDamage = false;
			}else {
				sBuilder.append(" and ");
			}
			sBuilder.append(damage.getHumanReadableDamage());
		}
		
		return sBuilder.toString();
	}
}
