package fft_battleground.event.detector.model.composite;

import java.util.List;

import fft_battleground.event.detector.model.ExpEvent;
import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.event.model.BattleGroundEventType;
import fft_battleground.event.model.DatabaseResultsData;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class OtherPlayerExpEvent 
extends BattleGroundEvent 
implements DatabaseResultsData {

	private static final BattleGroundEventType type = BattleGroundEventType.OTHER_PLAYER_EXP;
	
	private List<ExpEvent> expEvents;

	public OtherPlayerExpEvent(List<ExpEvent> events) {
		super(type);
		this.expEvents = events;
	}
	
	@Override
	public String toString() {
		return "OtherPlayerExpEvent [expEvents=" + expEvents + "]";
	}

}
