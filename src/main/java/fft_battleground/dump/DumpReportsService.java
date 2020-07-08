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
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import fft_battleground.dump.model.ExpLeaderboardEntry;
import fft_battleground.dump.model.GlobalGilPageData;
import fft_battleground.dump.model.LeaderboardBalanceData;
import fft_battleground.dump.model.LeaderboardBalanceHistoryEntry;
import fft_battleground.dump.model.LeaderboardData;
import fft_battleground.dump.model.PlayerLeaderboard;
import fft_battleground.dump.model.PrestigeTableEntry;
import fft_battleground.repo.GlobalGilHistoryRepo;
import fft_battleground.repo.PlayerRecordRepo;
import fft_battleground.repo.model.BalanceHistory;
import fft_battleground.repo.model.GlobalGilHistory;
import fft_battleground.repo.model.PlayerRecord;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DumpReportsService {

	private static final String LEADERBOARD_KEY = "leaderboard";
	private static final String BOT_LEADERBOARD_KEY = "botleaderboard";
	private static final int HIGHEST_PLAYERS = 10;
	private static final int TOP_PLAYERS = 100;
	
	@Autowired
	private DumpService dumpService;
	
	@Autowired
	private PlayerRecordRepo playerRecordRepo;
	
	@Autowired
	private GlobalGilHistoryRepo globalGilHistoryRepo;
	
	private Cache<String, PlayerLeaderboard> leaderboardCache = Caffeine.newBuilder()
			  .expireAfterWrite(12, TimeUnit.HOURS)
			  .maximumSize(1)
			  .build();
	
	private Cache<String, Map<String, Integer>> botLeaderboardCache = Caffeine.newBuilder()
			  .expireAfterWrite(12, TimeUnit.HOURS)
			  .maximumSize(1)
			  .build();
	
	public GlobalGilPageData getGlobalGilData() {
		GlobalGilPageData data = null;
		GlobalGilHistory todaysData = this.globalGilHistoryRepo.getFirstGlobalGilHistory();
		List<GlobalGilHistory> historyByDay = this.globalGilHistoryRepo.getGlobalGilHistoryByCalendarTimeType(ChronoUnit.DAYS);
		List<GlobalGilHistory> historyByWeek = this.globalGilHistoryRepo.getGlobalGilHistoryByCalendarTimeType(ChronoUnit.WEEKS);
		List<GlobalGilHistory> historyByMonth = this.globalGilHistoryRepo.getGlobalGilHistoryByCalendarTimeType(ChronoUnit.MONTHS);
		
		data = new GlobalGilPageData(todaysData, historyByDay, historyByWeek, historyByMonth);
		
		return data;
	}
	
	@SneakyThrows
	public Double percentageOfGlobalGil(Integer balance) {
		Double percentage = new Double(0);
		if(balance != null) {
			GlobalGilHistory todaysData = this.globalGilHistoryRepo.getFirstGlobalGilHistory();
			percentage = ((new Double(balance)/new Double(todaysData.getGlobal_gil_count())));
		}
		return percentage;
	}
	
	public Integer getLeaderboardPosition(String player) {
		String lowercasePlayer = StringUtils.lowerCase(player);
		Integer position =  this.dumpService.getLeaderboard().get(lowercasePlayer);
		return position;
	}
	
	public synchronized Map<String, Integer> getBotLeaderboard() {
		Map<String, Integer> botLeaderboard = this.botLeaderboardCache.getIfPresent(BOT_LEADERBOARD_KEY);
		if(botLeaderboard == null) {
			log.warn("bot leaderboard cache was busted, creating new value");
			botLeaderboard = this.generateBotLeaderboard();
			this.botLeaderboardCache.put(BOT_LEADERBOARD_KEY, botLeaderboard);
		}
		
		return botLeaderboard;
	}
	
	protected Map<String, Integer> generateBotLeaderboard() {
		Map<String, Integer> botBalances = new TreeMap<String, Integer>(this.dumpService.getBotCache().parallelStream().filter(botName -> this.dumpService.getBalanceCache().containsKey(botName))
				.collect(Collectors.toMap(Function.identity(), bot -> this.dumpService.getBalanceCache().get(bot))));
		return botBalances;
	}
	
	public Map<String, Integer> getTopPlayers(Integer count) {
		BiMap<String, Integer> topPlayers = HashBiMap.create();
		topPlayers.putAll(this.dumpService.getLeaderboard().keySet().parallelStream().filter(player -> !this.dumpService.getBotCache().contains(player))
				.filter(player -> this.playerRecordRepo.findById(StringUtils.lowerCase(player)).isPresent())
				.collect(Collectors.toMap(Function.identity(), player -> this.dumpService.getLeaderboard().get(player))));
		Set<Integer> topValues = topPlayers.values().stream().sorted().limit(count).collect(Collectors.toSet());
		
		BiMap<Integer, String> topPlayersInverseMap = topPlayers.inverse();
		Map<String, Integer> leaderboardWithoutBots = topValues.stream().collect(Collectors.toMap(rank -> topPlayersInverseMap.get(rank), Function.identity()));
		return leaderboardWithoutBots;
	}
	
	
	public synchronized PlayerLeaderboard getLeaderboard() {
		PlayerLeaderboard leaderboard = this.leaderboardCache.getIfPresent(LEADERBOARD_KEY);
		if(leaderboard == null) {
			log.warn("Leaderboard cache was busted, creating new value");
			leaderboard = this.generatePlayerLeaderboardData();
			this.leaderboardCache.put(LEADERBOARD_KEY, leaderboard);
		}
		
		return leaderboard;
	}
	
	protected PlayerLeaderboard generatePlayerLeaderboardData() {
		Map<String, Integer> topPlayers = this.getTopPlayers(TOP_PLAYERS);
		List<LeaderboardData> allPlayers =  topPlayers.keySet().parallelStream().map(player-> this.collectPlayerLeaderboardDataByPlayer(player)).filter(result -> result != null).sorted().collect(Collectors.toList());
		Collections.reverse(allPlayers);
		for(int i = 0; i < allPlayers.size(); i++) { 
			allPlayers.get(i).setRank(i + 1); 
		}
		
		List<LeaderboardData> highestPlayers = allPlayers.parallelStream().filter(leaderboardData -> leaderboardData.getRank() <= HIGHEST_PLAYERS).collect(Collectors.toList());
		List<LeaderboardData> topPlayersList = allPlayers.parallelStream().filter(leaderboardData -> leaderboardData.getRank() > HIGHEST_PLAYERS && leaderboardData.getRank() <= TOP_PLAYERS).collect(Collectors.toList());
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
		for(int rank = 1; rank <= TOP_PLAYERS; rank++) {
			ExpLeaderboardEntry result = null;
			String player = this.dumpService.getExpRankLeaderboardByRank().get(rank);
			Optional<PlayerRecord> maybePlayer = this.playerRecordRepo.findById(player);
			if(maybePlayer.isPresent()) {
				Short level = maybePlayer.get().getLastKnownLevel();
				Short exp = maybePlayer.get().getLastKnownRemainingExp();
				SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy");
				String lastActive = format.format(maybePlayer.get().getLastActive());
				
				Integer prestigeLevel = 0;
				List<String> prestigeSkills = this.dumpService.getPrestigeSkillsCache().get(player);
				if(prestigeSkills != null) {
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
			.map(player -> new PrestigeTableEntry(player, this.dumpService.getPrestigeSkillsCache().get(player).size())).collect(Collectors.toList());
		SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy");
		results.stream().forEach(prestigeTableEntry -> prestigeTableEntry.setLastActive(format.format(this.dumpService.getLastActiveCache().get(prestigeTableEntry.getName()))));
		Collections.sort(results);
		
		return results;
	}
	
	//this time let's take it hour by hour, and then use my original date slice algorithm
	@SneakyThrows
	public LeaderboardBalanceData getLabelsAndSetRelevantBalanceHistories(List<LeaderboardBalanceHistoryEntry> entries, Integer count) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh");
		
		//dynamically pick best hours to find entries for one week
		int hoursToTrack = 168;
		int hoursPerSlice = hoursToTrack / count;
		List<Date> dateSlices = new ArrayList<>();
	    for(int i = 0 ; i < count; i++) { 
	    	Calendar calendar = Calendar.getInstance();
	    	int hourSliceSize = i * hoursPerSlice * -1; calendar.add(Calendar.HOUR,hourSliceSize); 
	    	Date dateSlice = calendar.getTime();
	    	sdf.parse(sdf.format(dateSlice));
			dateSlices.add(dateSlice); 
	    }
	    
	    Collections.sort(dateSlices);
	    
		//simplify all dates to nearest hour
		for(LeaderboardBalanceHistoryEntry entry : entries) {
			for(BalanceHistory balanceHistory : entry.getBalanceHistory()) {
				String simplifiedDateString = sdf.format(balanceHistory.getCreate_timestamp());
				Date simplifiedDate = sdf.parse(simplifiedDateString);
				balanceHistory.setCreate_timestamp(new Timestamp(simplifiedDate.getTime()));
			}
		}
	    
	    //now lets extrapolate each player's balance history by hour
	    List<LeaderboardBalanceHistoryEntry> extrapolatedData = new ArrayList<>();
	    for(LeaderboardBalanceHistoryEntry entry: entries) {
	    	LeaderboardBalanceHistoryEntry extrapolatedEntry = new LeaderboardBalanceHistoryEntry(entry.getPlayerName(), new ArrayList<BalanceHistory>());
	    	Integer currentAmount = 0;
	    	for(int i = 0; i < hoursToTrack; i++) {
	    		Calendar calendar = Calendar.getInstance();
	    		calendar.add(Calendar.HOUR, (-1 * hoursToTrack) + i);
	    		
	    		//find nearest balanceHistory match
	    		boolean matchFound = false;
	    		for(int j = 0; j < entry.getBalanceHistory().size() && !matchFound; j++) {
	    			if(this.twoDatesMatchSameExactHour(calendar.getTime(), entry.getBalanceHistory().get(j).getCreate_timestamp())) {
	    				matchFound = true;
	    				currentAmount = entry.getBalanceHistory().get(j).getBalance();
	    			}
	    		}
	    		
	    		extrapolatedEntry.getBalanceHistory().add(new BalanceHistory(entry.getPlayerName(), currentAmount,calendar.getTime()));
	    	}
	    	extrapolatedData.add(extrapolatedEntry);
    	}
	    
		//reduce balance history to appropriate values
		for(LeaderboardBalanceHistoryEntry entry: extrapolatedData) {
			List<BalanceHistory> truncatedHistory = new ArrayList<>();
			for(int i = 0; i < dateSlices.size(); i++) {
				Date currentSlice = dateSlices.get(i);
				boolean foundEntry = false;
				//search balance history for first entry with the correct hour
				Integer currentAmount = 0;
				for(int j = 0; j < entry.getBalanceHistory().size() && !foundEntry; j++) {
					Calendar currentSliceCalendar = Calendar.getInstance();
					currentSliceCalendar.setTime(currentSlice);
					Calendar currentBalanceHistoryDateCalendar = Calendar.getInstance();
					Date currentBalanceHistoryDate = entry.getBalanceHistory().get(j).getCreate_timestamp();
					currentBalanceHistoryDateCalendar.setTime(currentBalanceHistoryDate);
					if(this.twoDatesMatchSameExactHour(currentSlice, currentBalanceHistoryDate)) 
					{
						foundEntry = true;
						truncatedHistory.add(entry.getBalanceHistory().get(j));
						currentAmount = entry.getBalanceHistory().get(j).getBalance();
					}
				}
				
				if (!foundEntry && this.twoDatesMatchSameExactHour(currentSlice, new Date())) {
					foundEntry = true;
					Optional<PlayerRecord> maybePlayer = this.playerRecordRepo.findById(StringUtils.lowerCase(entry.getPlayerName()));
					if(maybePlayer.isPresent()) {
						foundEntry = true;
						Date simplifiedDate = sdf.parse(sdf.format(new Date()));
						truncatedHistory.add(new BalanceHistory(entry.getPlayerName(), maybePlayer.get().getLastKnownAmount(), simplifiedDate));
					}
				}
				//if none found, create a valid blank entry
				if(!foundEntry) {
					log.warn("could not find an entry even with fully extrapolated data, something went wrong");
					truncatedHistory.add(new BalanceHistory(entry.getPlayerName(), currentAmount, currentSlice));
				}
				//reset foundEntry
				foundEntry = false;
			}
			entry.setBalanceHistory(truncatedHistory);
		}
		
		//smooth out zero results.  0 is not possible for balances, so must be caused by missing data
		for(LeaderboardBalanceHistoryEntry entry: extrapolatedData) {
			this.smoothOutZeroes(entry);
		}
		
		LeaderboardBalanceData data = new LeaderboardBalanceData(dateSlices, extrapolatedData);
	    	
		return data;
	}
	
	/**
	 * Zero is not a valid result for balance, and must be caused by missing data.
	 * This function iterates over all balance history data backwards, setting any 0 value to
	 * the first valid value that follows it.
	 * 
	 * @param extrapolatedData
	 */
	protected void smoothOutZeroes(LeaderboardBalanceHistoryEntry extrapolatedData) {
		//increment in reverse
		Integer previousAmount = null;
		List<BalanceHistory> balanceHistory = extrapolatedData.getBalanceHistory();
		for(int i = balanceHistory.size() - 1; i >= 0; i--) {
			BalanceHistory currentBalanceHistory = balanceHistory.get(i);
			if(previousAmount == null) {
				previousAmount = currentBalanceHistory.getBalance();
			} else if(currentBalanceHistory.getBalance().equals(0)) {
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
		boolean result = currentSliceCalendar.get(Calendar.MONTH) == currentBalanceHistoryDateCalendar.get(Calendar.MONTH)
				&& currentSliceCalendar.get(Calendar.DAY_OF_MONTH) == currentBalanceHistoryDateCalendar.get(Calendar.DAY_OF_MONTH)
				&& currentSliceCalendar.get(Calendar.HOUR_OF_DAY) == currentBalanceHistoryDateCalendar.get(Calendar.HOUR_OF_DAY);
		
		return result;
	}
}
