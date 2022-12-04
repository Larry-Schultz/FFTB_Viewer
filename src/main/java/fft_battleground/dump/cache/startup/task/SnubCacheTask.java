package fft_battleground.dump.cache.startup.task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import fft_battleground.dump.cache.startup.CacheTask;
import fft_battleground.repo.model.PlayerRecord;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SnubCacheTask
extends CacheTask
implements Callable<Map<String, Integer>> {
	
	public SnubCacheTask(List<PlayerRecord> playerRecords) {
		super(playerRecords);
	}
	
	@Override
	public Map<String, Integer> call() throws Exception {
		Map<String, Integer> snubMap = new HashMap<>();
		log.info("loading snub cache");
		for(PlayerRecord record: this.playerRecords) {
			snubMap.put(record.getPlayer(), record.getSnubStreak());
		}
		log.info("finished loading snub cache");
		return snubMap;
	}
	
	
}