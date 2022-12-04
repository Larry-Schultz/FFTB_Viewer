package fft_battleground.dump.scheduled.daily;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import fft_battleground.dump.DumpScheduledTasksManagerImpl;
import fft_battleground.dump.DumpService;
import fft_battleground.dump.scheduled.DumpDailyScheduledTask;
import fft_battleground.event.detector.model.PrestigeSkillsEvent;
import fft_battleground.exception.DumpException;
import fft_battleground.exception.TournamentApiException;
import fft_battleground.repo.model.BatchDataEntry;
import fft_battleground.repo.model.PrestigeSkills;
import fft_battleground.repo.util.BatchDataEntryType;
import fft_battleground.skill.SkillUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PrestigeSkillDailyTask extends DumpDailyScheduledTask {
	private static final Logger ascensionLogger = LoggerFactory.getLogger("AscensionPrestigeLogger");
	
	public PrestigeSkillDailyTask(DumpScheduledTasksManagerImpl dumpScheduledTasks, DumpService dumpService) {
		super(dumpScheduledTasks, dumpService);
	}

	@Override
	protected void task() {
		this.updateAllSkills();
	}
	
	protected void updateAllSkills() {
		Date startDate = new Date();
		BatchDataEntry previousBatchDataEntry = this.batchDataEntryRepoRef.getLastestBatchDataEntryForBatchEntryType(BatchDataEntryType.PRESTIGE_SKILL);
		final AtomicInteger playersAnalyzed = new AtomicInteger();
		final AtomicInteger playersUpdated = new AtomicInteger(0);
		try {
			log.info("updating prestige skills caches");
			Set<String> prestigeSkillPlayers = Sets.newConcurrentHashSet(this.dumpDataProviderRef.getPlayersForPrestigeSkillsDump()); //use the larger set of names from the leaderboard
			
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
					this.errorWebhookManagerRef.sendException(e, "error in Update Skills batch job for player: " + player);
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
			this.dumpScheduledTasksRef.writeToBatchDataEntryRepo(newBatchDataEntry);
			this.errorWebhookManagerRef.sendException(e, "error in Update Skills batch job");
			return;
		}
		Date endDate = new Date();
		BatchDataEntry newBatchDataEntry = new BatchDataEntry(BatchDataEntryType.PRESTIGE_SKILL, playersAnalyzed.get(), playersUpdated.get(), 
				startDate, endDate);
		this.dumpScheduledTasksRef.writeToBatchDataEntryRepo(newBatchDataEntry);
		log.info("user and prestige skill cache updates complete");
	}
	
	private void handlePlayerSkillUpdate(String player, Set<String> prestigeSkillPlayers) throws DumpException, TournamentApiException {
		List<PrestigeSkills> prestigeSkills = null;
		if(prestigeSkillPlayers.contains(player)) {
			try {
				//attempt to get prestige skills, this is allowed to fail meaning this player has no prestige
				prestigeSkills = this.dumpDataProviderRef.getPrestigeSkillsForPlayer(player);
			} catch(Exception e) {
				ascensionLogger.warn("Player {} does not have prestige", player);
			}
		}
		if(prestigeSkills != null && prestigeSkills.size() > 0) {
			List<String> prestigeCacheResult = this.dumpServiceRef.getPrestigeSkillsCache().get(player);
			int cacheSize = prestigeCacheResult != null ? prestigeCacheResult.size() : 0;
			if(cacheSize < prestigeSkills.size()) {
				ascensionLogger.info("New Ascension found for player {}!  From level {} to level {}", player, 
						cacheSize, prestigeSkills.size());
			}
			//store prestige skills
			if(prestigeCacheResult != null) {
				this.dumpServiceRef.getPrestigeSkillsCache().remove(player);
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
			
			this.dumpServiceRef.getPrestigeSkillsCache().put(player, prestigeSkillNames);
			PrestigeSkillsEvent prestigeEvent = new PrestigeSkillsEvent(prestigeSkills, player);
			this.betResultsRouterRef.sendDataToQueues(prestigeEvent);
		}
		
		log.info("refreshed prestige skills for player: {}", player);
	}

}
