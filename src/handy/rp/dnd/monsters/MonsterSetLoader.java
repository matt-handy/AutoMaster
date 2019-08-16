package handy.rp.dnd.monsters;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MonsterSetLoader {

	public static List<MonsterInstance> getMonsterSet(List<MonsterTemplate> monstersAvailable, String setName){
		List<MonsterInstance> monsters = new ArrayList<>();
		
		try {
		
		File file = new File(setName);
		Scanner scanner = new Scanner(file);
		
		try {	
			
			while (scanner.hasNextLine()) {
				String elems[] = scanner.nextLine().split(",");
				boolean foundMonster = false;
				for(MonsterTemplate mt : monstersAvailable) {
					if(mt.humanReadableName.equalsIgnoreCase(elems[0])) {
						monsters.add(mt.getInstance(elems[1]));
						foundMonster = true;
						break;
					}
				}
				if(!foundMonster) {
					throw new IllegalArgumentException("Non-existant monster given: " + elems[0]);
				}
			}
		} finally {
			scanner.close();
		}
		}catch(IOException ex) {
			throw new IllegalArgumentException("Gave me a bad filename: " + setName);
		}
		
		return monsters;
	}
}
