package fft_battleground.event.model;

import fft_battleground.botland.model.BattleGroundEventType;
import fft_battleground.model.BattleGroundTeam;
import lombok.Data;

@Data
public class BettingEndsEvent extends BattleGroundEvent {

	private static final BattleGroundEventType event = BattleGroundEventType.BETTING_ENDS;
	
	private BattleGroundTeam team1;
	private Integer team1Bets;
	private Integer team1Amount;
	
	private BattleGroundTeam team2;
	private Integer team2Bets;
	private Integer team2Amount;
	
	public BettingEndsEvent(BattleGroundTeam team1, Integer team1Bets, Integer team1Amount, 
			BattleGroundTeam team2, Integer team2Bets, Integer team2Amount) {
		super(event);
		this.team1 = team1;
		this.team1Bets = team1Bets;
		this.team1Amount = team1Amount;
		this.team2 = team2;
		this.team2Bets = team2Bets;
		this.team2Amount = team2Amount;
	}

	@Override
	public String toString() {
		return "BettingEndsEvent [team1=" + BattleGroundTeam.getTeamName(team1) + ", team1Bets=" + team1Bets + ", team1Amount=" + team1Amount
				+ ", team2=" + BattleGroundTeam.getTeamName(team2) + ", team2Bets=" + team2Bets + ", team2Amount=" + team2Amount + "]";
	}
	
	
}
