package fft_battleground.dump.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import fft_battleground.repo.model.ClassBonus;
import fft_battleground.repo.model.PlayerRecord;
import fft_battleground.repo.repository.ClassBonusRepo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClassBonusCacheTask
extends CacheTask
implements Callable<Map<String, Set<String>>> {

	private ClassBonusRepo classBonusRepoRef;
	
	public ClassBonusCacheTask(List<PlayerRecord> playerRecords, ClassBonusRepo classBonusRepoRef) {
		super(playerRecords);
		this.classBonusRepoRef = classBonusRepoRef;
	}
	
	@Override
	public Map<String, Set<String>> call() throws Exception {
		log.info("calling class bonus cache task");
		Map<String, Set<String>> map = new HashMap<>();
		
		for(PlayerRecord playerRecord: this.playerRecords) {
			String player = playerRecord.getPlayer();
			List<ClassBonus> skillBonuses = this.classBonusRepoRef.getClassBonusForPlayer(player);
			Set<String> playerClassBonusSet = skillBonuses.stream().map(classBonus -> classBonus.getClassName()).collect(Collectors.toSet());
			map.put(player, playerClassBonusSet);
		}
		log.info("class bonus cache task complete");
		
		return map;
	}
	
}