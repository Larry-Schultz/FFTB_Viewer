package fft_battleground.event.detector.model;

import java.util.Arrays;
import java.util.List;

import fft_battleground.event.model.BattleGroundEventType;
import fft_battleground.event.model.DatabaseResultsData;
import fft_battleground.repo.util.BalanceType;
import fft_battleground.repo.util.BalanceUpdateSource;
import lombok.Data;

@Data
public class OtherPlayerBalanceEvent extends BattleGroundEvent implements DatabaseResultsData {

	private List<BalanceEvent> otherPlayerBalanceEvents;
	
	public OtherPlayerBalanceEvent(BattleGroundEventType eventType, List<BalanceEvent> otherPlayerBalanceEvents) {
		super(eventType);
		this.otherPlayerBalanceEvents = otherPlayerBalanceEvents;
	}
	
	public OtherPlayerBalanceEvent(String player, Integer amount, BalanceType balanceType, BalanceUpdateSource updateSource) {
		super(BattleGroundEventType.OTHER_PLAYER_BALANCE);
		BalanceEvent event = new BalanceEvent(player, amount, balanceType, updateSource);
		this.otherPlayerBalanceEvents = Arrays.asList(new BalanceEvent[] {event});
	}

	@Override
	public String toString() {
		return "OtherPlayerBalanceEvent [otherPlayerBalanceEvents=" + otherPlayerBalanceEvents + "]";
	}
}
