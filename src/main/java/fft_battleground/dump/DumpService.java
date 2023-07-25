package fft_battleground.dump;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.google.common.collect.MapDifference.ValueDifference;
import com.google.common.collect.Maps;

import fft_battleground.discord.WebhookManager;
import fft_battleground.dump.cache.startup.DumpCacheBuilder;
import fft_battleground.event.detector.model.BalanceEvent;
import fft_battleground.event.detector.model.ExpEvent;
import fft_battleground.event.detector.model.LastActiveEvent;
import fft_battleground.event.detector.model.SnubEvent;
import fft_battleground.event.detector.model.composite.OtherPlayerBalanceEvent;
import fft_battleground.event.detector.model.composite.OtherPlayerExpEvent;
import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.event.model.BattleGroundEventType;
import fft_battleground.exception.CacheBuildException;
import fft_battleground.exception.DumpException;
import fft_battleground.image.model.Images;
import fft_battleground.metrics.DetectorAuditManager;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.music.MusicService;
import fft_battleground.music.model.Music;
import fft_battleground.repo.RepoManager;
import fft_battleground.repo.model.PlayerRecord;
import fft_battleground.repo.repository.BattleGroundCacheEntryRepo;
import fft_battleground.repo.repository.ClassBonusRepo;
import fft_battleground.repo.repository.PlayerRecordRepo;
import fft_battleground.repo.repository.PlayerSkillRepo;
import fft_battleground.repo.repository.PrestigeSkillsRepo;
import fft_battleground.repo.repository.SkillBonusRepo;
import fft_battleground.repo.util.BalanceType;
import fft_battleground.repo.util.BalanceUpdateSource;
import fft_battleground.reports.BotlandLeaderboardReportGenerator;
import fft_battleground.reports.ReportGenerator;
import fft_battleground.scheduled.DumpScheduledTasksForceStartup;
import fft_battleground.scheduled.DumpScheduledTasksManager;
import fft_battleground.skill.SkillUtils;
import fft_battleground.tournament.TournamentService;
import fft_battleground.util.GambleUtil;
import fft_battleground.util.Router;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@CacheConfig(cacheNames = {"music"})
public class DumpService {
	// Wed Jan 01 00:00:00 EDT 2020
	public static final String dateActiveFormatString = "EEE MMM dd HH:mm:ss z yyyy";
	
	@Value("${fft_battleground.enableCache}")
	private boolean isCacheEnabled;
	
	@Value("${runBatchAtStartup}")
	private Boolean runBatch;
	
	@Autowired
	@Getter private DumpDataProvider dumpDataProvider;
	
	@Autowired
	@Getter private DumpScheduledTasksManager dumpScheduledTasks;
	
	@Autowired
	@Getter private DumpScheduledTasksForceStartup dumpScheduledTasksForceStartup;
	
	@Autowired
	@Getter private TournamentService tournamentService;
	
	@Autowired
	@Getter private Images images;
	
	@Autowired
	@Getter private SkillUtils monsterUtils;
	
	@Autowired
	@Getter private RepoManager repoManager;
	
	@Autowired
	@Getter private PlayerRecordRepo playerRecordRepo;
	
	@Autowired
	@Getter private PlayerSkillRepo playerSkillRepo;
	
	@Autowired
	@Getter private PrestigeSkillsRepo prestigeSkillsRepo;
	
	@Autowired
	@Getter private BattleGroundCacheEntryRepo battleGroundCacheEntryRepo;
	
	@Autowired
	@Getter private ClassBonusRepo classBonusRepo;
	
	@Autowired
	@Getter private SkillBonusRepo skillBonusRepo;
	
	@Autowired
	@Getter private Router<BattleGroundEvent> eventRouter;
	
	@Autowired
	@Getter private WebhookManager errorWebhookManager;
	
	@Autowired
	@Getter private DetectorAuditManager detectorAuditManager;
	
	@Autowired
	@Getter private MusicService musicService;
	
	@Autowired
	@Getter private List<ReportGenerator<?>> allReportGenerators;
	
	@Autowired
	@Getter private BotlandLeaderboardReportGenerator botlandLeaderboardReportGenerator;
	
