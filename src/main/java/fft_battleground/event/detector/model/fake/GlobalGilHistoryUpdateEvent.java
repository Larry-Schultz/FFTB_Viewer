package fft_battleground.event.detector.model.fake;

import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.event.model.BattleGroundEventType;
import fft_battleground.event.model.DatabaseResultsData;
import fft_battleground.repo.model.GlobalGilHistory;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
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
