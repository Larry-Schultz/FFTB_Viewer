package fft_battleground.event.detector.model;

import fft_battleground.event.model.BattleGroundEventType;
import fft_battleground.model.BattleGroundTeam;
import lombok.Data;

@Data
public class ResultEvent extends BattleGroundEvent {
	private static final BattleGroundEventType event = BattleGroundEventType.RESULT;
	
	private BattleGroundTeam winner;
	
	public ResultEvent() {}
	
	public ResultEvent(BattleGroundTeam winner) {
		super(event);
		this.winner = winner;
	}

}
