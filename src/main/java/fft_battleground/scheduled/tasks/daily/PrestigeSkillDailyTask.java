package fft_battleground.scheduled.tasks.daily;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Sets;

import fft_battleground.discord.WebhookManager;
import fft_battleground.dump.DumpDataProvider;
import fft_battleground.dump.cache.map.LastActiveCache;
import fft_battleground.dump.cache.map.LastFightActiveCache;
import fft_battleground.dump.cache.map.PrestigeSkillsCache;
import fft_battleground.event.detector.model.PrestigeSkillsEvent;
import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.exception.DumpException;
import fft_battleground.exception.TournamentApiException;
import fft_battleground.repo.model.BatchDataEntry;
import fft_battleground.repo.model.PrestigeSkills;
import fft_battleground.repo.repository.BatchDataEntryRepo;
import fft_battleground.repo.util.BatchDataEntryType;
import fft_battleground.scheduled.DumpScheduledTasksManager;
import fft_battleground.scheduled.tasks.DumpDailyScheduledTask;
import fft_battleground.skill.SkillUtils;
import fft_battleground.util.Router;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PrestigeSkillDailyTask extends DumpDailyScheduledTask {
	private static final Logger ascensionLogger = LoggerFactory.getLogger("AscensionPrestigeLogger");
	
	@Autowired
	private DumpDataProvider dumpDataProvider;
	
	@Autowired
	private BatchDataEntryRepo batchDataEntryRepo;
	
	@Autowired
	private WebhookManager errorWebhookManager;
	
	@Autowired
	private Router<BattleGroundEvent> eventRouter;
	
	@Autowired
	private DumpScheduledTasksManager dumpScheduledTaskManager;
	
	@Autowired
	private PrestigeSkillsCache prestigeSkillsCache;
	
	public PrestigeSkillDailyTask(@Autowired LastActiveCache lastActiveCache, 
			@Autowired LastFightActiveCache lastFightActiveCache) { 
		super(lastActiveCache, lastFightActiveCache);
	}

	@Override
	protected void task() {
		this.updateAllSkills();
	}
	
	protected void updateAllSkills() {
		Date startDate = new Date();
		BatchDataEntry previousBatchDataEntry = this.batchDataEntryRepo.getLastestBatchDataEntryForBatchEntryType(BatchDataEntryType.PRESTIGE_SKILL);
		final AtomicInteger playersAnalyzed = new AtomicInteger();
		final AtomicInteger playersUpdated = new AtomicInteger(0);
		try {
			log.info("updating prestige skills caches");
			Set<String> prestigeSkillPlayers = Sets.newConcurrentHashSet(this.dumpDataProvider.getPlayersForPrestigeSkillsDump()); //use the larger set of names from the leaderboard
			
			//filter userSkillPlayers by lastActiveDate
			final Set<String> activePrestigeSkillPlayers = Sets.newConcurrentHashSet(this.filterPlayerListToActiveUsers(prestigeSkillPlayers, previousBatchDataEntry));
			playersAnalyzed.set(activePrestigeSkillPlayers.size());
			
			final Set<String> playersToAnalyze = this.isCheckAllUsers() ? prestigeSkillPlayers : activePrestigeSkillPlayers;
			
			AtomicInteger count = new AtomicInteger(0);
			//assume all players with prestige skills have user skills
			playersToAnalyze.parallelStream().forEach(player -> {
				try {
					this.handlePlayerSkillUpdate(player, prestigeSkillPlayers);
				} catch (DumpException | TournamentApiException e) {
					log.error("error handling player skill data for player {}", player, e);
					this.errorWebhookManager.sendException(e, "error in Update Skills batch job for player: " + player);
				}
				playersUpdated.incrementAndGet();
				
				if(count.incrementAndGet() % 20 == 0) {
					log.info("Read user skill data for {} users out of {}", count, playersAnalyzed.get());
				}
				
			});
		} catch(Exception e) {
			Date endDate = new Date();
			BatchDataEntry newBatchDataEntry = new BatchDataEntry(BatchDataEntryType.PRESTIGE_SKILL, playersAnalyzed.get(), playersUpdated.get(), 
					startDate, endDate, e.getClass().toString(), e.getStackTrace()[0].getLineNumber());
			this.dumpScheduledTaskManager.writeToBatchDataEntryRepo(newBatchDataEntry);
			this.errorWebhookManager.sendException(e, "error in Update Skills batch job");
			return;
		}
		Date endDate = new Date();
		BatchDataEntry newBatchDataEntry = new BatchDataEntry(BatchDataEntryType.PRESTIGE_SKILL, playersAnalyzed.get(), playersUpdated.get(), 
				startDate, endDate);
		this.dumpScheduledTaskManager.writeToBatchDataEntryRepo(newBatchDataEntry);
		log.info("user and prestige skill cache updates complete");
	}
	
	private void handlePlayerSkillUpdate(String player, Set<String> prestigeSkillPlayers) throws DumpException, TournamentApiException {
		List<PrestigeSkills> prestigeSkills = null;
		if(prestigeSkillPlayers.contains(player)) {
			try {
				//attempt to get prestige skills, this is allowed to fail meaning this player has no prestige
				prestigeSkills = this.dumpDataProvider.getPrestigeSkillsForPlayer(player);
			} catch(Exception e) {
				ascensionLogger.warn("Player {} does not have prestige", player);
			}
		}
		if(prestigeSkills != null && prestigeSkills.size() > 0) {
			List<String> prestigeCacheResult = this.prestigeSkillsCache.get(player);
			int cacheSize = prestigeCacheResult != null ? prestigeCacheResult.size() : 0;
			if(cacheSize < prestigeSkills.size()) {
				ascensionLogger.info("New Ascension found for player {}!  From level {} to level {}", player, 
						cacheSize, prestigeSkills.size());
			}
			//store prestige skills
			if(prestigeCacheResult != null) {
				this.prestigeSkillsCache.remove(player);
			}
			List<String> prestigeSkillNames = prestigeSkills.stream().map(PrestigeSkills::getSkill)
					.collect(Collectors.toList());
			
			boolean nonPrestigeFound = false;
			for(String skill : prestigeSkillNames) {
				if(!SkillUtils.isPrestigeSkill(skill)) {
					nonPrestigeFound = true;
					break;
				}
			}
			if(nonPrestigeFound) {
				ascensionLogger.error("Non Prestige Skill Found, filtering out");
				prestigeSkillNames = prestigeSkillNames.stream().filter(SkillUtils::isPrestigeSkill).collect(Collectors.toList());
			}
			
			this.prestigeSkillsCache.put(player, prestigeSkillNames);
			PrestigeSkillsEvent prestigeEvent = new PrestigeSkillsEvent(prestigeSkills, player);
			this.eventRouter.sendDataToQueues(prestigeEvent);
		}
		
		log.info("refreshed prestige skills for player: {}", player);
	}

}
