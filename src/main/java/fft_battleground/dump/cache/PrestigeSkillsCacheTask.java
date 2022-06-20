package fft_battleground.dump.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import fft_battleground.repo.model.PlayerRecord;
import fft_battleground.repo.model.PrestigeSkills;
import fft_battleground.repo.repository.PrestigeSkillsRepo;
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
		
		List<PrestigeSkills> allSkills = this.prestigeSkillRepoRef.findAll();
		Function<PrestigeSkills, Predicate<PlayerRecord>> findMatchingPlayerForSkill = skill -> record -> StringUtils.equalsIgnoreCase(this.getPlayerFromPrestigeSkill(skill), record.getPlayer());
		Map<String, List<String>> prestigeCache = allSkills.stream().filter(skill -> this.playerRecords.stream().anyMatch(findMatchingPlayerForSkill.apply(skill)))
				.collect(Collectors.groupingBy(this::getPlayerFromPrestigeSkill, HashMap::new, Collectors.mapping(PrestigeSkills::getSkill, Collectors.toList())));
		
		log.info("finished loading prestige skills cache");
		
		return prestigeCache;
	}
	
	private String getPlayerFromPrestigeSkill(PrestigeSkills prestigeSkill) {
		String result = prestigeSkill.getPlayer_record().getPlayer();
		return result;
	}
	
}