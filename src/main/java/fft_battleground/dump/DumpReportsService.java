package fft_battleground.dump;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import fft_battleground.dump.model.GlobalGilPageData;
import fft_battleground.dump.model.PrestigeTableEntry;
import fft_battleground.dump.reports.ReportGenerator;
import fft_battleground.dump.reports.model.AllegianceLeaderboardWrapper;
import fft_battleground.dump.reports.model.BotLeaderboard;
import fft_battleground.dump.reports.model.ExpLeaderboardEntry;
import fft_battleground.dump.reports.model.LeaderboardBalanceData;
import fft_battleground.dump.reports.model.LeaderboardBalanceHistoryEntry;
import fft_battleground.dump.reports.model.LeaderboardData;
import fft_battleground.dump.reports.model.PlayerLeaderboard;
import fft_battleground.exception.CacheBuildException;
import fft_battleground.exception.CacheMissException;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.repo.model.BalanceHistory;
import fft_battleground.repo.model.GlobalGilHistory;
import fft_battleground.repo.model.PlayerRecord;
import fft_battleground.repo.repository.GlobalGilHistoryRepo;
import fft_battleground.repo.repository.PlayerRecordRepo;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DumpReportsService {


	public static final int HIGHEST_PLAYERS = 10;
	public static final int TOP_PLAYERS = 100;
	public static final int PERCENTILE_THRESHOLD = 10;

	@Autowired
	private DumpService dumpService;

	@Autowired
	private PlayerRecordRepo playerRecordRepo;

	@Autowired
	private GlobalGilHistoryRepo globalGilHistoryRepo;

	@Autowired
	@Getter private ReportGenerator<PlayerLeaderboard> playerLeaderboardReportGenerator;
	
	@Autowired
	@Getter private ReportGenerator<BotLeaderboard> botLeaderboardReportGenerator;
	
	@Autowired
	@Getter private ReportGenerator<Map<Integer, Double>> betPercentileReportGenerator;
	
	@Autowired
	@Getter private ReportGenerator<Map<Integer, Double>> fightPercentileReportGenerator;
	
	@Autowired
	@Getter private ReportGenerator<AllegianceLeaderboardWrapper> allegianceReportGenerator;

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

	public BotLeaderboard getBotLeaderboard() throws CacheMissException {
		BotLeaderboard result = this.botLeaderboardReportGenerator.getReport();
		return result;
	}
	
	public BotLeaderboard writeBotLeaderboardToCaches() {
		BotLeaderboard result = this.botLeaderboardReportGenerator.writeReport();
		return result;
	}

	public Map<String, Integer> getTopPlayers(Integer count) throws CacheBuildException {
		BiMap<String, Integer> topPlayers = HashBiMap.create();
		Map<String, Integer> topPlayerDataMap = this.dumpService.getLeaderboard().keySet().parallelStream()
				.filter(player -> !this.dumpService.getBotCache().contains(player))
				.filter(player -> this.playerRecordRepo.findById(StringUtils.lowerCase(player)).isPresent())
				.filter(player -> {
					Date lastActive = this.playerRecordRepo.findById(StringUtils.lowerCase(player)).get()
							.getLastActive();
					boolean result = lastActive != null && this.isPlayerActiveInLastMonth(lastActive);
					return result;
				}).collect(Collectors.toMap(Function.identity(),
						player -> this.dumpService.getLeaderboard().get(player)));
		Map.Entry<String, Integer> currentEntry = null;
		try {
			for(Map.Entry<String, Integer> entry : topPlayerDataMap.entrySet()) {
				currentEntry = entry;
				topPlayers.put(entry.getKey(), entry.getValue());
			}
		} catch(IllegalArgumentException e) {
			String errorMessageFormat = "Illegal argument exception populating BiMap.  The current entry is %1$s and %2$o";
			String errorMessage = String.format(errorMessageFormat, currentEntry.getKey(), currentEntry.getValue());
			log.error(errorMessage, e);
			throw new CacheBuildException(errorMessage, e);
		}
		Set<Integer> topValues = topPlayers.values().stream().sorted().limit(count).collect(Collectors.toSet());

		BiMap<Integer, String> topPlayersInverseMap = topPlayers.inverse();
		Map<String, Integer> leaderboardWithoutBots = topValues.stream()
				.collect(Collectors.toMap(rank -> topPlayersInverseMap.get(rank), Function.identity()));
		return leaderboardWithoutBots;
	}

	public PlayerLeaderboard getLeaderboard() throws CacheMissException {
		PlayerLeaderboard leaderboard = this.playerLeaderboardReportGenerator.getReport();
		return leaderboard;
	}
	
	public PlayerLeaderboard writeLeaderboard() {
		PlayerLeaderboard leaderboard = this.playerLeaderboardReportGenerator.writeReport();
		return leaderboard;
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
				BattleGroundTeam team = maybePlayer.get().getAllegiance();

				Integer prestigeLevel = 0;
				List<String> prestigeSkills = this.dumpService.getPrestigeSkillsCache().get(player);
				if (prestigeSkills != null) {
					prestigeLevel = prestigeSkills.size();
				}

				result = new ExpLeaderboardEntry(rank, player, level, exp, prestigeLevel, lastActive, team);
				results.add(result);
			}
		}

		return results;
	}

	@SneakyThrows
	public List<PrestigeTableEntry> generatePrestigeTable() {
		List<PrestigeTableEntry> results = this.dumpService.getPrestigeSkillsCache().keySet().parallelStream()
				.filter(player -> this.dumpService.getPrestigeSkillsCache().get(player) != null)
				.filter(player -> !this.dumpService.getPrestigeSkillsCache().get(player).isEmpty())
				.filter(player -> this.dumpService.getPrestigeSkillsCache().get(player).size() != 417)
				.map(player -> new PrestigeTableEntry(player,
						this.dumpService.getPrestigeSkillsCache().get(player).size()))
				.collect(Collectors.toList());
		SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy");
		results.stream().forEach(prestigeTableEntry -> prestigeTableEntry
				.setLastActive(format.format(this.dumpService.getLastActiveCache().get(prestigeTableEntry.getName()))));
		Collections.sort(results);

		return results;
	}

	public Integer getBetPercentile(Double ratio) throws CacheMissException {
		Map<Integer, Double> betPercentiles = this.betPercentileReportGenerator.getReport();

		Integer result = null;
		for (Map.Entry<Integer, Double> entry: betPercentiles.entrySet()) {
			Double currentPercentile = entry.getValue();
			try {
				if (ratio < currentPercentile) {
					Integer key = Integer.valueOf(entry.getKey());
					result = key - 1;
					break;
				}
			}catch(NullPointerException e) {
				log.error("NullPointerException caught", e);
			} catch(ClassCastException e) {
				log.error("ClassCast exception caught", e);
			}
		}

		return result;
	}
	
	public Map<Integer, Double> writeBetPercentile() {
		Map<Integer, Double> betPercentiles = this.betPercentileReportGenerator.writeReport();
		return betPercentiles;
	}

	public Integer getFightPercentile(Double ratio) throws CacheMissException {
		Map<Integer, Double> fightPercentiles = this.fightPercentileReportGenerator.getReport();

		Integer result = null;
		for (int i = 0; result == null && i <= 100; i++) {
			Double currentPercentile = fightPercentiles.get(i);
			if (ratio < currentPercentile) {
				result = i - 1;
			}
		}

		return result;
	}
	
	public Map<Integer, Double> writeFightPercentile() {
		Map<Integer, Double> fightPercentiles = this.fightPercentileReportGenerator.writeReport();
		return fightPercentiles;
	}

	public AllegianceLeaderboardWrapper getAllegianceData() throws CacheMissException {
		AllegianceLeaderboardWrapper allegianceLeaderboardWrapper = this.allegianceReportGenerator.getReport();
		return allegianceLeaderboardWrapper;
	}
	
	public AllegianceLeaderboardWrapper writeAllegianceWrapper() {
		AllegianceLeaderboardWrapper wrapper = this.allegianceReportGenerator.writeReport();
		return wrapper;
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

	public boolean isPlayerActiveInLastMonth(Date lastActiveDate) {
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

