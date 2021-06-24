package fft_battleground.event.detector.model;

import java.util.Date;

import fft_battleground.event.model.BattleGroundEventType;
import fft_battleground.event.model.DatabaseResultsData;
import lombok.Data;

@Data
public class LastActiveEvent 
extends BattleGroundEvent
implements DatabaseResultsData {
	private static final BattleGroundEventType type = BattleGroundEventType.LAST_ACTIVE;
	
	private String player;
	private Date lastActive;
	
	public LastActiveEvent(String player, Date lastActive) {
		super(type);
		this.player = player;
		this.lastActive = lastActive;
	}
	
	@Override
	public String toString() {
		return "LastActiveEvent [player=" + player + ", lastActive=" + lastActive + "]";
	}

}
