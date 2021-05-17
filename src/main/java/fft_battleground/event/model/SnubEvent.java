package fft_battleground.event.model;

import fft_battleground.botland.model.BattleGroundEventType;
import fft_battleground.botland.model.DatabaseResultsData;

import lombok.Data;

@Data
public class SnubEvent 
extends BattleGroundEvent 
implements DatabaseResultsData {
	private static final BattleGroundEventType type = BattleGroundEventType.SNUB;
	
	private String player;
	private Integer snub;

	public SnubEvent() {
		super(type);
	}
	
	public SnubEvent(String player, Integer snub) {
		super(type);
		this.player = player;
		this.snub = snub;
	}
}
