package fft_battleground.event.detector.model;

import fft_battleground.event.model.BattleGroundEventType;
import fft_battleground.event.model.DatabaseResultsData;
import lombok.Data;

@Data
public class PrestigeAscensionEvent 
extends BattleGroundEvent
implements DatabaseResultsData {
	private static final BattleGroundEventType type = BattleGroundEventType.PRESTIGE_ASCENSION;
	
	private PrestigeSkillsEvent prestigeSkillsEvent;
	private Integer currentBalance;
	
	public PrestigeAscensionEvent(String player, String skill) {
		super(type);
		this.prestigeSkillsEvent = new PrestigeSkillsEvent(player, skill);
	}

	@Override
	public String toString() {
		return "PrestigeAscensionEvent [prestigeSkillsEvent=" + prestigeSkillsEvent + ", currentBalance="
				+ currentBalance + "]";
	}
	
}
