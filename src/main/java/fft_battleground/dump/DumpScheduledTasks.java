package fft_battleground.dump;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.MapDifference.ValueDifference;

import fft_battleground.discord.WebhookManager;
import fft_battleground.dump.scheduled.AscensionRefreshRetry;
import fft_battleground.event.detector.model.AllegianceEvent;
import fft_battleground.event.detector.model.BattleGroundEvent;
import fft_battleground.event.detector.model.PlayerSkillEvent;
import fft_battleground.event.detector.model.PortraitEvent;
import fft_battleground.event.detector.model.PrestigeSkillsEvent;
import fft_battleground.event.detector.model.fake.ClassBonusEvent;
import fft_battleground.event.detector.model.fake.SkillBonusEvent;
import fft_battleground.event.model.DatabaseResultsData;
import fft_battleground.event.model.PlayerSkillRefresh;
import fft_battleground.exception.DumpException;
import fft_battleground.exception.TournamentApiException;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.repo.model.BatchDataEntry;
import fft_battleground.repo.model.ClassBonus;
import fft_battleground.repo.model.PlayerSkills;
import fft_battleground.repo.repository.BatchDataEntryRepo;
import fft_battleground.repo.util.BatchDataEntryType;
import fft_battleground.tournament.MonsterUtils;
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
	@Getter private MonsterUtils monsterUtils;
	
	@Autowired
	private AscensionRefreshRetry ascensionRefreshRetry;
	
	@Autowired
	private Router<DatabaseResultsData> betResultsRouter;
	
	@Autowired
	private BatchDataEntryRepo batchDataEntryRepo;
	
	@Autowired
	private WebhookManager errorWebhookManager;
	
	@Autowired
	private WebhookManager ascensionWebhookManager;
	
	private Timer batchTimer = new Timer();
	private Timer cacheTimer = new Timer();
	private Timer forceTimer = new Timer();
	
	//every 3 hours, with an initial delay of 5 minutes
	@Scheduled(fixedDelay = 10800000, initialDelay=500000)
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
				dumpReportsService.writeExpLeaderboard();
			}
			
		};
		this.cacheTimer.schedule(task, 0);
	}
	
	@Scheduled(cron = "0 0 1 * * ?")
	public void runAllUpdates() {
		DumpScheduledTask[] dumpScheduledTasks = new DumpScheduledTask[] {
				new BadAccountsTask(this),
				new AllegianceTask(this), 
				new BotListTask(this), 
				new PortraitsTask(this),
				new UserSkillsTask(this),
				new ClassBonusTask(this),
			};
		for(DumpScheduledTask task : dumpScheduledTasks) {
			this.batchTimer.schedule(task, 0);
		}
		
	}
	
	public void generateOutOfSyncPlayerRecordsFile() {
		log.info("starting out of sync player record file batch");
		try {
			Map<String, Integer> balanceMap = this.dumpDataProvider.getHighScoreDump();
			List<String> realPlayers = balanceMap.keySet().parallelStream().map(key -> StringUtils.lowerCase(key)).collect(Collectors.toList());
			List<String> currentAccounts = this.dumpService.getPlayerRecordRepo().findPlayerNames();
			
			List<String> badAccounts = currentAccounts.parallelStream().filter(account -> !realPlayers.contains(account)).collect(Collectors.toList());
			
			try(BufferedWriter writer = new BufferedWriter(new FileWriter("badAccounts.txt"))) {
				for(String badAccountName : badAccounts) {
					writer.write(badAccountName);
					writer.newLine();
				}
			}
		} catch(IOException | DumpException e) {
			log.error("Error writing bad accounts file");
		}
		
		log.info("finished writing bad accounts file");
	}
	
	public void updatePortraits() {
		log.info("updating portrait cache");
		Date startDate = new Date();
		BatchDataEntry portraitPreviousBatchDataEntry = this.batchDataEntryRepo.getLastestBatchDataEntryForBatchEntryType(BatchDataEntryType.PORTRAIT);
		final AtomicInteger playersAnalyzed = new AtomicInteger(0);
		int playersUpdated = 0;
		Map<String, String> portraitsFromDump = new HashMap<>();
		Set<String> playerNamesSet = this.dumpDataProvider.getPlayersForPortraitDump();
		playerNamesSet = this.filterPlayerListToActiveUsers(playerNamesSet, portraitPreviousBatchDataEntry);
		playersAnalyzed.set(playerNamesSet.size());
		
		final AtomicInteger count = new AtomicInteger(0);
		playerNamesSet.parallelStream().forEach(player -> {
			String portrait;
			PortraitEvent event = null;
			try {
				portrait = this.dumpDataProvider.getPortraitForPlayer(player);
				
				if(!StringUtils.isBlank(portrait)) {
					event = new PortraitEvent(player, portrait);
					log.info("Found event from Dump: {} with data: {}", event.getEventType().getEventStringName(), event.toString());
					this.eventRouter.sendDataToQueues(event);
				}
				
				if(count.incrementAndGet() % 20 == 0) {
					log.info("Read portrait data for {} users out of {}", count, playersAnalyzed);
				}
			} catch (Exception e) {
				log.error("Error updating portrait for player {}", player, e);
				this.errorWebhookManager.sendException(e, "Error updating portrait for player " + player);
			}
		});
		
		
		//generate new BatchDataEntry for this batch run
		Date endDate = new Date();
		BatchDataEntry newBatchDataEntry = new BatchDataEntry(BatchDataEntryType.PORTRAIT, playersAnalyzed.get(), playersUpdated, startDate, endDate);
		this.writeToBatchDataEntryRepo(newBatchDataEntry);
		log.info("portrait cache update complete");

		return;
	}
	
	public void updateAllegiances() {
		log.info("updating allegiances cache");
		Date startDate = new Date();
		BatchDataEntry allegiancePreviousBatchDataEntry = this.batchDataEntryRepo.getLastestBatchDataEntryForBatchEntryType(BatchDataEntryType.ALLEGIANCE);
		int numberOfPlayersUpdated = 0;
		final AtomicInteger numberOfPlayersAnalyzed = new AtomicInteger(0);
		Map<String, BattleGroundTeam> allegiancesFromDump = new HashMap<>();
		Set<String> playerNamesSet = this.dumpDataProvider.getPlayersForAllegianceDump();
		playerNamesSet = this.filterPlayerListToActiveUsers(playerNamesSet, allegiancePreviousBatchDataEntry);
		numberOfPlayersAnalyzed.set(playerNamesSet.size());
		
		final AtomicInteger count = new AtomicInteger(0);
		playerNamesSet.parallelStream().forEach(player -> {
			BattleGroundTeam team = null;
			try {
				team = this.dumpDataProvider.getAllegianceForPlayer(player);
			} catch (Exception e) {
				log.error("Error getting allegiance for player {}", player, e);
				this.errorWebhookManager.sendException(e, "\"Error getting allegiance for player " + player);
			}
			if(team != null) {
				allegiancesFromDump.put(player, team);
			}
			
			if(count.incrementAndGet() % 20 == 0) {
				log.info("Read allegiance data for {} users out of {}", count, numberOfPlayersAnalyzed.get());
			}

		});
		
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
				numberOfPlayersAnalyzed.getAndIncrement();
			}
		}
		
		allegianceEvents.stream().forEach(event -> log.info("Found event from Dump: {} with data: {}", event.getEventType().getEventStringName(), event.toString()));
		this.eventRouter.sendAllDataToQueues(allegianceEvents);
		
		Date endDate = new Date();
		BatchDataEntry newBatchDataEntry = new BatchDataEntry(BatchDataEntryType.ALLEGIANCE, numberOfPlayersAnalyzed.get(), numberOfPlayersUpdated, startDate, endDate);
		this.writeToBatchDataEntryRepo(newBatchDataEntry);
		log.info("allegiances cache update complete.");
		
		return;
	}
	
	public void updateAllSkills() {
		Date startDate = new Date();
		BatchDataEntry previousBatchDataEntry = this.batchDataEntryRepo.getLastestBatchDataEntryForBatchEntryType(BatchDataEntryType.USERSKILL);
		final AtomicInteger playersAnalyzed = new AtomicInteger();
		final AtomicInteger playersUpdated = new AtomicInteger(0);
		try {
			log.info("updating user and prestige skills caches");
			Set<String> userSkillPlayersFromDump = this.dumpDataProvider.getPlayersForUserSkillsDump(); //use the larger set of names from the leaderboard
			Set<String> prestigeSkillPlayers = Sets.newConcurrentHashSet(this.dumpDataProvider.getPlayersForPrestigeSkillsDump()); //use the larger set of names from the leaderboard
			
			//filter userSkillPlayers by lastActiveDate
			final Set<String> activeUserSkillPlayers = Sets.newConcurrentHashSet(this.filterPlayerListToActiveUsers(userSkillPlayersFromDump, previousBatchDataEntry));
			playersAnalyzed.set(userSkillPlayersFromDump.size());
			
			AtomicInteger count = new AtomicInteger(0);
			//assume all players with prestige skills have user skills
			activeUserSkillPlayers.parallelStream().forEach(player -> {
				try {
					this.handlePlayerSkillUpdate(player, activeUserSkillPlayers, prestigeSkillPlayers);
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
			BatchDataEntry newBatchDataEntry = new BatchDataEntry(BatchDataEntryType.USERSKILL, playersAnalyzed.get(), playersUpdated.get(), startDate, endDate, e.getClass().toString(), e.getStackTrace()[0].getLineNumber());
			this.writeToBatchDataEntryRepo(newBatchDataEntry);
			this.errorWebhookManager.sendException(e, "error in Update Skills batch job");
			return;
		}
		Date endDate = new Date();
		BatchDataEntry newBatchDataEntry = new BatchDataEntry(BatchDataEntryType.USERSKILL, playersAnalyzed.get(), playersUpdated.get(), startDate, endDate);
		this.writeToBatchDataEntryRepo(newBatchDataEntry);
		log.info("user and prestige skill cache updates complete");
	}
	
	public void updateAllClassBonuses() {
		Date startDate = new Date();
		BatchDataEntry previousBatchDataEntry = this.batchDataEntryRepo.getLastestBatchDataEntryForBatchEntryType(BatchDataEntryType.CLASS_BONUS);
		int playersAnalyzed = 0;
		int playersUpdated = 0;
		try {
			log.info("updating class bonuses caches");
			Set<String> classBonusPlayers = this.dumpDataProvider.getPlayersForClassBonusDump();

			classBonusPlayers = this.filterPlayerListToActiveUsers(classBonusPlayers, previousBatchDataEntry);
			playersAnalyzed = classBonusPlayers.size();
			
			int count = 0;
			for(String player: classBonusPlayers) {
				Set<String> currentClassBonuses = this.dumpDataProvider.getClassBonus(player);
				currentClassBonuses = ClassBonus.convertToBotOutput(currentClassBonuses); //convert to bot output
				this.dumpService.getClassBonusCache().put(player, currentClassBonuses);
				ClassBonusEvent eventToSendToRepo = new ClassBonusEvent(player, currentClassBonuses);
				this.eventRouter.sendDataToQueues(eventToSendToRepo);
				
				playersUpdated++; count++;
				if(count % 20 == 0) {
					log.info("Read class bonus data for {} users out of {}", count, playersAnalyzed);
				}
			}
		} catch(DumpException e) {
			log.error("Error updating class bonus", e);
			this.errorWebhookManager.sendException(e, "Error updating class bonus");
			return;
		} catch(Exception e) {
			log.error("Error updating class bonus", e);
			this.errorWebhookManager.sendException(e, "Error updating class bonus");
			return;
		}
		log.info("updating class bonuses caches successful");
		Date endDate = new Date();
		BatchDataEntry newBatchDataEntry = new BatchDataEntry(BatchDataEntryType.CLASS_BONUS, playersAnalyzed, playersUpdated, startDate, endDate);
		this.writeToBatchDataEntryRepo(newBatchDataEntry);
	}
	
	public void updateAllSkillBonuses() {
		Date startDate = new Date();
		BatchDataEntry previousBatchDataEntry = this.batchDataEntryRepo.getLastestBatchDataEntryForBatchEntryType(BatchDataEntryType.SKILL_BONUS);
		int playersAnalyzed = 0;
		int playersUpdated = 0;
		try {
			log.info("updating skill bonuses caches");
			Set<String> skillBonusPlayers = this.dumpDataProvider.getPlayersForSkillBonusDump();
			
			skillBonusPlayers = this.filterPlayerListToActiveUsers(skillBonusPlayers, previousBatchDataEntry);
			playersAnalyzed = skillBonusPlayers.size();
			
			int count = 0;
			for(String player: skillBonusPlayers) {
				Set<String> currentSkillBonuses = this.dumpDataProvider.getSkillBonus(player);
				this.dumpService.getSkillBonusCache().put(player, currentSkillBonuses);
				SkillBonusEvent eventToSendToRepo = new SkillBonusEvent(player, currentSkillBonuses);
				this.eventRouter.sendDataToQueues(eventToSendToRepo);
				
				playersUpdated++; count++;
				if(count % 20 == 0) {
					log.info("Read skill bonus data for {} users out of {}", count, playersAnalyzed);
				}
			}
		} catch(DumpException e) {
			log.error("Error updating skill bonus", e);
			this.errorWebhookManager.sendException(e, "Error updating skill bonus");
			return;
		} catch(Exception e) {
			log.error("Error updating class bonus", e);
			this.errorWebhookManager.sendException(e, "Error updating class bonus");
			return;
		}
		log.info("updating skill bonuses cache successful");
		Date endDate = new Date();
		BatchDataEntry newBatchDataEntry = new BatchDataEntry(BatchDataEntryType.SKILL_BONUS, playersAnalyzed, playersUpdated, startDate, endDate);
		this.writeToBatchDataEntryRepo(newBatchDataEntry);
	}
	
	public void handlePlayerSkillUpdate(String player, Set<String> userSkillPlayers, Set<String> prestigeSkillPlayers) throws DumpException, TournamentApiException {
		PlayerSkillRefresh refresh = new PlayerSkillRefresh(player);
		//delete all skills from cache
		this.dumpService.getUserSkillsCache().remove(player);
		
		//get user skills
		List<PlayerSkills> userPlayerSkills = this.dumpDataProvider.getSkillsForPlayer(player);
		this.monsterUtils.categorizeSkillsList(userPlayerSkills);
		this.monsterUtils.regulateMonsterSkillCooldowns(userPlayerSkills);
		List<String> userSkills = PlayerSkills.convertToListOfSkillStrings(userPlayerSkills);
		
		//store user skills
		this.dumpService.getUserSkillsCache().put(player, userSkills);
		PlayerSkillEvent userSkillsEvent = new PlayerSkillEvent(userPlayerSkills, player);
		refresh.setPlayerSkillEvent(userSkillsEvent);
	
		List<String> prestigeSkills = null;
		if(prestigeSkillPlayers.contains(player)) {
			try {
				//attempt to get prestige skills, this is allowed to fail meaning this player has no prestige
				prestigeSkills = this.dumpDataProvider.getPrestigeSkillsForPlayer(player);
			} catch(Exception e) {
				log.warn("Player {} does not have prestige", player);
			}
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
	
	public void forceScheduleAllegianceBatch() {
		this.forceSchedule(new AllegianceTask(this));
	}
	
	public void forceScheduleBotListTask() {
		this.forceSchedule(new BotListTask(this));
	}
	
	public void forceSchedulePortraitsBatch() {
		this.forceSchedule(new PortraitsTask(this));
	}
	
	public void forceScheduleUserSkillsTask() {
		this.forceSchedule(new UserSkillsTask(this));
	}
	
	public void forceScheduleClassBonusTask() {
		this.forceSchedule(new ClassBonusTask(this));
	}
	
	public void forceScheduleSkillBonusTask() {
		this.forceSchedule(new SkillBonusTask(this));
	}
	
	public void forceScheduledBadAccountsTask() {
		this.forceSchedule(new BadAccountsTask(this));
	}
	
	protected void forceSchedule(DumpScheduledTask task) {
		this.forceTimer.schedule(task, 0);
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
			.filter(player -> this.dumpService.getLastActiveCache().containsKey(player) || this.dumpService.getLastFightActiveCache().containsKey(player))
			.filter(player -> this.dumpService.getLastActiveCache().get(player) != null || this.dumpService.getLastFightActiveCache().get(player) != null)
			.filter(player -> {
				Date lastActiveDate = this.dumpService.getLastActiveCache().get(player);
				Date lastFightActiveDate = this.dumpService.getLastFightActiveCache().get(player);
				
				boolean lastActiveBeforeLastSuccessfulRun = false;
				if(this.dumpService.getLastActiveCache().get(player) != null) {
					lastActiveBeforeLastSuccessfulRun = lastActiveDate.compareTo(compareToPerviousUpdateComplete) > 0 || compareToPerviousUpdateComplete == null;
				}
				
				boolean lastFightActiveBeforeLastSuccessfulRun = false;
				if(this.dumpService.getLastFightActiveCache().get(player) != null) {
					lastFightActiveBeforeLastSuccessfulRun = lastFightActiveDate.compareTo(compareToPerviousUpdateComplete) > 0 || compareToPerviousUpdateComplete == null;
				}
				
				boolean beforeSuccessfulRun = false;
				if(lastFightActiveDate != null && lastActiveDate != null) {
					int compareResult = lastActiveDate.compareTo(lastFightActiveDate); //greater than 0 means lastActive is after lastFightActive
					
					if(compareResult == 0) {
						beforeSuccessfulRun = lastActiveBeforeLastSuccessfulRun; //if equal somehow just use the last active
					} else if(compareResult == -1) {
						beforeSuccessfulRun = lastFightActiveBeforeLastSuccessfulRun; //this means fight active is more recent
					} else {
						beforeSuccessfulRun = lastActiveBeforeLastSuccessfulRun; //this means last active is more recent than fight active
					}
				} else if(lastActiveDate != null) {
					beforeSuccessfulRun = lastActiveBeforeLastSuccessfulRun;
				} else if(lastFightActiveDate != null) {
					beforeSuccessfulRun = lastFightActiveBeforeLastSuccessfulRun;
				} else {
					beforeSuccessfulRun = false;
				}
				
				return beforeSuccessfulRun;
			})
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

class BadAccountsTask extends DumpScheduledTask {
	public BadAccountsTask(DumpScheduledTasks dumpScheduledTasks) {super(dumpScheduledTasks); }
	protected void task() {this.dumpScheduledTasksRef.generateOutOfSyncPlayerRecordsFile();}
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

class ClassBonusTask extends DumpScheduledTask {
	public ClassBonusTask(DumpScheduledTasks dumpScheduledTasks) {super(dumpScheduledTasks);}
	protected void task() {this.dumpScheduledTasksRef.updateAllClassBonuses();}
}

class SkillBonusTask extends DumpScheduledTask {
	public SkillBonusTask(DumpScheduledTasks dumpScheduledTasks) {super(dumpScheduledTasks);}
	protected void task() {this.dumpScheduledTasksRef.updateAllSkillBonuses();}
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
