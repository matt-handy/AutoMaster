package handy.rp.dnd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import handy.rp.Dice;
import handy.rp.dnd.CharClass.SPELLCASTING_MODIFIER;
import handy.rp.dnd.spells.Spell;
import handy.rp.dnd.spells.Spell.SLOTLEVEL;

public abstract class ManagedEntity extends Entity {

	protected int str;
	protected int dex;
	protected int con;
	protected int inte;
	protected int wis;
	protected int cha;

	protected int strsave;
	protected int dexsave;
	protected int consave;
	protected int intsave;
	protected int wissave;
	protected int chasave;

	protected int maxHP;
	protected int currentHp;

	protected Spell concentratedSpell = null;
	protected Spell.SLOTLEVEL concratedSpellCastLevel = null;
	protected SPELLCASTING_MODIFIER spellcastingMod;

	protected boolean actedThisTurn = false;
	protected boolean bonusActedThisTurn = false;
	protected boolean hasCastNonCantripSpell = false;

	protected Map<Spell.SLOTLEVEL, List<Spell>> spells;
	protected Map<Spell.SLOTLEVEL, List<Spell>> freeSpells = new HashMap<>();
	protected Map<Spell.SLOTLEVEL, Integer> slotsRemaining;
	protected Map<Spell.SLOTLEVEL, Integer> maxSpellSlots;

	public ManagedEntity(String personalName, int str, int dex, int con, int inte, int wis, int cha,
			Map<Spell.SLOTLEVEL, List<Spell>> spells, Map<Spell.SLOTLEVEL, Integer> slotMapping, int maxHp,
			int currentHp, SPELLCASTING_MODIFIER spellcastingMod) {
		super(personalName);

		this.str = str;
		this.dex = dex;
		this.con = con;
		this.inte = inte;
		this.wis = wis;
		this.cha = cha;

		this.maxHP = maxHp;
		this.currentHp = currentHp;

		this.spells = spells;

		regenerateSpellSlots(slotMapping);
		replenishSpellSlots();
		this.spellcastingMod = spellcastingMod;
	}

	public void regenerateSpellSlots(Map<Spell.SLOTLEVEL, Integer> slotMapping) {
		maxSpellSlots = new HashMap<>();
		if (slotMapping != null) {
			for (Spell.SLOTLEVEL slot : slotMapping.keySet()) {
				maxSpellSlots.put(slot, slotMapping.get(slot));
			}
		}
	}

	public int getMaxHp() {
		return maxHP;
	}

	public int getStr() {
		return str;
	}

	public int getDex() {
		return dex;
	}

	public int getCon() {
		return con;
	}

	public int getInte() {
		return inte;
	}

	public int getWis() {
		return wis;
	}

	public int getCha() {
		return cha;
	}

	protected void restoreSpellSlots(Map<SLOTLEVEL, Integer> slots) {
		this.slotsRemaining = slots;
	}

	protected static void addSpell(Spell spell, Map<SLOTLEVEL, List<Spell>> spells) {
		if (spells.containsKey(spell.minimumLevel)) {
			if (!spells.get(spell.minimumLevel).contains(spell)) {
				spells.get(spell.minimumLevel).add(spell);
			}
		} else {
			List<Spell> newList = new ArrayList<>();
			newList.add(spell);
			spells.put(spell.minimumLevel, newList);
		}
	}

	protected void replenishSpellSlots() {
		slotsRemaining = new HashMap<>();
		for (Spell.SLOTLEVEL slot : maxSpellSlots.keySet()) {
			slotsRemaining.put(slot, maxSpellSlots.get(slot));
		}
	}

	public int getCurrentHp() {
		return currentHp;
	}

	public void heal(int hp) {
		currentHp += hp;
	}

	public String hit(int hp) {
		currentHp -= hp;
		if (concentratedSpell() != null) {
			int conSaveTarget = 10;
			if (hp / 2 > conSaveTarget) {
				conSaveTarget = hp / 2;
			}
			int conSaveRoll = conSaveThrow();
			if (conSaveRoll < conSaveTarget) {
				breakConcentration();
				return personalName + " rolled a CON save of " + conSaveRoll + " against a target of " + conSaveTarget
						+ " and failed. They are no longer concentrating on a spell.";
			}
		}
		return null;
	}

