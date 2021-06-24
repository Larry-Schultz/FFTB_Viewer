package fft_battleground.event.detector.model;

import java.util.List;

import fft_battleground.event.model.BattleGroundEventType;
import lombok.Data;

@Data
public class OtherPlayerUnownedSkillEvent extends BattleGroundEvent {
	private static final BattleGroundEventType type = BattleGroundEventType.OTHER_PLAYER_UNOWNED_SKILL;
	
	private List<UnownedSkillEvent> unownedSkillEvents;
	
	public OtherPlayerUnownedSkillEvent() {
		super(type);
	}

	public OtherPlayerUnownedSkillEvent(List<UnownedSkillEvent> eventList) {
		super(type);
		this.unownedSkillEvents = eventList;
	}

}
