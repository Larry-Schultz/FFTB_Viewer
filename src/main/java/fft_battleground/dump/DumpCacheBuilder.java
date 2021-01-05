package fft_battleground.dump;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;

import com.fasterxml.jackson.databind.ObjectMapper;

import fft_battleground.dump.cache.AllegianceCacheTask;
import fft_battleground.dump.cache.BalanceCacheTask;
import fft_battleground.dump.cache.ExpCacheTask;
import fft_battleground.dump.cache.LastActiveCacheTask;
import fft_battleground.dump.cache.LastFightActiveCacheTask;
import fft_battleground.dump.cache.PortraitCacheTask;
import fft_battleground.dump.cache.PrestigeSkillsCacheTask;
import fft_battleground.dump.cache.UserSkillsCacheTask;
import fft_battleground.dump.reports.model.AllegianceLeaderboardWrapper;
import fft_battleground.dump.reports.model.BotLeaderboard;
import fft_battleground.dump.reports.model.PlayerLeaderboard;
import fft_battleground.event.model.ExpEvent;
import fft_battleground.exception.DumpException;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.repo.BattleGroundCacheEntryKey;
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
		Future<Map<String, Date>> lastActiveTaskFuture = this.threadPool.submit(new LastActiveCacheTask(playerRecords));
		Future<Map<String, Date>> lastFightActiveTaskFuture = this.threadPool.submit(new LastFightActiveCacheTask(playerRecords));
		Future<Map<String, String>> portraitCacheTaskFuture = this.threadPool.submit(new PortraitCacheTask(playerRecords));
		Future<Map<String, BattleGroundTeam>> allegianceCacheTaskFuture = this.threadPool.submit(new AllegianceCacheTask(playerRecords));
		Future<Map<String, List<String>>> userSkillsCacheTaskFuture = this.threadPool.submit(new UserSkillsCacheTask(playerRecords));
		Future<Map<String, List<String>>> prestigeSkillsCacheTaskFuture = this.threadPool.submit(new PrestigeSkillsCacheTask(playerRecords));

		this.dumpService.setBalanceCache(balanceCacheTaskFuture.get());
		this.dumpService.setExpCache(expCacheTaskFuture.get());
		this.dumpService.setLastActiveCache(lastActiveTaskFuture.get());
		this.dumpService.setLastActiveCache(lastFightActiveTaskFuture.get());
		this.dumpService.setPortraitCache(portraitCacheTaskFuture.get());
		this.dumpService.setAllegianceCache(allegianceCacheTaskFuture.get());
		this.dumpService.setUserSkillsCache(userSkillsCacheTaskFuture.get());
		this.dumpService.setPrestigeSkillsCache(prestigeSkillsCacheTaskFuture.get());
		log.info("finished loading player cache");

	}
	
	@SneakyThrows
	public void buildLeaderboard() {
		this.threadPool.submit(new LeaderboardBuilder(this.dumpService));
	}
	
}

