package fft_battleground.tournament.tracker;

import fft_battleground.event.detector.model.BettingBeginsEvent;
import fft_battleground.event.detector.model.fake.TournamentStatusUpdateEvent;
import fft_battleground.exception.DumpException;
import fft_battleground.tournament.model.Tournament;

public interface TournamentTracker {
	TournamentStatusUpdateEvent generateTournamentStatus(BettingBeginsEvent bettingBeginsEvent, Tournament currentTournamentDetails) throws DumpException;
}
