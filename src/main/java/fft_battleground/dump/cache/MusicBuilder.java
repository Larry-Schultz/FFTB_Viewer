package fft_battleground.dump.cache;

import fft_battleground.dump.DumpService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MusicBuilder
implements Runnable {
	private DumpService dumpServiceRef;
	
	public MusicBuilder(DumpService dumpServiceRef) {
		this.dumpServiceRef = dumpServiceRef;
	}
	
	@Override
	public void run() {
		this.dumpServiceRef.setPlaylist();
	}
	
}