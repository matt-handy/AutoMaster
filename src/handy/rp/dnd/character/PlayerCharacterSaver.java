package handy.rp.dnd.character;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import handy.rp.Dice.DICE_TYPE;
import handy.rp.dnd.CharClass;
import handy.rp.dnd.ClassFeature;
import handy.rp.dnd.SkillCheckInfo.SKILL_CHECK;
import handy.rp.dnd.attacks.CharacterWeapon;
import handy.rp.dnd.spells.Spell;
import handy.rp.dnd.spells.Spell.SLOTLEVEL;

public class PlayerCharacterSaver {
	
	//This class is located in the character package so that it can access protected members 
	//of PlayerCharacter without muddying up the API for PlayerCharacter with a bunch of stuff 
	//that the rest of the program doesn't need to see. Might think about making this
	//an anonymous inner class or something...
	
	public static void saveCharacter(PlayerCharacter pc, Path path) {
		try {
			BufferedWriter writer = Files.newBufferedWriter(path);
			PrintWriter pw = new PrintWriter(writer);
			pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			pw.println("<player_char>");
			pw.println("<name>" + pc.personalName + "</name>");

			for (CharClass cClass : pc.getClassInfo().keySet()) {
				pw.println("<classInfo>");
				pw.println("<levels>" + pc.getClassInfo().get(cClass) + "</levels>");
				pw.println("<class>" + cClass.name + "</class>");
				pw.println("</classInfo>");
			}

			pw.println("<maxhp>" + pc.maxHP + "</maxhp>");
			pw.println("<currenthp>" + pc.getCurrentHp() + "</currenthp>");

			pw.println("<str>" + pc.str + "</str>");
			pw.println("<dex>" + pc.dex + "</dex>");
			pw.println("<con>" + pc.con + "</con>");
			pw.println("<int>" + pc.inte + "</int>");
			pw.println("<wis>" + pc.wis + "</wis>");
			pw.println("<cha>" + pc.cha + "</cha>");

			pw.println("<skill_proficiencies>");
			for (SKILL_CHECK skill : pc.getSkillProficiencies()) {
				pw.println("<proficiency>" + skill.name().toLowerCase() + "</proficiency>");
			}
			pw.println("</skill_proficiencies>");

			CharacterSpellInfo spellSummary = pc.getSpellSummary();
			if (spellSummary != null) {
				pw.println("<cantrips>");
				for (Spell spell : spellSummary.getKnownCantrips()) {
					pw.println("<spell>" + spell.computerName + "</spell>");
				}
				pw.println("</cantrips>");
				pw.println("<prepared_spells>");
				for (Spell spell : spellSummary.getPreparedSpells()) {
					pw.println("<spell>" + spell.computerName + "</spell>");
				}
				pw.println("</prepared_spells>");
				pw.println("<spellslots>");
				for (int idx = 1; idx <= 9; idx++) {
					SLOTLEVEL level = SLOTLEVEL.get(idx);
					if (spellSummary.getSpellSlotsRemaining().containsKey(level)) {
						int numberOfSlots = spellSummary.getSpellSlotsRemaining().get(level);
						String slevelStr = "slevel" + idx;
						pw.println("<" + slevelStr + ">" + numberOfSlots + "</" + slevelStr + ">");

					}
				}
				pw.println("</spellslots>");
			}

			pw.println("<proficient_weapons>");
			for (CharacterWeapon weapon : pc.getWeaponsInfo()) {
				if (weapon.isProficient) {
					pw.println("<weapon>" + weapon.weapon.cname + "</weapon>");
				}
			}
			pw.println("</proficient_weapons>");
			pw.println("<nonproficient_weapons>");
			for (CharacterWeapon weapon : pc.getWeaponsInfo()) {
				if (!weapon.isProficient) {
					pw.println("<weapon>" + weapon.weapon.cname + "</weapon>");
				}
			}
			pw.println("</nonproficient_weapons>");

			List<ClassFeature> activeFeatures = pc.getActiveFeatures();
			if (activeFeatures.size() > 0) {
				pw.println("<active_features>");
				for (ClassFeature feature : activeFeatures) {
					pw.println("<feature>" + feature.featureName + "</feature>");
				}
				pw.println("</active_features>");
			}

			pw.println("<class-resources>");
			for (CharClass cClass : pc.classResourceCounters.keySet()) {
				pw.println("<resource>");
				pw.println("<class>" + cClass.name + "</class>");
				pw.println("<counter>" + pc.classResourceCounters.get(cClass) + "</counter>");
				pw.println("</resource>");
			}
			pw.println("</class-resources>");
			
			pw.println("<feature-charges>");
			for (ClassFeature feature : pc.featureCharges.keySet()) {
				pw.println("<feature>");
				pw.println("<name>" + feature.featureName + "</name>");
				pw.println("<charges>" + pc.featureCharges.get(feature) + "</charges>");
				pw.println("</feature>");
			}
			pw.println("</feature-charges>");
			
			pw.println("<current_hitdice>");
			for(DICE_TYPE dice : pc.hitDice.keySet()) {
				pw.println("<dice>");
				pw.println("<type>" + dice.name() + "</type>");
				pw.println("<count>" + pc.hitDice.get(dice) + "</count>");
				pw.println("</dice>");
			}
			pw.println("</current_hitdice>");

			pw.println("</player_char>");
			pw.flush();
			pw.close();
		} catch (IOException ex) {

		}
	}
}
