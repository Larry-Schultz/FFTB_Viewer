package fft_battleground.event.detector.model.composite;

import java.util.List;

import fft_battleground.event.detector.model.InvalidFightEntryCombinationEvent;
import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.event.model.BattleGroundEventType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class OtherPlayerInvalidFightCombinationEvent extends BattleGroundEvent {
	private static final BattleGroundEventType type = BattleGroundEventType.OTHER_PLAYER_INVALID_FIGHT_COMBINATION;
	
	private List<InvalidFightEntryCombinationEvent> events;
	
	public OtherPlayerInvalidFightCombinationEvent() {
		super(type);
	}
	
	public OtherPlayerInvalidFightCombinationEvent(List<InvalidFightEntryCombinationEvent> events) {
		super(type);
		this.events = events;
	}


}
