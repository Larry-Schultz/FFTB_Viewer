package fft_battleground.dump.cache.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import fft_battleground.dump.cache.CacheTask;
import fft_battleground.repo.model.PlayerRecord;
import fft_battleground.skill.model.SkillType;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserSkillsCacheTask
extends CacheTask
implements Callable<Map<String, List<String>>> {
	
	public UserSkillsCacheTask(List<PlayerRecord> playerRecords) {
		super(playerRecords);
	}

	@Override
	public Map<String, List<String>> call() throws Exception {
		log.info("started loading user skills cache");
		playerRecords.parallelStream().filter(playerRecord -> playerRecord.getPlayerSkills() == null).forEach(playerRecord -> playerRecord.setPlayerSkills(new ArrayList<>()));
		Map<String, List<String>> userSkillsCache = playerRecords.parallelStream().collect(
				Collectors.toMap(PlayerRecord::getPlayer, playerRecord -> playerRecord.getPlayerSkills().stream()
						.filter(playerSkill -> playerSkill.getSkillType() == SkillType.USER)
						.map(playerSkill -> playerSkill.getSkill()).collect(Collectors.toList())
				)
			);
		log.info("finished loading user skills cache");
		
		return userSkillsCache;
	}
	
}