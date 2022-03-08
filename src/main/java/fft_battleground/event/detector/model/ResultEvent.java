package fft_battleground.event.detector.model;

import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.event.model.BattleGroundEventType;
import fft_battleground.model.BattleGroundTeam;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class ResultEvent extends BattleGroundEvent {
	private static final BattleGroundEventType event = BattleGroundEventType.RESULT;
	
	protected BattleGroundTeam winner;
	
	public ResultEvent() {}
	
	public ResultEvent(BattleGroundTeam winner) {
		super(event);
		this.winner = winner;
	}

	public ResultEvent(BattleGroundEventType type, BattleGroundTeam winner) {
		super(type);
		this.winner = winner;
	}

}