	@Getter @Setter private Map<String, Integer> balanceCache = new ConcurrentHashMap<>();
	@Getter @Setter private Map<String, ExpEvent> expCache = new ConcurrentHashMap<>();
	@Getter @Setter private Map<String, Date> lastActiveCache = new ConcurrentHashMap<>();
	@Getter @Setter private Map<String, Integer> snubCache = new ConcurrentHashMap<>();
	@Getter @Setter private Map<String, Date> lastFightActiveCache = new ConcurrentHashMap<>();
	@Getter @Setter private Map<String, String> portraitCache = new ConcurrentHashMap<>();
	@Getter @Setter private Map<String, BattleGroundTeam> allegianceCache = new ConcurrentHashMap<>();
	@Getter @Setter private Map<String, List<String>> userSkillsCache = new ConcurrentHashMap<>();
	@Getter @Setter private Map<String, List<String>> prestigeSkillsCache = new ConcurrentHashMap<>();
	@Getter @Setter private Set<String> botCache;
	@Getter @Setter private Map<String, Set<String>> classBonusCache = new ConcurrentHashMap<>();
	@Getter @Setter private Map<String, Set<String>> skillBonusCache = new ConcurrentHashMap<>();
	@Getter @Setter private Set<String> softDeleteCache; //threadsafe
	
	@Getter private Map<String, Integer> leaderboard = new ConcurrentHashMap<>();
	@Getter private Map<Integer, String> expRankLeaderboardByRank = new ConcurrentHashMap<>();
	@Getter private Map<String, Integer> expRankLeaderboardByPlayer = new ConcurrentHashMap<>(); 
	
	@Getter private Collection<Music> musicCache = Collections.<Music>emptyList();
	
	public DumpService() {}
	
	@Transactional
	public void setUpCaches() throws CacheBuildException {
		if (this.isCacheEnabled) {
			this.loadCache();
		}
		
	}
	
	private void loadCache() throws CacheBuildException {
		log.info("loading player data cache");
		
		this.getBotlandLeaderboardReportGenerator().writeReport();
		
		List<PlayerRecord> playerRecords = this.playerRecordRepo.findAll();
		playerRecords.parallelStream().filter(playerRecord -> playerRecord.getLastKnownAmount() == null)
				.forEach(playerRecord -> playerRecord.setLastKnownAmount(GambleUtil.MINIMUM_BET));
		log.info("finished loading player cache");

		DumpCacheBuilder builder = new DumpCacheBuilder(this);
		builder.buildCache(playerRecords);
		builder.buildBotCache();

		if(this.runBatch != null && this.runBatch) {
			//this.dumpScheduledTasks.runAllUpdates();
		}
		
		builder.runStartupBuilders();

		log.info("player data cache load complete");
		
		this.dumpScheduledTasksForceStartup.forceSpecificDailyTasks();
		
		Date latestDate = this.getLatestActiveDate();
	}
	
	protected Date getLatestActiveDate() {
		List<Date> date = new ArrayList<Date>();
		date.addAll(this.lastActiveCache.values());
		date.addAll(this.lastFightActiveCache.values());
		Collections.sort(date, Collections.reverseOrder());
		Date latestDate = date.get(0);
		return latestDate;
	}
	
	public Collection<BattleGroundEvent> getBalanceUpdatesFromDumpService() throws DumpException {
		Collection<BattleGroundEvent> data = new LinkedList<BattleGroundEvent>();
		log.info("updating balance cache");
		Map<String, Integer> newBalanceDataFromDump = this.dumpDataProvider.getHighScoreDump();
		Map<String, ValueDifference<Integer>> balanceDelta = Maps.difference(this.balanceCache, newBalanceDataFromDump).entriesDiffering();
		OtherPlayerBalanceEvent otherPlayerBalance= new OtherPlayerBalanceEvent(BattleGroundEventType.OTHER_PLAYER_BALANCE, new ArrayList<BalanceEvent>());
		//find differences in balance
		for(String key: balanceDelta.keySet()) {
			BalanceEvent newEvent = new BalanceEvent(key, balanceDelta.get(key).rightValue(), BalanceType.DUMP, BalanceUpdateSource.DUMP);
			otherPlayerBalance.getOtherPlayerBalanceEvents().add(newEvent);
			//update cache with new data
			this.balanceCache.put(key, balanceDelta.get(key).rightValue());
		}
		data.add(otherPlayerBalance);
		
		//find missing players
		for(String key: newBalanceDataFromDump.keySet()) {
			if(!this.balanceCache.containsKey(key)) {
				BalanceEvent newEvent = new BalanceEvent(key, newBalanceDataFromDump.get(key), BalanceType.DUMP, BalanceUpdateSource.DUMP);
				otherPlayerBalance.getOtherPlayerBalanceEvents().add(newEvent);
				this.balanceCache.put(key, newBalanceDataFromDump.get(key));
			}
		}
		log.info("balance cache update complete");
		
		return data;
	}
	
