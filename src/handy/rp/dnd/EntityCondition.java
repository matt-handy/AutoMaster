package handy.rp.dnd;

import java.util.EnumSet;
import java.util.List;

public class EntityCondition {
	public enum CONDITIONS {
		BLINDED, CHARMED, DEAFENED, FRIGHTENED, GRAPPLED, INCAPACITATED, INVISIBLE, PARALYZED, PETRIFIED, POISONED,
		PRONE, RESTRAINED, STUNNED, UNCONSCIOUS
	}

	public static CONDITIONS getCondition (String abbrev) {
		if(abbrev.equalsIgnoreCase("BLI")) {
			return CONDITIONS.BLINDED;
		}else if(abbrev.equalsIgnoreCase("CHA")) {
			return CONDITIONS.CHARMED;
		}else if(abbrev.equalsIgnoreCase("DEAF")) {
			return CONDITIONS.DEAFENED;
		}else if(abbrev.equalsIgnoreCase("FRI")) {
			return CONDITIONS.FRIGHTENED;
		}else if(abbrev.equalsIgnoreCase("GRAP")) {
			return CONDITIONS.GRAPPLED;
		}else if(abbrev.equalsIgnoreCase("INC")) {
			return CONDITIONS.INCAPACITATED;
		}else if(abbrev.equalsIgnoreCase("INV")) {
			return CONDITIONS.INVISIBLE;
		}else if(abbrev.equalsIgnoreCase("PARA")) {
			return CONDITIONS.PARALYZED;
		}else if(abbrev.equalsIgnoreCase("PET")) {
			return CONDITIONS.PETRIFIED;
		}else if(abbrev.equalsIgnoreCase("POI")) {
			return CONDITIONS.POISONED;
		}else if(abbrev.equalsIgnoreCase("PRO")) {
			return CONDITIONS.PRONE;
		}else if(abbrev.equalsIgnoreCase("STUN")) {
			return CONDITIONS.STUNNED;
		}else if(abbrev.equalsIgnoreCase("REST")) {
			return CONDITIONS.RESTRAINED;
		}else if(abbrev.equalsIgnoreCase("UNC")) {
			return CONDITIONS.UNCONSCIOUS;
		}
		throw new IllegalArgumentException("Unknown condition");
	}
	
	public static boolean hasAdvantageOnAttack(EnumSet<CONDITIONS> conditions) {
		if(conditions.contains(CONDITIONS.INVISIBLE)) {
			return true;
		}else {
			return false;
		}
	}
	
	public static boolean hasDisadvantageOnAttack(EnumSet<CONDITIONS> conditions) {
		if (conditions.contains(CONDITIONS.BLINDED) || 
				conditions.contains(CONDITIONS.POISONED) ||
				conditions.contains(CONDITIONS.PRONE) ||
				conditions.contains(CONDITIONS.RESTRAINED)) {
			return true;
		}
		return false;
	}
	
	public static int getSpeed(EnumSet<CONDITIONS> conditions, int defaultSpeed) {
		if(conditions.contains(CONDITIONS.GRAPPLED) ||
				conditions.contains(CONDITIONS.PARALYZED) ||
				conditions.contains(CONDITIONS.PETRIFIED) ||
				conditions.contains(CONDITIONS.RESTRAINED) ||
				conditions.contains(CONDITIONS.STUNNED) ||
				conditions.contains(CONDITIONS.UNCONSCIOUS)) {
			return 0;
		}else {
			return defaultSpeed;
		}
	}
	
	public static boolean canAttack(EnumSet<CONDITIONS> conditions) {
		if(conditions.contains(CONDITIONS.INCAPACITATED) ||
				conditions.contains(CONDITIONS.PARALYZED) ||
				conditions.contains(CONDITIONS.PETRIFIED) ||
				conditions.contains(CONDITIONS.STUNNED) || 
				conditions.contains(CONDITIONS.UNCONSCIOUS)) {
			return false;
		}else {
			return true;
		}
	}
}
