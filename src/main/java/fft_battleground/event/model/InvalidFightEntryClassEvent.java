package fft_battleground.event.model;

import fft_battleground.botland.model.BattleGroundEventType;

import lombok.Data;

@Data
public class InvalidFightEntryClassEvent extends BattleGroundEvent {
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
