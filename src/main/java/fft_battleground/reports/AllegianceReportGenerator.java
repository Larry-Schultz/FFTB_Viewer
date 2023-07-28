package fft_battleground.reports;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import fft_battleground.discord.WebhookManager;
import fft_battleground.dump.DumpDataProvider;
import fft_battleground.dump.cache.DumpCacheManager;
import fft_battleground.dump.cache.map.AllegianceCache;
import fft_battleground.dump.cache.map.PortraitCache;
import fft_battleground.dump.cache.set.BotCache;
import fft_battleground.dump.service.BalanceHistoryServiceImpl;
import fft_battleground.dump.service.PlayerPageDataService;
import fft_battleground.exception.CacheBuildException;
import fft_battleground.exception.CacheMissException;
import fft_battleground.exception.DumpException;
import fft_battleground.image.model.Images;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.repo.model.PlayerRecord;
import fft_battleground.repo.repository.BattleGroundCacheEntryRepo;
import fft_battleground.repo.repository.PlayerRecordRepo;
import fft_battleground.repo.repository.PrestigeSkillsRepo;
import fft_battleground.repo.util.BattleGroundCacheEntryKey;
import fft_battleground.reports.model.AllegianceLeaderboard;
import fft_battleground.reports.model.AllegianceLeaderboardEntry;
import fft_battleground.reports.model.AllegianceLeaderboardWrapper;
import fft_battleground.tournament.ChampionService;
import fft_battleground.util.FunctionCallableListResult;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AllegianceReportGenerator extends AbstractReportGenerator<AllegianceLeaderboardWrapper> {
	private static final BattleGroundCacheEntryKey key = BattleGroundCacheEntryKey.ALLEGIANCE_LEADERBOARD;
	private static final String reportName = "Allegiance Leaderboard";
	private static int GIL_CAP = 0;
	
	static {
		try {
			GIL_CAP = NumberFormat.getNumberInstance(Locale.US).parse("1,000,000").intValue();
		} catch (ParseException e) {
			log.error("Error parsing GIL_CAP", e);
		}
	}
	
	@Autowired
	private DumpCacheManager dumpCacheManager;
	
	@Autowired
	private DumpDataProvider dumpDataProvider;
	
	@Autowired
	private BalanceHistoryServiceImpl balanceHistoryUtil;
	
	@Autowired
	private ChampionService championService;
	
	@Autowired
	private PrestigeSkillsRepo prestigeSkillsRepo;
	
	@Autowired
	private PlayerRecordRepo playerRecordRepo;
	
	@Autowired
	private PlayerPageDataService playerPageDataService;
	
	@Autowired
	private Images images;
	
	@Autowired
	private AllegianceCache allegianceCache;
	
	@Autowired
	private PortraitCache portraitCache;
	
	@Autowired
	private BotCache botCache;
		
	public AllegianceReportGenerator(BattleGroundCacheEntryRepo battleGroundCacheEntryRepo, WebhookManager errorWebhookManager, 
			Timer battlegroundCacheTimer) {
		super(key, reportName, battleGroundCacheEntryRepo, errorWebhookManager, battlegroundCacheTimer);
	}

	@Override
	public AllegianceLeaderboardWrapper generateReport() throws CacheBuildException {
		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(20);

		Map<BattleGroundTeam, Map<String, Integer>> allegianceData = new ConcurrentHashMap<>();

		List<BattleGroundTeam> teams = BattleGroundTeam.coreTeams();
		teams.stream().forEach(team -> allegianceData.put(team, new ConcurrentHashMap<String, Integer>()));

		Map<String, Integer> highScoreDataFromDump;
		try {
			highScoreDataFromDump = this.dumpDataProvider.getHighScoreDump();
		} catch (DumpException e) {
			log.error("Error getting high score data from the dump", e);
			CacheBuildException exception = new CacheBuildException("Error getting high score data from the dump has stopped the generation of allegiance data", e);
			this.errorWebhookManager.sendException(exception, "Error getting high score data from the dump has stopped the generation of allegiance data");
			throw exception;
		}

		Future<List<Optional<String>>> activeFilter = executor
				.submit(new FunctionCallableListResult<String, Optional<String>>(highScoreDataFromDump.keySet(),
						(playerName -> {
							Optional<String> result = null;
							Date lastActive = this.dumpCacheManager.getLastActiveDateFromCache(playerName);
							boolean active = this.balanceHistoryUtil.isPlayerActiveInLastMonth(lastActive);
							result = active ? Optional.<String>empty() : Optional.<String>of(playerName);
							return result;
						})));
		Future<List<Optional<String>>> botFilter = executor
				.submit(new FunctionCallableListResult<String, Optional<String>>(highScoreDataFromDump.keySet(),
						(playerName -> {
							Optional<String> result = null;
							boolean isBot = this.botCache.contains(playerName);
							result = isBot ? Optional.<String>of(playerName) : Optional.<String>empty();
							return result;
						})));
		Future<List<Optional<String>>> noAllegianceFilter = executor
				.submit(new FunctionCallableListResult<String, Optional<String>>(highScoreDataFromDump.keySet(),
						(playerName -> {
							boolean hasAllegiance = teams
									.contains(this.allegianceCache.get(playerName));
							Optional<String> result = hasAllegiance ? Optional.<String>empty()
									: Optional.<String>of(playerName);
							return result;
						})));

		Set<String> filteredPlayers = new HashSet<>();
		List<String> botFiltered = this.filterConversionFunction(botFilter);
		filteredPlayers.addAll(botFiltered);
		List<String> activeFiltered = this.filterConversionFunction(activeFilter);
		filteredPlayers.addAll(activeFiltered);
		List<String> noAllegianceFiltered = this.filterConversionFunction(noAllegianceFilter);
		filteredPlayers.addAll(noAllegianceFiltered);
		filteredPlayers.parallelStream().forEach(playerName -> highScoreDataFromDump.remove(playerName));

		Calendar currentTimeOfCompletion = Calendar.getInstance();

		log.debug("The currentTime is {}", currentTimeOfCompletion);

		highScoreDataFromDump.keySet().parallelStream().forEach(playerName -> {
			BattleGroundTeam allegiance = this.allegianceCache.get(playerName);
			allegianceData.get(allegiance).put(playerName, highScoreDataFromDump.get(playerName));
		});

		//apparently the database cache gets befuddled if too many threads try to run the query we use for the allegiance leaderboard
		Object playerRepoLock = new Object(); 
		
		// find player total
		// find top 5 players
		// determine the relative position of each team
		List<AllegianceLeaderboard> leaderboard = teams.parallelStream().map(team -> this.generateAllegianceLeaderboardByTeam(team, allegianceData, playerRepoLock))
				.collect(Collectors.toList());

		// determine the leaderboard order
		Collections.sort(leaderboard, Collections.reverseOrder());
		for (int i = 0; i < leaderboard.size(); i++) {
			leaderboard.get(i).setPosition(i + 1);
		}

		// determine gil underdog
		BattleGroundTeam gilUnderdog = BattleGroundTeam.NONE;
		int currentPlayerGilRatioToBeat = 0;
		for (AllegianceLeaderboard board : leaderboard) {
			if (board.getGilPerPlayer() > currentPlayerGilRatioToBeat) {
				gilUnderdog = board.getTeam();
				currentPlayerGilRatioToBeat = board.getGilPerPlayer();
			}
		}
		for (AllegianceLeaderboard board : leaderboard) {
			if (board.getTeam() == gilUnderdog) {
				board.setGilUnderdog(true);
			}
		}

		// determine bet underdog
		BattleGroundTeam betWinUnderdog = BattleGroundTeam.NONE;
		for (AllegianceLeaderboard board : leaderboard) {
			if (board.getBetRatio() > currentPlayerGilRatioToBeat) {
				betWinUnderdog = board.getTeam();
			}
		}
		for (AllegianceLeaderboard board : leaderboard) {
			if (board.getTeam() == betWinUnderdog) {
				board.setBetWinUnderdog(true);
			}
		}

		// determine fight underdog
		BattleGroundTeam fightWinUnderdog = BattleGroundTeam.NONE;
		double currentFightWinUnderdog = 0;
		for (AllegianceLeaderboard board : leaderboard) {
			if (board.getFightRatio() > currentFightWinUnderdog) {
				fightWinUnderdog = board.getTeam();
				currentFightWinUnderdog = board.getFightRatio();
			}
		}
		for (AllegianceLeaderboard board : leaderboard) {
			if (board.getTeam() == fightWinUnderdog) {
				board.setFightWinUnderdog(true);
			}
		}
		
		//get champion data
		Map<BattleGroundTeam, Integer> seasonChampionWins = this.championService.getSeasonChampWinsByAllegiance();
		for(BattleGroundTeam seasonChampionTeam : seasonChampionWins.keySet()) {
			boolean matchFound = false;
			for(int i = 0; i < leaderboard.size() && !matchFound; i++) {
				AllegianceLeaderboard board = leaderboard.get(i);
				if(board.getTeam() == seasonChampionTeam) {
					board.setCurrentSeasonFightWinsAsChampion(seasonChampionWins.get(seasonChampionTeam));
					matchFound = true;
				}
			}
		}

		log.info("Allegiance Leaderboard rebuild complete");
		
		AllegianceLeaderboardWrapper wrapper = new AllegianceLeaderboardWrapper(leaderboard);
		wrapper.setGilCap(GIL_CAP);
		
		return wrapper;
	}
	
	protected List<String> filterConversionFunction(Future<List<Optional<String>>> futureList) {
		List<String> names = new ArrayList<String>();
		try {
			for (Optional<String> maybePlayer : futureList.get()) {
				if (maybePlayer.isPresent())
					names.add(maybePlayer.get());
			}
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return names;
	}
	
	protected AllegianceLeaderboard generateAllegianceLeaderboardByTeam(BattleGroundTeam team, Map<BattleGroundTeam, Map<String, Integer>> allegianceData, Object playerRepoLock) 
	{
		Map<String, Integer> teamData = allegianceData.get(team);

		log.info("Starting initialization of PlayerRecords for {} team", team);

		// access player data from database using a projection
		List<PlayerRecord> teamPlayers = null;
		synchronized(playerRepoLock) {
			teamPlayers = this.playerRecordRepo.getPlayerDataForAllegiance(teamData.keySet()).parallelStream()
				.filter(playerRecord -> playerRecord != null)
				//.filter(playerRecord -> allegianceData.get(team).containsKey(playerRecord.getPlayer()))
				.collect(Collectors.toList());
		}

		// sort the players by gil balance.
		
		Collections.sort(teamPlayers, new Comparator<PlayerRecord>() {
			@Override 
			public int compare(PlayerRecord arg0, PlayerRecord arg1) { 
				int compare = arg0.getLastKnownAmount().compareTo(arg1.getLastKnownAmount());
				//lets reverse 
				switch(compare) { 
				case 1: 
					  compare = -1; break; 
				case -1:
					  compare = 1; 
					  break; 
				case 0: default: 
					  break; 
				} 
				return compare; 
			} 
		});
		
		teamPlayers = Collections.synchronizedList(teamPlayers);
		 
		// teamPlayers = Collections.synchronizedList(teamPlayers);
		log.info("Intialization of PlayerRecords for {} complete", team);

		Integer totalBetWins =(new CountingCallable<PlayerRecord>(teamPlayers, (playerRecord -> playerRecord.getWins()))).call();
		Integer totalBetLosses = (new CountingCallable<PlayerRecord>(teamPlayers, (playerRecord -> playerRecord.getLosses()))).call();
		Integer totalFightWins = (new CountingCallable<PlayerRecord>(teamPlayers, (playerRecord -> playerRecord.getFightWins()))).call();
		Integer totalFightLosses =(new CountingCallable<PlayerRecord>(teamPlayers, (playerRecord -> playerRecord.getFightLosses()))).call();
		Integer totalLevels = (new CountingCallable<PlayerRecord>(teamPlayers,(playerRecord -> playerRecord.getLastKnownLevel()))).call();
		Integer totalGil = (new CountingCallable<PlayerRecord>(teamPlayers,(playerRecord -> playerRecord.getLastKnownAmount() < GIL_CAP ? playerRecord.getLastKnownAmount() : GIL_CAP))).call();
		Integer totalMillionaires = (new CountingCallable<PlayerRecord>(teamPlayers, (playerRecord -> playerRecord.getLastKnownAmount() >= GIL_CAP ? 1 : 0))).call();

		// perform the prestige lookup in this thread
		Integer numberOfPrestigeSkills = this.prestigeSkillsRepo.getPrestigeSkillsCount(
				teamPlayers.stream().map(PlayerRecord::getPlayer).collect(Collectors.toList())
			);
		
		log.info("Allegiance data lookup complete for team {}", team);

		List<AllegianceLeaderboardEntry> top5Players = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			PlayerRecord currentPlayer = teamPlayers.get(i);
			AllegianceLeaderboardEntry entry = new AllegianceLeaderboardEntry(currentPlayer.getPlayer(),
					currentPlayer.getLastKnownAmount());
			entry.setPosition(i);
			top5Players.add(entry);
		}
		
		log.info("Top5 player leaderboard complete for team {}", team);

		String topPlayerPortrait = this.portraitCache.get(top5Players.get(0).getName());
		String portraitUrl = this.images.getPortraitByName(topPlayerPortrait, team);
		if (StringUtils.isNotBlank(topPlayerPortrait)) {
			portraitUrl = this.images.getPortraitByName(topPlayerPortrait, team);
		}
		if (StringUtils.isBlank(topPlayerPortrait) || portraitUrl == null) {
			portraitUrl = this.images.getPortraitByName("Ramza");
		}
		
		log.info("Portrait lookup complete for team {}", team);

		AllegianceLeaderboard data = new AllegianceLeaderboard(team, portraitUrl, top5Players);
		data.setBetWins(totalBetWins);
		data.setBetLosses(totalBetLosses);
		data.setFightWins(totalFightWins);
		data.setFightLosses(totalFightLosses);
		data.setTotalLevels(totalLevels);
		data.setTotalPrestiges(numberOfPrestigeSkills);
		data.setTotalGil(totalGil);
		data.setTotalPlayers(teamData.keySet().size());
		data.setTotalMillionaires(totalMillionaires);

		Integer gilPerPlayer = data.getTotalGil() / data.getTotalPlayers();
		data.setGilPerPlayer(gilPerPlayer);

		Double levelsPerPlayer = (double) data.getTotalLevels() / (double) data.getTotalPlayers();
		data.setTotalLevelsPerPlayer(levelsPerPlayer);

		Double betRatio = (data.getBetWins().doubleValue() + 1) / (data.getBetWins().doubleValue() + data.getBetLosses().doubleValue() + 1);
		Double fightRatio = (data.getFightWins().doubleValue() + 1) / (data.getFightWins().doubleValue() + data.getFightLosses().doubleValue() + 1);

		data.setBetRatio(betRatio);
		data.setFightRatio(fightRatio);

		log.info("Getting quantiles for team {}", team);
		
		Integer betQuantile;
		Integer fightQuantile;
		try {
			betQuantile = this.playerPageDataService.getBetPercentile(betRatio);
			fightQuantile = this.playerPageDataService.getFightPercentile(fightRatio);
		} catch (CacheMissException e) {
			log.error("Error pulling Quantile data", e);
			betQuantile = 0;
			fightQuantile = 0;
		}
		
		log.info("Team {} is done getting quantiles", team);

		data.setBetQuantile(betQuantile);
		data.setFightQuantile(fightQuantile);
		
		log.info("Team {} allegiance data is compiled and ready", team);

		return data;
	}

	@Override
	@SneakyThrows
	public AllegianceLeaderboardWrapper deserializeJson(String json) {
		ObjectMapper mapper = new ObjectMapper();
		AllegianceLeaderboardWrapper leaderboard = mapper.readValue(json, AllegianceLeaderboardWrapper.class);
		return leaderboard;
	}

}

class CountingCallable<T> implements Callable<Integer> {
	private ToIntFunction<T> countingFunction;
	private List<T> iteratedObject;

	public CountingCallable(List<T> iteratedObject, ToIntFunction<T> countingFunction) {
		this.iteratedObject = iteratedObject;
		this.countingFunction = countingFunction;
	}

	@Override
	public Integer call() {
		Integer result = this.iteratedObject.parallelStream().mapToInt(this.countingFunction).sum();
		return result;
	}
}
