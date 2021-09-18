package fft_battleground.event.detector.model;

import java.util.List;

import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.event.model.BattleGroundEventType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class BadBetEvent extends BattleGroundEvent {
	private static final BattleGroundEventType type = BattleGroundEventType.BAD_BET;
	
	private List<String> players;
	
	public BadBetEvent(List<String> players) {
		super(type);
		this.players = players;
	}

}
