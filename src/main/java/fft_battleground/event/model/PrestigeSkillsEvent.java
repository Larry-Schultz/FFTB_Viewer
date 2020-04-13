package fft_battleground.event.model;

import java.util.List;

import fft_battleground.bot.model.BattleGroundEventType;
import fft_battleground.bot.model.DatabaseResultsData;
import fft_battleground.bot.model.SkillType;
import lombok.Data;

@Data
public class PrestigeSkillsEvent 
extends PlayerSkillEvent
implements DatabaseResultsData {
	private static final BattleGroundEventType type = BattleGroundEventType.PRESTIGE_SKILLS;

	public PrestigeSkillsEvent(String player, List<String> skills) {
		super(type, player, skills);
	}

	@Override
	public String toString() {
		return "PrestigeSkills [toString()=" + super.toString() + "]";
	}
}
