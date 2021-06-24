package fft_battleground.event.detector.model;

import java.util.List;

import fft_battleground.event.model.BattleGroundEventType;
import fft_battleground.event.model.DatabaseResultsData;
import lombok.Data;

@Data
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
