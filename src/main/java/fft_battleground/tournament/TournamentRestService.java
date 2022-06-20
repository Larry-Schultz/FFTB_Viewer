package fft_battleground.tournament;

import fft_battleground.exception.TournamentApiException;
import fft_battleground.tournament.model.Tournament;
import fft_battleground.tournament.model.TournamentInfo;
import fft_battleground.tournament.tips.Tips;

public interface TournamentRestService {
	Tips getTips() throws TournamentApiException;
	TournamentInfo getLatestTournamentInfo() throws TournamentApiException;
	Tournament getTournamentById(Long id) throws TournamentApiException;
}