@Slf4j
class LeaderboardBuilder
implements Runnable {
	
	private DumpService dumpServiceRef;
	
	public LeaderboardBuilder(DumpService dumpService) {
		this.dumpServiceRef = dumpService;
	}
	
	@Override
	public void run() {
		// run this at startup so leaderboard data works properly
		log.info("pre-cache leaderboard data");
		try {
			this.dumpServiceRef.getDumpDataProvider().getHighScoreDump();
			this.dumpServiceRef.getDumpDataProvider().getHighExpDump();
		} catch(DumpException e) {
			log.error("error getting high score dump", e);
		}

		this.loadDatabaseData();
		
		this.runCacheRebuildFunctions();
		
		log.info("leaderboard data cache complete");
	}
	
	@SuppressWarnings("unchecked")
	protected void loadDatabaseData() {
		log.info("Searching for player leaderboard data from database cache");
		PlayerLeaderboard leaderboardCacheData = new PlayerLeaderboard();
		leaderboardCacheData = this.readBattleGroundCacheEntryRepo(BattleGroundCacheEntryKey.LEADERBOARD, this::deserializePlayerLeaderboard) ;
		if(leaderboardCacheData != null) {
			log.info("Loading player leaderboard data from database cache");
			this.dumpServiceRef.getDumpReportsService().getPlayerLeaderboardReportGenerator().getCache().put(BattleGroundCacheEntryKey.LEADERBOARD.getKey(), leaderboardCacheData);
		} else {
			log.info("Player leaderboard data from database cache not found");
		}
		
		log.info("Searching for bot leaderboard data from database cache");
		BotLeaderboard botLeaderboardCacheData = new BotLeaderboard();
		botLeaderboardCacheData = this.readBattleGroundCacheEntryRepo(BattleGroundCacheEntryKey.BOT_LEADERBOARD, this::deserializeBotLeaderboard);
		if(botLeaderboardCacheData != null) {
			log.info("Loading bot leaderboard data from database cache");
			this.dumpServiceRef.getDumpReportsService().getBotLeaderboardReportGenerator().getCache().put(BattleGroundCacheEntryKey.BOT_LEADERBOARD.getKey(), botLeaderboardCacheData);
		} else {
			log.info("bot leaderboard data from database cache not found");
		}
		
		log.info("Searching for bet percentiles data from database cache");
		Map<Integer, Double> betPercentilesCacheData = new HashMap<Integer, Double>();
		betPercentilesCacheData = this.readBattleGroundCacheEntryRepo(BattleGroundCacheEntryKey.BET_PERCENTILES, this::deserializeMapIntegerDouble);
		if(betPercentilesCacheData != null) {
			log.info("Loading bet percentiles data from database cache");
			this.dumpServiceRef.getDumpReportsService().getBetPercentileReportGenerator().getCache().put(BattleGroundCacheEntryKey.BET_PERCENTILES.getKey(), betPercentilesCacheData);
		} else {
			log.info("bet percentiles data from database cache not found");
		}
		
		log.info("Searching for fight percentiles data from database cache");
		Map<Integer, Double> fightPercentilesCacheData = new HashMap<Integer, Double>();
		fightPercentilesCacheData = this.readBattleGroundCacheEntryRepo(BattleGroundCacheEntryKey.FIGHT_PERCENTILES, this::deserializeMapIntegerDouble);
		if(fightPercentilesCacheData != null) {
			log.info("Loading fight percentiles data from database cache");
			this.dumpServiceRef.getDumpReportsService().getFightPercentileReportGenerator().getCache().put(BattleGroundCacheEntryKey.FIGHT_PERCENTILES.getKey(), fightPercentilesCacheData);
		} else {
			log.info("fight percentiles data from database cache not found");
		}
		
		log.info("Searching for allegiance leaderboard data from database cache");
		AllegianceLeaderboardWrapper wrapper = new AllegianceLeaderboardWrapper();
		wrapper = this.readBattleGroundCacheEntryRepo(BattleGroundCacheEntryKey.ALLEGIANCE_LEADERBOARD, this::deserializeAllegianceLeaderboard);
		if(wrapper != null) {
			log.info("Loading allegiance leaderboard data from database cache");
			this.dumpServiceRef.getDumpReportsService().getAllegianceReportGenerator().getCache().put(BattleGroundCacheEntryKey.ALLEGIANCE_LEADERBOARD.getKey(), wrapper);
		} else {
			log.info("allegiance ledaerboard data from database cache not found");
		}
		
		return;
	}
	
	protected void runCacheRebuildFunctions() {
		this.dumpServiceRef.getDumpScheduledTasks().runCacheUpdates();
	}
	
	protected <T> T readBattleGroundCacheEntryRepo(BattleGroundCacheEntryKey key, Function<String, T> deserializer) {
		T result = null;
		try {
			result = this.dumpServiceRef.getBattleGroundCacheEntryRepo().readCacheEntry(key, deserializer);
		} catch(Exception e) {
			log.error(e.getMessage(), e);
		}
		
		return result;
	}
	
	@SneakyThrows
	protected PlayerLeaderboard deserializePlayerLeaderboard(String str) {
		ObjectMapper mapper = new ObjectMapper();
		PlayerLeaderboard leaderboard = mapper.readValue(str, PlayerLeaderboard.class);
		return leaderboard;
	}
	
	@SneakyThrows
	protected BotLeaderboard deserializeBotLeaderboard(String str) {
		ObjectMapper mapper = new ObjectMapper();
		BotLeaderboard leaderboard = mapper.readValue(str,  BotLeaderboard.class);
		return leaderboard;
	}
	
	@SneakyThrows
	protected Map<String, Integer> deserializeMapStringInteger(String str) {
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Integer> result = mapper.readValue(str, Map.class);
		return result;
	}
	
	@SneakyThrows
	protected Map<Integer, Double> deserializeMapIntegerDouble(String str) {
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Double> stringDoubleMap = mapper.readValue(str,  Map.class);
		
		Map<Integer, Double> result = new HashMap<>();
		for(Map.Entry<String, Double> stringDoubleMapEntry: stringDoubleMap.entrySet()) {
			Integer intKey = Integer.valueOf(stringDoubleMapEntry.getKey());
			result.put(intKey, stringDoubleMapEntry.getValue());
		}
		return result;
	}
	
	@SneakyThrows
	protected AllegianceLeaderboardWrapper deserializeAllegianceLeaderboard(String str) {
		ObjectMapper mapper = new ObjectMapper();
		AllegianceLeaderboardWrapper leaderboard = mapper.readValue(str, AllegianceLeaderboardWrapper.class);
		return leaderboard;
	}
}

