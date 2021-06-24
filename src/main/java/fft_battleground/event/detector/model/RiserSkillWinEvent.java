package fft_battleground.event.detector.model;

import fft_battleground.event.model.BattleGroundEventType;
import fft_battleground.event.model.DatabaseResultsData;
import lombok.Data;

@Data
public class RiserSkillWinEvent 
extends SkillWinEvent 
implements DatabaseResultsData {
	private static final BattleGroundEventType event = BattleGroundEventType.RISER_SKILL_WIN;

	public RiserSkillWinEvent() {
		super(event);
	}
	
	public RiserSkillWinEvent(String player, String skill) {
		super(player, skill, event);
	}

	@Override
	public String toString() {
		return super.toString();
	}
	
	
	

}
