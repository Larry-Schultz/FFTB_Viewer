package fft_battleground.dump;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Maps;
import com.google.common.collect.MapDifference.ValueDifference;

import fft_battleground.botland.model.DatabaseResultsData;
import fft_battleground.discord.WebhookManager;
import fft_battleground.event.PlayerSkillRefresh;
import fft_battleground.event.model.AllegianceEvent;
import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.event.model.PlayerSkillEvent;
import fft_battleground.event.model.PortraitEvent;
import fft_battleground.event.model.PrestigeSkillsEvent;
import fft_battleground.exception.DumpException;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.repo.BatchDataEntryType;
import fft_battleground.repo.model.BatchDataEntry;
import fft_battleground.repo.repository.BatchDataEntryRepo;
import fft_battleground.util.Router;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DumpScheduledTasks {

	@Autowired
	private Router<BattleGroundEvent> eventRouter;
	
	@Autowired
	@Getter private DumpService dumpService;
	
	@Autowired
	private DumpDataProvider dumpDataProvider;
	
	@Autowired
	private Router<DatabaseResultsData> betResultsRouter;
	
	@Autowired
	private BatchDataEntryRepo batchDataEntryRepo;
	
	@Autowired
	private WebhookManager errorWebhookManager;
	
	private Timer batchTimer = new Timer();
	private Timer cacheTimer = new Timer();
	
	//every 3 hours, with an initial delay of 5 minutes
	@Scheduled(fixedDelay = 10800000, initialDelay=300000)
	public void runCacheUpdates() {
		DumpScheduledTask task = new DumpScheduledTask(this) {
			@Override
			protected void task() {
				DumpReportsService dumpReportsService = this.dumpScheduledTasksRef.getDumpService().getDumpReportsService();
				dumpReportsService.writeBetPercentile();
				dumpReportsService.writeFightPercentile();
				dumpReportsService.writeBotLeaderboardToCaches();
				dumpReportsService.writeLeaderboard();
				dumpReportsService.writeAllegianceWrapper();
			}
			
		};
		this.cacheTimer.schedule(task, 0);
	}
	
	@Scheduled(cron = "0 0 1 * * ?")
	public void runAllUpdates() {
		DumpScheduledTask[] dumpScheduledTasks = new DumpScheduledTask[] {
				new AllegianceTask(this), 
				new BotListTask(this), 
				new PortraitsTask(this),
				new UserSkillsTask(this)
			};
		for(DumpScheduledTask task : dumpScheduledTasks) {
			this.batchTimer.schedule(task, 0);
		}
	}
	
	public Map<String, String> updatePortraits() {
		log.info("updating portrait cache");
		Date startDate = new Date();
		BatchDataEntry portraitPreviousBatchDataEntry = this.batchDataEntryRepo.getLastestBatchDataEntryForBatchEntryType(BatchDataEntryType.PORTRAIT);
		Integer playersAnalyzed = 0;
		int playersUpdated = 0;
		Map<String, String> portraitsFromDump = new HashMap<>();
		try {
			Set<String> playerNamesSet = this.dumpDataProvider.getPlayersForPortraitDump();
			playerNamesSet = this.filterPlayerListToActiveUsers(playerNamesSet, portraitPreviousBatchDataEntry);
			playersAnalyzed = playerNamesSet.size();
			
			int count = 0;
			for(String player: playerNamesSet) {
				String portrait = this.dumpDataProvider.getPortraitForPlayer(player);
				if(!StringUtils.isBlank(portrait)) {
					portraitsFromDump.put(player, portrait);
				}
				
				if(count % 20 == 0) {
					log.info("Read portrait data for {} users out of {}", count, playersAnalyzed);
				}
				count++;
			}
			
			Map<String, ValueDifference<String>> balanceDelta = Maps.difference(this.dumpService.getPortraitCache(), portraitsFromDump).entriesDiffering();
			List<BattleGroundEvent> portraitEvents = new LinkedList<>();
			//find differences
			for(String key: balanceDelta.keySet()) {
				PortraitEvent event = new PortraitEvent(key, balanceDelta.get(key).rightValue());
				portraitEvents.add(event);
				//update cache with new data
				this.dumpService.getPortraitCache().put(key, balanceDelta.get(key).rightValue());
				//increment updated players
				playersUpdated++;
			}
			
			//add missing players
			for(String key: portraitsFromDump.keySet()) {
				if(!this.dumpService.getPortraitCache().containsKey(key)) {
					PortraitEvent event = new PortraitEvent(key, portraitsFromDump.get(key));
					portraitEvents.add(event);
					this.dumpService.getPortraitCache().put(key, portraitsFromDump.get(key));
					//increment updated players
					playersUpdated++;
					//increment playersAnalyzed since they weren't initially considered
					playersAnalyzed++;
				}
			}
			
			portraitEvents.stream().forEach(event -> log.info("Found event from Dump: {} with data: {}", event.getEventType().getEventStringName(), event.toString()));
			this.eventRouter.sendAllDataToQueues(portraitEvents);
			//generate new BatchDataEntry for this batch run
		} catch(Exception e) {
			Date endDate = new Date();
			BatchDataEntry newBatchDataEntry = new BatchDataEntry(BatchDataEntryType.PORTRAIT, playersAnalyzed, playersUpdated, startDate, endDate, e.getClass().toString(), e.getStackTrace()[0].getLineNumber());
			this.writeToBatchDataEntryRepo(newBatchDataEntry);
			this.errorWebhookManager.sendException(e, "Error in updatePortraits batch job");
			return null;
		}
		Date endDate = new Date();
		BatchDataEntry newBatchDataEntry = new BatchDataEntry(BatchDataEntryType.PORTRAIT, playersAnalyzed, playersUpdated, startDate, endDate);
		this.writeToBatchDataEntryRepo(newBatchDataEntry);
		log.info("portrait cache update complete");
		
		return portraitsFromDump;
	}
	
	public Map<String, BattleGroundTeam> updateAllegiances() {
		log.info("updating allegiances cache");
		Date startDate = new Date();
		BatchDataEntry allegiancePreviousBatchDataEntry = this.batchDataEntryRepo.getLastestBatchDataEntryForBatchEntryType(BatchDataEntryType.ALLEGIANCE);
		int numberOfPlayersUpdated = 0;
		int numberOfPlayersAnalyzed = 0;
		Map<String, BattleGroundTeam> allegiancesFromDump = new HashMap<>();
		try {
			Set<String> playerNamesSet = this.dumpDataProvider.getPlayersForAllegianceDump();
			playerNamesSet = this.filterPlayerListToActiveUsers(playerNamesSet, allegiancePreviousBatchDataEntry);
			numberOfPlayersAnalyzed = playerNamesSet.size();
			
			int count = 0;
			for(String player : playerNamesSet) {
				BattleGroundTeam team = this.dumpDataProvider.getAllegianceForPlayer(player);
				if(team != null) {
					allegiancesFromDump.put(player, team);
				}
				
				if(count % 20 == 0) {
					log.info("Read allegiance data for {} users out of {}", count, numberOfPlayersAnalyzed);
				}
				count++;
			}
			
			Map<String, ValueDifference<BattleGroundTeam>> balanceDelta = Maps.difference(this.dumpService.getAllegianceCache(), allegiancesFromDump).entriesDiffering();
			List<BattleGroundEvent> allegianceEvents = new LinkedList<>();
			//find differences
			for(String key: balanceDelta.keySet()) {
				AllegianceEvent event = new AllegianceEvent(key, balanceDelta.get(key).rightValue());
				allegianceEvents.add(event);
				//update cache with new data
				this.dumpService.getAllegianceCache().put(key, balanceDelta.get(key).rightValue());
				//increment players updated
				numberOfPlayersUpdated++;
			}
			
			//add missing players
			for(String key: allegiancesFromDump.keySet()) {
				if(!this.dumpService.getAllegianceCache().containsKey(key)) {
					AllegianceEvent event = new AllegianceEvent(key, allegiancesFromDump.get(key));
					allegianceEvents.add(event);
					this.dumpService.getAllegianceCache().put(key, allegiancesFromDump.get(key));
					//increment players updated
					numberOfPlayersUpdated++;
					//increment players analyzed
					numberOfPlayersAnalyzed++;
				}
			}
			
			allegianceEvents.stream().forEach(event -> log.info("Found event from Dump: {} with data: {}", event.getEventType().getEventStringName(), event.toString()));
			this.eventRouter.sendAllDataToQueues(allegianceEvents);
		} catch(Exception e) {
			Date endDate = new Date();
			BatchDataEntry newBatchDataEntry = new BatchDataEntry(BatchDataEntryType.ALLEGIANCE, numberOfPlayersAnalyzed, numberOfPlayersUpdated, startDate, endDate, e.getClass().toString(), e.getStackTrace()[0].getLineNumber());
			this.writeToBatchDataEntryRepo(newBatchDataEntry);
			this.errorWebhookManager.sendException(e, "error in Allegiances batch job");
			return null;
		}
		Date endDate = new Date();
		BatchDataEntry newBatchDataEntry = new BatchDataEntry(BatchDataEntryType.ALLEGIANCE, numberOfPlayersAnalyzed, numberOfPlayersUpdated, startDate, endDate);
		this.writeToBatchDataEntryRepo(newBatchDataEntry);
		log.info("allegiances cache update complete.");
		
		return allegiancesFromDump;
	}
	
	public void updateAllSkills() {
		Date startDate = new Date();
		BatchDataEntry previousBatchDataEntry = this.batchDataEntryRepo.getLastestBatchDataEntryForBatchEntryType(BatchDataEntryType.USERSKILL);
		int playersAnalyzed = 0;
		int playersUpdated = 0;
		try {
			log.info("updating user and prestige skills caches");
			Set<String> userSkillPlayers = this.dumpDataProvider.getPlayersForUserSkillsDump(); //use the larger set of names from the leaderboard
			Set<String> prestigeSkillPlayers = this.dumpDataProvider.getPlayersForPrestigeSkillsDump(); //use the larger set of names from the leaderboard
			
			//filter userSkillPlayers by lastActiveDate
			userSkillPlayers = this.filterPlayerListToActiveUsers(userSkillPlayers, previousBatchDataEntry);
			playersAnalyzed = userSkillPlayers.size();
			
			int count = 0;
			//assume all players with prestige skills have user skills
			for(String player: userSkillPlayers) {
				this.handlePlayerSkillUpdate(player, userSkillPlayers, prestigeSkillPlayers);
				playersUpdated++;
				
				if(count % 20 == 0) {
					log.info("Read user skill data for {} users out of {}", count, playersAnalyzed);
				}
				count++;
			}
		} catch(Exception e) {
			Date endDate = new Date();
			BatchDataEntry newBatchDataEntry = new BatchDataEntry(BatchDataEntryType.USERSKILL, playersAnalyzed, playersUpdated, startDate, endDate, e.getClass().toString(), e.getStackTrace()[0].getLineNumber());
			this.writeToBatchDataEntryRepo(newBatchDataEntry);
			this.errorWebhookManager.sendException(e, "error in Update Skills batch job");
			return;
		}
		Date endDate = new Date();
		BatchDataEntry newBatchDataEntry = new BatchDataEntry(BatchDataEntryType.USERSKILL, playersAnalyzed, playersUpdated, startDate, endDate);
		this.writeToBatchDataEntryRepo(newBatchDataEntry);
		log.info("user and prestige skill cache updates complete");
	}
	
	public void handlePlayerSkillUpdateFromRepo(String player) throws DumpException {
		try {
			List<String> prestigeSkillsBefore = this.dumpService.getPrestigeSkillsCache().get(player);
			int prestigeSkillsCount = prestigeSkillsBefore != null ? prestigeSkillsBefore.size() : 0;
			PlayerSkillRefresh refresh = this.forcePlayerSkillRefreshForAscension(player, prestigeSkillsCount);
			this.betResultsRouter.sendDataToQueues(refresh);
		} catch(Exception| AssertionError e) {
			log.error("Error updating player skills for player", player);
			this.errorWebhookManager.sendException(e, "Error with processing ascension for player" + player);
		}
	}
	
	@Retryable( value = AssertionError.class, maxAttempts = 3, backoff = @Backoff(delay = 2000, multiplier=2))
	protected PlayerSkillRefresh forcePlayerSkillRefreshForAscension(String player, int prestigeSkillsBeforeCount) throws AssertionError {
		PlayerSkillRefresh refresh = new PlayerSkillRefresh(player);
		//delete all skills from cache
		this.dumpService.getUserSkillsCache().remove(player);
		
		List<String> userSkills = Collections.emptyList();
		
		this.dumpService.getUserSkillsCache().put(player, userSkills);
		PlayerSkillEvent userSkillsEvent = new PlayerSkillEvent(player, userSkills);
		refresh.setPlayerSkillEvent(userSkillsEvent);
	
		List<String> prestigeSkills = null;
		try {
			//attempt to get prestige skills, this is allowed to fail meaning this player has no prestige
			prestigeSkills = this.dumpDataProvider.getPrestigeSkillsForPlayer(player);
		} catch(Exception e) {
			log.warn("Player {} does not have prestige", player);
		}
		
		if(prestigeSkills != null && prestigeSkills.size() > 0) {
			//store prestige skills
			this.dumpService.getPrestigeSkillsCache().remove(player);
			this.dumpService.getPrestigeSkillsCache().put(player, prestigeSkills);
			PrestigeSkillsEvent prestigeEvent = new PrestigeSkillsEvent(player, prestigeSkills);
			refresh.setPrestigeSkillEvent(prestigeEvent);
		}
		
		Assert.assertNotNull(prestigeSkills);
		Assert.assertNotEquals(prestigeSkills.size(), prestigeSkillsBeforeCount);
		
		return refresh;
	}
	
	public void handlePlayerSkillUpdate(String player, Set<String> userSkillPlayers,Set<String> prestigeSkillPlayers) throws DumpException {
		PlayerSkillRefresh refresh = new PlayerSkillRefresh(player);
		//delete all skills from cache
		this.dumpService.getUserSkillsCache().remove(player);
		
		//get user skills
		List<String> userSkills = this.dumpDataProvider.getSkillsForPlayer(player);
		
		//store user skills
		this.dumpService.getUserSkillsCache().put(player, userSkills);
		PlayerSkillEvent userSkillsEvent = new PlayerSkillEvent(player, userSkills);
		refresh.setPlayerSkillEvent(userSkillsEvent);
	
		List<String> prestigeSkills = null;
		try {
			//attempt to get prestige skills, this is allowed to fail meaning this player has no prestige
			prestigeSkills = this.dumpDataProvider.getPrestigeSkillsForPlayer(player);
		} catch(Exception e) {
			log.warn("Player {} does not have prestige", player);
		}
		if(prestigeSkills != null && prestigeSkills.size() > 0) {
			//store prestige skills
			this.dumpService.getPrestigeSkillsCache().remove(player);
			this.dumpService.getPrestigeSkillsCache().put(player, prestigeSkills);
			PrestigeSkillsEvent prestigeEvent = new PrestigeSkillsEvent(player, prestigeSkills);
			refresh.setPrestigeSkillEvent(prestigeEvent);
		}
		
		this.betResultsRouter.sendDataToQueues(refresh);
		
		log.info("refreshed skills for player: {}", player);
	}
	
	public Set<String> updateBotList() {
		log.info("updating bot list");
		Date startDate = new Date();
		BatchDataEntry previousBatchDataEntry = this.batchDataEntryRepo.getLastestBatchDataEntryForBatchEntryType(BatchDataEntryType.BOT);
		int numberOfPlayersAnalyzed = 0;
		int numberOfPlayersUpdated = 0;
		try {
			Set<String> dumpBots = this.dumpDataProvider.getBots();
			dumpBots.stream().forEach(botName -> this.dumpService.getBotCache().add(botName));
			numberOfPlayersAnalyzed = dumpBots.size();
			numberOfPlayersUpdated = dumpBots.size();
		} catch(Exception e) {
			Date endDate = new Date();
			BatchDataEntry newBatchDataEntry = new BatchDataEntry(BatchDataEntryType.BOT, numberOfPlayersAnalyzed, numberOfPlayersUpdated, startDate, endDate, e.getClass().toString(), e.getStackTrace()[0].getLineNumber());
			this.writeToBatchDataEntryRepo(newBatchDataEntry);
			this.errorWebhookManager.sendException(e, "Error in Update Bot List batch job");
			return null;
		}
		Date endDate = new Date();
		BatchDataEntry newBatchDataEntry = new BatchDataEntry(BatchDataEntryType.BOT, numberOfPlayersAnalyzed, numberOfPlayersUpdated, startDate, endDate);
		this.writeToBatchDataEntryRepo(newBatchDataEntry);
		log.info("bot list update complete");
		Set<String> result = this.dumpService.getBotCache();
		return result;
	}
	
	@Transactional
	protected void writeToBatchDataEntryRepo(BatchDataEntry batchDataEntry) {
		this.batchDataEntryRepo.saveAndFlush(batchDataEntry);
	}
	
	protected Set<String> filterPlayerListToActiveUsers(Set<String> players, BatchDataEntry batchDataEntry) {
		Date previousUpdateComplete = batchDataEntry != null && (batchDataEntry.getSuccessfulRun() != null && batchDataEntry.getSuccessfulRun()) ? batchDataEntry.getUpdateStarted() : null;
		Set<String> result;
		
		//if value is null, just use yesterday
		if(previousUpdateComplete == null) {
			Calendar today = Calendar.getInstance();
			today.add(Calendar.DAY_OF_WEEK, -1);
			previousUpdateComplete = today.getTime();
		} else {
			//set time to day before last update date.
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(previousUpdateComplete);
			calendar.add(Calendar.DAY_OF_WEEK, -1);
			previousUpdateComplete = calendar.getTime();
		}
		final Date compareToPerviousUpdateComplete = previousUpdateComplete; //because the compile complains that previousUpdateComplete was not a final variable
		result = players.parallelStream()
			.filter(player -> this.dumpService.getLastActiveCache().containsKey(player))
			.filter(player -> this.dumpService.getLastActiveCache().get(player) != null)
			.filter(player -> this.dumpService.getLastActiveCache().get(player).compareTo(compareToPerviousUpdateComplete) > 0 || compareToPerviousUpdateComplete == null)
			.collect(Collectors.toSet());
		return result;
	}
}

