package fft_battleground.tournament;

import java.util.Map;

import fft_battleground.model.BattleGroundTeam;

public interface ChampionService {
	Map<BattleGroundTeam, Integer> getSeasonChampWinsByAllegiance();
}
