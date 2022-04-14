package handy.rp.dnd;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Console;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import handy.rp.GameRunner;
import handy.rp.OutcomeNotification;
import handy.rp.dnd.EntityCondition.CONDITIONS;
import handy.rp.dnd.character.PlayerCharacter;
import handy.rp.dnd.monsters.MonsterInstance;
import handy.rp.dnd.spells.Spell;

public class EncounterRunner extends GameRunner{

	protected List<Entity> currentInitiativeList = new ArrayList<>();
	protected Entity currentEntity;
	
	protected int roundCount;

	private BufferedWriter logFile = null;

	protected void log(String message) {
		if (logFile != null) {
			try {
				logFile.write(message + System.lineSeparator());
				logFile.flush();
			} catch (IOException e) {
				
			}
		}
	}

	

	public Entity getCurrentEntity() {
		return currentEntity;
	}

	

	public static void main(String args[]) {
		

		try {
			Console console = System.console();
			if (console == null) {
				throw new Error("Cannot start console");
			}
			if (args.length > 0) {// Single player mode
				SinglePlayerEncounterRunner main = new SinglePlayerEncounterRunner();
				main.singlePlayerMode(console.writer(), new BufferedReader(console.reader()), args[0]);
			} else {
				DungeonMasterEncounterRunner main = new DungeonMasterEncounterRunner();
				main.initialize();
				main.runEncounter(console.writer(), new BufferedReader(console.reader()));
				main.shutdown();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	

	public void initialize() throws Exception {
		try {
			logFile = new BufferedWriter(new FileWriter("log", true));
		} catch (IOException e) {
			e.printStackTrace();
			logFile = null;
		}
	}

	public void shutdown() {
		try {
			logFile.close();
			logFile = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	

	void listSpells(PrintWriter pw) {
		if (currentEntity instanceof ManagedEntity) {
			ManagedEntity mi = (ManagedEntity) currentEntity;

			String spells = mi.listSpells();
			if (spells.length() != 0) {
				pw.println("Available Spells:");
				pw.println(spells);
			}
		} else {
			pw.println("Current actor does not have managed spells");
		}
		if (currentEntity instanceof MonsterInstance) {
			MonsterInstance mi = (MonsterInstance) currentEntity;

			String innateSpells = mi.listInnateSpells();
			if (innateSpells.length() != 0) {
				pw.println("Innate Spells:");
				pw.println(innateSpells);
			}
		}
	}

	

	OutcomeNotification takeReaction(String[] args) {
		String reaction = args[1];
		int midx;
		try {
			midx = Integer.parseInt(args[2]);
		} catch (NumberFormatException ex) {
			return new OutcomeNotification("Need reactor index", false);
		}
		if (midx >= currentInitiativeList.size()) {
			return new OutcomeNotification("Invalid index", false);
		}
		Entity reactorE = currentInitiativeList.get(midx);

		Entity target = null;
		if (args.length == 4) {
			int tidx;
			try {
				tidx = Integer.parseInt(args[3]);
			} catch (NumberFormatException ex) {
				return new OutcomeNotification("Need entity index", false);
			}
			target = currentInitiativeList.get(tidx);
		}
		if (reactorE instanceof MonsterInstance) {
			MonsterInstance mi = (MonsterInstance) reactorE;
			OutcomeNotification outcome = mi.expendReaction(reaction);
			if (outcome.outcome) {
				String actionString = outcome.humanMessage;
				String message = mi.personalName + " takes reaction";
				if (target != null) {
					message += " on " + target.personalName;
				}
				message += System.lineSeparator();
				message += actionString;
				log(message);
				return new OutcomeNotification(message, true);
			} else {
				return outcome;
			}
		} else {
			return new OutcomeNotification("Only monsters can take reactions now", false);
		}
	}

	OutcomeNotification rollSave(String[] args) {
		if (args.length != 2 && args.length != 3) {
			return new OutcomeNotification("rollSave <str|dex|con|int|wis|cha> <optional - entity index>", false);
		}
		String saveType = args[1];
		Entity entity = currentEntity;
		if (args.length == 3) {
			int midx;
			try {
				midx = Integer.parseInt(args[2]);
			} catch (NumberFormatException ex) {
				return new OutcomeNotification("Need entity index", false);
			}
			entity = currentInitiativeList.get(midx);
		}

		if (entity instanceof ManagedEntity) {
			ManagedEntity mi = (ManagedEntity) entity;
			if (saveType.equalsIgnoreCase("str")) {
				return new OutcomeNotification(
						mi.personalName + " rolls a strength saving throw of " + mi.strSaveThrow(), true);
			} else if (saveType.equalsIgnoreCase("dex")) {
				return new OutcomeNotification(
						mi.personalName + " rolls a dexterity saving throw of " + mi.dexSaveThrow(), true);
			} else if (saveType.equalsIgnoreCase("con")) {
				return new OutcomeNotification(
						mi.personalName + " rolls a constitution saving throw of " + mi.conSaveThrow(), true);
			} else if (saveType.equalsIgnoreCase("wis")) {
				return new OutcomeNotification(mi.personalName + " rolls a wisdom saving throw of " + mi.wisSaveThrow(),
						true);
			} else if (saveType.equalsIgnoreCase("int")) {
				return new OutcomeNotification(
						mi.personalName + " rolls a intelligence saving throw of " + mi.intSaveThrow(), true);
			} else if (saveType.equalsIgnoreCase("cha")) {
				return new OutcomeNotification(
						mi.personalName + " rolls a charisma saving throw of " + mi.chaSaveThrow(), true);
			} else {
				return new OutcomeNotification("Invalid saving throw type: " + saveType, false);
			}
		} else {
			return new OutcomeNotification("Only monsters and managed players can roll saving throws for now", false);
		}
	}

	OutcomeNotification addOrRemoveCondition(String[] args) {
		boolean amAdding = false;
		if (args[0].equals("addCon")) {
			amAdding = true;
		}
		try {
			CONDITIONS condition = EntityCondition.getCondition(args[1]);
			Entity entity = currentEntity;
			if (args.length == 3) {
				int midx;
				try {
					midx = Integer.parseInt(args[1]);
				} catch (NumberFormatException ex) {
					return new OutcomeNotification("Need entity index", false);
				}
				entity = currentInitiativeList.get(midx);
			}

			String message;
			if (amAdding) {
				entity.addCondition(condition);
				message = "Adding condition to " + entity.personalName + ": " + condition;
			} else {
				entity.removeConditions(condition);
				message = "Removing condition from " + entity.personalName + ": " + condition;
			}
			log(message);
			return new OutcomeNotification(message, true);
		} catch (IllegalArgumentException ex) {
			return new OutcomeNotification(ex.getMessage(), false);
		}

	}

	String getAttrs(String[] args) {
		int midx;
		try {
			midx = Integer.parseInt(args[1]);
		} catch (NumberFormatException ex) {
			return "Need monster index";
		}
		Entity target = currentInitiativeList.get(midx);
		return target.listStats();
	}

	String listLegendaryActions(String args[]) {
		if (args.length > 2) {
			return "listlact <optional creature index>";
		}
		if (args.length == 1) {
			if (currentEntity instanceof MonsterInstance) {
				MonsterInstance mi = (MonsterInstance) currentEntity;
				return mi.listLegendaryActions();
			} else {
				return "Can only list legendary actions for monsters";
			}
		} else {
			try {
				Integer idx = Integer.parseInt(args[1]);
				if (idx >= currentInitiativeList.size()) {
					return "Index out of range of current monster set";
				} else {
					Entity entity = currentInitiativeList.get(idx);
					if (entity instanceof MonsterInstance) {
						MonsterInstance mi = (MonsterInstance) entity;
						return mi.listLegendaryActions();
					} else {
						return "Can only list legendary actions for monsters";
					}
				}
			} catch (NumberFormatException ex) {
				return "Need a proper index for the monster";
			}
		}
	}

	String listActions() {
		if (currentEntity instanceof MonsterInstance) {
			MonsterInstance mi = (MonsterInstance) currentEntity;
			return mi.listActions();
		} else {
			return "Can only list actions for monsters";
		}
	}

	OutcomeNotification hpMod(String args[]) {
		String cmd = args[0];

		if (args.length != 3 && args.length != 4) {
			if (cmd.equals("heal")) {
				return new OutcomeNotification("heal <monster> <hp> <option - action taker index>", false);
			} else {
				return new OutcomeNotification("hit <monster> <hp> <option - action taker index>", false);
			}
		}

		int midx;
		try {
			midx = Integer.parseInt(args[1]);
		} catch (NumberFormatException ex) {
			return new OutcomeNotification("Need monster index", false);
		}
		Entity target = currentInitiativeList.get(midx);

		if (target instanceof MonsterInstance) {
			MonsterInstance mi = (MonsterInstance) target;
			try {
				StringBuilder output = new StringBuilder();
				int hp = Integer.parseInt(args[2]);
				if (cmd.equals("heal")) {
					mi.heal(hp);
					if (args.length == 4) {
						try {
							int healer = Integer.parseInt(args[3]);
							if (healer >= currentInitiativeList.size()) {
								return new OutcomeNotification("Invalid healer specified", false);
							}
							log(mi.personalName + " is healed by " + currentInitiativeList.get(healer).personalName
									+ " for " + hp);
						} catch (NumberFormatException | IndexOutOfBoundsException ex) {
							log(mi.personalName + " is healed for " + hp);
						}
					} else {
						log(mi.personalName + " is healed for " + hp);
					}
				} else {
					String hitMsg = mi.hit(hp);
					if (args.length == 4) {
						try {
							int healer = Integer.parseInt(args[3]);
							if (healer >= currentInitiativeList.size()) {
								return new OutcomeNotification("Invalid attacker specified", false);
							}
							log(mi.personalName + " is hit by " + currentInitiativeList.get(healer).personalName
									+ " for " + hp);
						} catch (NumberFormatException | IndexOutOfBoundsException ex) {
							log(mi.personalName + " is hit for " + hp);
						}
					} else {
						log(mi.personalName + " is hit for " + hp);
					}
					if (hitMsg != null) {
						log(hitMsg);
						output.append(hitMsg + System.lineSeparator());
					}
				}
				log(mi.personalName + " HP: " + mi.getCurrentHp());
				output.append("Current HP: " + mi.getCurrentHp());
				return new OutcomeNotification(output.toString(), true);
			} catch (NumberFormatException ex) {
				return new OutcomeNotification("Invalid HP supplied.", false);
			}
		} else {
			return new OutcomeNotification("Current actor does not have managed HP", false);
		}
	}

	

	OutcomeNotification doLegendaryAction(String args[]) {
		if (args.length != 3 && args.length != 4) {
			return new OutcomeNotification(
					"lact <actor index> <action name> <optional - actee name> => take legendary action", false);
		}

		try {
			Integer monsterIdx = Integer.parseInt(args[1]);
			Integer actee = null;
			if (args.length == 4) {
				actee = Integer.parseInt(args[3]);
				if (actee >= currentInitiativeList.size()) {
					return new OutcomeNotification("Invalid index for actee", false);
				}
			}
			if (monsterIdx >= currentInitiativeList.size()) {
				return new OutcomeNotification("Monster index too high", false);
			}
			if (!(currentInitiativeList.get(monsterIdx) instanceof MonsterInstance)) {
				return new OutcomeNotification("Must be a monster", false);
			}
			MonsterInstance monster = (MonsterInstance) currentInitiativeList.get(monsterIdx);
			OutcomeNotification notice = monster.expandLegendaryAction(args[2]);
			String actionResult = notice.humanMessage;
			if (actee != null) {
				Entity target = currentInitiativeList.get(actee);
				actionResult += "against target " + target.personalName;
			}
			log(actionResult);
			return new OutcomeNotification(actionResult, notice.outcome);
		} catch (NumberFormatException ex) {
			return new OutcomeNotification("Need a valid index for both actor and actee", false);
		}
	}

	

	OutcomeNotification castSpell(String args[]) {
		if (args.length != 3 && args.length != 2) {
			return new OutcomeNotification("cast <spellname> <level> | cast <spellname>", false);
		}

		if (!(currentEntity instanceof ManagedEntity)) {
			return new OutcomeNotification("Must be a ManagedEntity to attack", false);
		}

		ManagedEntity monster = (ManagedEntity) currentEntity;

		if (args.length == 3) {
			try {
				int spellLevel = Integer.parseInt(args[2]);
				try {
					Spell.SLOTLEVEL slotLevel = Spell.SLOTLEVEL.get(spellLevel);
					Spell spell = monster.expendSpell(args[1], slotLevel);
					PlayerCharacter optionalPc = null;
					String result = spell.cast(slotLevel, monster.getCasterLevel(), monster.getSpellSaveDC(),
							monster.getSpellToHit(), monster.getSpellcastingModifierValue(), optionalPc);
					log(currentEntity.personalName + " cast " + spell.readableName);
					log(result);
					return new OutcomeNotification(result, true);
				} catch (IllegalArgumentException ex) {
					return new OutcomeNotification(ex.getMessage(), false);
				}

			} catch (NumberFormatException e) {
				return new OutcomeNotification("Invalid attack index supplied", false);
			}
		} else {
			try {
				Spell spell = monster.expendSpell(args[1]);
				String result = spell.cast(spell.minimumLevel, monster.getCasterLevel(), monster.getSpellSaveDC(),
						monster.getSpellToHit(), monster.getSpellcastingModifierValue());
				log(currentEntity.personalName + " cast " + spell.readableName);
				log(result);
				return new OutcomeNotification(result, true);
			} catch (IllegalArgumentException ex) {
				return new OutcomeNotification(ex.getMessage(), false);
			}
		}
	}

	
}
