package fft_battleground.dump.scheduled.daily;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import com.google.common.collect.Sets;

import fft_battleground.dump.DumpScheduledTasksManagerImpl;
import fft_battleground.dump.DumpService;
import fft_battleground.dump.scheduled.DumpDailyScheduledTask;
import fft_battleground.event.detector.model.PlayerSkillEvent;
import fft_battleground.event.model.PlayerSkillRefresh;
import fft_battleground.exception.DumpException;
import fft_battleground.exception.TournamentApiException;
import fft_battleground.repo.model.BatchDataEntry;
import fft_battleground.repo.model.PlayerSkills;
import fft_battleground.repo.util.BatchDataEntryType;
import fft_battleground.skill.model.Skill;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserSkillsDailyTask extends DumpDailyScheduledTask {

	public UserSkillsDailyTask(DumpScheduledTasksManagerImpl dumpScheduledTasks, DumpService dumpService) {
		super(dumpScheduledTasks, dumpService);
	}
	
	protected void task() {
		this.updateAllSkills();
	}
	
	protected void updateAllSkills() {
		Date startDate = new Date();
		BatchDataEntry previousBatchDataEntry = this.batchDataEntryRepoRef.getLastestBatchDataEntryForBatchEntryType(BatchDataEntryType.USERSKILL);
		final AtomicInteger playersAnalyzed = new AtomicInteger();
		final AtomicInteger playersUpdated = new AtomicInteger(0);
		try {
			log.info("updating user and prestige skills caches");
			Set<String> userSkillPlayersFromDump = this.dumpDataProviderRef.getPlayersForUserSkillsDump(); //use the larger set of names from the leaderboard
			
			//filter userSkillPlayers by lastActiveDate
			final Set<String> activeUserSkillPlayers = Sets.newConcurrentHashSet(this.filterPlayerListToActiveUsers(userSkillPlayersFromDump, previousBatchDataEntry));
			playersAnalyzed.set(userSkillPlayersFromDump.size());
			
			final Set<String> playersToAnalyze = this.isCheckAllUsers() ? userSkillPlayersFromDump : activeUserSkillPlayers;
			
			AtomicInteger count = new AtomicInteger(0);
			//assume all players with prestige skills have user skills
			activeUserSkillPlayers.parallelStream().forEach(player -> {
				try {
					this.handlePlayerSkillUpdate(player, activeUserSkillPlayers);
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
			BatchDataEntry newBatchDataEntry = new BatchDataEntry(BatchDataEntryType.USERSKILL, playersAnalyzed.get(), playersUpdated.get(), startDate, endDate, e.getClass().toString(), e.getStackTrace()[0].getLineNumber());
			this.dumpScheduledTasksRef.writeToBatchDataEntryRepo(newBatchDataEntry);
			this.errorWebhookManagerRef.sendException(e, "error in Update Skills batch job");
			return;
		}
		Date endDate = new Date();
		BatchDataEntry newBatchDataEntry = new BatchDataEntry(BatchDataEntryType.USERSKILL, playersAnalyzed.get(), playersUpdated.get(), startDate, endDate);
		this.dumpScheduledTasksRef.writeToBatchDataEntryRepo(newBatchDataEntry);
		log.info("user and prestige skill cache updates complete");
	}
	
	private void handlePlayerSkillUpdate(String player, Set<String> userSkillPlayers) throws DumpException, TournamentApiException {
		PlayerSkillRefresh refresh = new PlayerSkillRefresh(player);
		//delete all skills from cache
		this.dumpServiceRef.getUserSkillsCache().remove(player);
		
		//get user skills
		List<PlayerSkills> userPlayerSkills = this.dumpDataProviderRef.getSkillsForPlayer(player);
		this.monsterUtilsRef.categorizeSkillsList(userPlayerSkills);
		this.monsterUtilsRef.regulateMonsterSkillCooldowns(userPlayerSkills);
		List<String> userSkills = Skill.convertToListOfSkillStrings(userPlayerSkills);
		
		//store user skills
		this.dumpServiceRef.getUserSkillsCache().put(player, userSkills);
		PlayerSkillEvent userSkillsEvent = new PlayerSkillEvent(userPlayerSkills, player);
		refresh.setPlayerSkillEvent(userSkillsEvent);
		
		this.betResultsRouterRef.sendDataToQueues(refresh);
		
		log.info("refreshed skills for player: {}", player);
	}
	
}
