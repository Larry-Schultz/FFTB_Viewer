package fft_battleground.tournament.tracker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fft_battleground.model.BattleGroundTeam;
import fft_battleground.tournament.tracker.model.TournamentWinData;

public class TournamentWinTrackerCalculator {

	public Map<BattleGroundTeam, TournamentWinData> calculateWinTracker(List<BattleGroundTeam> currentWins) {
		Map<BattleGroundTeam, TournamentWinData> winTracker = new HashMap<>();
		
		
		
		return winTracker;
	}
	
	private TournamentWinData calculateForRedTeam(List<BattleGroundTeam> winners) {
		TournamentWinData tournamentWinData = new TournamentWinData();
		
		if(winners == null || winners.size() < 1) {
			return tournamentWinData;
		}
		
		return tournamentWinData;
	}
}
