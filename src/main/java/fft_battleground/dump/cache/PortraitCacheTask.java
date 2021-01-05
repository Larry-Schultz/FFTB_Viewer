package fft_battleground.dump.cache;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import fft_battleground.repo.model.PlayerRecord;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PortraitCacheTask
extends CacheTask
implements Callable<Map<String, String>> {
	
	public PortraitCacheTask(List<PlayerRecord> playerRecords) {
		super(playerRecords);
	}

	@Override
	public Map<String, String> call() throws Exception {
		Map<String, String> portraitCache;
		log.info("started loading portrait cache");
		playerRecords.parallelStream().filter(playerRecord -> playerRecord.getPortrait() == null).forEach(playerRecord -> playerRecord.setPortrait(""));
		portraitCache = playerRecords.parallelStream().collect(Collectors.toMap(PlayerRecord::getPlayer, PlayerRecord::getPortrait));
		log.info("finished loading portrait cache");
		
		return portraitCache;
	}
	
}