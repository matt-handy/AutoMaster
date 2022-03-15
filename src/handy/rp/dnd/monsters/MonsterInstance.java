package handy.rp.dnd.monsters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import handy.rp.Dice;
import handy.rp.OutcomeNotification;
import handy.rp.dnd.Action;
import handy.rp.dnd.CharClass.SPELLCASTING_MODIFIER;
import handy.rp.dnd.Entity;
import handy.rp.dnd.Helpers;
import handy.rp.dnd.LegendaryAction;
import handy.rp.dnd.ManagedEntity;
import handy.rp.dnd.attacks.Attack;
import handy.rp.dnd.attacks.Damage;
import handy.rp.dnd.spells.Spell;

public class MonsterInstance extends ManagedEntity {

	public final String humanReadableName;
	List<List<Attack>> attackLists;

	public final int casterLevel;
	public final int casterDc;
	public final int casterToHit;
	public final int casterInnateDc;
	
	private List<Attack> attacksThisTurn = null;

	// Innate spells possessed, plus number of charges per day
	private Map<Spell, Integer> innateSpells;
	public static final int AT_WILL = 9999;

	private Map<Action, Integer> actions;
	private Map<Action, Boolean> actionReadiness = new HashMap<>();

	private int maxCharges;
	private int currentCharges;
	private Map<LegendaryAction, Integer> legendaryActions;

	public final int ac;
	public final int speed;
	public final String attrs;

	private Map<String, String> reactions = null;

	MonsterInstance(String humanReadableName, int maxHP, List<List<Attack>> attackLists, String personalName,
			int currentHp, int str, int dex, int con, int inte, int wis, int cha, int casterLevel, int casterDc,
			int casterInnateDc, int casterToHit, SPELLCASTING_MODIFIER spellcastingMod, int strsave, int dexsave, int consave, int intsave, int wissave,
			int chasave, Map<Spell.SLOTLEVEL, List<Spell>> spells, Map<Spell.SLOTLEVEL, Integer> slotMapping,
			Map<Spell, Integer> innateSpells, Map<Action, Integer> actions, int legendaryCharges,
			Map<LegendaryAction, Integer> legendaryActions, int ac, int speed, String attrs,
			Map<String, String> reactions) {
		super(personalName, str, dex, con, inte, wis, cha, spells, slotMapping, currentHp, currentHp, spellcastingMod);
		this.humanReadableName = humanReadableName;
		this.attackLists = attackLists;

		this.strsave = strsave;
		this.dexsave = dexsave;
		this.consave = consave;
		this.intsave = intsave;
		this.wissave = wissave;
		this.chasave = chasave;

		this.ac = ac;
		this.speed = speed;
		this.attrs = attrs;

		this.casterLevel = casterLevel;
		this.casterDc = casterDc;
		this.casterInnateDc = casterInnateDc;
		this.casterToHit = casterToHit;

		this.innateSpells = innateSpells;

		if (actions != null) {
			this.actions = new HashMap<>();
			for (Action action : actions.keySet()) {
				this.actions.put(action, actions.get(action));
			}
			for (Action action : actions.keySet()) {
				if (action.rechargeDice != null) {
					actionReadiness.put(action, true);
				}
			}
		} else {
			this.actions = actions;
		}
		maxCharges = currentCharges = legendaryCharges;
		this.legendaryActions = legendaryActions;

		this.reactions = reactions;
	}

	MonsterInstance(String humanReadableName, int maxHP, List<List<Attack>> attackLists, String personalName, int str,
			int dex, int con, int inte, int wis, int cha, int casterLevel, int casterDc, int casterInnateDc,
			int casterToHit, SPELLCASTING_MODIFIER spellcastingMod, int strsave, int dexsave, int consave, int intsave, int wissave, int chasave,
			Map<Spell.SLOTLEVEL, List<Spell>> spells, Map<Spell.SLOTLEVEL, Integer> slotMapping,
			Map<Spell, Integer> innateSpells, Map<Action, Integer> actions, int legendaryCharges,
			Map<LegendaryAction, Integer> legendaryActions, int ac, int speed, String attrs,
			Map<String, String> reactions) {
		this(humanReadableName, maxHP, attackLists, personalName, maxHP, str, dex, con, inte, wis, cha, casterLevel,
				casterDc, casterInnateDc, casterToHit, spellcastingMod, strsave, dexsave, consave, intsave, wissave, chasave, spells,
				slotMapping, innateSpells, actions, legendaryCharges, legendaryActions, ac, speed, attrs, reactions);
	}
	
