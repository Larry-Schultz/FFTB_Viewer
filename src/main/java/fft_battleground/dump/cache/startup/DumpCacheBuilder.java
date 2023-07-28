package fft_battleground.dump.cache.startup;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import fft_battleground.dump.DumpDataProvider;
import fft_battleground.dump.cache.map.AllegianceCache;
import fft_battleground.dump.cache.map.BalanceCache;
import fft_battleground.dump.cache.map.ClassBonusCache;
import fft_battleground.dump.cache.map.ExpCache;
import fft_battleground.dump.cache.map.LastActiveCache;
import fft_battleground.dump.cache.map.LastFightActiveCache;
import fft_battleground.dump.cache.map.PortraitCache;
import fft_battleground.dump.cache.map.PrestigeSkillsCache;
import fft_battleground.dump.cache.map.SkillBonusCache;
import fft_battleground.dump.cache.map.SnubCache;
import fft_battleground.dump.cache.map.UserSkillsCache;
import fft_battleground.dump.cache.set.BotCache;
import fft_battleground.dump.cache.set.SoftDeleteCache;
import fft_battleground.dump.cache.startup.builder.BotCacheBuilder;
import fft_battleground.dump.cache.startup.builder.MusicBuilder;
import fft_battleground.dump.cache.startup.builder.ReportBuilder;
import fft_battleground.dump.cache.startup.builder.SoftDeleteBuilder;
import fft_battleground.dump.cache.startup.task.AllegianceCacheTask;
import fft_battleground.dump.cache.startup.task.BalanceCacheTask;
import fft_battleground.dump.cache.startup.task.ClassBonusCacheTask;
import fft_battleground.dump.cache.startup.task.ExpCacheTask;
import fft_battleground.dump.cache.startup.task.LastActiveCacheTask;
import fft_battleground.dump.cache.startup.task.LastFightActiveCacheTask;
import fft_battleground.dump.cache.startup.task.PortraitCacheTask;
import fft_battleground.dump.cache.startup.task.PrestigeSkillsCacheTask;
import fft_battleground.dump.cache.startup.task.SkillBonusCacheTask;
import fft_battleground.dump.cache.startup.task.SnubCacheTask;
import fft_battleground.dump.cache.startup.task.UserSkillsCacheTask;
import fft_battleground.event.detector.model.ExpEvent;
import fft_battleground.exception.CacheBuildException;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.music.MusicService;
import fft_battleground.repo.model.PlayerRecord;
import fft_battleground.repo.repository.ClassBonusRepo;
import fft_battleground.repo.repository.PlayerRecordRepo;
import fft_battleground.repo.repository.PrestigeSkillsRepo;
import fft_battleground.repo.repository.SkillBonusRepo;
import fft_battleground.reports.BotlandLeaderboardReportGenerator;
import fft_battleground.reports.ReportGenerator;
import fft_battleground.scheduled.DumpScheduledTasksForceStartup;
import fft_battleground.util.GambleUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DumpCacheBuilder {
	private static final int THREAD_POOL_COUNT = 7;
	
	@Value("${fft_battleground.enableCache}")
	private boolean isCacheEnabled;
	
	@Value("${runBatchAtStartup}")
	private Boolean runBatch;
	
	@Autowired
	private DumpDataProvider dumpDataProvider;
	
	@Autowired
	private DumpScheduledTasksForceStartup dumpScheduledTasksForceStartup;
	
	@Autowired
	private BotlandLeaderboardReportGenerator botlandLeaderboardReportGenerator;
	
	@Autowired
	private PlayerRecordRepo playerRecordRepo;
	
	@Autowired
	private PrestigeSkillsRepo prestigeSkillsRepo;
	
	@Autowired
	private ClassBonusRepo classBonusRepo;
	
	@Autowired
	private SkillBonusRepo skillBonusRepo;
	
	@Autowired
	private MusicService musicService;
	
	@Autowired
	private List<ReportGenerator<?>> allReportGenerators;
	
	@Autowired
	private BalanceCache balanceCache;
	
	@Autowired
	private ExpCache expCache;
	
	@Autowired
	private LastActiveCache lastActiveCache;
	
	@Autowired
	private SnubCache snubCache;
	
	@Autowired
	private LastFightActiveCache lastFightActiveCache;
	
	@Autowired
	private PortraitCache portraitCache;
	
	@Autowired
	private AllegianceCache allegianceCache;
	
	@Autowired
	private UserSkillsCache userSkillsCache;
	
	@Autowired
	private PrestigeSkillsCache prestigeSkillsCache;
	
	@Autowired
	private ClassBonusCache classBonusCache;
	
	@Autowired
	private SkillBonusCache skillBonusCache;
	
	@Autowired
	private BotCache botCache;
	
	@Autowired
	private SoftDeleteCache softDeleteCache;
	
	private ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_COUNT);
	
	@Transactional
	public void setUpCaches() throws CacheBuildException {
		if (this.isCacheEnabled) {
			this.loadCache();
		}
		
	}
	
	private void loadCache() throws CacheBuildException {
		log.info("loading player data cache");
		
		this.botlandLeaderboardReportGenerator.writeReport();
		
		List<PlayerRecord> playerRecords = this.playerRecordRepo.findAll();
		playerRecords.parallelStream().filter(playerRecord -> playerRecord.getLastKnownAmount() == null)
				.forEach(playerRecord -> playerRecord.setLastKnownAmount(GambleUtil.MINIMUM_BET));
		log.info("finished loading player cache");

		this.buildCache(playerRecords);
		this.runStartupBuilders();

		log.info("player data cache load complete");
		
		this.dumpScheduledTasksForceStartup.forceSpecificDailyTasks();
	}
	
	@SneakyThrows
	public void buildCache(List<PlayerRecord> playerRecords) {
		log.info("loading player data cache");
		
		
		Future<Map<String, Integer>> balanceCacheTaskFuture = this.threadPool.submit(new BalanceCacheTask(playerRecords));
		Future<Map<String, ExpEvent>> expCacheTaskFuture = this.threadPool.submit(new ExpCacheTask(playerRecords));
		Future<Map<String, Integer>> snubMapTaskFuture = this.threadPool.submit(new SnubCacheTask(playerRecords));
		Future<Map<String, Date>> lastActiveTaskFuture = this.threadPool.submit(new LastActiveCacheTask(playerRecords));
		Future<Map<String, Date>> lastFightActiveTaskFuture = this.threadPool.submit(new LastFightActiveCacheTask(playerRecords));
		Future<Map<String, String>> portraitCacheTaskFuture = this.threadPool.submit(new PortraitCacheTask(playerRecords));
		Future<Map<String, BattleGroundTeam>> allegianceCacheTaskFuture = this.threadPool.submit(new AllegianceCacheTask(playerRecords));
		Future<Map<String, List<String>>> userSkillsCacheTaskFuture = this.threadPool.submit(new UserSkillsCacheTask(playerRecords));
		Future<Map<String, List<String>>> prestigeSkillsCacheTaskFuture = this.threadPool.submit(new PrestigeSkillsCacheTask(playerRecords, this.prestigeSkillsRepo));
		Future<Map<String, Set<String>>> classBonusCacheTaskFuture = this.threadPool.submit(new ClassBonusCacheTask(playerRecords, this.classBonusRepo));
		Future<Map<String, Set<String>>> skillBonusCacheTaskFuture = this.threadPool.submit(new SkillBonusCacheTask(playerRecords, this.skillBonusRepo));
		
		Future<Set<String>> softDeleteCacheBuilderFuture = this.threadPool.submit(new SoftDeleteBuilder(this.playerRecordRepo));

		this.balanceCache.bulkLoad(balanceCacheTaskFuture.get());
		this.expCache.bulkLoad(expCacheTaskFuture.get());
		this.snubCache.bulkLoad(snubMapTaskFuture.get());
		this.lastActiveCache.bulkLoad(lastActiveTaskFuture.get());
		this.lastFightActiveCache.bulkLoad(lastFightActiveTaskFuture.get());
		this.portraitCache.bulkLoad(portraitCacheTaskFuture.get());
		this.allegianceCache.bulkLoad(allegianceCacheTaskFuture.get());
		this.userSkillsCache.bulkLoad(userSkillsCacheTaskFuture.get());
		this.prestigeSkillsCache.bulkLoad(prestigeSkillsCacheTaskFuture.get());
		this.classBonusCache.bulkLoad(classBonusCacheTaskFuture.get());
		this.skillBonusCache.bulkLoad(skillBonusCacheTaskFuture.get());
		this.softDeleteCache.reload(softDeleteCacheBuilderFuture.get());
		log.info("finished loading player cache");
	}
	
	public void runStartupBuilders() {
		List<Runnable> builderTasks = new ArrayList<>();
		builderTasks.add(new ReportBuilder(this.allReportGenerators, this.dumpDataProvider));
		builderTasks.add(new MusicBuilder(this.musicService));
		builderTasks.add(new BotCacheBuilder(this.dumpDataProvider, this.botCache));
		builderTasks.forEach(builderTask -> this.threadPool.submit(builderTask));
	}
	
}

