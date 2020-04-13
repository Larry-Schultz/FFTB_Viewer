package fft_battleground.event.model;

import java.util.Date;

import fft_battleground.bot.model.BattleGroundEventType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public abstract class BattleGroundEvent {
	protected BattleGroundEventType eventType;
	protected Date eventTime;

	public BattleGroundEvent() {}
	
	public BattleGroundEvent(BattleGroundEventType type) {
		this.eventType = type;
		this.eventTime = new Date();
	}
	
	public abstract String toString();
}
