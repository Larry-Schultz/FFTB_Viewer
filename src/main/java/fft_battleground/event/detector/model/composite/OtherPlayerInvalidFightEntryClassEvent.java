package fft_battleground.event.detector.model.composite;

import java.util.List;

import fft_battleground.event.detector.model.InvalidFightEntryClassEvent;
import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.event.model.BattleGroundEventType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
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
