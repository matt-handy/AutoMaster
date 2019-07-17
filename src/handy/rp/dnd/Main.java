package handy.rp.dnd;

import java.io.Console;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import handy.rp.dnd.monsters.MonsterInstance;
import handy.rp.dnd.monsters.MonsterTemplate;
import handy.rp.xml.MonsterParser;

public class Main {

	private List<Entity> currentInitiativeList = new ArrayList<>();
	private Entity currentEntity;
	private int currentPlace;
	private int roundCount;

	private List<MonsterTemplate> monstersAvailable;

	public void addEntity(Entity entity, boolean assignStart) {
		currentInitiativeList.add(entity);
		Collections.sort(currentInitiativeList, new EntityComparator());

		if (currentEntity == null) {
			if (assignStart) {
				startCombat();
			}
		} else {
			currentPlace = currentInitiativeList.indexOf(currentEntity);
		}
	}

	public void startCombat() {
		currentEntity = currentInitiativeList.get(0);
		currentPlace = 0;
		roundCount = 1;
	}

	public static void main(String args[]) {
		Main main = new Main();
		try {
			main.initialize();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		main.runEncounter();
	}

	public void initialize() throws Exception {
		monstersAvailable = MonsterParser.loadAll("monsters");
	}

	public void runEncounter() {
		Console console = System.console();
		if (console == null) {
			throw new Error("Cannot start console");
		}

		String nextCommand;
		while ((nextCommand = console.readLine()) != null) {
			String args[] = nextCommand.split(" ");

			if (args.length < 1) {
				console.printf("Improper command");
				continue;
			}

			String command = args[0];

			switch (command) {
			case "amon":
			case "addmonster":
				console.writer().println(addMonster(args));
				break;
			case "apc":
			case "addplayercharacter":
				if (args.length != 3) {
					console.writer().println("apc <character name> <initiative>");
					continue;
				}

				Entity pc = new Entity(args[1]);
				try {
					int init = Integer.parseInt(args[2]);
					pc.setInitiative(init);
					addEntity(pc, false);
				} catch (NumberFormatException e) {
					console.writer().println("Second argument must be an initiative number");
					continue;
				}
				break;
			case "gr":
			case "getround":
				console.writer().println("Current round is: " + roundCount);
				break;
			case "sc":
			case "startcombat":
				startCombat();
				console.writer().println("First in order: " + currentInitiativeList.get(currentPlace).personalName);
				break;
			case "advturn":
			case "advanceturn":
				if (currentPlace + 1 == currentInitiativeList.size()) {
					roundCount++;
					console.writer().println("New round! Current round: " + roundCount);
					currentPlace = 0;
				}else {
					currentPlace++;
				}
				Entity entity = currentInitiativeList.get(currentPlace);
				console.writer().println("Next in order: " + entity.personalName);
				break;
			case "li":
			case "listinitiative":
				for (Entity ent : currentInitiativeList) {
					console.writer().println(ent.personalName + " " + ent.getCurrentInitiative());
				}
				break;
			case "lam":
			case "listmonsters":
				for (MonsterTemplate m : monstersAvailable) {
					console.writer().println("Monster " + m.humanReadableName);
				}
				break;
			default:
				console.writer().println("Unknown command: " + command);
				break;
			}
		}
	}

	String addMonster(String args[]) {
		if (args.length != 3) {
			return "amon <name or index of template> <unique name>";
		}
		MonsterInstance monster;
		try {
			int idx = Integer.parseInt(args[1]);
			if (idx >= monstersAvailable.size()) {
				return "Invalid monster index supplied";
			}
			monster = monstersAvailable.get(idx).getInstance(args[2]);
		} catch (NumberFormatException ex) {
			// we were given a name
			MonsterTemplate mt = getMonsterByName(args[1]);
			if (mt == null) {
				return "Unknown monster name specified";
			}
			monster = mt.getInstance(args[2]);
		}

		monster.rollInitiative();
		addEntity(monster, false);
		return "Added " + monster.humanReadableName + " as " + monster.personalName + " with initiative "
				+ monster.getCurrentInitiative();
	}

	private MonsterTemplate getMonsterByName(String name) {
		for (MonsterTemplate tm : monstersAvailable) {
			if (tm.humanReadableName.equalsIgnoreCase(name)) {
				return tm;
			}
		}
		return null;
	}
}
