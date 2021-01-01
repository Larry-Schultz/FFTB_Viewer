package fft_battleground.event.model.fake;

import fft_battleground.botland.model.BattleGroundEventType;
import fft_battleground.event.model.BattleGroundEvent;

import lombok.Data;

@Data
public class PingEvent extends BattleGroundEvent {
	private static final BattleGroundEventType type = BattleGroundEventType.PING;
	
	public PingEvent() {
		super(type);
	}
}
