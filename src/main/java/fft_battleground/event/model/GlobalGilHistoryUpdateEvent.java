package fft_battleground.event.model;

import fft_battleground.botland.model.BattleGroundEventType;
import fft_battleground.botland.model.DatabaseResultsData;
import fft_battleground.repo.model.GlobalGilHistory;
import lombok.Data;

@Data
public class GlobalGilHistoryUpdateEvent 
extends BattleGroundEvent 
implements DatabaseResultsData {
	private static final BattleGroundEventType type = BattleGroundEventType.GLOBAL_GIL_COUNT_UPDATE;

	public GlobalGilHistory globalGilHistory;
	
	public GlobalGilHistoryUpdateEvent (GlobalGilHistory globalGilHistory) {
		super(type);
		this.globalGilHistory = globalGilHistory;
	}

	@Override
	public String toString() {
		return "GlobalGilHistoryUpdateEvent [globalGilHistory=" + globalGilHistory + "]";
	}

}
