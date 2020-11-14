package fft_battleground.dump;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.math.Quantiles;

import fft_battleground.dump.model.AllegianceLeaderboard;
import fft_battleground.dump.model.AllegianceLeaderboardEntry;
import fft_battleground.dump.model.ExpLeaderboardEntry;
import fft_battleground.dump.model.GlobalGilPageData;
import fft_battleground.dump.model.LeaderboardBalanceData;
import fft_battleground.dump.model.LeaderboardBalanceHistoryEntry;
import fft_battleground.dump.model.LeaderboardData;
import fft_battleground.dump.model.PlayerLeaderboard;
import fft_battleground.dump.model.PrestigeTableEntry;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.model.Images;
import fft_battleground.repo.GlobalGilHistoryRepo;
import fft_battleground.repo.MatchRepo;
import fft_battleground.repo.PlayerRecordRepo;
import fft_battleground.repo.PlayerSkillRepo;
import fft_battleground.repo.model.BalanceHistory;
import fft_battleground.repo.model.GlobalGilHistory;
import fft_battleground.repo.model.PlayerRecord;
import fft_battleground.repo.model.TeamInfo;
import fft_battleground.tournament.ChampionService;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DumpReportsService {

	private static final String LEADERBOARD_KEY = "leaderboard";
	private static final String BOT_LEADERBOARD_KEY = "botleaderboard";
	private static final String BET_PERCENTILES_KEY = "betpercentiles";
	private static final String FIGHT_PERCENTILES_KEY = "fightpercentiles";
	private static final String ALLEGIANCE_LEADERBOARD_KEY = "allegianceleaderboard";
	private static final int HIGHEST_PLAYERS = 10;
	private static final int TOP_PLAYERS = 100;
	private static final int PERCENTILE_THRESHOLD = 10;

	@Autowired
	private DumpService dumpService;
	
	@Autowired
	private ChampionService championService;

	@Autowired
	private PlayerRecordRepo playerRecordRepo;

	@Autowired
	private GlobalGilHistoryRepo globalGilHistoryRepo;

	@Autowired
	private MatchRepo matchRepo;

	@Autowired
	private PlayerSkillRepo playerSkillRepo;

	@Autowired
	private Images images;

	private Cache<String, PlayerLeaderboard> leaderboardCache = Caffeine.newBuilder()
			.expireAfterWrite(24, TimeUnit.HOURS).maximumSize(1).build();

	private Cache<String, Map<String, Integer>> botLeaderboardCache = Caffeine.newBuilder()
			.expireAfterWrite(24, TimeUnit.HOURS).maximumSize(1).build();

	private Cache<String, Map<Integer, Double>> betPercentilesCache = Caffeine.newBuilder()
			.expireAfterWrite(24, TimeUnit.HOURS).maximumSize(1).build();

	private Cache<String, Map<Integer, Double>> fightPercentilesCache = Caffeine.newBuilder()
			.expireAfterWrite(24, TimeUnit.HOURS).maximumSize(1).build();

	private Cache<String, List<AllegianceLeaderboard>> allegianceLeaderboardCache = Caffeine.newBuilder()
			.expireAfterWrite(24, TimeUnit.HOURS).maximumSize(1).build();

	public GlobalGilPageData getGlobalGilData() {
		GlobalGilPageData data = null;
		GlobalGilHistory todaysData = this.globalGilHistoryRepo.getFirstGlobalGilHistory();
		List<GlobalGilHistory> historyByDay = this.globalGilHistoryRepo
				.getGlobalGilHistoryByCalendarTimeType(ChronoUnit.DAYS);
		List<GlobalGilHistory> historyByWeek = this.globalGilHistoryRepo
				.getGlobalGilHistoryByCalendarTimeType(ChronoUnit.WEEKS);
		List<GlobalGilHistory> historyByMonth = this.globalGilHistoryRepo
				.getGlobalGilHistoryByCalendarTimeType(ChronoUnit.MONTHS);

		data = new GlobalGilPageData(todaysData, historyByDay, historyByWeek, historyByMonth);

		return data;
	}

	@SneakyThrows
	public Double percentageOfGlobalGil(Integer balance) {
		Double percentage = new Double(0);
		if (balance != null) {
			GlobalGilHistory todaysData = this.globalGilHistoryRepo.getFirstGlobalGilHistory();
			percentage = ((new Double(balance) / new Double(todaysData.getGlobal_gil_count())));
		}
		return percentage;
	}

	public Integer getLeaderboardPosition(String player) {
		String lowercasePlayer = StringUtils.lowerCase(player);
		Integer position = this.dumpService.getLeaderboard().get(lowercasePlayer);
		return position;
	}

	public synchronized Map<String, Integer> getBotLeaderboard() {
		Map<String, Integer> botLeaderboard = this.botLeaderboardCache.getIfPresent(BOT_LEADERBOARD_KEY);
		if (botLeaderboard == null) {
			log.warn("bot leaderboard cache was busted, creating new value");
			botLeaderboard = this.generateBotLeaderboard();
			this.botLeaderboardCache.put(BOT_LEADERBOARD_KEY, botLeaderboard);
		}

		return botLeaderboard;
	}

	protected Map<String, Integer> generateBotLeaderboard() {
		Map<String, Integer> botBalances = new TreeMap<String, Integer>(this.dumpService.getBotCache().parallelStream()
				.filter(botName -> this.dumpService.getBalanceCache().containsKey(botName))
				.collect(Collectors.toMap(Function.identity(), bot -> this.dumpService.getBalanceCache().get(bot))));
		return botBalances;
	}

	public Map<String, Integer> getTopPlayers(Integer count) {
		BiMap<String, Integer> topPlayers = HashBiMap.create();
		topPlayers.putAll(this.dumpService.getLeaderboard().keySet().parallelStream()
				.filter(player -> !this.dumpService.getBotCache().contains(player))
				.filter(player -> this.playerRecordRepo.findById(StringUtils.lowerCase(player)).isPresent())
				.filter(player -> {
					Date lastActive = this.playerRecordRepo.findById(StringUtils.lowerCase(player)).get()
							.getLastActive();
					boolean result = lastActive != null && this.isPlayerActiveInLastMonth(lastActive);
					return result;
				}).collect(Collectors.toMap(Function.identity(),
						player -> this.dumpService.getLeaderboard().get(player))));
		Set<Integer> topValues = topPlayers.values().stream().sorted().limit(count).collect(Collectors.toSet());

		BiMap<Integer, String> topPlayersInverseMap = topPlayers.inverse();
		Map<String, Integer> leaderboardWithoutBots = topValues.stream()
				.collect(Collectors.toMap(rank -> topPlayersInverseMap.get(rank), Function.identity()));
		return leaderboardWithoutBots;
	}

	public synchronized PlayerLeaderboard getLeaderboard() {
		PlayerLeaderboard leaderboard = this.leaderboardCache.getIfPresent(LEADERBOARD_KEY);
		if (leaderboard == null) {
			log.warn("Leaderboard cache was busted, creating new value");
			leaderboard = this.generatePlayerLeaderboardData();
			this.leaderboardCache.put(LEADERBOARD_KEY, leaderboard);
		}

		return leaderboard;
	}

	protected PlayerLeaderboard generatePlayerLeaderboardData() {
		Map<String, Integer> topPlayers = this.getTopPlayers(TOP_PLAYERS);
		List<LeaderboardData> allPlayers = topPlayers.keySet().parallelStream()
				.map(player -> this.collectPlayerLeaderboardDataByPlayer(player)).filter(result -> result != null)
				.sorted().collect(Collectors.toList());
		Collections.reverse(allPlayers);
		for (int i = 0; i < allPlayers.size(); i++) {
			allPlayers.get(i).setRank(i + 1);
		}

		List<LeaderboardData> highestPlayers = allPlayers.parallelStream()
				.filter(leaderboardData -> leaderboardData.getRank() <= HIGHEST_PLAYERS).collect(Collectors.toList());
		List<LeaderboardData> topPlayersList = allPlayers.parallelStream()
				.filter(leaderboardData -> leaderboardData.getRank() > HIGHEST_PLAYERS
						&& leaderboardData.getRank() <= TOP_PLAYERS)
				.collect(Collectors.toList());
		PlayerLeaderboard leaderboard = new PlayerLeaderboard(highestPlayers, topPlayersList);

		return leaderboard;
	}

	@SneakyThrows
	protected LeaderboardData collectPlayerLeaderboardDataByPlayer(String player) {
		NumberFormat myFormat = NumberFormat.getInstance();
		myFormat.setGroupingUsed(true);
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
		DecimalFormat decimalFormat = new DecimalFormat("##.#########");

		LeaderboardData data = null;
		Integer gil = this.dumpService.getBalanceFromCache(player);
		Date lastActive = this.dumpService.getLastActiveDateFromCache(player);

		String gilString = myFormat.format(gil);
		String percentageOfGlobalGil = decimalFormat.format(this.percentageOfGlobalGil(gil) * (double) 100);
		String activeDate = dateFormat.format(lastActive);
		data = new LeaderboardData(player, gilString, activeDate);
		data.setPercentageOfGlobalGil(percentageOfGlobalGil);

		return data;
	}

	public List<ExpLeaderboardEntry> generateExpLeaderboardData() {
		List<ExpLeaderboardEntry> results = new ArrayList<>();
		for (int rank = 1; rank <= TOP_PLAYERS; rank++) {
			ExpLeaderboardEntry result = null;
			String player = this.dumpService.getExpRankLeaderboardByRank().get(rank);
			Optional<PlayerRecord> maybePlayer = this.playerRecordRepo.findById(player);
			if (maybePlayer.isPresent() && this.isPlayerActiveInLastMonth(maybePlayer.get().getLastActive())) {
				Short level = maybePlayer.get().getLastKnownLevel();
				Short exp = maybePlayer.get().getLastKnownRemainingExp();
				SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy");
				String lastActive = format.format(maybePlayer.get().getLastActive());

				Integer prestigeLevel = 0;
				List<String> prestigeSkills = this.dumpService.getPrestigeSkillsCache().get(player);
				if (prestigeSkills != null) {
					prestigeLevel = prestigeSkills.size();
				}

				result = new ExpLeaderboardEntry(rank, player, level, exp, prestigeLevel, lastActive);
				results.add(result);
			}
		}

		return results;
	}

	@SneakyThrows
	public List<PrestigeTableEntry> generatePrestigeTable() {
		List<PrestigeTableEntry> results = this.dumpService.getPrestigeSkillsCache().keySet().parallelStream()
				.filter(player -> this.dumpService.getPrestigeSkillsCache().get(player) != null)
				.filter(player -> this.dumpService.getPrestigeSkillsCache().get(player).size() != 0)
				.map(player -> new PrestigeTableEntry(player,
						this.dumpService.getPrestigeSkillsCache().get(player).size()))
				.collect(Collectors.toList());
		SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy");
		results.stream().forEach(prestigeTableEntry -> prestigeTableEntry
				.setLastActive(format.format(this.dumpService.getLastActiveCache().get(prestigeTableEntry.getName()))));
		Collections.sort(results);

		return results;
	}

	public synchronized Integer getBetPercentile(Double ratio) {
		Map<Integer, Double> betPercentiles = this.betPercentilesCache.getIfPresent(BET_PERCENTILES_KEY);
		if (betPercentiles == null) {
			log.warn("The Bet Percentiles cache was busted.  Rebuilding");
			betPercentiles = this.calculateBetPercentiles();
			this.betPercentilesCache.put(BET_PERCENTILES_KEY, betPercentiles);
			log.warn("Bet Percentiles rebuild complete");
		}

		Integer result = null;
		for (int i = 0; result == null && i <= 100; i++) {
			Double currentPercentile = betPercentiles.get(i);
			if (ratio < currentPercentile) {
				result = i - 1;
			}
		}

		return result;
	}

	public synchronized Integer getFightPercentile(Double ratio) {
		Map<Integer, Double> fightPercentiles = this.fightPercentilesCache.getIfPresent(FIGHT_PERCENTILES_KEY);
		if (fightPercentiles == null) {
			log.warn("The Fight Percentiles cache was busted.  Rebuilding");
			fightPercentiles = this.calculateFightPercentiles();
			this.fightPercentilesCache.put(FIGHT_PERCENTILES_KEY, fightPercentiles);
			log.warn("Fight Percentiles rebuild complete");
		}

		Integer result = null;
		for (int i = 0; result == null && i <= 100; i++) {
			Double currentPercentile = fightPercentiles.get(i);
			if (ratio < currentPercentile) {
				result = i - 1;
			}
		}

		return result;
	}

	public List<AllegianceLeaderboard> getAllegianceData() {
		List<AllegianceLeaderboard> allegianceLeaderboard = this.allegianceLeaderboardCache
				.getIfPresent(ALLEGIANCE_LEADERBOARD_KEY);
		if (allegianceLeaderboard == null) {
			log.warn("Allegiance Leaderboard cache busted.  Rebuilding.");
			allegianceLeaderboard = this.generateAllegianceData();
			this.allegianceLeaderboardCache.put(ALLEGIANCE_LEADERBOARD_KEY, allegianceLeaderboard);
			log.warn("Allegiance Leaderboard cache rebuild complete");
		}

		return allegianceLeaderboard;
	}

	@SneakyThrows
	@Transactional
	protected List<AllegianceLeaderboard> generateAllegianceData() {
		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(20);

		Map<BattleGroundTeam, Map<String, Integer>> allegianceData = new ConcurrentHashMap<>();

		List<BattleGroundTeam> teams = BattleGroundTeam.coreTeams();
		teams.stream().forEach(team -> allegianceData.put(team, new ConcurrentHashMap<String, Integer>()));

		Map<String, Integer> highScoreDataFromDump = this.dumpService.getDumpDataProvider().getHighScoreDump();
		Map<String, Integer> topTenPlayers = this.getTopPlayers(HIGHEST_PLAYERS);

		Future<List<Optional<String>>> topTenFilter = executor
				.submit(new FunctionCallableListResult<String, Optional<String>>(highScoreDataFromDump.keySet(),
						(playerName -> {
							Optional<String> result = null;
							boolean isTopTenPlayer = topTenPlayers.containsKey(playerName);
							result = isTopTenPlayer ? Optional.<String>of(playerName) : Optional.<String>empty();
							return result;
						})));
		Future<List<Optional<String>>> activeFilter = executor
				.submit(new FunctionCallableListResult<String, Optional<String>>(highScoreDataFromDump.keySet(),
						(playerName -> {
							Optional<String> result = null;
							Date lastActive = this.dumpService.getLastActiveDateFromCache(playerName);
							boolean active = this.isPlayerActiveInLastMonth(lastActive);
							result = active ? Optional.<String>empty() : Optional.<String>of(playerName);
							return result;
						})));
		Future<List<Optional<String>>> botFilter = executor
				.submit(new FunctionCallableListResult<String, Optional<String>>(highScoreDataFromDump.keySet(),
						(playerName -> {
							Optional<String> result = null;
							boolean isBot = this.dumpService.getBotCache().contains(playerName);
							result = isBot ? Optional.<String>of(playerName) : Optional.<String>empty();
							return result;
						})));
		Future<List<Optional<String>>> noAllegianceFilter = executor
				.submit(new FunctionCallableListResult<String, Optional<String>>(highScoreDataFromDump.keySet(),
						(playerName -> {
							boolean hasAllegiance = teams
									.contains(this.dumpService.getAllegianceCache().get(playerName));
							Optional<String> result = hasAllegiance ? Optional.<String>empty()
									: Optional.<String>of(playerName);
							return result;
						})));

		Set<String> filteredPlayers = new HashSet<>();
		Function<Future<List<Optional<String>>>, List<String>> conversionFunction = (futureList -> {
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
		});
		filteredPlayers.addAll(conversionFunction.apply(topTenFilter));
		filteredPlayers.addAll(conversionFunction.apply(botFilter));
		filteredPlayers.addAll(conversionFunction.apply(activeFilter));
		filteredPlayers.addAll(conversionFunction.apply(noAllegianceFilter));
		filteredPlayers.parallelStream().forEach(playerName -> highScoreDataFromDump.remove(playerName));

		Calendar currentTimeOfCompletion = Calendar.getInstance();

		log.debug("The currentTime is {}", currentTimeOfCompletion);

		highScoreDataFromDump.keySet().parallelStream().forEach(playerName -> {
			BattleGroundTeam allegiance = this.dumpService.getAllegianceCache().get(playerName);
			allegianceData.get(allegiance).put(playerName, highScoreDataFromDump.get(playerName));
		});

		// find player total
		// find top 5 players
		// determine the relative position of each team
		List<AllegianceLeaderboard> leaderboard = teams.parallelStream().map(team -> {
			Map<String, Integer> teamData = allegianceData.get(team);

			log.info("Starting initialization of PlayerRecords for {} team", team);

			// access player data from database using a projection
			List<PlayerRecord> teamPlayers = this.playerRecordRepo.getPlayerDataForAllegiance(teamData.keySet())
					.parallelStream().filter(playerRecord -> playerRecord != null).collect(Collectors.toList());

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
			log.debug("Intialization of PlayerRecords for {} complete");

			Integer totalBetWins =(new CountingCallable<PlayerRecord>(teamPlayers, (playerRecord -> playerRecord.getWins()))).call();
			Integer totalBetLosses = (new CountingCallable<PlayerRecord>(teamPlayers, (playerRecord -> playerRecord.getLosses()))).call();
			Integer totalFightWins = (new CountingCallable<PlayerRecord>(teamPlayers, (playerRecord -> playerRecord.getFightWins()))).call();
			Integer totalFightLosses =(new CountingCallable<PlayerRecord>(teamPlayers, (playerRecord -> playerRecord.getFightLosses()))).call();
			Integer totalLevels = (new CountingCallable<PlayerRecord>(teamPlayers,(playerRecord -> playerRecord.getLastKnownLevel()))).call();
			Integer totalGil = (new CountingCallable<PlayerRecord>(teamPlayers,(playerRecord -> playerRecord.getLastKnownAmount()))).call();

			// perform the prestige lookup in this thread
			Integer numberOfPrestigeSkills = this.playerSkillRepo.getPrestigeSkillsCount(
					teamPlayers.stream().map(PlayerRecord::getPlayer).collect(Collectors.toList())
				);

			List<AllegianceLeaderboardEntry> top5Players = new ArrayList<>();
			for (int i = 0; i < 5; i++) {
				PlayerRecord currentPlayer = teamPlayers.get(i);
				AllegianceLeaderboardEntry entry = new AllegianceLeaderboardEntry(currentPlayer.getPlayer(),
						currentPlayer.getLastKnownAmount());
				entry.setPosition(i);
				top5Players.add(entry);
			}

			String topPlayerPortrait = this.dumpService.getPortraitCache().get(top5Players.get(0).getName());
			String portraitUrl = this.images.getPortraitByName(topPlayerPortrait, team);
			if (StringUtils.isNotBlank(topPlayerPortrait)) {
				portraitUrl = this.images.getPortraitByName(topPlayerPortrait, team);
			}
			if (StringUtils.isBlank(topPlayerPortrait) || portraitUrl == null) {
				List<TeamInfo> playerTeamInfo = this.matchRepo.getLatestTeamInfoForPlayer(top5Players.get(0).getName(),
						PageRequest.of(0, 1));
				if (playerTeamInfo != null && playerTeamInfo.size() > 0) {
					portraitUrl = this.images.getPortraitLocationByTeamInfo(playerTeamInfo.get(0));
				} else {
					portraitUrl = this.images.getPortraitByName("Ramza");
				}
			}

			AllegianceLeaderboard data = new AllegianceLeaderboard(team, portraitUrl, top5Players);
			data.setBetWins(totalBetWins);
			data.setBetLosses(totalBetLosses);
			data.setFightWins(totalFightWins);
			data.setFightLosses(totalFightLosses);
			data.setTotalLevels(totalLevels);
			data.setTotalPrestiges(numberOfPrestigeSkills);
			data.setTotalGil(totalGil);
			data.setTotalPlayers(teamData.keySet().size());

			Integer gilPerPlayer = data.getTotalGil() / data.getTotalPlayers();
			data.setGilPerPlayer(gilPerPlayer);

			Double levelsPerPlayer = (double) data.getTotalLevels() / (double) data.getTotalPlayers();
			data.setTotalLevelsPerPlayer(levelsPerPlayer);

			Double betRatio = (data.getBetWins().doubleValue() + 1) / (data.getBetWins().doubleValue() + data.getBetLosses().doubleValue() + 1);
			Double fightRatio = (data.getFightWins().doubleValue() + 1) / (data.getFightWins().doubleValue() + data.getFightLosses().doubleValue() + 1);

			data.setBetRatio(betRatio);
			data.setFightRatio(fightRatio);

			Integer betQuantile = this.getBetPercentile(betRatio);
			Integer fightQuantile = this.getFightPercentile(fightRatio);

			data.setBetQuantile(betQuantile);
			data.setFightQuantile(fightQuantile);

			return data;
		}).collect(Collectors.toList());

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
		double currentBetWinRatioToBeat = 0;
		for (AllegianceLeaderboard board : leaderboard) {
			if (board.getBetRatio() > currentPlayerGilRatioToBeat) {
				betWinUnderdog = board.getTeam();
				currentBetWinRatioToBeat = board.getBetRatio();
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

		return leaderboard;
	}

	// this time let's take it hour by hour, and then use my original date slice
	// algorithm
	@SneakyThrows
	public LeaderboardBalanceData getLabelsAndSetRelevantBalanceHistories(List<LeaderboardBalanceHistoryEntry> entries,
			Integer count) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh");

		// dynamically pick best hours to find entries for one week
		int hoursToTrack = 168;
		int hoursPerSlice = hoursToTrack / count;
		List<Date> dateSlices = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			Calendar calendar = Calendar.getInstance();
			int hourSliceSize = i * hoursPerSlice * -1;
			calendar.add(Calendar.HOUR, hourSliceSize);
			Date dateSlice = calendar.getTime();
			sdf.parse(sdf.format(dateSlice));
			dateSlices.add(dateSlice);
		}

		Collections.sort(dateSlices);

		// simplify all dates to nearest hour
		for (LeaderboardBalanceHistoryEntry entry : entries) {
			for (BalanceHistory balanceHistory : entry.getBalanceHistory()) {
				String simplifiedDateString = sdf.format(balanceHistory.getCreate_timestamp());
				Date simplifiedDate = sdf.parse(simplifiedDateString);
				balanceHistory.setCreate_timestamp(new Timestamp(simplifiedDate.getTime()));
			}
		}

		// now lets extrapolate each player's balance history by hour
		List<LeaderboardBalanceHistoryEntry> extrapolatedData = new ArrayList<>();
		for (LeaderboardBalanceHistoryEntry entry : entries) {
			LeaderboardBalanceHistoryEntry extrapolatedEntry = new LeaderboardBalanceHistoryEntry(entry.getPlayerName(),
					new ArrayList<BalanceHistory>());
			Integer currentAmount = 0;
			for (int i = 0; i < hoursToTrack; i++) {
				Calendar calendar = Calendar.getInstance();
				calendar.add(Calendar.HOUR, (-1 * hoursToTrack) + i);

				// find nearest balanceHistory match
				boolean matchFound = false;
				for (int j = 0; j < entry.getBalanceHistory().size() && !matchFound; j++) {
					if (this.twoDatesMatchSameExactHour(calendar.getTime(),
							entry.getBalanceHistory().get(j).getCreate_timestamp())) {
						matchFound = true;
						currentAmount = entry.getBalanceHistory().get(j).getBalance();
					}
				}

				extrapolatedEntry.getBalanceHistory()
						.add(new BalanceHistory(entry.getPlayerName(), currentAmount, calendar.getTime()));
			}
			extrapolatedData.add(extrapolatedEntry);
		}

		// reduce balance history to appropriate values
		for (LeaderboardBalanceHistoryEntry entry : extrapolatedData) {
			List<BalanceHistory> truncatedHistory = new ArrayList<>();
			for (int i = 0; i < dateSlices.size(); i++) {
				Date currentSlice = dateSlices.get(i);
				boolean foundEntry = false;
				// search balance history for first entry with the correct hour
				Integer currentAmount = 0;
				for (int j = 0; j < entry.getBalanceHistory().size() && !foundEntry; j++) {
					Calendar currentSliceCalendar = Calendar.getInstance();
					currentSliceCalendar.setTime(currentSlice);
					Calendar currentBalanceHistoryDateCalendar = Calendar.getInstance();
					Date currentBalanceHistoryDate = entry.getBalanceHistory().get(j).getCreate_timestamp();
					currentBalanceHistoryDateCalendar.setTime(currentBalanceHistoryDate);
					if (this.twoDatesMatchSameExactHour(currentSlice, currentBalanceHistoryDate)) {
						foundEntry = true;
						truncatedHistory.add(entry.getBalanceHistory().get(j));
						currentAmount = entry.getBalanceHistory().get(j).getBalance();
					}
				}

				if (!foundEntry && this.twoDatesMatchSameExactHour(currentSlice, new Date())) {
					foundEntry = true;
					Optional<PlayerRecord> maybePlayer = this.playerRecordRepo
							.findById(StringUtils.lowerCase(entry.getPlayerName()));
					if (maybePlayer.isPresent()) {
						foundEntry = true;
						Date simplifiedDate = sdf.parse(sdf.format(new Date()));
						truncatedHistory.add(new BalanceHistory(entry.getPlayerName(),
								maybePlayer.get().getLastKnownAmount(), simplifiedDate));
					}
				}
				// if none found, create a valid blank entry
				if (!foundEntry) {
					log.warn("could not find an entry even with fully extrapolated data, something went wrong");
					truncatedHistory.add(new BalanceHistory(entry.getPlayerName(), currentAmount, currentSlice));
				}
				// reset foundEntry
				foundEntry = false;
			}
			entry.setBalanceHistory(truncatedHistory);
		}

		// smooth out zero results. 0 is not possible for balances, so must be caused by
		// missing data
		for (LeaderboardBalanceHistoryEntry entry : extrapolatedData) {
			this.smoothOutZeroes(entry);
		}

		LeaderboardBalanceData data = new LeaderboardBalanceData(dateSlices, extrapolatedData);

		return data;
	}

	/**
	 * Zero is not a valid result for balance, and must be caused by missing data.
	 * This function iterates over all balance history data backwards, setting any 0
	 * value to the first valid value that follows it.
	 * 
	 * @param extrapolatedData
	 */
	protected void smoothOutZeroes(LeaderboardBalanceHistoryEntry extrapolatedData) {
		// increment in reverse
		Integer previousAmount = null;
		List<BalanceHistory> balanceHistory = extrapolatedData.getBalanceHistory();
		for (int i = balanceHistory.size() - 1; i >= 0; i--) {
			BalanceHistory currentBalanceHistory = balanceHistory.get(i);
			if (previousAmount == null) {
				previousAmount = currentBalanceHistory.getBalance();
			} else if (currentBalanceHistory.getBalance().equals(0)) {
				currentBalanceHistory.setBalance(previousAmount);
			} else {
				previousAmount = currentBalanceHistory.getBalance();
			}
		}

		return;
	}

	protected boolean twoDatesMatchSameExactHour(Date currentSlice, Date currentBalanceHistoryDate) {
		Calendar currentSliceCalendar = Calendar.getInstance();
		currentSliceCalendar.setTime(currentSlice);
		Calendar currentBalanceHistoryDateCalendar = Calendar.getInstance();
		currentBalanceHistoryDateCalendar.setTime(currentBalanceHistoryDate);
		boolean result = currentSliceCalendar.get(Calendar.MONTH) == currentBalanceHistoryDateCalendar
				.get(Calendar.MONTH)
				&& currentSliceCalendar.get(Calendar.DAY_OF_MONTH) == currentBalanceHistoryDateCalendar
						.get(Calendar.DAY_OF_MONTH)
				&& currentSliceCalendar.get(Calendar.HOUR_OF_DAY) == currentBalanceHistoryDateCalendar
						.get(Calendar.HOUR_OF_DAY);

		return result;
	}

	protected Map<Integer, Double> calculateBetPercentiles() {
		Map<Integer, Double> percentiles = Quantiles.percentiles().indexes(IntStream.rangeClosed(0, 100).toArray())
				.compute(this.playerRecordRepo.findAll().stream()
						.filter(playerRecord -> playerRecord.getWins() != null && playerRecord.getLosses() != null)
						.filter(playerRecord -> playerRecord.getLastActive() != null
								&& this.isPlayerActiveInLastMonth(playerRecord.getLastActive()))
						.filter(playerRecord -> (playerRecord.getWins()
								+ playerRecord.getLosses()) > PERCENTILE_THRESHOLD)
						.map(playerRecord -> ((double) playerRecord.getWins() + 1)
								/ ((double) playerRecord.getWins() + playerRecord.getLosses() + 1))
						.collect(Collectors.toList()));
		return percentiles;
	}

	protected Map<Integer, Double> calculateFightPercentiles() {
		Map<Integer, Double> percentiles = Quantiles.percentiles().indexes(IntStream.rangeClosed(0, 100).toArray())
				.compute(this.playerRecordRepo.findAll().stream()
						.filter(playerRecord -> playerRecord.getFightWins() != null
								&& playerRecord.getFightLosses() != null)
						.filter(playerRecord -> playerRecord.getLastActive() != null
								&& this.isPlayerActiveInLastMonth(playerRecord.getLastActive()))
						.filter(playerRecord -> (playerRecord.getFightWins()
								+ playerRecord.getFightLosses()) > PERCENTILE_THRESHOLD)
						.map(playerRecord -> ((double) playerRecord.getFightWins() + 1)
								/ ((double) playerRecord.getFightWins() + playerRecord.getFightLosses() + 1))
						.collect(Collectors.toList()));
		return percentiles;
	}

	protected boolean isPlayerActiveInLastMonth(Date lastActiveDate) {
		if (lastActiveDate == null) {
			return false;
		}

		Calendar thirtyDaysAgo = Calendar.getInstance();
		thirtyDaysAgo.add(Calendar.DAY_OF_MONTH, -30); // 2020-01-25

		Date thirtyDaysAgoDate = thirtyDaysAgo.getTime();
		boolean isActive = lastActiveDate.after(thirtyDaysAgoDate);
		return isActive;
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

class FunctionCallableListResult<T, S> implements Callable<List<S>> {
	private Function<T, S> callingFunction;
	private Collection<T> iteratedObject;

	public FunctionCallableListResult(Collection<T> obj, Function<T, S> callingFunction) {
		this.iteratedObject = obj;
		this.callingFunction = callingFunction;
	}

	@Override
	public List<S> call() throws Exception {
		List<S> result = this.iteratedObject.parallelStream().map(this.callingFunction).collect(Collectors.toList());
		return result;
	}
}
