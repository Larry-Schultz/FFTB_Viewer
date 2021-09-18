package fft_battleground.event.detector.model.composite;

import java.util.List;

import fft_battleground.event.detector.model.SnubEvent;
import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.event.model.BattleGroundEventType;
import fft_battleground.event.model.DatabaseResultsData;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class OtherPlayerSnubEvent 
extends BattleGroundEvent
implements DatabaseResultsData {
	private static final BattleGroundEventType type = BattleGroundEventType.OTHER_PLAYER_SNUB;
	
	private List<SnubEvent> snubEvents;
	
	public OtherPlayerSnubEvent() {
		super(type);
	}
	
	public OtherPlayerSnubEvent(List<SnubEvent> events) {
		super(type);
		this.snubEvents = events;
	}

}
