package fft_battleground.dump.cache.startup.builder;

import java.util.Collection;
import java.util.List;

import fft_battleground.exception.DumpException;
import fft_battleground.music.MusicService;
import fft_battleground.music.model.Music;
import fft_battleground.repo.model.MusicListenCount;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MusicBuilder
implements Runnable {
	private MusicService musicService;
	
	public MusicBuilder(MusicService musicService) {
		this.musicService = musicService;
	}
	
	@Override
	public void run() {
		log.info("Running Music Cache Build");
		Collection<Music> dumpData;
		try {
			List<MusicListenCount> repoData = this.musicService.loadMusicListenCountFromRepo();
			log.info("music listen count data loaded");
			dumpData = this.musicService.loadMusicDataFromDump();
			log.info("music data from dump loaded");
			this.musicService.freshLoad(dumpData, repoData);
		} catch (DumpException e) {
			log.error("Error starting up music service", e);
		}
		log.info("Music Cache Build complete");
	}
	
}