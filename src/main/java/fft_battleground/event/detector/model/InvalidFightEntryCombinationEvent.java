package fft_battleground.event.detector.model;

import fft_battleground.event.model.BattleGroundEventType;
import lombok.Data;

@Data
public class InvalidFightEntryCombinationEvent extends BattleGroundEvent {
	private static final BattleGroundEventType type = BattleGroundEventType.INVALID_FIGHT_ENTRY_COMBINATION;
	
	private String player;
	
	public InvalidFightEntryCombinationEvent() {
		super(type);
	}

	public InvalidFightEntryCombinationEvent(String player) {
		super(type);
		this.player = player;
	}

}
