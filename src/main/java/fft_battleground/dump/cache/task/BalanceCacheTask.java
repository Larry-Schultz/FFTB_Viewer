package fft_battleground.dump.cache.task;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import fft_battleground.dump.cache.CacheTask;
import fft_battleground.repo.model.PlayerRecord;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BalanceCacheTask 
extends CacheTask 
implements Callable<Map<String, Integer>> {

	public BalanceCacheTask(List<PlayerRecord> playerRecords) {
		super(playerRecords);
	}

	@Override
	public Map<String, Integer> call() throws Exception {
		Map<String, Integer> balanceCache = null;
		log.info("started loading balance cache");
		balanceCache = new ConcurrentHashMap<>(this.playerRecords.parallelStream().collect(Collectors.toMap(PlayerRecord::getPlayer, PlayerRecord::getLastKnownAmount)));
		log.info("finished loading balance cache");
		
		return balanceCache;
		
	}
}