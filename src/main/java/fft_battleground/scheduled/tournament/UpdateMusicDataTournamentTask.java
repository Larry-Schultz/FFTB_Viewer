 package fft_battleground.scheduled.tournament;

import fft_battleground.discord.WebhookManager;
import fft_battleground.dump.DumpScheduledTasksManagerImpl;
import fft_battleground.exception.DumpException;
import fft_battleground.music.MusicService;
import fft_battleground.scheduled.DumpTournamentScheduledTask;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UpdateMusicDataTournamentTask extends DumpTournamentScheduledTask {
	private MusicService musicService;
	private WebhookManager errorWebhookManager;
	
	public UpdateMusicDataTournamentTask(DumpScheduledTasksManagerImpl dumpScheduledTasks) {
		super(dumpScheduledTasks);
		this.musicService = this.dumpServiceRef.getMusicService();
		this.errorWebhookManager = this.dumpServiceRef.getErrorWebhookManager();
	}

	@Override
	protected void task() {
		log.info("Running music tournament task");
		try {
			this.musicService.updateOccurences();
			this.musicService.updatePlaylist();
		} catch (DumpException e) {
			log.error("Error reloading music data");
			this.errorWebhookManager.sendException(e, "Error reloading music data");
		}
		log.info("Music Tournament Task complete.");
	}

}
