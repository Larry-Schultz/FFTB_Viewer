package fft_battleground.dump;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import fft_battleground.dump.cache.AllegianceCacheTask;
import fft_battleground.dump.cache.BalanceCacheTask;
import fft_battleground.dump.cache.ClassBonusCacheTask;
import fft_battleground.dump.cache.ExpCacheTask;
import fft_battleground.dump.cache.LastActiveCacheTask;
import fft_battleground.dump.cache.LastFightActiveCacheTask;
import fft_battleground.dump.cache.LeaderboardBuilder;
import fft_battleground.dump.cache.MusicBuilder;
import fft_battleground.dump.cache.PortraitCacheTask;
import fft_battleground.dump.cache.PrestigeSkillsCacheTask;
import fft_battleground.dump.cache.SkillBonusCacheTask;
import fft_battleground.dump.cache.SnubCacheTask;
import fft_battleground.dump.cache.SoftDeleteBuilder;
import fft_battleground.dump.cache.UserSkillsCacheTask;
import fft_battleground.event.detector.model.ExpEvent;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.repo.model.PlayerRecord;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DumpCacheBuilder {
	private static final int THREAD_POOL_COUNT = 7;
	
	private ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_COUNT);
	
	private DumpService dumpService;
	
	public DumpCacheBuilder(DumpService dumpService) {
		this.dumpService = dumpService;
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
		Future<Map<String, List<String>>> prestigeSkillsCacheTaskFuture = this.threadPool.submit(new PrestigeSkillsCacheTask(playerRecords));
		Future<Map<String, Set<String>>> classBonusCacheTaskFuture = this.threadPool.submit(new ClassBonusCacheTask(playerRecords, dumpService.getClassBonusRepo()));
		Future<Map<String, Set<String>>> skillBonusCacheTaskFuture = this.threadPool.submit(new SkillBonusCacheTask(playerRecords, dumpService.getSkillBonusRepo()));
		
		Future<Set<String>> softDeleteCacheBuilderFuture = this.threadPool.submit(new SoftDeleteBuilder(this.dumpService));

		this.dumpService.setBalanceCache(balanceCacheTaskFuture.get());
		this.dumpService.setExpCache(expCacheTaskFuture.get());
		this.dumpService.setSnubCache(snubMapTaskFuture.get());
		this.dumpService.setLastActiveCache(lastActiveTaskFuture.get());
		this.dumpService.setLastActiveCache(lastFightActiveTaskFuture.get());
		this.dumpService.setPortraitCache(portraitCacheTaskFuture.get());
		this.dumpService.setAllegianceCache(allegianceCacheTaskFuture.get());
		this.dumpService.setUserSkillsCache(userSkillsCacheTaskFuture.get());
		this.dumpService.setPrestigeSkillsCache(prestigeSkillsCacheTaskFuture.get());
		this.dumpService.setClassBonusCache(classBonusCacheTaskFuture.get());
		this.dumpService.setSkillBonusCache(skillBonusCacheTaskFuture.get());
		this.dumpService.setSoftDeleteCache(softDeleteCacheBuilderFuture.get());
		log.info("finished loading player cache");

	}
	
	@SneakyThrows
	public void buildLeaderboard() {
		this.threadPool.submit(new LeaderboardBuilder(this.dumpService));
	}
	
	@SneakyThrows
	public void buildPlaylist() {
		this.threadPool.submit(new MusicBuilder(this.dumpService));
	}
	
}

