package handy.rp.dnd;

import java.util.Comparator;

public class EntityComparator implements Comparator<Entity> {

	@Override
	public int compare(Entity o1, Entity o2) {
		//TODO: does not handle ties and resolve by dex 
		return o2.currentInitiative - o1.currentInitiative;
	}

}
