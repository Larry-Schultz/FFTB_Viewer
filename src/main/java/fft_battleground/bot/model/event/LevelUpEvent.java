package fft_battleground.bot.model.event;

import fft_battleground.bot.model.BattleGroundEventType;
import fft_battleground.bot.model.DatabaseResultsData;
import lombok.Data;

@Data
public class LevelUpEvent extends BattleGroundEvent implements DatabaseResultsData {
	
	private static final BattleGroundEventType event = BattleGroundEventType.LEVEL_UP;
	
	private String player;
	private Short level;
	
	public LevelUpEvent() {}
	
	public LevelUpEvent(String player, Short level) {
		super(event);
		this.player = player;
		this.level = level;
	}
	
	public LevelUpEvent(BattleGroundEventType type, String player, Short level) {
		super(type);
		this.eventType = type;
		this.player = player;
		this.level = level;
	}

	@Override
	public String toString() {
		return "LevelUpEvent [player=" + player + ", level=" + level + "]";
	}
	
}
