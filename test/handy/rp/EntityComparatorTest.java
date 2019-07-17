package handy.rp;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import handy.rp.dnd.monsters.MonsterInstance;
import handy.rp.dnd.monsters.MonsterTemplate;
import handy.rp.xml.MonsterParser;
import handy.rp.dnd.Entity;
import handy.rp.dnd.EntityComparator;

class EntityComparatorTest {

	@Test
	void test() {
		try {
			MonsterTemplate hillGiant = MonsterParser.load("monsters\\HillGiant.xml");
			MonsterInstance giant1 = hillGiant.getInstance("Giant 1");
			giant1.rollInitiative();
			MonsterInstance giant2 = hillGiant.getInstance("Giant 2");
			giant2.rollInitiative();
			MonsterInstance giant3 = hillGiant.getInstance("Giant 3");
			giant3.rollInitiative();
			
			List<Entity> monsters = new ArrayList<>();
			monsters.add(giant1);
			monsters.add(giant2);
			monsters.add(giant3);
			
			Collections.sort(monsters, new EntityComparator());
			
			Entity lastMonster = null;
			for(Entity entity : monsters) {
				if(lastMonster != null) {
					assertTrue(lastMonster.getCurrentInitiative() >= entity.getCurrentInitiative());
				}
				lastMonster = entity;
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

}
