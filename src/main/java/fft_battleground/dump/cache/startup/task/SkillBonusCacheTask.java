package fft_battleground.dump.cache.startup.task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import fft_battleground.dump.cache.startup.CacheTask;
import fft_battleground.repo.model.PlayerRecord;
import fft_battleground.repo.model.SkillBonus;
import fft_battleground.repo.repository.SkillBonusRepo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SkillBonusCacheTask
extends CacheTask
implements Callable<Map<String, Set<String>>> {

	private SkillBonusRepo skillBonusRepoRef;
	
	public SkillBonusCacheTask(List<PlayerRecord> playerRecords, SkillBonusRepo skillBonusRepoRef) {
		super(playerRecords);
		this.skillBonusRepoRef = skillBonusRepoRef;
	}

	@Override
	public Map<String, Set<String>> call() throws Exception {
		log.info("calling skill bonus cache task");
		Map<String, Set<String>> map = new HashMap<>();
		
		for(PlayerRecord playerRecord: this.playerRecords) {
			String player = playerRecord.getPlayer();
			List<SkillBonus> skillBonuses = this.skillBonusRepoRef.getSkillBonusForPlayer(player);
			Set<String> playerSkillSet = skillBonuses.stream().map(skillBonus -> skillBonus.getSkill()).collect(Collectors.toSet());
			map.put(player, playerSkillSet);
		}
		log.info("skill bonus cache task complete");
		
		return map;
	}
	
}