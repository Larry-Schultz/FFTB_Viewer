package fft_battleground.botland.model;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import fft_battleground.botland.BetterBetBot;
import fft_battleground.event.detector.model.BetEvent;
import fft_battleground.event.detector.model.BettingEndsEvent;
import fft_battleground.event.detector.model.MatchInfoEvent;
import fft_battleground.event.detector.model.TeamInfoEvent;
import fft_battleground.event.model.DatabaseResultsData;
import fft_battleground.model.BattleGroundTeam;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BetResults implements DatabaseResultsData {

	private Pair<List<BetEvent>, List<BetEvent>> bets;
	private BattleGroundTeam winningTeam;
	
	private BattleGroundTeam leftTeam;
	private BattleGroundTeam rightTeam;
	
	private BettingEndsEvent fullBettingData;
	
	private MatchInfoEvent matchInfo;
	private TeamData teamData;
	
	private List<BetterBetBot> botsWithResults;
	
	public BetResults() {
		this.teamData = new TeamData();
	}
	
	public String betResultsInfo() {
		String info = String.format("The winning side was %1$s with %2$s total bets.  The losing side had %3$s bets", 
										BattleGroundTeam.getTeamName(winningTeam), bets.getLeft().size(), 
										bets.getRight().size());
		return info;
	}
	
	public Integer getLeftTeamBetAmount() {
		Integer sum = this.bets.getLeft().stream().mapToInt(BetEvent::getBetAmountInteger).sum();
		return sum;
	}
	
	public Integer getRightTeamBetAmount() {
		Integer sum = this.bets.getRight().stream().mapToInt(BetEvent::getBetAmountInteger).sum();
		return sum;
	}
}
