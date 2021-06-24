package fft_battleground.dump.cache;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import fft_battleground.repo.model.PlayerRecord;
import fft_battleground.repo.util.SkillCategory;
import fft_battleground.repo.util.SkillType;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PrestigeSkillsCacheTask
extends CacheTask
implements Callable<Map<String, List<String>>> {
	
	public PrestigeSkillsCacheTask(List<PlayerRecord> playerRecords) {
		super(playerRecords);
	}

	@Override
	public Map<String, List<String>> call() throws Exception {
		log.info("started loading prestige skills cache");
		Map<String, List<String>> prestigeSkillsCache = playerRecords.parallelStream().collect(
				Collectors.toMap(PlayerRecord::getPlayer, playerRecord -> playerRecord.getPlayerSkills().stream()
						.filter(playerSkill -> playerSkill.getSkillType() == SkillType.PRESTIGE)
						.map(playerSkill -> playerSkill.getSkill()).collect(Collectors.toList())
				)
			);
		log.info("finished loading prestige skills cache");
		
		return prestigeSkillsCache;
	}
	
}