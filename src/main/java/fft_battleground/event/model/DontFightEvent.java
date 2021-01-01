package fft_battleground.event.model;

import fft_battleground.botland.model.BattleGroundEventType;

import lombok.Data;

@Data
public class DontFightEvent extends FightEntryEvent {
	private static final BattleGroundEventType type = BattleGroundEventType.DONT_FIGHT;
	
	public DontFightEvent() {
		super(type);
	}
	
	public DontFightEvent(String player, String command) {
		super(type, player, command);
	}

}
