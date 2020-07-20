package fft_battleground.dump;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

import fft_battleground.botland.model.SkillType;
import fft_battleground.event.model.ExpEvent;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.repo.PlayerRecordRepo;
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
		/*
		 * List<String> playerNames = new
		 * ArrayList<String>(this.dumpService.getDumpDataProvider().getHighExpDump().
		 * keySet()); List<List<String>> playerNamePartitions =
		 * Lists.partition(playerNames, playerNames.size());
		 * List<Future<List<PlayerRecord>>> partitionPlayerRecordFutures = new
		 * ArrayList<>(); for(List<String> partition: playerNamePartitions) {
		 * Future<List<PlayerRecord>> playerRecordFuture = this.threadPool.submit(new
		 * PlayerRecordFindTask(partition, this.dumpService.getPlayerRecordRepo()));
		 * partitionPlayerRecordFutures.add(playerRecordFuture); }
		 * 
		 * List<PlayerRecord> playerRecords = new ArrayList<>();
		 * for(Future<List<PlayerRecord>> future : partitionPlayerRecordFutures) {
		 * List<PlayerRecord> records = future.get(); playerRecords.addAll(records); }
		 */
		log.info("finished loading player cache");
		
		Future<Map<String, Integer>> balanceCacheTaskFuture = this.threadPool.submit(new BalanceCacheTask(playerRecords));
		Future<Map<String, ExpEvent>> expCacheTaskFuture = this.threadPool.submit(new ExpCacheTask(playerRecords));
		Future<Map<String, Date>> lastActiveTaskFuture = this.threadPool.submit(new LastActiveCacheTask(playerRecords));
		Future<Map<String, String>> portraitCacheTaskFuture = this.threadPool.submit(new PortraitCacheTask(playerRecords));
		Future<Map<String, BattleGroundTeam>> allegianceCacheTaskFuture = this.threadPool.submit(new AllegianceCacheTask(playerRecords));
		Future<Map<String, List<String>>> userSkillsCacheTaskFuture = this.threadPool.submit(new UserSkillsCacheTask(playerRecords));
		Future<Map<String, List<String>>> prestigeSkillsCacheTaskFuture = this.threadPool.submit(new PrestigeSkillsCacheTask(playerRecords));

		this.dumpService.setBalanceCache(balanceCacheTaskFuture.get());
		this.dumpService.setExpCache(expCacheTaskFuture.get());
		this.dumpService.setLastActiveCache(lastActiveTaskFuture.get());
		this.dumpService.setPortraitCache(portraitCacheTaskFuture.get());
		this.dumpService.setAllegianceCache(allegianceCacheTaskFuture.get());
		this.dumpService.setUserSkillsCache(userSkillsCacheTaskFuture.get());
		this.dumpService.setPrestigeSkillsCache(prestigeSkillsCacheTaskFuture.get());

	}
	
	@SneakyThrows
	public void buildLeaderboard() {
		this.threadPool.submit(new LeaderboardBuilder(this.dumpService));
	}
	
}

class PlayerRecordFindTask 
implements Callable<List<PlayerRecord>> {
	
	private List<String> players;
	private PlayerRecordRepo playerRepo;
	
	public PlayerRecordFindTask(List<String> playerNames, PlayerRecordRepo playerRepo) {
		this.players = playerNames;
		this.playerRepo = playerRepo;
	}

	@Override
	public List<PlayerRecord> call() throws Exception {
		List<PlayerRecord> result = this.playerRepo.findAllById(players);
		return result;
	}
}

abstract class CacheTask {
	protected List<PlayerRecord> playerRecords;
	
	public CacheTask(List<PlayerRecord> playerRecords) {
		this.playerRecords = playerRecords;
	}
}

@Slf4j
class BalanceCacheTask 
extends CacheTask 
implements Callable<Map<String, Integer>> {

	public BalanceCacheTask(List<PlayerRecord> playerRecords) {
		super(playerRecords);
	}

	@Override
	public Map<String, Integer> call() throws Exception {
		Map<String, Integer> balanceCache = null;
		log.info("started loading balance cache");
		balanceCache = new ConcurrentHashMap<>(this.playerRecords.parallelStream().collect(Collectors.toMap(PlayerRecord::getPlayer, PlayerRecord::getLastKnownAmount)));
		log.info("finished loading balance cache");
		
		return balanceCache;
		
	}
}

@Slf4j
class ExpCacheTask
extends CacheTask
implements Callable<Map<String, ExpEvent>> {

	public ExpCacheTask(List<PlayerRecord> playerRecords) {
		super(playerRecords);

	}

	@Override
	public Map<String, ExpEvent> call() throws Exception {
		Map<String, ExpEvent> expCache;
		log.info("started loading exp cache");
		playerRecords.parallelStream().filter(playerRecord -> playerRecord.getLastKnownLevel() == null).forEach(playerRecord -> playerRecord.setLastKnownLevel((short) 1));
		expCache = playerRecords.parallelStream().map(playerRecord -> new ExpEvent(playerRecord.getPlayer(), playerRecord.getLastKnownLevel(), playerRecord.getLastKnownRemainingExp()))
							.collect(Collectors.toMap(ExpEvent::getPlayer, Function.identity()));
		log.info("finished loading exp cache");
		
		return expCache;
	}
	
}

