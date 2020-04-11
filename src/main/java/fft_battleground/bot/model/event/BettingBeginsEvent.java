package fft_battleground.bot.model.event;

import fft_battleground.bot.model.BattleGroundEventType;
import fft_battleground.model.BattleGroundTeam;
import lombok.Data;

@Data
public class BettingBeginsEvent extends BattleGroundEvent {
	private static final BattleGroundEventType event = BattleGroundEventType.BETTING_BEGINS;
	
	protected BattleGroundTeam team1;
	protected BattleGroundTeam team2;
	
	public BettingBeginsEvent(BattleGroundTeam team1, BattleGroundTeam team2) {
		super(event);
		this.team1 = team1;
		this.team2 = team2;
	}

	@Override
	public String toString() {
		return "BettingBegins [team1=" + BattleGroundTeam.getTeamName(this.team1) + ", team2=" + BattleGroundTeam.getTeamName(this.team2) + "]";
	}
	
}
