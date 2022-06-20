package fft_battleground.tournament;

import java.util.List;
import java.util.Set;

import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.exception.DumpException;
import fft_battleground.exception.TournamentApiException;
import fft_battleground.tournament.model.Tournament;
import fft_battleground.tournament.tips.Tips;

public interface TournamentService {
	Tournament getCurrentTournament();
	Tournament createNewCurrentTournament() throws DumpException, TournamentApiException;
	Tips getCurrentTips() throws TournamentApiException;
	List<BattleGroundEvent> getRealBetInfoFromLatestPotFile(Long tournamentId) throws DumpException;
	Set<String> getPrestigeSkills() throws DumpException;
}
