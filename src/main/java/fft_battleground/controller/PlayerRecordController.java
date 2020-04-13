package fft_battleground.controller;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Sets;

import fft_battleground.bot.model.BalanceType;
import fft_battleground.bot.model.BalanceUpdateSource;
import fft_battleground.dump.DumpService;
import fft_battleground.repo.BalanceHistoryRepo;
import fft_battleground.repo.PlayerRecordRepo;
import fft_battleground.repo.model.BalanceHistory;
import fft_battleground.repo.model.PlayerRecord;
import fft_battleground.tournament.TournamentService;
import fft_battleground.util.GenericResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/api/players")
@Slf4j
@CacheConfig(cacheNames = {"botBalanceHistory", "playerBalanceHistory"})
public class PlayerRecordController {

	@Autowired
	private PlayerRecordRepo playerRecordRepo;
	
	@Autowired
	private BalanceHistoryRepo balanceHistoryRepo;
	
	@Autowired
	private TournamentService tournamentService;
	
	@Autowired
	private DumpService dumpService;
	
	@Scheduled(cron = "0 30 0 * * ?")
	public void clearPlayerLeaderboard() {
		synchronized(this.playerLeaderboardData) {
			this.playerLeaderboardData.clear();
		}
	}
	
	private Map<String, LeaderboardBalanceData> playerLeaderboardData = new HashMap<>();
	
	@GetMapping("/playerRecord/{playerName}")
	public ResponseEntity<GenericResponse<PlayerRecord>> getPlayerData(@PathVariable("playerName") String playerName) {
		String idString = StringUtils.lowerCase(playerName);
		Optional<PlayerRecord> maybePlayer =  this.playerRecordRepo.findById(idString);
		if(maybePlayer.isPresent()) {
			//map metadata
			maybePlayer.get().getPlayerSkills().stream().forEach(playerSkill -> playerSkill.setMetadata(this.tournamentService.getCurrentTips().getPlayerSkillMetadata(playerSkill.getSkill())));
			
			return GenericResponse.createGenericResponseEntity(maybePlayer.get());
		} else {
			return GenericResponse.<PlayerRecord>createGenericResponseEntity(null, "Player could not be found", HttpStatus.NOT_FOUND);
		}
	}
	
	@GetMapping("/balanceHistory")
	public @ResponseBody ResponseEntity<GenericResponse<LeaderboardBalanceData>>
	getBalanceHistory(@RequestParam(name="player", required=true) String player, @RequestParam(name="count", required=true) Integer count) {
		LinkedList<BalanceHistory> balanceHistories = new LinkedList<>(this.balanceHistoryRepo.getTournamentBalanceHistory(player, PageRequest.of(0,count)));
		
		if(balanceHistories.size() < count) {
			for(int i = 0; i < balanceHistories.size() - count; i++) {
				balanceHistories.push(new BalanceHistory(player, 0, BalanceType.DUMP, BalanceUpdateSource.NONE));
			}
		}
		
		List<LeaderboardBalanceHistoryEntry> entries = Arrays.asList(new LeaderboardBalanceHistoryEntry[] {new LeaderboardBalanceHistoryEntry(player, balanceHistories)});
		
		LeaderboardBalanceData data = this.getLabelsAndSetRelevantBalanceHistories(entries, count);
		
		return GenericResponse.createGenericResponseEntity(data);
	}
	
	@GetMapping("/botLeaderboardBalanceHistory")
	public @ResponseBody ResponseEntity<GenericResponse<LeaderboardBalanceData>> 
	getBotBalanceHistory(@RequestParam(name="count", required=true) Integer count) {
		LeaderboardBalanceData data = this.generateBotBalanceHistory(count);
		
		return GenericResponse.createGenericResponseEntity(data);
	}
	
	@Cacheable("botBalanceHistory")
	public LeaderboardBalanceData generateBotBalanceHistory(Integer count) {
		Map<String, List<BalanceHistory>> botBalanceHistories = this.dumpService.getBotNames().parallelStream()
				.collect(Collectors.toMap(Function.identity(), botName -> this.balanceHistoryRepo.getTournamentBalanceHistoryFromPastWeek(botName)));
		List<LeaderboardBalanceHistoryEntry> botBalanceHistoryEntries = botBalanceHistories.keySet().parallelStream()
				.map(playerName -> new LeaderboardBalanceHistoryEntry(playerName, botBalanceHistories.get(playerName)))
				.filter(leaderboardBalanceHistory -> leaderboardBalanceHistory.getBalanceHistory().size() >= count).collect(Collectors.toList());
		
		//Map<LeaderboardBalanceHistoryEntry, Integer> balanceHistorySizes = botBalanceHistoryEntries.stream().collect(Collectors.toMap(Function.identity(), balanceHistoryEntry -> balanceHistoryEntry.getBalanceHistory().size()));
		
		LeaderboardBalanceData data = this.getLabelsAndSetRelevantBalanceHistories(botBalanceHistoryEntries, count);
		
		return data;
	}
	
	@GetMapping("/playerLeaderboardBalanceHistory")
	public @ResponseBody ResponseEntity<GenericResponse<LeaderboardBalanceData>>
	getPlayerBalanceHistories(@RequestParam(name="players", required=true) String players, @RequestParam(name="count", required=true) int count) {
		LeaderboardBalanceData data;
		synchronized(this.playerLeaderboardData) {
			if(this.playerLeaderboardData.get(players) == null) {
				data = this.generatePlayerBalanceHistory(players, count);
				this.playerLeaderboardData.put(players, data);
			} else {
				data = this.playerLeaderboardData.get(players);
			}
		}
		
		return GenericResponse.createGenericResponseEntity(data);
	}
	