	public abstract int getCasterLevel();

	public abstract Integer getSpellSaveDC();

	public abstract Integer getSpellToHit();

	public SPELLCASTING_MODIFIER getSpellcastingModifier() {
		return spellcastingMod;
	}

	public int getSpellcastingModifierValue() {
		if (spellcastingMod == SPELLCASTING_MODIFIER.CHARISMA) {
			return Helpers.getModifierFromAbility(cha);
		} else if (spellcastingMod == SPELLCASTING_MODIFIER.WISDOM) {
			return Helpers.getModifierFromAbility(wis);
		} else if (spellcastingMod == SPELLCASTING_MODIFIER.INTELLIGENCE) {
			return Helpers.getModifierFromAbility(inte);
		} else {
			throw new IllegalArgumentException("This player does not cast spells");
		}
	}

	public void breakConcentration() {
		concentratedSpell = null;
	}

	public Spell concentratedSpell() {
		return concentratedSpell;
	}

	public Spell.SLOTLEVEL getConcentratedSpellCastLevel() {
		return concratedSpellCastLevel;
	}

	public int rollInitiative() {
		int candInitiative1 = Dice.d20() + Helpers.getModifierFromAbility(dex);
		if (hasInitiativeAdvantage()) {
			int candInitiative2 = Dice.d20() + Helpers.getModifierFromAbility(dex);
			if (candInitiative1 > candInitiative2) {
				currentInitiative = candInitiative1;
			} else {
				currentInitiative = candInitiative2;
			}
		} else {
			currentInitiative = candInitiative1;
		}
		return currentInitiative;
	}

	public boolean hasInitiativeAdvantage() {
		return false;
	}

	public Spell expendSpell(String spellName, Spell.SLOTLEVEL slotLvl) {
		int minSpellLevel = getMinSpellLevel(spellName);
		if (minSpellLevel > slotLvl.level) {
			throw new IllegalArgumentException("Need a spell slot of at least minimum level: " + minSpellLevel);
		}
		return expendSpellWorker(spellName, slotLvl);
	}

	public String listSpells() {
		if (spells == null) {
			return "";
		}

		StringBuilder sb = new StringBuilder();

		for (Spell.SLOTLEVEL slot : spells.keySet()) {
			List<Spell> spells = this.spells.get(slot);
			sb.append("Level: " + slot.toString() + " ");
			for (Spell spell : spells) {
				sb.append(spell.readableName + ", ");
			}
			sb.append(System.lineSeparator());
		}
		/*
		for (Spell.SLOTLEVEL slot : freeSpells.keySet()) {
			List<Spell> spells = this.freeSpells.get(slot);
			sb.append("Level: " + slot.toString() + " ");
			for (Spell spell : spells) {
				sb.append(spell.readableName + ", ");
			}
			sb.append(System.lineSeparator());
		}
		 */
		return sb.toString();
	}

	public int strSaveThrow() {
		int save = Dice.d20() + Helpers.getModifierFromAbility(str) + strsave;
		return save;
	}

	public int dexSaveThrow() {
		int save = Dice.d20() + Helpers.getModifierFromAbility(dex) + dexsave;
		return save;
	}

	public int conSaveThrow() {
		int save = Dice.d20() + Helpers.getModifierFromAbility(con) + consave;
		return save;
	}

	public int intSaveThrow() {
		int save = Dice.d20() + Helpers.getModifierFromAbility(inte) + intsave;
		return save;
	}

	public int wisSaveThrow() {
		int save = Dice.d20() + Helpers.getModifierFromAbility(wis) + wissave;
		return save;
	}

	public int chaSaveThrow() {
		int save = Dice.d20() + Helpers.getModifierFromAbility(cha) + chasave;
		return save;
	}

