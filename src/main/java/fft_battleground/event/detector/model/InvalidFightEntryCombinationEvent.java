package fft_battleground.event.detector.model;

import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.event.model.BattleGroundEventType;
import fft_battleground.event.model.FightEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class InvalidFightEntryCombinationEvent 
extends BattleGroundEvent
implements FightEvent {
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
