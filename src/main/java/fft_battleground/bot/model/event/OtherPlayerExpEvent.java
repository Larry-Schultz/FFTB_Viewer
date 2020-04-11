package fft_battleground.bot.model.event;

import java.util.List;

import fft_battleground.bot.model.BattleGroundEventType;
import fft_battleground.bot.model.DatabaseResultsData;
import lombok.Data;

@Data
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
