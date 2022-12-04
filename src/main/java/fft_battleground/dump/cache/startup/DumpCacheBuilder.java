package fft_battleground.dump.cache.startup;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import fft_battleground.dump.DumpScheduledTasksManagerImpl;
import fft_battleground.dump.DumpService;
import fft_battleground.dump.cache.startup.task.AllegianceCacheTask;
import fft_battleground.dump.cache.startup.task.BalanceCacheTask;
import fft_battleground.dump.cache.startup.task.ClassBonusCacheTask;
import fft_battleground.dump.cache.startup.task.ExpCacheTask;
import fft_battleground.dump.cache.startup.task.LastActiveCacheTask;
import fft_battleground.dump.cache.startup.task.LastFightActiveCacheTask;
import fft_battleground.dump.cache.startup.task.MusicBuilder;
import fft_battleground.dump.cache.startup.task.PortraitCacheTask;
import fft_battleground.dump.cache.startup.task.PrestigeSkillsCacheTask;
import fft_battleground.dump.cache.startup.task.ReportBuilder;
import fft_battleground.dump.cache.startup.task.SkillBonusCacheTask;
import fft_battleground.dump.cache.startup.task.SnubCacheTask;
import fft_battleground.dump.cache.startup.task.SoftDeleteBuilder;
import fft_battleground.dump.cache.startup.task.UserSkillsCacheTask;
import fft_battleground.dump.scheduled.ScheduledTask;
import fft_battleground.dump.scheduled.daily.AllegianceDailyTask;
import fft_battleground.dump.scheduled.daily.BadAccountsDailyTask;
import fft_battleground.dump.scheduled.daily.BotListDailyTask;
import fft_battleground.dump.scheduled.daily.CheckCertificateDailyTask;
import fft_battleground.dump.scheduled.daily.ClassBonusDailyTask;
import fft_battleground.dump.scheduled.daily.PortraitsDailyTask;
import fft_battleground.dump.scheduled.daily.PrestigeSkillDailyTask;
import fft_battleground.dump.scheduled.daily.SkillBonusDailyTask;
import fft_battleground.dump.scheduled.daily.UserSkillsDailyTask;
import fft_battleground.event.detector.model.ExpEvent;
import fft_battleground.exception.CacheBuildException;
import fft_battleground.exception.DumpException;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.repo.model.PlayerRecord;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DumpCacheBuilder {
	private static final int THREAD_POOL_COUNT = 7;
	
	private ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_COUNT);
	
	private DumpService dumpService;
	private DumpScheduledTasksManagerImpl dumpScheduledTasks;
	
	public DumpCacheBuilder(DumpService dumpService) {
		this.dumpService = dumpService;
		this.dumpScheduledTasks = (DumpScheduledTasksManagerImpl) dumpService.getDumpScheduledTasks();
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
		Future<Map<String, List<String>>> prestigeSkillsCacheTaskFuture = this.threadPool.submit(new PrestigeSkillsCacheTask(playerRecords, dumpService.getPrestigeSkillsRepo()));
		Future<Map<String, Set<String>>> classBonusCacheTaskFuture = this.threadPool.submit(new ClassBonusCacheTask(playerRecords, dumpService.getClassBonusRepo()));
		Future<Map<String, Set<String>>> skillBonusCacheTaskFuture = this.threadPool.submit(new SkillBonusCacheTask(playerRecords, dumpService.getSkillBonusRepo()));
		
		Future<Set<String>> softDeleteCacheBuilderFuture = this.threadPool.submit(new SoftDeleteBuilder(this.dumpService));

		this.dumpService.setBalanceCache(balanceCacheTaskFuture.get());
		this.dumpService.setExpCache(expCacheTaskFuture.get());
		this.dumpService.setSnubCache(snubMapTaskFuture.get());
		this.dumpService.setLastActiveCache(lastActiveTaskFuture.get());
		this.dumpService.setLastFightActiveCache(lastFightActiveTaskFuture.get());
		this.dumpService.setPortraitCache(portraitCacheTaskFuture.get());
		this.dumpService.setAllegianceCache(allegianceCacheTaskFuture.get());
		this.dumpService.setUserSkillsCache(userSkillsCacheTaskFuture.get());
		this.dumpService.setPrestigeSkillsCache(prestigeSkillsCacheTaskFuture.get());
		this.dumpService.setClassBonusCache(classBonusCacheTaskFuture.get());
		this.dumpService.setSkillBonusCache(skillBonusCacheTaskFuture.get());
		this.dumpService.setSoftDeleteCache(softDeleteCacheBuilderFuture.get());
		log.info("finished loading player cache");
	}
	
	public void buildBotCache() throws CacheBuildException {
		log.info("started loading bot cache");
		try {
			Set<String> bots = this.dumpService.getDumpDataProvider().getBots();
			this.dumpService.setBotCache(bots);
		} catch (DumpException e) {
			log.error("error loading bot file");
			throw new CacheBuildException("error building bot file", e);
		}
		log.info("finished loading bot cache");
	}
	
	public void runStartupBuilders() {
		List<BuilderTask> builderTasks = new ArrayList<>();
		builderTasks.add(new ReportBuilder(this.dumpService));
		builderTasks.add(new MusicBuilder(this.dumpService));
		builderTasks.forEach(builderTask -> this.threadPool.submit(builderTask));
	}
	
	public void forceSpecificDailyTasks(DumpService dumpService) {
		//this.dumpScheduledTasks.forceScheduleAllegianceBatch();
		//.dumpScheduledTasks.forceScheduleUserSkillsTask(this, true);
		this.forceCertificateCheck(dumpService);
		//this.forceScheduleUserSkillsTask(false, dumpService);
		//this.forceScheduleClassBonusTask(dumpService);
		//this.forceScheduleSkillBonusTask(dumpService);
		this.forceScheduledPrestigeSkillTask(dumpService);
		//this.dumpScheduledTasks.forceScheduledBadAccountsTask();
		/*
		 * 
		 * 
		 * 
		 * 
		 */
	}
	
	protected void forceCertificateCheck(DumpService dumpService) {
		this.forceSchedule(new CheckCertificateDailyTask(this.dumpScheduledTasks, dumpService));
	}
	
	protected void forceScheduleAllegianceBatch(DumpService dumpService) {
		this.forceSchedule(new AllegianceDailyTask(this.dumpScheduledTasks, dumpService));
	}
	
	protected void forceScheduleBotListTask(DumpService dumpService) {
		this.forceSchedule(new BotListDailyTask(this.dumpScheduledTasks, dumpService));
	}
	
	protected void forceSchedulePortraitsBatch(DumpService dumpService) {
		this.forceSchedule(new PortraitsDailyTask(this.dumpScheduledTasks, dumpService));
	}
	
	protected void forceScheduleUserSkillsTask(boolean runAll, DumpService dumpService) {
		UserSkillsDailyTask task = new UserSkillsDailyTask(this.dumpScheduledTasks, dumpService);
		task.setCheckAllUsers(runAll);
		this.forceSchedule(task);
	}
	
	protected void forceScheduleClassBonusTask(DumpService dumpService) {
		this.forceSchedule(new ClassBonusDailyTask(this.dumpScheduledTasks, dumpService));
	}
	
	protected void forceScheduleSkillBonusTask(DumpService dumpService) {
		this.forceSchedule(new SkillBonusDailyTask(this.dumpScheduledTasks, dumpService));
	}
	
	protected void forceScheduledBadAccountsTask(DumpService dumpService) {
		this.forceSchedule(new BadAccountsDailyTask(this.dumpScheduledTasks, dumpService));
	}
	
	protected void forceScheduledPrestigeSkillTask(DumpService dumpService) {
		this.forceSchedule(new PrestigeSkillDailyTask(this.dumpScheduledTasks, dumpService));
	}
	
	protected void forceSchedule(ScheduledTask task) {
		this.threadPool.submit(task);
	}
	
}