	public String listSpellSlotsRemaining() {
		if (slotsRemaining == null) {
			return "";
		}

		StringBuilder sb = new StringBuilder();
		List<Spell.SLOTLEVEL> slots = new ArrayList<>();
		slots.addAll(slotsRemaining.keySet());
		Collections.sort(slots);
		for (Spell.SLOTLEVEL slot : slots) {
			if(slot != SLOTLEVEL.CANTRIP) {
				sb.append("Level " + slot.level + ": " + slotsRemaining.get(slot));
				sb.append(", ");
			}
		}
		sb.append(System.lineSeparator());
		return sb.toString();
	}

	private Spell getSpellWithConcentrationCheck(String spellName, Spell.SLOTLEVEL castLevel) {
		Spell targetSpell = getSpellByCompName(spellName);
		if (targetSpell.concentrate && concentratedSpell != null) {
			throw new IllegalArgumentException("Already concentrating on: " + concentratedSpell.readableName);
		} else if (targetSpell.concentrate) {
			concentratedSpell = targetSpell;
			concratedSpellCastLevel = castLevel;
		}
		return targetSpell;
	}

	private Spell expendSpellWorker(String spellName, Spell.SLOTLEVEL slotLvl) {
		if (slotLvl == Spell.SLOTLEVEL.CANTRIP) {
			// Action check only, can cast cantrip after bonus action
			Spell cantrip = getSpellWithConcentrationCheck(spellName, slotLvl);
			// TODO: Possible error condition here - can swap out concentrated spells after
			// action taken
			if (actedThisTurn) {
				throw new IllegalArgumentException("Can't cast spell, already taken action");
			} else {
				actedThisTurn = true;
			}
			return cantrip;
		}
		if (slotsRemaining.get(slotLvl) < 1) {
			throw new IllegalArgumentException("Insufficient slots at level: " + slotLvl.level);
		} else {
			// Check action && bonus action. Only set has acted or bonus action based on
			// need
			Spell targetSpell = getSpellWithConcentrationCheck(spellName, slotLvl);
			if (hasCastNonCantripSpell) {
				throw new IllegalArgumentException(
						"Can only cast cantrip after casting a spell on the same turn, and only if prior spell was a bonus action spell");
			}
			if (targetSpell.bonusAction) {
				if (bonusActedThisTurn) {
					throw new IllegalArgumentException("Already taken bonus action");
				} else {
					bonusActedThisTurn = true;
				}
			} else {
				if (actedThisTurn) {
					throw new IllegalArgumentException("Already taken action this turn");
				} else {
					actedThisTurn = true;
				}
			}
			hasCastNonCantripSpell = true;
			if (!(hasFeatureToIgnoreSpellCast(targetSpell.computerName) && slotLvl == targetSpell.minimumLevel)) {
				slotsRemaining.put(slotLvl, slotsRemaining.get(slotLvl) - 1);
			}
			return targetSpell;
		}
	}

	protected boolean hasFeatureToIgnoreSpellCast(String spellName) {
		return false;
	}

	public Spell expendSpell(String spellName) {

		int minSpellLevel = getMinSpellLevel(spellName);
		Spell.SLOTLEVEL spellLevel = Spell.SLOTLEVEL.get(minSpellLevel);
		return expendSpellWorker(spellName, spellLevel);

	}

	private int getMinSpellLevel(String spellName) {
		return getSpellByCompName(spellName).minimumLevel.level;
	}

	protected Spell getSpellByCompName(String spellName) {
		for (Spell.SLOTLEVEL slot : spells.keySet()) {
			List<Spell> spells = this.spells.get(slot);
			for (Spell spell : spells) {
				if (spell.computerName.equals(spellName)) {
					return spell;
				}
			}
		}
		for (Spell.SLOTLEVEL slot : freeSpells.keySet()) {
			List<Spell> spells = this.freeSpells.get(slot);
			for (Spell spell : spells) {
				if (spell.computerName.equals(spellName)) {
					return spell;
				}
			}
		}
		throw new IllegalArgumentException("I have no such spell: " + spellName);
	}

	@Override
	public void notifyNewTurn() {
		super.notifyNewTurn();
		actedThisTurn = false;
		bonusActedThisTurn = false;
		hasCastNonCantripSpell = false;
	}
}
