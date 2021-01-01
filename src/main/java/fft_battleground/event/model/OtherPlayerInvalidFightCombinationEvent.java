package fft_battleground.event.model;

import java.util.List;

import fft_battleground.botland.model.BattleGroundEventType;

import lombok.Data;

@Data
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
