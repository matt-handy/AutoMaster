package handy.rp.dnd.attacks;

import java.util.ArrayList;
import java.util.List;

import handy.rp.Dice;
import handy.rp.Dice.DICE_TYPE;
import handy.rp.dnd.Helpers;
import handy.rp.dnd.attacks.DamageComponent.DAMAGE_TYPE;
import handy.rp.dnd.character.PlayerCharacter;
import handy.rp.dnd.ClassFeature;

public class Weapon {

	public enum WEAPON_ATTRIBUTES {
		FINESSE, LIGHT, HEAVY, TWO_HANDED, THROWN, LOADING, AMMUNITION, VERSATILE
	};

	public final String name;
	public final String cname;
	public final boolean isRanged;
	public final boolean hasThrownOption;
	public final DAMAGE_TYPE damageType;
	public final DICE_TYPE diceType;
	public final DICE_TYPE versatileDiceType;
	public final int diceCount;
	public final int modifier;
	public final String range;
	public final String thrownRange;
	private List<WEAPON_ATTRIBUTES> attrs;

	public Weapon(String name, String cname, DAMAGE_TYPE damageType, DICE_TYPE diceType, int diceCount, int modifier,
			String range, List<WEAPON_ATTRIBUTES> attrs, String thrownRange, DICE_TYPE versatileDiceType) {
		this.name = name;
		this.cname = cname;
		this.damageType = damageType;
		this.diceCount = diceCount;
		this.diceType = diceType;
		this.modifier = modifier;
		this.range = range;
		this.thrownRange = thrownRange;
		this.attrs = attrs;
		this.versatileDiceType = versatileDiceType;

		if (range.equalsIgnoreCase("melee")) {
			isRanged = false;
		} else {
			isRanged = true;
		}

		if (attrs.contains(WEAPON_ATTRIBUTES.THROWN) && thrownRange != null) {
			hasThrownOption = true;
		} else {
			hasThrownOption = false;
		}
	}

	public List<WEAPON_ATTRIBUTES> getAttributes() {
		return new ArrayList<>(attrs);
	}

	public String getReadableInfo() {
		StringBuilder info = new StringBuilder();
		info.append(name + "(" + cname+") hits for " + diceCount + diceType.toString());
		if(modifier != 0) {
			info.append(" + " + modifier);
		}
		if(isRanged) {
			info.append(" with a range of " + range);
		}
		if(hasThrownOption) {
			info.append(" and can be thrown for a range of " + thrownRange);
		}
		info.append(". Attributes include: ");
		for(WEAPON_ATTRIBUTES attr : attrs) {
			info.append(attr + ", ");
		}
		return info.toString();
	}

	private int rollDamage(int playerModifier, DICE_TYPE dice) {
		int diceDamage = 0;
		for (int idx = 0; idx < diceCount; idx++) {
			diceDamage += dice.roll();
		}
		diceDamage += modifier;
		diceDamage += playerModifier;
		return diceDamage;
	}
	
	private int rollCritDamage(int playerModifier, DICE_TYPE dice, int extraCritDice) {
		int diceDamage = 0;
		int critDiceCount = (diceCount * 2) + extraCritDice;
		for (int idx = 0; idx < critDiceCount; idx++) {
			diceDamage += dice.roll();
		}
		diceDamage += modifier;
		diceDamage += playerModifier;
		return diceDamage;
	}

	public String rollAttack(PlayerCharacter entity, boolean entityIsProficient, boolean useRangedOption)
			throws Exception {
		StringBuilder message = new StringBuilder();
		int toHitRoll = 0;
		int advHitRoll = 0;
		if (isRanged) {
			toHitRoll = Dice.d20() + Helpers.getModifierFromAbility(entity.dex);
			advHitRoll = Dice.d20() + Helpers.getModifierFromAbility(entity.dex);
			if (entityIsProficient) {
				toHitRoll += entity.getProficiencyBonus();
				advHitRoll += entity.getProficiencyBonus();
			}
			if(advHitRoll < toHitRoll) {
				advHitRoll = toHitRoll;
			}
			
			int damage = rollDamage(Helpers.getModifierFromAbility(entity.dex), diceType);
			int critDamage = rollCritDamage(Helpers.getModifierFromAbility(entity.dex), diceType, entity.extraCritDice());
			
			message.append(entity.personalName + " uses " + name + " to hit at range(" + range + ") with a to hit of "
					+ toHitRoll + " (with adv " + advHitRoll + " ) for " + damage + " ( crit " + critDamage + " ) " 
					+ damageType.readableName + " damage.");
		} else {
			int abilityMod = Helpers.getModifierFromAbility(entity.str);
			if (attrs.contains(WEAPON_ATTRIBUTES.FINESSE)) {
				int dexOption = Helpers.getModifierFromAbility(entity.dex);
				if (dexOption > abilityMod) {
					abilityMod = dexOption;
					message.append(entity.personalName + " uses DEX to attack with finesse." + System.lineSeparator());
				}
			}

			toHitRoll = Dice.d20() + abilityMod;
			advHitRoll = Dice.d20() + abilityMod;
			if (entityIsProficient) {
				toHitRoll += entity.getProficiencyBonus();
				advHitRoll += entity.getProficiencyBonus();
			}

			int damageRoll = rollDamage(abilityMod, diceType);
			int critDamage = rollCritDamage(abilityMod, diceType, entity.extraCritDice());

			if (useRangedOption) {
				if (!hasThrownOption) {
					throw new Exception(name + " cannot be thrown");
				}
				message.append(
						entity.personalName + " throws " + name + " at range(" + thrownRange + ") with a to hit of "
								+ toHitRoll + " for " + damageRoll + " " + damageType.readableName + " damage.");
			} else {
				if (versatileDiceType != null) {
					int vDamageRoll = rollDamage(abilityMod, versatileDiceType);
					int vCritDamageRoll = rollCritDamage(abilityMod, versatileDiceType, entity.extraCritDice());
					message.append(entity.personalName + " strikes with " + name + " with a to hit of " + toHitRoll
							+ " (with adv " + advHitRoll + " ) for " + damageRoll + " ( crit " + critDamage + " ) " + damageType.readableName
							+ " damage if used one-handed, or for " + vDamageRoll +" ( crit " + vCritDamageRoll + " ) if used two-handed.");
				} else {
					message.append(entity.personalName + " strikes with " + name + " with a to hit of " + toHitRoll
							+ " (with adv " + advHitRoll + " ) for " + damageRoll + " ( crit " + critDamage + " ) " + damageType.readableName + " damage.");
				}
				for(ClassFeature feature : entity.getFeatureMeleeBonus()) {
					//TODO: Technically this should be class level and not character level.
					CoreDamageComponent component = feature.getCoreDamageComponentForLevel(entity.getCharacterLevel());
					CoreDamage damage = component.rollDamage();
					String extraDamage = damage.getHumanReadableDamage();
					message.append(" " + feature.featureName + " hits for an extra " + extraDamage);
				}
			}
		}
		return message.toString();
	}
}
