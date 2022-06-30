package fft_battleground.dump.cache.task;

import java.util.Collection;
import java.util.List;

import fft_battleground.dump.DumpService;
import fft_battleground.dump.cache.BuilderTask;
import fft_battleground.exception.DumpException;
import fft_battleground.music.MusicService;
import fft_battleground.music.model.Music;
import fft_battleground.repo.model.MusicListenCount;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MusicBuilder
extends BuilderTask {
	private MusicService musicService;
	
	public MusicBuilder(DumpService dumpServiceRef) {
		super(dumpServiceRef);
		this.musicService = this.dumpService.getMusicService();
	}
	
	@Override
	public void run() {
		
		Collection<Music> dumpData;
		try {
			List<MusicListenCount> repoData = this.musicService.loadMusicListenCountFromRepo();
			dumpData = this.musicService.loadMusicDataFromDump();
			this.musicService.freshLoad(dumpData, repoData);
		} catch (DumpException e) {
			log.error("Error starting up music service", e);
		}
		
	}
	
}