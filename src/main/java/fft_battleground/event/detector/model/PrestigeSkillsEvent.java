package fft_battleground.event.detector.model;

import java.util.Arrays;
import java.util.List;

import fft_battleground.event.model.BattleGroundEventType;
import fft_battleground.event.model.DatabaseResultsData;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class PrestigeSkillsEvent 
extends PlayerSkillEvent
implements DatabaseResultsData {
	private static final BattleGroundEventType type = BattleGroundEventType.PRESTIGE_SKILLS;

	public PrestigeSkillsEvent(String player, List<String> skills) {
		super(type, player, skills);
	}
	
	public PrestigeSkillsEvent(String player, String skill) {
		super(type, player, Arrays.asList(new String[] {skill}));
	}

	@Override
	public String toString() {
		return "PrestigeSkills [toString()=" + super.toString() + "]";
	}
}
