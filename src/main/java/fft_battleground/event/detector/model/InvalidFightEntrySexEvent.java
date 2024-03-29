package fft_battleground.event.detector.model;

import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.event.model.BattleGroundEventType;
import fft_battleground.event.model.FightEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class InvalidFightEntrySexEvent 
extends BattleGroundEvent
implements FightEvent {
	private static final BattleGroundEventType type = BattleGroundEventType.INVALID_FIGHT_ENTRY_SEX;
	
	private String player;
	private String sex;
	
	public InvalidFightEntrySexEvent() {
		super(type);
	}
	
	public InvalidFightEntrySexEvent(String player, String sex) {
		super(type);
		this.player = player;
		this.sex = sex;
	}
}
