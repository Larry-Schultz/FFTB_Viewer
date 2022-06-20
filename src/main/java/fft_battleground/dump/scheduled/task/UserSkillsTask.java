package fft_battleground.dump.scheduled.task;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import fft_battleground.discord.WebhookManager;
import fft_battleground.dump.DumpDataProvider;
import fft_battleground.dump.DumpScheduledTasks;
import fft_battleground.dump.DumpService;
import fft_battleground.dump.scheduled.DumpScheduledTask;
import fft_battleground.event.detector.model.PlayerSkillEvent;
import fft_battleground.event.detector.model.PrestigeSkillsEvent;
import fft_battleground.event.model.DatabaseResultsData;
import fft_battleground.event.model.PlayerSkillRefresh;
import fft_battleground.exception.DumpException;
import fft_battleground.exception.TournamentApiException;
import fft_battleground.repo.model.BatchDataEntry;
import fft_battleground.repo.model.PlayerSkills;
import fft_battleground.repo.repository.BatchDataEntryRepo;
import fft_battleground.repo.util.BatchDataEntryType;
import fft_battleground.skill.SkillUtils;
import fft_battleground.util.Router;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserSkillsTask extends DumpScheduledTask {
	private static final Logger ascensionLogger = LoggerFactory.getLogger("AscensionPrestigeLogger");
	
	private DumpService dumpServiceRef;
	private BatchDataEntryRepo batchDataEntryRepoRef;
	private DumpDataProvider dumpDataProviderRef;
	private WebhookManager errorWebhookManagerRef;
	private SkillUtils monsterUtilsRef;
	private Router<DatabaseResultsData> betResultsRouterRef;
	
	@Getter @Setter private boolean checkAllUsers = false;
	
	public UserSkillsTask(DumpScheduledTasks dumpScheduledTasks, DumpService dumpService) {
		super(dumpScheduledTasks);
		
		this.dumpServiceRef = dumpService;
		this.batchDataEntryRepoRef = this.dumpScheduledTasksRef.getBatchDataEntryRepo();
		this.dumpDataProviderRef = this.dumpScheduledTasksRef.getDumpDataProvider();
		this.errorWebhookManagerRef = this.dumpScheduledTasksRef.getErrorWebhookManager();
		this.monsterUtilsRef = this.dumpScheduledTasksRef.getMonsterUtils();
		this.betResultsRouterRef = this.dumpScheduledTasksRef.getBetResultsRouter();
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
			Set<String> prestigeSkillPlayers = Sets.newConcurrentHashSet(this.dumpDataProviderRef.getPlayersForPrestigeSkillsDump()); //use the larger set of names from the leaderboard
			
			//filter userSkillPlayers by lastActiveDate
			final Set<String> activeUserSkillPlayers = Sets.newConcurrentHashSet(this.dumpScheduledTasksRef.filterPlayerListToActiveUsers(userSkillPlayersFromDump, previousBatchDataEntry));
			playersAnalyzed.set(userSkillPlayersFromDump.size());
			
			AtomicInteger count = new AtomicInteger(0);
			//assume all players with prestige skills have user skills
			activeUserSkillPlayers.parallelStream().forEach(player -> {
				try {
					this.handlePlayerSkillUpdate(player, activeUserSkillPlayers, prestigeSkillPlayers);
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
	
	private void handlePlayerSkillUpdate(String player, Set<String> userSkillPlayers, Set<String> prestigeSkillPlayers) throws DumpException, TournamentApiException {
		PlayerSkillRefresh refresh = new PlayerSkillRefresh(player);
		//delete all skills from cache
		this.dumpServiceRef.getUserSkillsCache().remove(player);
		
		//get user skills
		List<PlayerSkills> userPlayerSkills = this.dumpDataProviderRef.getSkillsForPlayer(player);
		this.monsterUtilsRef.categorizeSkillsList(userPlayerSkills);
		this.monsterUtilsRef.regulateMonsterSkillCooldowns(userPlayerSkills);
		List<String> userSkills = PlayerSkills.convertToListOfSkillStrings(userPlayerSkills);
		
		//store user skills
		this.dumpServiceRef.getUserSkillsCache().put(player, userSkills);
		PlayerSkillEvent userSkillsEvent = new PlayerSkillEvent(userPlayerSkills, player);
		refresh.setPlayerSkillEvent(userSkillsEvent);
	
		List<String> prestigeSkills = null;
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
			this.dumpServiceRef.getPrestigeSkillsCache().put(player, prestigeSkills);
			PrestigeSkillsEvent prestigeEvent = new PrestigeSkillsEvent(player, prestigeSkills);
			refresh.setPrestigeSkillEvent(prestigeEvent);
		}
		
		this.betResultsRouterRef.sendDataToQueues(refresh);
		
		log.info("refreshed skills for player: {}", player);
	}
}
