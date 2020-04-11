package fft_battleground.bot.model.event;

import fft_battleground.bot.model.BattleGroundEventType;
import fft_battleground.bot.model.DatabaseResultsData;
import lombok.Data;

@Data
public class PortraitEvent 
extends BattleGroundEvent
implements DatabaseResultsData {
	private static final BattleGroundEventType event = BattleGroundEventType.PORTRAIT;

	private String player;
	private String portrait;

	public PortraitEvent() {}
	
	public PortraitEvent(String player, String portrait) {
		super(event);
		this.player = player;
		this.portrait = portrait;
	}

	@Override
	public String toString() {
		return "PortraitEvent [player=" + player + ", portrait=" + portrait + "]";
	}

}
