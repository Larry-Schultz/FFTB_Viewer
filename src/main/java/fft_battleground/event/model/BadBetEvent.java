package fft_battleground.event.model;

import java.util.Date;
import java.util.List;

import fft_battleground.botland.model.BattleGroundEventType;
import lombok.Data;

@Data
public class BadBetEvent extends BattleGroundEvent {
	private static final BattleGroundEventType type = BattleGroundEventType.BAD_BET;
	
	private List<String> players;
	
	public BadBetEvent(List<String> players) {
		super(type);
		this.players = players;
	}

}
