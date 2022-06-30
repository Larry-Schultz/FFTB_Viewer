package fft_battleground.dump.cache.task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import fft_battleground.dump.cache.CacheTask;
import fft_battleground.repo.model.PlayerRecord;
import fft_battleground.repo.model.PrestigeSkills;
import fft_battleground.repo.repository.PrestigeSkillsRepo;
import fft_battleground.skill.model.Skill;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PrestigeSkillsCacheTask
extends CacheTask
implements Callable<Map<String, List<String>>> {
	
	private PrestigeSkillsRepo prestigeSkillRepoRef;
	
	public PrestigeSkillsCacheTask(List<PlayerRecord> playerRecords, PrestigeSkillsRepo prestigeSkillsRepo) {
		super(playerRecords);
		this.prestigeSkillRepoRef = prestigeSkillsRepo;
	}

	@Override
	public Map<String, List<String>> call() throws Exception {
		log.info("started loading prestige skills cache");
		
		Map<String, List<String>> prestigeCache = new HashMap<>();
		List<String> playersWithPrestigeSkills = this.prestigeSkillRepoRef.getPlayersWithPrestigeSkills();
		List<PlayerRecord> playerRecordsToUpdate = this.playerRecords.stream().filter(playerRecord -> playersWithPrestigeSkills.contains(playerRecord.getPlayer()))
				.collect(Collectors.toList());
		for(PlayerRecord record: playerRecordsToUpdate) {
			try {
				List<PrestigeSkills> prestigeSkills = this.prestigeSkillRepoRef.getSkillsByPlayer(record.getPlayer());
				List<String> prestigeSkillNames = Skill.convertToListOfSkillStrings(prestigeSkills);
				prestigeCache.put(record.getPlayer(), prestigeSkillNames);
			} catch(Exception e) {
				log.warn("Error pulling prestige skills for user {}", e);
			}
		}
		
		log.info("finished loading prestige skills cache");
		
		return prestigeCache;
	}
	
}