package fft_battleground.bot.model.event;

import fft_battleground.bot.model.BattleGroundEventType;
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
