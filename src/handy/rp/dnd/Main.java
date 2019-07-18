package handy.rp.dnd;

import java.io.Console;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import handy.rp.dnd.attacks.Attack;
import handy.rp.dnd.attacks.Damage;
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
			case "rm":
			case "remove":
				console.writer().println(rmMonster(args));
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
				if(currentInitiativeList.get(currentPlace) instanceof MonsterInstance) {
					MonsterInstance mi = (MonsterInstance) currentInitiativeList.get(currentPlace);
					console.writer().println(mi.listAttacksReadable());
				}
				break;
			case "advturn":
			case "advanceturn":
				if(currentInitiativeList.get(currentPlace) instanceof MonsterInstance) {
					MonsterInstance mi = (MonsterInstance) currentInitiativeList.get(currentPlace);
					mi.resetTurn();
				}
				if (currentPlace + 1 == currentInitiativeList.size()) {
					roundCount++;
					console.writer().println("New round! Current round: " + roundCount);
					currentPlace = 0;
				}else {
					currentPlace++;
				}
				Entity entity = currentInitiativeList.get(currentPlace);
				console.writer().println("Next in order: " + entity.personalName);
				if(currentInitiativeList.get(currentPlace) instanceof MonsterInstance) {
					MonsterInstance mi = (MonsterInstance) currentInitiativeList.get(currentPlace);
					console.writer().println(mi.listAttacksReadable());
				}
				break;
			case "li":
			case "listinitiative":
				int idx = 0;
				for (Entity ent : currentInitiativeList) {
					console.writer().println(idx + " " + ent.personalName + " " + ent.getCurrentInitiative());
					idx++;
				}
				break;
			case "lam":
			case "listmonsters":
				int jdx = 0;
				for (MonsterTemplate m : monstersAvailable) {
					console.writer().println("Monster: " + jdx + " - " + m.humanReadableName);
					jdx++;
				}
				break;
			case "la":
			case "listattack":
				if(currentInitiativeList.get(currentPlace) instanceof MonsterInstance) {
					MonsterInstance mi = (MonsterInstance) currentInitiativeList.get(currentPlace);
					console.writer().println(mi.listRemainingAttacksReadable());
				}else {
					console.writer().println("Must be a monster to list attacks");
				}
				break;
			case "at":
			case "attack":
				console.writer().println(attack(args));
				break;
			default:
				console.writer().println("Unknown command: " + command);
				break;
			}
		}
	}
	
	String attack(String args[]) {
		if (args.length != 2) {
			return "at <attack index>";
		}
		
		if (!(currentInitiativeList.get(currentPlace) instanceof MonsterInstance)) {
			return "Must be a monster to attack";
		}
		
		MonsterInstance monster = (MonsterInstance) currentInitiativeList.get(currentPlace);
		
		try {
			int attackIdx = Integer.parseInt(args[1]);
			try {
				Attack chosenAttack = monster.expendAttack(attackIdx);
				chosenAttack.rollDamage();
				return chosenAttack.readDamage();
			}catch(IllegalArgumentException ex) {
				return "Too high an index, not a valid attack";
			}
			
		}catch(NumberFormatException e) {
			return "Invalid attack index supplied";
		}
	}
	
	String rmMonster(String args[]) {
		if (args.length != 2) {
			return "rm <character name>";
		}
		
		try {
			int idx = Integer.parseInt(args[1]);
			if(idx <= currentInitiativeList.size()) {
				Entity ent = currentInitiativeList.remove(idx);
				return "Removed: " + ent.personalName;
			}else {
				return "Invalid index supplied";
			}
		} catch (NumberFormatException e) {
			int idx = getIndexOfNamedEntity(args[1]);
			if(idx >= 0) {
				Entity ent = currentInitiativeList.remove(idx);
				return "Removed: " + ent.personalName;
			}else {
				return "Invalid name";
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
	
	private int getIndexOfNamedEntity(String name) {
		for(int idx = 0; idx < currentInitiativeList.size(); idx++) {
			Entity ent = currentInitiativeList.get(idx);
			if(ent.personalName.equals(name)) {
				return idx;
			}
		}
		return -1;
	}
}
