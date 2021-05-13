package fft_battleground.dump;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

import fft_battleground.dump.cache.AllegianceCacheTask;
import fft_battleground.dump.cache.BalanceCacheTask;
import fft_battleground.dump.cache.CacheTask;
import fft_battleground.dump.cache.ExpCacheTask;
import fft_battleground.dump.cache.LastActiveCacheTask;
import fft_battleground.dump.cache.LastFightActiveCacheTask;
import fft_battleground.dump.cache.PortraitCacheTask;
import fft_battleground.dump.cache.PrestigeSkillsCacheTask;
import fft_battleground.dump.cache.UserSkillsCacheTask;
import fft_battleground.dump.reports.model.AllegianceLeaderboardWrapper;
import fft_battleground.dump.reports.model.BotLeaderboard;
import fft_battleground.dump.reports.model.ExpLeaderboard;
import fft_battleground.dump.reports.model.PlayerLeaderboard;
import fft_battleground.event.model.ExpEvent;
import fft_battleground.exception.DumpException;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.repo.BattleGroundCacheEntryKey;
import fft_battleground.repo.model.ClassBonus;
import fft_battleground.repo.model.PlayerRecord;
import fft_battleground.repo.model.SkillBonus;
import fft_battleground.repo.repository.ClassBonusRepo;
import fft_battleground.repo.repository.SkillBonusRepo;
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
		Future<Map<String, Set<String>>> classBonusCacheTaskFuture = this.threadPool.submit(new ClassBonusCacheTask(playerRecords, dumpService.getClassBonusRepo()));
		Future<Map<String, Set<String>>> skillBonusCacheTaskFuture = this.threadPool.submit(new SkillBonusCacheTask(playerRecords, dumpService.getSkillBonusRepo()));

		this.dumpService.setBalanceCache(balanceCacheTaskFuture.get());
		this.dumpService.setExpCache(expCacheTaskFuture.get());
		this.dumpService.setLastActiveCache(lastActiveTaskFuture.get());
		this.dumpService.setLastActiveCache(lastFightActiveTaskFuture.get());
		this.dumpService.setPortraitCache(portraitCacheTaskFuture.get());
		this.dumpService.setAllegianceCache(allegianceCacheTaskFuture.get());
		this.dumpService.setUserSkillsCache(userSkillsCacheTaskFuture.get());
		this.dumpService.setPrestigeSkillsCache(prestigeSkillsCacheTaskFuture.get());
		this.dumpService.setClassBonusCache(classBonusCacheTaskFuture.get());
		this.dumpService.setSkillBonusCache(skillBonusCacheTaskFuture.get());
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

@Slf4j
class MusicBuilder
implements Runnable {
	private DumpService dumpServiceRef;
	
	public MusicBuilder(DumpService dumpServiceRef) {
		this.dumpServiceRef = dumpServiceRef;
	}
	
	@Override
	public void run() {
		this.dumpServiceRef.setPlaylist();
	}
	
}

@Slf4j
class ClassBonusCacheTask
extends CacheTask
implements Callable<Map<String, Set<String>>> {

	private ClassBonusRepo classBonusRepoRef;
	
	public ClassBonusCacheTask(List<PlayerRecord> playerRecords, ClassBonusRepo classBonusRepoRef) {
		super(playerRecords);
		this.classBonusRepoRef = classBonusRepoRef;
	}
	
	@Override
	public Map<String, Set<String>> call() throws Exception {
		log.info("calling class bonus cache task");
		Map<String, Set<String>> map = new HashMap<>();
		
		for(PlayerRecord playerRecord: this.playerRecords) {
			String player = playerRecord.getPlayer();
			List<ClassBonus> skillBonuses = this.classBonusRepoRef.getClassBonusForPlayer(player);
			Set<String> playerClassBonusSet = skillBonuses.stream().map(classBonus -> classBonus.getClassName()).collect(Collectors.toSet());
			map.put(player, playerClassBonusSet);
		}
		log.info("class bonus cache task complete");
		
		return map;
	}
	
}

@Slf4j
class SkillBonusCacheTask
extends CacheTask
implements Callable<Map<String, Set<String>>> {

	private SkillBonusRepo skillBonusRepoRef;
	
	public SkillBonusCacheTask(List<PlayerRecord> playerRecords, SkillBonusRepo skillBonusRepoRef) {
		super(playerRecords);
		this.skillBonusRepoRef = skillBonusRepoRef;
	}

	@Override
	public Map<String, Set<String>> call() throws Exception {
		log.info("calling skill bonus cache task");
		Map<String, Set<String>> map = new HashMap<>();
		
		for(PlayerRecord playerRecord: this.playerRecords) {
			String player = playerRecord.getPlayer();
			List<SkillBonus> skillBonuses = this.skillBonusRepoRef.getSkillBonusForPlayer(player);
			Set<String> playerSkillSet = skillBonuses.stream().map(skillBonus -> skillBonus.getSkill()).collect(Collectors.toSet());
			map.put(player, playerSkillSet);
		}
		log.info("skill bonus cache task complete");
		
		return map;
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
		
		log.info("Searching for exp leaderboard data from database cache");
		ExpLeaderboard expLeaderboard = new ExpLeaderboard();
		expLeaderboard = this.readBattleGroundCacheEntryRepo(BattleGroundCacheEntryKey.EXPERIENCE_LEADERBOARD, this::deserializeExpLeaderboard);
		if(expLeaderboard != null) {
			log.info("Loading exp leaderboard data from database cache");
			this.dumpServiceRef.getDumpReportsService().getExpLeaderboardGenerator().getCache().put(BattleGroundCacheEntryKey.EXPERIENCE_LEADERBOARD.getKey(), expLeaderboard);
		} else {
			log.info("exp leaderboard data from database cache not found");
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
	
	@SneakyThrows
	protected ExpLeaderboard deserializeExpLeaderboard(String str) {
		ObjectMapper mapper = new ObjectMapper();
		ExpLeaderboard leaderboard = mapper.readValue(str, ExpLeaderboard.class);
		return leaderboard;
	}
}

