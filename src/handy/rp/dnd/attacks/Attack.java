package handy.rp.dnd.attacks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import handy.rp.Dice;

public class Attack {
	
	private List<DamageComponent> damagers = new ArrayList<DamageComponent>();
	public final String readableAttackName;
	public final int toHit;
	
	//Keep at package, folks should use Builder
	Attack(List<DamageComponent> damagers, String readableAttackName, int toHit){
		this.damagers = damagers;
		this.readableAttackName = readableAttackName;
		this.toHit = toHit;
	}
	
	private Set<Damage> lastDamage;
	
	public Set<Damage> rollDamage(){
		lastDamage = new HashSet<Damage>();
		
		for(DamageComponent dc : damagers) {
			lastDamage.add(dc.rollDamage());
		}
		
		return lastDamage;
	}
	
	public static String readDamage(Set<Damage> damages, Attack attack) {
		StringBuilder sBuilder = new StringBuilder(attack.readableAttackName);
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
		
		sBuilder.append(" with a hit dice of " + (Dice.d20() + attack.toHit) + " | "+ (Dice.d20() + attack.toHit));
		
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