@Slf4j
class LastActiveCacheTask
extends CacheTask
implements Callable<Map<String, Date>> {
	public static final String dateActiveFormatString = "EEE MMM dd HH:mm:ss z yyyy";
	
	public LastActiveCacheTask(List<PlayerRecord> playerRecords) {
		super(playerRecords);
	}

	@Override
	public Map<String, Date> call() throws Exception {
		Map<String, Date> lastActiveCache;
		
		log.info("started loading last active cache");
		playerRecords.parallelStream().filter(playerRecord -> playerRecord.getLastActive() == null).forEach(playerRecord -> {
			try {
				SimpleDateFormat dateFormatter = new SimpleDateFormat(dateActiveFormatString);
				playerRecord.setLastActive(dateFormatter.parse("Wed Jan 01 00:00:00 EDT 2020"));
			} catch (ParseException e) {
				log.error("error parsing date for lastActive", e);
			}
		});
		lastActiveCache = playerRecords.parallelStream().collect(Collectors.toMap(PlayerRecord::getPlayer, PlayerRecord::getLastActive));
		log.info("finished loading last active cache");
		
		return lastActiveCache;
	}
	
}

@Slf4j
class PortraitCacheTask
extends CacheTask
implements Callable<Map<String, String>> {
	
	public PortraitCacheTask(List<PlayerRecord> playerRecords) {
		super(playerRecords);
	}

	@Override
	public Map<String, String> call() throws Exception {
		Map<String, String> portraitCache;
		log.info("started loading portrait cache");
		playerRecords.parallelStream().filter(playerRecord -> playerRecord.getPortrait() == null).forEach(playerRecord -> playerRecord.setPortrait(""));
		portraitCache = playerRecords.parallelStream().collect(Collectors.toMap(PlayerRecord::getPlayer, PlayerRecord::getPortrait));
		log.info("finished loading portrait cache");
		
		return portraitCache;
	}
	
}

@Slf4j
class AllegianceCacheTask
extends CacheTask
implements Callable<Map<String, BattleGroundTeam>> {
	
	public AllegianceCacheTask(List<PlayerRecord> playerRecords) {
		super(playerRecords);
	}

	@Override
	public Map<String, BattleGroundTeam> call() throws Exception {
		log.info("started loading allegiance cache");
		playerRecords.parallelStream().filter(playerRecord -> playerRecord.getAllegiance() == null).forEach(playerRecord -> playerRecord.setAllegiance(BattleGroundTeam.NONE));
		Map<String, BattleGroundTeam> allegianceCache = playerRecords.parallelStream().collect(Collectors.toMap(PlayerRecord::getPlayer, PlayerRecord::getAllegiance));
		log.info("finished loading allegiance cache");
		
		return allegianceCache;
	}
	
}

@Slf4j
class UserSkillsCacheTask
extends CacheTask
implements Callable<Map<String, List<String>>> {
	
	public UserSkillsCacheTask(List<PlayerRecord> playerRecords) {
		super(playerRecords);
	}

	@Override
	public Map<String, List<String>> call() throws Exception {
		log.info("started loading user skills cache");
		playerRecords.parallelStream().filter(playerRecord -> playerRecord.getPlayerSkills() == null).forEach(playerRecord -> playerRecord.setPlayerSkills(new ArrayList<>()));
		Map<String, List<String>> userSkillsCache = playerRecords.parallelStream().collect(Collectors.toMap(PlayerRecord::getPlayer, 
				playerRecord -> playerRecord.getPlayerSkills().stream().filter(playerSkill -> playerSkill.getSkillType() == SkillType.USER)
				.map(playerSkill -> playerSkill.getSkill()).collect(Collectors.toList())
				));
		log.info("finished loading user skills cache");
		
		return userSkillsCache;
	}
	
}

@Slf4j
class PrestigeSkillsCacheTask
extends CacheTask
implements Callable<Map<String, List<String>>> {
	
	public PrestigeSkillsCacheTask(List<PlayerRecord> playerRecords) {
		super(playerRecords);
	}

	@Override
	public Map<String, List<String>> call() throws Exception {
		log.info("started loading prestige skills cache");
		Map<String, List<String>> prestigeSkillsCache = playerRecords.parallelStream().collect(Collectors.toMap(PlayerRecord::getPlayer, 
				playerRecord -> playerRecord.getPlayerSkills().stream().filter(playerSkill -> playerSkill.getSkillType() == SkillType.PRESTIGE)
				.map(playerSkill -> playerSkill.getSkill()).collect(Collectors.toList())
				));
		log.info("finished loading prestige skills cache");
		
		return prestigeSkillsCache;
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
		this.dumpServiceRef.getDumpDataProvider().getHighScoreDump();
		this.dumpServiceRef.getDumpDataProvider().getHighExpDump();

		// run this at startup so the leaderboard caches are pre-loaded (and don't cause
		// lag for the rest of the machine
		log.info("calling bot leaderboard");
		this.dumpServiceRef.getDumpReportsService().getBotLeaderboard();
		
		log.info("calling player leaderboard");
		this.dumpServiceRef.getDumpReportsService().getLeaderboard();
		
		log.info("leaderboard data cache complete");
	}
	
}

