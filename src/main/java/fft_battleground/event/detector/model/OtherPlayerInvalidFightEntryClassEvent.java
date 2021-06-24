package fft_battleground.event.detector.model;

import java.util.List;

import fft_battleground.event.model.BattleGroundEventType;
import lombok.Data;

@Data
public class OtherPlayerInvalidFightEntryClassEvent extends BattleGroundEvent {
	private static final BattleGroundEventType type = BattleGroundEventType.OTHER_PLAYER_INVALID_FIGHT_CLASS;
	
	private List<InvalidFightEntryClassEvent> events;
	
	public OtherPlayerInvalidFightEntryClassEvent() {
		super(type);
	}
	
	public OtherPlayerInvalidFightEntryClassEvent(List<InvalidFightEntryClassEvent> events) {
		super(type);
		this.events = events;
	}

}
