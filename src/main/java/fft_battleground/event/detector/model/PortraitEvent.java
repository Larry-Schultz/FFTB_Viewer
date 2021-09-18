package fft_battleground.event.detector.model;

import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.event.model.BattleGroundEventType;
import fft_battleground.event.model.DatabaseResultsData;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
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
