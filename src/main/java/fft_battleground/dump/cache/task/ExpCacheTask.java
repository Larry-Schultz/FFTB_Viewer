package fft_battleground.dump.cache.task;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Collectors;

import fft_battleground.dump.cache.CacheTask;
import fft_battleground.event.detector.model.ExpEvent;
import fft_battleground.repo.model.PlayerRecord;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExpCacheTask
extends CacheTask
implements Callable<Map<String, ExpEvent>> {

	public ExpCacheTask(List<PlayerRecord> playerRecords) {
		super(playerRecords);

	}

	@Override
	public Map<String, ExpEvent> call() throws Exception {
		Map<String, ExpEvent> expCache;
		log.info("started loading exp cache");
		playerRecords.parallelStream().filter(playerRecord -> playerRecord.getLastKnownLevel() == null).forEach(playerRecord -> playerRecord.setLastKnownLevel((short) 1));
		expCache = playerRecords.parallelStream().map(playerRecord -> new ExpEvent(playerRecord.getPlayer(), playerRecord.getLastKnownLevel(), playerRecord.getLastKnownRemainingExp()))
							.collect(Collectors.toMap(ExpEvent::getPlayer, Function.identity()));
		log.info("finished loading exp cache");
		
		return expCache;
	}
	
}