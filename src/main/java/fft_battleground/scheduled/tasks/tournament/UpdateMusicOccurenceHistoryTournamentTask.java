package fft_battleground.scheduled.tasks.tournament;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fft_battleground.repo.repository.MusicListenCountHistoryRepo;
import fft_battleground.repo.repository.MusicListenCountRepo;
import fft_battleground.scheduled.tasks.DumpTournamentScheduledTask;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class UpdateMusicOccurenceHistoryTournamentTask extends DumpTournamentScheduledTask {

	@Autowired
	private MusicListenCountRepo musicListenCountRepo;
	
	@Autowired
	private MusicListenCountHistoryRepo musicListenCountHistoryRepo;
	
	public UpdateMusicOccurenceHistoryTournamentTask() {}

	@Override
	protected void task() {
		log.info("Starting Update Music Occurence History Update Task");
		long occurences = this.musicListenCountRepo.sumMusicListens();
		log.info("Current Music Listen Occurence Count is {} occurences", occurences);
		this.musicListenCountHistoryRepo.updateTodayMusicListenCountHistory(occurences);
		log.info("Update Music Occurence History Update Task Complete");
	}

}
