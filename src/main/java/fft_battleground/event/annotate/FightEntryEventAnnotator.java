package fft_battleground.event.annotate;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import fft_battleground.discord.WebhookManager;
import fft_battleground.event.model.FightEntryEvent;
import fft_battleground.exception.DumpException;
import fft_battleground.exception.TournamentApiException;
import fft_battleground.model.Gender;
import fft_battleground.repo.model.PlayerRecord;
import fft_battleground.repo.repository.PlayerRecordRepo;
import fft_battleground.tournament.Tips;
import fft_battleground.tournament.TournamentService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class FightEntryEventAnnotator implements BattleGroundEventAnnotator<FightEntryEvent> {

	private static final List<String> ELITE_MONSTERS = Arrays.asList(new String[]{"UltimaDemon", "SteelGiant", "Byblos", "Serpentarius", "Tiamat", "DarkBehemoth", "HolyDragon"});
	private static final List<String> STRONG_MONSTERS = Arrays.asList(new String[]{"Apanda", "ArchaicDemon", "KingBehemoth", "Hydra", "RedDragon", "Sehkret"});
	
	private static final int defaultOptionCost = 100;
	private static final int defaultMonsterCost = 200;
	private static final int strongMonsterCost = 500;
	private static final int eliteMonsterCost = 800;
	
	private static final int prestigeSortingCost = 10000;
	
	private static final String monsterSetCacheKey = "MONSTERSET";
	private Cache<String, Set<String>> monsterSetCache = Caffeine.newBuilder()
			  .expireAfterWrite(24, TimeUnit.HOURS)
			  .maximumSize(1)
			  .build();
	private Object monsterSetCacheLock = new Object();
	
	@Autowired
	private TournamentService tournamentService;
	
	@Autowired
	private PlayerRecordRepo playerRecordRepo;
	
	@Autowired
	private WebhookManager errorWebhookManager;
	
	@Override
	public void annotateEvent(FightEntryEvent event) {
		this.annotatePlayerData(event);
		this.annotateGilCost(event);
		this.annotatePrestigeSkill(event);
		this.annotateMonsterGender(event);
		this.annotateDescriptions(event);
		this.annotateColors(event);
	}
	
	private void annotatePlayerData(FightEntryEvent event) {
		PlayerRecord metadata = new PlayerRecord();
		Optional<PlayerRecord> maybeRecord = this.playerRecordRepo.findById(StringUtils.lowerCase(event.getPlayer()));
		if(maybeRecord.isPresent()) {
			PlayerRecord record = maybeRecord.get();
			metadata.setPlayer(event.getPlayer());
			metadata.setFightWins(record.getFightWins());
			metadata.setFightLosses(record.getFightLosses());
			metadata.setAllegiance(record.getAllegiance());
			event.setMetadata(metadata);
		}
		
		return;
	}
	
	private void annotatePrestigeSkill(FightEntryEvent event) {
		Set<String> prestigeSkillSet;
		try {
			prestigeSkillSet = this.prestigeSkillSet();
		} catch (DumpException e) {
			log.error("Error pulling prestige data, not annotating prestige data", e);
			this.errorWebhookManager.sendException(e, "Error pulling prestige data, not annotating prestige data");
			return;
		}
		if(event.getSkill() != null && prestigeSkillSet.contains(event.getSkill())) {
			event.setSkillPrestige(true);
		}
		if(event.getExclusionSkill() != null && prestigeSkillSet.contains(event.getExclusionSkill())) {
			event.setExclusionSkillPrestige(true);
		}
		
		if(event.isSkillPrestige()) {
			event.setSortingGilCost(event.getGilCost() + prestigeSortingCost);
		}
		if(event.isExclusionSkillPrestige()) {
			event.setSortingGilCost(event.getGilCost() + prestigeSortingCost);
		}
	}
	
	private void annotateMonsterGender(FightEntryEvent event) {
		try {
			if(event.getGender() == null && event.getClassName() != null && this.monsterSet().contains(event.getClassName())) {
				event.setGender(Gender.MONSTER);
			}
		} catch (TournamentApiException e) {
			log.error("Error pulling tips data, not annotating FightEntry monster gender", e);
			this.errorWebhookManager.sendException(e, "Error pulling tips data, not annotating FightEntry monster gender");
		}
		return;
	}
	
	private void annotateGilCost(FightEntryEvent event) {
		Integer gilCost = 0;
		if(event.getGender() != null && (event.getGender() == Gender.FEMALE || event.getGender() == Gender.MALE)) {
			gilCost+= defaultOptionCost;
		}
		if(event.getSkill() != null) {
			gilCost+= defaultOptionCost;
		}
		if(event.getExclusionSkill() != null) {
			gilCost+= defaultOptionCost;
		}
		if(STRONG_MONSTERS.contains(event.getClassName()) || ELITE_MONSTERS.contains(event.getClassName()) || event.getGender() == Gender.MONSTER) {
			if(STRONG_MONSTERS.contains(event.getClassName())) {
				gilCost+= strongMonsterCost;
			} else if(ELITE_MONSTERS.contains(event.getClassName())) {
				gilCost+= eliteMonsterCost;
			} else {
				gilCost+= defaultMonsterCost;
			}
		} else {
			gilCost += defaultOptionCost;
		}
		
		event.setGilCost(gilCost);
		event.setSortingGilCost(gilCost);
		return;
	}
	
	private void annotateDescriptions(FightEntryEvent event) {
		Tips tips;
		try {
			tips = this.tournamentService.getCurrentTips();
			if(StringUtils.isNotBlank(event.getClassName())) {
				if(tips.getClassMap().containsKey(event.getClassName())) {
					String description = tips.getClassMap().get(event.getClassName());
					event.setClassDescription(description);
				}
			}
			
			if(StringUtils.isNotBlank(event.getSkill())) {
				if(tips.getUserSkill().containsKey(event.getSkill())) {
					String description = tips.getUserSkill().get(event.getSkill());
					event.setSkillDescription(description);
				}
			}
			
			if(StringUtils.isNotBlank(event.getExclusionSkill())) {
				if(tips.getUserSkill().containsKey(event.getExclusionSkill())) {
					String description = tips.getUserSkill().get(event.getExclusionSkill());
					event.setExclusionSkillDescription(description);
				}
			}
		} catch (TournamentApiException e) {
			log.error("Could not access tips, not annotating class and skill descriptions for FightEntryEvent", e);
			this.errorWebhookManager.sendException(e, "Could not access tips, not annotating class and skill descriptions for FightEntryEvent");
		}
		
	}
	
	private void annotateColors(FightEntryEvent event) {
		if(event.isSkillPrestige()) {
			event.setSkillColor("orange");
		}
		if(event.isExclusionSkillPrestige()) {
			event.setExclusionSkillColor("orange");
		}
		if(ELITE_MONSTERS.contains(event.getClassName()) || STRONG_MONSTERS.contains(event.getClassName())) {
			event.setClassColor("red");
		}
	}
	
	private Set<String> monsterSet() throws TournamentApiException {
		synchronized(this.monsterSetCacheLock) {
			Set<String> monsterSet = this.monsterSetCache.getIfPresent(monsterSetCacheKey);
			if(monsterSet == null) {
				monsterSet = this.generateMonsterSet();
				this.monsterSetCache.put(monsterSetCacheKey, monsterSet);
			}
			
			return monsterSet;
		}
	}
	
	private Set<String> generateMonsterSet() throws TournamentApiException {
		Tips tips = this.tournamentService.getCurrentTips();
		Set<String> monsterList = tips.getClassMap().keySet().parallelStream()
				.filter(className -> !StringUtils.contains(className, Gender.MALE.toString()))
				.filter(className -> !StringUtils.contains(className, Gender.FEMALE.toString()))
				.collect(Collectors.toSet());
		return monsterList;
	}
	
	private Set<String> prestigeSkillSet() throws DumpException { 
		Set<String> prestigeSkills = this.tournamentService.getPrestigeSkills();
		return prestigeSkills;
	}

}
