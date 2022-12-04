package fft_battleground.dump.cache.startup;

import fft_battleground.dump.DumpService;
import fft_battleground.exception.CacheBuildException;

public abstract class BuilderTask implements Runnable {
	protected DumpService dumpService;
	
	public BuilderTask(DumpService dumpService) {
		this.dumpService = dumpService;
	}
	
	public abstract void run();
}
