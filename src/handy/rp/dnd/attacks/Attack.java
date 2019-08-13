package handy.rp.dnd.attacks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Attack {
	
	private List<DamageComponent> damagers = new ArrayList<DamageComponent>();
	public final String readableAttackName;
	
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
	
	public static String readDamage(String attackName, Set<Damage> damages) {
		StringBuilder sBuilder = new StringBuilder(attackName);
		sBuilder.append(" hits for ");
		
		boolean firstDamage = true;
		
		for(Damage damage : damages) {
			if(firstDamage) {
				firstDamage = false;
			}else {
				sBuilder.append(" and ");
			}
			sBuilder.append(damage.getHumanReadableDamage());
		}
		
		return sBuilder.toString();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(readableAttackName);
		sb.append(" hits for ");
		
		boolean firstDamage = true;
		
		for(DamageComponent damage : damagers) {
			if(firstDamage) {
				firstDamage = false;
			}else {
				sb.append(" and ");
			}
			sb.append(damage.toString());
		}
		
		return sb.toString();
	}
}