	@Cacheable("playerBalanceHistory")
	public LeaderboardBalanceData generatePlayerBalanceHistory(String players, int count) {
		Map<String, List<BalanceHistory>> playerBalanceHistories = Arrays.asList(StringUtils.split(players, ",")).parallelStream()
				.collect(Collectors.toMap(Function.identity(), playerName -> this.balanceHistoryRepo.getTournamentBalanceHistoryFromPastWeek(playerName)));
		List<LeaderboardBalanceHistoryEntry> playerBalanceHistoryEntries = playerBalanceHistories.keySet().parallelStream().map(playerName -> new LeaderboardBalanceHistoryEntry(playerName, playerBalanceHistories.get(playerName)))
				.filter(leaderboardBalanceHistory -> leaderboardBalanceHistory.getBalanceHistory().size() >= count).collect(Collectors.toList());
		
		LeaderboardBalanceData data = this.getLabelsAndSetRelevantBalanceHistories(playerBalanceHistoryEntries, count);
		
		return data;
	}
	
	@GetMapping("/playerList")
	public ResponseEntity<GenericResponse<List<String>>> playerNames() {
		List<String> playerNames = this.playerRecordRepo.findPlayerNames().stream().sorted().collect(Collectors.toList());
		return GenericResponse.createGenericResponseEntity(playerNames);
	}
	
	@SneakyThrows
	protected List<Date> reduceLeaderboardEntriesToFit(List<LeaderboardBalanceHistoryEntry> entries, Integer count) {
		//dynamically pick best hours to find entries for one week
		int hoursPerSlice = 168 / count;
		List<Date> dateSlices = new ArrayList<>();
		/*
		 * for(int i = 0 ; i < count; i++) { Calendar calendar = Calendar.getInstance();
		 * int hourSliceSize = i * hoursPerSlice * -1; calendar.add(Calendar.HOUR,
		 * hourSliceSize); Date dateSlice = calendar.getTime();
		 * dateSlices.add(dateSlice); }
		 */
		
		//simplify all dates to nearest hour
		SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh");
		for(LeaderboardBalanceHistoryEntry entry : entries) {
			for(BalanceHistory balanceHistory : entry.getBalanceHistory()) {
				String simplifiedDateString = sdf.format(balanceHistory.getCreate_timestamp());
				Date simplifiedDate = sdf.parse(simplifiedDateString);
				balanceHistory.setCreate_timestamp(new Timestamp(simplifiedDate.getTime()));
			}
		}
		
		//instead try to first find all shared dates between the leaderboards
		List<Set<Date>> listOfAllBalanceHistoryDates = new ArrayList<>();
		for(LeaderboardBalanceHistoryEntry entry: entries) {
			Set<Date> getAllBalanceHistoryDates = new HashSet<>();
			for(BalanceHistory balanceHistory: entry.getBalanceHistory()) {
				getAllBalanceHistoryDates.add(balanceHistory.getCreate_timestamp());
			}
			listOfAllBalanceHistoryDates.add(getAllBalanceHistoryDates);
		}
		
		//find the intersect between all of the sets
		Set<Date> currentIntersection = null;
		for(int i = 0; i < listOfAllBalanceHistoryDates.size(); i++) {
			if(i == 0) {
				currentIntersection = listOfAllBalanceHistoryDates.get(i);
			} else {
				currentIntersection = Sets.intersection(currentIntersection, listOfAllBalanceHistoryDates.get(i));
			}
		}
		
		//find the best slices based on count
		int indexesPerSlice = currentIntersection.size() / count;
		List<Date> currentIntersectionList = new ArrayList<>(currentIntersection);
		for(int i = 0; i < currentIntersectionList.size(); i = i + indexesPerSlice) {
			dateSlices.add(currentIntersectionList.get(i));
		}
		
		//be sure to limit the number of items starting from the most recent going backwards
		Collections.sort(dateSlices);
		Collections.reverse(dateSlices);
		//be sure we only have the number of slices we need
		dateSlices = dateSlices.stream().limit(count).collect(Collectors.toList());
		Collections.reverse(dateSlices);
		
		//reduce balance history to appropriate values
		for(LeaderboardBalanceHistoryEntry entry: entries) {
			List<BalanceHistory> truncatedHistory = new ArrayList<>();
			for(int i = 0; i < dateSlices.size(); i++) {
				Date currentSlice = dateSlices.get(i);
				boolean foundEntry = false;
				//search balance history for first entry with the correct hour
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
					}
				}
				//if none found, create a valid blank entry
				if(!foundEntry) {
					truncatedHistory.add(new BalanceHistory(entry.getPlayerName(), 0, currentSlice));
				}
				//reset foundEntry
				foundEntry = false;
			}
			entry.setBalanceHistory(truncatedHistory);
		}
		
		return dateSlices;
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
		
		LeaderboardBalanceData data = new LeaderboardBalanceData(dateSlices, extrapolatedData);
	    	
		return data;
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

@Data
@AllArgsConstructor
@NoArgsConstructor
class LeaderboardBalanceHistoryEntry {
	private String playerName;
	private List<BalanceHistory> balanceHistory;
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class LeaderboardBalanceData {
	private List<Date> labels;
	private List<LeaderboardBalanceHistoryEntry> leaderboardBalanceHistories;
}
