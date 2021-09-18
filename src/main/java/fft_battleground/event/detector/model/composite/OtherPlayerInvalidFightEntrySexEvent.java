package fft_battleground.event.detector.model.composite;

import java.util.List;

import fft_battleground.event.detector.model.InvalidFightEntrySexEvent;
import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.event.model.BattleGroundEventType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class OtherPlayerInvalidFightEntrySexEvent extends BattleGroundEvent {
	private static final BattleGroundEventType type = BattleGroundEventType.OTHER_PLAYER_INVALID_SEX_EVENT;
	
	private List<InvalidFightEntrySexEvent> invalidSexEvents;
	
	public OtherPlayerInvalidFightEntrySexEvent() {
		super(type);
	}
	
	public OtherPlayerInvalidFightEntrySexEvent(List<InvalidFightEntrySexEvent> events) {
		super(type);
		this.invalidSexEvents = events;
	}
}
