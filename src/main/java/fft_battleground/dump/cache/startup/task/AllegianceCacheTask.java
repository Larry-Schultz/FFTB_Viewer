package fft_battleground.dump.cache.startup.task;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import fft_battleground.dump.cache.startup.CacheTask;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.repo.model.PlayerRecord;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AllegianceCacheTask
extends CacheTask
implements Callable<Map<String, BattleGroundTeam>> {
	
	public AllegianceCacheTask(List<PlayerRecord> playerRecords) {
		super(playerRecords);
	}

	@Override
	public Map<String, BattleGroundTeam> call() throws Exception {
		log.info("started loading allegiance cache");
		playerRecords.parallelStream().filter(playerRecord -> playerRecord.getAllegiance() == null).forEach(playerRecord -> playerRecord.setAllegiance(BattleGroundTeam.NONE));
		Map<String, BattleGroundTeam> allegianceCache = playerRecords.parallelStream().collect(Collectors.toMap(PlayerRecord::getPlayer, PlayerRecord::getAllegiance));
		log.info("finished loading allegiance cache");
		
		return allegianceCache;
	}
	
}