	@Override
	public int strSaveThrow() {
		int save = Dice.d20();
		if(strsave != 0) {
			save += strsave;
		}else {
			 save += Helpers.getModifierFromAbility(str);
		}
		return save;
	}
	@Override
	public int dexSaveThrow() {
		int save = Dice.d20();
		if(dexsave != 0) {
			save += dexsave;
		}else {
			 save += Helpers.getModifierFromAbility(dex);
		}
		return save;
	}
	@Override
	public int conSaveThrow() {
		int save = Dice.d20();
		if(consave != 0) {
			save += consave;
		}else {
			 save += Helpers.getModifierFromAbility(con);
		}
		return save;
	}
	@Override
	public int intSaveThrow() {
		int save = Dice.d20();
		if(intsave != 0) {
			save += intsave;
		}else {
			 save += Helpers.getModifierFromAbility(inte);
		}
		return save;
	}
	@Override
	public int wisSaveThrow() {
		int save = Dice.d20();
		if(wissave != 0) {
			save += wissave;
		}else {
			 save += Helpers.getModifierFromAbility(wis);
		}
		return save;
	}
	@Override
	public int chaSaveThrow() {
		int save = Dice.d20();
		if(chasave != 0) {
			save += chasave;
		}else {
			 save += Helpers.getModifierFromAbility(cha);
		}
		return save;
	}
	
	@Override
	public int getCasterLevel() {
		return casterLevel;
	}
	
	@Override
	public Integer getSpellSaveDC() {
		return casterDc;
	}
	
	@Override
	public Integer getSpellToHit() {
		return casterToHit;
	}
	
	public void resetTurn() {
		if (attackLists.size() == 1) {
			attacksThisTurn = new ArrayList<>();
			attacksThisTurn.addAll(attackLists.get(0));
		} else {
			attacksThisTurn = null;
		}
		for (Action action : actionReadiness.keySet()) {
			if (!actionReadiness.get(action)) {
				int roll = action.rechargeDice.roll();
				if (roll >= action.rechargeDiceMeets) {
					actionReadiness.put(action, true);
				}
			}
		}
	}

	@Override
	public OutcomeNotification expendReaction(String reactionName) {
		if (canTakeReaction()) {
			reactionsRemaining--;
			String response = personalName + " takes reaction: " + reactionName;
			if (reactionName.equalsIgnoreCase("oppAtt")) {
				StringBuilder sb = new StringBuilder();
				sb.append("Available attacks for opportunity attack: ");
				for (List<Attack> set : attackLists) {
					for (Attack attack : set) {
						Set<Damage> damages = attack.rollDamage();
						String result = Attack.readDamage(damages, attack, this);
						sb.append(result);
						sb.append(System.lineSeparator());
					}
				}
				return new OutcomeNotification(sb.toString(), true);
			} else if (reactions != null || reactions.containsKey(reactionName)) {
				response += System.lineSeparator();
				response += reactions.get(reactionName);
				return new OutcomeNotification(response, true);
			} else {
				return new OutcomeNotification(response, true);
			}
		} else {
			return new OutcomeNotification(personalName + " cannot take reaction: " + reactionName, true);
		}
	}

	public List<Attack> getCurrentAttacks() {
		return attacksThisTurn;
	}

	public OutcomeNotification expandLegendaryAction(String cName) {
		try {
			LegendaryAction laction = null;
			boolean dropActionCharge = false;
			int charges = 0;
			for (LegendaryAction action : legendaryActions.keySet()) {
				if (action.cname.contentEquals(cName)) {
					charges = legendaryActions.get(action);
					if (charges == AT_WILL) {
						laction = action;
					} else {
						if (charges > 0) {
							dropActionCharge = true;
							laction = action;
						} else {
							throw new IllegalArgumentException("No remaining charges for action");
						}
					}
					break;
				}
			}
			if (laction == null) {
				throw new IllegalArgumentException("No such action: " + cName);
			}
			if (currentCharges - laction.charges < 0) {
				return new OutcomeNotification("Insufficient charges for legendary action: " + cName, false);
			} else {
				currentCharges -= laction.charges;
				if (dropActionCharge) {
					legendaryActions.put(laction, charges - 1);
				}
				return  new OutcomeNotification(laction.expendAction(this), true);
			}
		} catch (IllegalArgumentException ex) {
			return  new OutcomeNotification(ex.getMessage(), false);
		}
	}

	public OutcomeNotification expendAction(String cName) {
		if (actedThisTurn) {
			return new OutcomeNotification("Cannot take action, already acted this turn", false);
		}
		try {
			Action action = returnAction(cName, actions);

			if (actionReadiness.containsKey(action)) {
				if (actionReadiness.get(action)) {
					actionReadiness.put(action, false);
				} else {
					return new OutcomeNotification("Rechargable Action - Need " + action.rechargeDiceMeets + " or better from "
							+ action.rechargeDice, false);
				}
			}

			actedThisTurn = true;
			return new OutcomeNotification(action.expendAction(this), true);
		} catch (IllegalArgumentException ex) {
			return new OutcomeNotification(ex.getMessage(), false);
		}
	}

	@Override
	public String listAvailableActionsAttackSpells() {
		StringBuilder sb = new StringBuilder();
		sb.append(listRemainingAttacksReadable());
		sb.append(listSpells());
		sb.append(listSpellSlotsRemaining());
		sb.append(listInnateSpells());
		return sb.toString();
	}

