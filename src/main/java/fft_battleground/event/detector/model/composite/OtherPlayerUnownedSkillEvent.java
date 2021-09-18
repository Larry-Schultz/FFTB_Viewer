package fft_battleground.event.detector.model.composite;

import java.util.List;

import fft_battleground.event.detector.model.UnownedSkillEvent;
import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.event.model.BattleGroundEventType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
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
