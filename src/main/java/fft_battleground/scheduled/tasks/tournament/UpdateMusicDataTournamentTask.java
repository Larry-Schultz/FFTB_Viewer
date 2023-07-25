 package fft_battleground.scheduled.tasks.tournament;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fft_battleground.discord.WebhookManager;
import fft_battleground.exception.DumpException;
import fft_battleground.music.MusicService;
import fft_battleground.scheduled.tasks.DumpTournamentScheduledTask;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class UpdateMusicDataTournamentTask extends DumpTournamentScheduledTask {
	
	@Autowired
	private MusicService musicService;
	
	@Autowired
	private WebhookManager errorWebhookManager;
	
	public UpdateMusicDataTournamentTask() {}

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
