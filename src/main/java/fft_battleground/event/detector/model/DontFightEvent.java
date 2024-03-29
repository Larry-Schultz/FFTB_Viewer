package fft_battleground.event.detector.model;

import fft_battleground.event.model.BattleGroundEventType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class DontFightEvent extends FightEntryEvent {
	private static final BattleGroundEventType type = BattleGroundEventType.DONT_FIGHT;
	
	public DontFightEvent() {
		super(type);
	}
	
	public DontFightEvent(String player, String command) {
		super(type, player, command);
	}

}
