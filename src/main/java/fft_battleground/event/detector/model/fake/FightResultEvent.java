package fft_battleground.event.detector.model.fake;

import fft_battleground.event.detector.model.ResultEvent;
import fft_battleground.event.model.BattleGroundEventType;
import fft_battleground.model.BattleGroundTeam;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class FightResultEvent extends ResultEvent {
	private static final BattleGroundEventType type = BattleGroundEventType.FIGHT_RESULT;
	
	protected BattleGroundTeam loser;
	
	public FightResultEvent(BattleGroundTeam winner, BattleGroundTeam loser) {
		super(type, winner);
		this.loser = loser;
	}
	
	public FightResultEvent(ResultEvent event, BattleGroundTeam loser) {
		super(type, event.getWinner());
		this.loser = loser;
	}

	@Override
	public String toString() {
		return "FightResultEvent [loser=" + loser + ", winner=" + winner + "]";
	}

	
}
