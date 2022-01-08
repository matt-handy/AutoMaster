package handy.rp.dnd;

public class SkillCheckInfo {

	public enum SKILL_MODIFIER {
		STR, DEX, CON, INT, WIS, CHA
	}

	public enum SKILL_CHECK {
		Acrobatics(SKILL_MODIFIER.DEX), Animal_Handling(SKILL_MODIFIER.WIS), Arcana(SKILL_MODIFIER.INT),
		Athletics(SKILL_MODIFIER.STR), Deception(SKILL_MODIFIER.CHA), History(SKILL_MODIFIER.INT),
		Insight(SKILL_MODIFIER.WIS), Intimidation(SKILL_MODIFIER.CHA), Investigation(SKILL_MODIFIER.INT),
		Medicine(SKILL_MODIFIER.WIS), Nature(SKILL_MODIFIER.INT), Perception(SKILL_MODIFIER.WIS),
		Performance(SKILL_MODIFIER.CHA), Persuasion(SKILL_MODIFIER.CHA), Religion(SKILL_MODIFIER.INT),
		Sleight_of_Hand(SKILL_MODIFIER.DEX), Stealth(SKILL_MODIFIER.DEX), Survival(SKILL_MODIFIER.WIS);

		public final SKILL_MODIFIER modifier;

		private SKILL_CHECK(SKILL_MODIFIER modifier) {
			this.modifier = modifier;
		};
		
		public static SKILL_CHECK getSkillFromString(String name) {
			switch(name) {
			case "acrobatics":
				return SKILL_CHECK.Acrobatics;
			case "animal_handling":
				return SKILL_CHECK.Animal_Handling;
			case "arcana":
				return SKILL_CHECK.Arcana;
			case "athletics":
				return SKILL_CHECK.Athletics;
			case "deception":
				return SKILL_CHECK.Acrobatics;
			case "history":
				return SKILL_CHECK.History;
			case "insight":
				return SKILL_CHECK.Insight;
			case "intimidation":
				return SKILL_CHECK.Intimidation;
			case "investigation":
				return SKILL_CHECK.Investigation;
			case "medicine":
				return SKILL_CHECK.Medicine;
			case "nature":
				return SKILL_CHECK.Nature;
			case "perception":
				return SKILL_CHECK.Perception;
			case "performance":
				return SKILL_CHECK.Performance;
			case "persuasion":
				return SKILL_CHECK.Persuasion;
			case "religion":
				return SKILL_CHECK.Religion;
			case "slight_of_hand":
				return SKILL_CHECK.Sleight_of_Hand;
			case "stealth":
				return SKILL_CHECK.Stealth;
			case "survival":
				return SKILL_CHECK.Survival;
			default:
				throw new IllegalArgumentException("unknown skill proficiency: " + name);
			}
		}
	}
}
