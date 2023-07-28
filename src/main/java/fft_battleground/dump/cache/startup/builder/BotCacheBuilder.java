package fft_battleground.dump.cache.startup.builder;

import java.util.Set;

import fft_battleground.dump.DumpDataProvider;
import fft_battleground.dump.cache.set.BotCache;
import fft_battleground.exception.CacheBuildException;
import fft_battleground.exception.DumpException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BotCacheBuilder implements Runnable {

	private DumpDataProvider dumpDataProvider;
	private BotCache botCache;
	
	public BotCacheBuilder(DumpDataProvider dumpDataProvider, BotCache botCache) {
		this.dumpDataProvider = dumpDataProvider;
		this.botCache = botCache;
	}
	
	@Override
	public void run() {
		log.info("started loading bot cache");
		try {
			Set<String> bots = this.dumpDataProvider.getBots();
			this.botCache.reload(bots);
		} catch (DumpException e) {
			log.error("error loading bot file");
		}
		log.info("finished loading bot cache");
	}

}