	public Collection<BattleGroundEvent> getExpUpdatesFromDumpService() throws DumpException {
		Collection<BattleGroundEvent> data = new LinkedList<BattleGroundEvent>();
		
		log.info("updating exp cache");
		Map<String, ExpEvent> newExpDataFromDump = this.dumpDataProvider.getHighExpDump();
		Map<String, ValueDifference<ExpEvent>> expDelta = Maps.difference(this.expCache, newExpDataFromDump).entriesDiffering();
		OtherPlayerExpEvent expEvents = new OtherPlayerExpEvent(new ArrayList<ExpEvent>());
		//find difference in xp
		for(String key: expDelta.keySet()) {
			ExpEvent newEvent = expDelta.get(key).rightValue();
			expEvents.getExpEvents().add(newEvent);
			this.expCache.put(key, newEvent);
		}
		data.add(expEvents);
		
		//find missing players
		for(String key : newExpDataFromDump.keySet()) {
			if(!this.expCache.containsKey(key)) {
				ExpEvent newEvent = newExpDataFromDump.get(key);
				expEvents.getExpEvents().add(newEvent);
				this.expCache.put(key, newEvent);
			}
		}
		log.info("exp cache update complete");
		
		return data;
	}
	
	public Collection<BattleGroundEvent> getLastActiveUpdatesFromDumpService() throws DumpException {
		Collection<BattleGroundEvent> data = new LinkedList<BattleGroundEvent>();

		log.info("updating last active cache");
		Map<String, Date> newLastActiveFromDump = this.dumpDataProvider.getLastActiveDump();
		Map<String, ValueDifference<Date>> lastActiveDelta = Maps.difference(this.lastActiveCache, newLastActiveFromDump).entriesDiffering();
		//find difference in last Active
		for(String key: lastActiveDelta.keySet()) {
			LastActiveEvent newEvent = new LastActiveEvent(key, lastActiveDelta.get(key).rightValue());
			data.add(newEvent);
			//update cache with new data
			this.lastActiveCache.put(key, lastActiveDelta.get(key).rightValue());
		}
		
		//find missing players
		for(String key: newLastActiveFromDump.keySet()) {
			if(!this.lastActiveCache.containsKey(key)) {
				LastActiveEvent newEvent = new LastActiveEvent(key, newLastActiveFromDump.get(key));
				data.add(newEvent);
				this.lastActiveCache.put(key, newLastActiveFromDump.get(key));
			}
		}
		log.info("last active cache update complete");
		
		return data;
	}
	
	public Collection<BattleGroundEvent> getSnubUpdatesFromDumpService() throws DumpException {
		Collection<BattleGroundEvent> data = new LinkedList<BattleGroundEvent>();
		
		log.info("updating snub cache");
		Map<String, Integer> newSnubDataFromDump = this.dumpDataProvider.getSnubData();
		Map<String, ValueDifference<Integer>> snubDelta = Maps.difference(this.snubCache, newSnubDataFromDump).entriesDiffering();
		//find difference in snub
		for(String key: snubDelta.keySet()) {
			SnubEvent newEvent = new SnubEvent(key, snubDelta.get(key).rightValue());
			data.add(newEvent);
		}
		
		//find missing players
		for(String key: newSnubDataFromDump.keySet()) {
			if(!this.snubCache.containsKey(key)) {
				SnubEvent newEvent = new SnubEvent(key, newSnubDataFromDump.get(key));
				data.add(newEvent);
				this.snubCache.put(key, newSnubDataFromDump.get(key));
			}
		}
		log.info("snub data update complete");
		
		return data;
	}
	
	public Date getLastActiveDateFromCache(String player) {
		Date date = this.lastActiveCache.get(player);
		return date;
	}
	
	public Integer getBalanceFromCache(String player) {
		Integer balance = this.balanceCache.get(player);
		return balance;
	}
	
	public Set<String> getBotNames() {
		Set<String> botNames = this.botCache;
		return botNames;
	}
	
	public void updateBalanceCache(BalanceEvent event) {
		this.balanceCache.put(event.getPlayer(), event.getAmount());
	}
	
}
