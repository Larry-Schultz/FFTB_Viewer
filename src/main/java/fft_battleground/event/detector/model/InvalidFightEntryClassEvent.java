package fft_battleground.event.detector.model;

import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.event.model.BattleGroundEventType;
import fft_battleground.event.model.FightEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class InvalidFightEntryClassEvent 
extends BattleGroundEvent
implements FightEvent {
	private static final BattleGroundEventType type = BattleGroundEventType.INVALID_FIGHT_ENTRY_CLASS;
	
	private String player;
	private String className;
	
	public InvalidFightEntryClassEvent() {
		super(type);
	}
	
	public InvalidFightEntryClassEvent(String player, String className) {
		super(type);
		this.player = player;
		this.className = className;
	}
}