abstract class DumpScheduledTask extends TimerTask {
	protected DumpScheduledTasks dumpScheduledTasksRef;
	
	public DumpScheduledTask(DumpScheduledTasks dumpScheduledTasks) {
		this.dumpScheduledTasksRef = dumpScheduledTasks;
	}
	
	public void run() {
		this.task();
	}
	
	protected abstract void task();
}

class AllegianceTask extends DumpScheduledTask {
	public AllegianceTask(DumpScheduledTasks dumpScheduledTasks) { super(dumpScheduledTasks);}
	protected void task() {this.dumpScheduledTasksRef.updateAllegiances();}
}

class UserSkillsTask extends DumpScheduledTask {
	public UserSkillsTask(DumpScheduledTasks dumpScheduledTasks) {super(dumpScheduledTasks);}
	protected void task() {this.dumpScheduledTasksRef.updateAllSkills();}
}

class BotListTask extends DumpScheduledTask {
	public BotListTask(DumpScheduledTasks dumpScheduledTasks) {super(dumpScheduledTasks);}
	protected void task() {this.dumpScheduledTasksRef.updateBotList();}
}

class PortraitsTask extends DumpScheduledTask {
	public PortraitsTask(DumpScheduledTasks dumpScheduledTasks) {super(dumpScheduledTasks);}
	protected void task() {this.dumpScheduledTasksRef.updatePortraits();}
}

class CacheScheduledTask<T> extends DumpScheduledTask {

	private Callable<T> cacheGeneratorFunction;
	
	public CacheScheduledTask(DumpScheduledTasks dumpScheduledTasks, Callable<T> cacheGeneratorFunction) {
		super(dumpScheduledTasks);
		this.cacheGeneratorFunction = cacheGeneratorFunction;
	}
	
	@SneakyThrows
	protected void task() {
		this.cacheGeneratorFunction.call();
	}
	
}
