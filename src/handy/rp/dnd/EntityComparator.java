package handy.rp.dnd;

import java.util.Comparator;

public class EntityComparator implements Comparator<Entity> {

	@Override
	public int compare(Entity o1, Entity o2) {
		return o2.currentInitiative - o1.currentInitiative;
	}

}