	public Action returnAction(String cName, Map<Action, Integer> actions) {
		for (Action action : actions.keySet()) {
			if (action.cname.contentEquals(cName)) {
				int charges = actions.get(action);
				if (charges == AT_WILL) {
					return action;
				} else {
					if (charges > 0) {
						actions.put(action, charges - 1);
						return action;
					} else {
						throw new IllegalArgumentException("No remaining charges for action");
					}
				}
			}
		}
		throw new IllegalArgumentException("No such action: " + cName);
	}

	public String listLegendaryActions() {
		if (legendaryActions == null || legendaryActions.size() == 0) {
			return "Monster has no legendary actions";
		}
		StringBuilder msg = new StringBuilder("Charges for legendary actions: " + currentCharges);
		msg.append(System.lineSeparator());
		msg.append(listActions(legendaryActions));
		return msg.toString();
	}

	public String listActions() {
		return listActions(actions);
	}

	public String listActions(Map<? extends Action, Integer> actions) {
		if (actions == null) {
			return "";
		}

		StringBuilder sb = new StringBuilder();
		for (Action action : actions.keySet()) {
			int charges = actions.get(action);
			String chargeMsg = charges + "";
			if (charges == AT_WILL) {
				chargeMsg = "At will";
			}
			sb.append("Name: " + action.name + " (" + action.cname + ") Daily charges: " + chargeMsg);
			sb.append(System.lineSeparator());
			if (actionReadiness.containsKey(action)) {
				sb.append("Rechargable spell, ready? " + actionReadiness.get(action));
				sb.append(System.lineSeparator());
			}
			if (action.text != null) {
				sb.append(action.text);
				sb.append(System.lineSeparator());
			}
		}
		return sb.toString();
	}

	public Attack expendAttack(int number) {
		if (attacksThisTurn == null) {
			int totalIdx = 0;
			for (List<Attack> set : attackLists) {
				for (Attack attack : set) {
					if (totalIdx == number) {
						attacksThisTurn = new ArrayList<>();
						attacksThisTurn.addAll(set);
						attacksThisTurn.remove(attack);
						return attack;
					}
					totalIdx++;
				}
			}
			throw new IllegalArgumentException("Picked too high an attack number");
		} else {
			if (number >= attacksThisTurn.size()) {
				throw new IllegalArgumentException("Picked too high an attack number");
			} else {
				return attacksThisTurn.remove(number);
			}
		}
	}

	public boolean isMultioptionAttack() {
		return attackLists.size() > 1;
	}

	public String listAttacksReadable() {
		StringBuilder sb = new StringBuilder();
		int setIdx = 0;
		int aIdx = 0;
		for (List<Attack> set : attackLists) {
			if (attackLists.size() != 1) {
				sb.append("Set: " + setIdx++ + System.lineSeparator());
			}

			for (Attack attack : set) {
				sb.append("Attack: " + aIdx++ + " " + attack.toString() + System.lineSeparator());
			}
		}
		return sb.toString();
	}

	public String listRemainingAttacksReadable() {
		if (attacksThisTurn == null) {
			return listAttacksReadable();
		}
		StringBuilder sb = new StringBuilder();
		int aIdx = 0;
		for (Attack attack : attacksThisTurn) {
			sb.append("Attack: " + aIdx++ + " " + attack.toString() + System.lineSeparator());
		}
		return sb.toString();
	}

	public String listInnateSpells() {
		if (innateSpells == null) {
			return "";
		}

		StringBuilder sb = new StringBuilder();

		for (Spell spell : innateSpells.keySet()) {
			sb.append(spell.readableName + " - ");
			if (innateSpells.get(spell) != AT_WILL) {
				sb.append(innateSpells.get(spell) + "/day, charges remaining: " + innateSpells.get(spell));
			} else {
				sb.append("at will, ");
			}
		}
		sb.append(System.lineSeparator());

		return sb.toString();
	}

	public Spell expendInnateSpell(String spellName) {
		Spell spell = getInnateSpell(spellName);
		Integer remainingCharges = innateSpells.get(spell);
		if (remainingCharges > 0) {
			if (spell.concentrate && concentratedSpell != null) {
				throw new IllegalArgumentException("Already concentrating on: " + concentratedSpell.readableName);
			} else if (spell.concentrate) {
				concentratedSpell = spell;
			}

			if (innateSpells.get(spell) != AT_WILL) {
				innateSpells.put(spell, remainingCharges - 1);
			}
		} else {
			throw new IllegalArgumentException("No more charges for spell: " + spellName);
		}

		return spell;
	}

	private Spell getInnateSpell(String spellName) {
		for (Spell spell : innateSpells.keySet()) {
			if (spell.computerName.equals(spellName)) {
				return spell;
			}
		}
		throw new IllegalArgumentException("Unknown spell: " + spellName);
	}

	@Override
	public void notifyNewTurn() {
		super.notifyNewTurn();
		currentCharges = maxCharges;
	}

	@Override
	public String listStats() {
		StringBuilder sb = new StringBuilder();

		sb.append("AC: " + ac);
		sb.append(System.lineSeparator());
		sb.append("Speed: " + speed);
		sb.append(System.lineSeparator());
		sb.append("Attributes: " + attrs);

		return sb.toString();
	}
}
