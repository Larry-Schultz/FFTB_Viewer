package fft_battleground.event.model;

import java.util.Date;

import lombok.Data;

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
