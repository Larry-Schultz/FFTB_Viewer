package fft_battleground.dump.scheduled.tournament;

import fft_battleground.dump.DumpScheduledTasksManagerImpl;
import fft_battleground.dump.scheduled.DumpTournamentScheduledTask;
import fft_battleground.repo.repository.MusicListenCountHistoryRepo;
import fft_battleground.repo.repository.MusicListenCountRepo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UpdateMusicOccurenceHistoryTournamentTask extends DumpTournamentScheduledTask {

	private MusicListenCountRepo musicListenCountRepo;
	private MusicListenCountHistoryRepo musicListenCountHistoryRepo;
	
	public UpdateMusicOccurenceHistoryTournamentTask(DumpScheduledTasksManagerImpl dumpScheduledTasks) {
		super(dumpScheduledTasks);
		this.musicListenCountRepo = dumpScheduledTasks.getMusicListenCountRepo();
		this.musicListenCountHistoryRepo = dumpScheduledTasks.getMusicListenCountHistoryRepo();
	}

	@Override
	protected void task() {
		log.info("Starting Update Music Occurence History Update Task");
		long occurences = this.musicListenCountRepo.sumMusicListens();
		log.info("Current Music Listen Occurence Count is {} occurences", occurences);
		this.musicListenCountHistoryRepo.updateTodayMusicListenCountHistory(occurences);
		log.info("Update Music Occurence History Update Task Complete");
	}

}
