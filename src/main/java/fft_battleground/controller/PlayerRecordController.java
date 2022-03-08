package fft_battleground.controller;

import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import fft_battleground.controller.response.model.GilDateGraphEntry;
import fft_battleground.dump.DumpReportsService;
import fft_battleground.dump.DumpService;
import fft_battleground.dump.reports.model.LeaderboardBalanceData;
import fft_battleground.dump.reports.model.LeaderboardBalanceHistoryEntry;
import fft_battleground.exception.TournamentApiException;
import fft_battleground.repo.model.BalanceHistory;
import fft_battleground.repo.model.GlobalGilHistory;
import fft_battleground.repo.model.PlayerRecord;
import fft_battleground.repo.model.PlayerSkills;
import fft_battleground.repo.repository.BalanceHistoryRepo;
import fft_battleground.repo.repository.GlobalGilHistoryRepo;
import fft_battleground.repo.repository.PlayerRecordRepo;
import fft_battleground.repo.util.BalanceType;
import fft_battleground.repo.util.BalanceUpdateSource;
import fft_battleground.tournament.TournamentService;
import fft_battleground.util.GenericResponse;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import springfox.documentation.annotations.ApiIgnore;

@Controller
@RequestMapping("/api/players")
@Slf4j
@CacheConfig(cacheNames = {"botBalanceHistory", "playerBalanceHistory"})
@ApiIgnore
public class PlayerRecordController {

	@Autowired
	private PlayerRecordRepo playerRecordRepo;
	
	@Autowired
	private BalanceHistoryRepo balanceHistoryRepo;
	
	@Autowired
	private GlobalGilHistoryRepo globalGilHistoryRepo;
	
	@Autowired
	private TournamentService tournamentService;
	
	@Autowired
	private DumpService dumpService;
	
	@Autowired
	private DumpReportsService dumpReportsService;
	
	@Scheduled(cron = "0 30 0 * * ?")
	public void clearPlayerLeaderboard() {
		synchronized(this.playerLeaderboardData) {
			this.playerLeaderboardData.clear();
		}
	}
	
	private Map<String, LeaderboardBalanceData> playerLeaderboardData = new HashMap<>();
	
	@ApiIgnore
	@GetMapping("/playerRecord/{playerName}")
	public ResponseEntity<GenericResponse<PlayerRecord>> getPlayerData(@PathVariable("playerName") String playerName, HttpServletRequest request) throws TournamentApiException {
		log.info("Player api called for player {}", playerName);
		String idString = StringUtils.lowerCase(playerName);
		Optional<PlayerRecord> maybePlayer =  this.playerRecordRepo.findById(idString);
		if(maybePlayer.isPresent()) {
			//map metadata
			for(PlayerSkills playerSkill : maybePlayer.get().getPlayerSkills()) {
				playerSkill.setMetadata(this.tournamentService.getCurrentTips().getPlayerSkillMetadata(playerSkill.getSkill()));
			}
			
			return GenericResponse.createGenericResponseEntity(maybePlayer.get());
		} else {
			return GenericResponse.<PlayerRecord>createGenericResponseEntity(null, "Player could not be found", HttpStatus.NOT_FOUND);
		}
	}
	@ApiIgnore
	@GetMapping("/balanceHistory")
	public @ResponseBody ResponseEntity<GenericResponse<LeaderboardBalanceData>>
	getBalanceHistory(@RequestParam(name="player", required=true) String player, @RequestParam(name="count", required=true) Integer count) {
		LinkedList<BalanceHistory> balanceHistories = new LinkedList<>(this.balanceHistoryRepo.getTournamentBalanceHistoryFromPastWeek(player));
		
		if(balanceHistories.size() < count) {
			for(int i = 0; i < balanceHistories.size() - count; i++) {
				balanceHistories.push(new BalanceHistory(player, 0, BalanceType.DUMP, BalanceUpdateSource.NONE));
			}
		}
		
		List<LeaderboardBalanceHistoryEntry> entries = Arrays.asList(new LeaderboardBalanceHistoryEntry[] {new LeaderboardBalanceHistoryEntry(player, balanceHistories)});
		
		LeaderboardBalanceData data = this.dumpReportsService.getLabelsAndSetRelevantBalanceHistories(entries, count);
		
		return GenericResponse.createGenericResponseEntity(data);
	}
	
	@ApiIgnore
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
		
		LeaderboardBalanceData data = this.dumpReportsService.getLabelsAndSetRelevantBalanceHistories(botBalanceHistoryEntries, count);
		
		return data;
	}
	
	@ApiIgnore
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
	
	@ApiOperation(value="Returns a list of all players recorded in the Viewer database")
	@GetMapping("/playerList")
	public ResponseEntity<GenericResponse<List<String>>> playerNames() {
		List<String> playerNames = this.playerRecordRepo.findPlayerNames().stream().map(player -> StringUtils.trim(StringUtils.lowerCase(player)))
				.collect(Collectors.toSet()).stream().sorted().collect(Collectors.toList());
		return GenericResponse.createGenericResponseEntity(playerNames);
	}
	
	@ApiIgnore
	@GetMapping("/globalGilHistoryGraphData")
	public ResponseEntity<GenericResponse<List<GilDateGraphEntry>>> 
	getGlobalGilHistoryGraphData(@RequestParam("timeUnit") String unit) {
		List<GilDateGraphEntry> results = null;
		ChronoUnit timeUnit = null;
		if(StringUtils.equalsIgnoreCase(unit, "day")) {
			timeUnit = ChronoUnit.DAYS;
		} else if(StringUtils.equalsIgnoreCase(unit, "week")) {
			timeUnit = ChronoUnit.WEEKS;
		} else if(StringUtils.equalsIgnoreCase(unit, "month")) {
			timeUnit = ChronoUnit.MONTHS;
		} else if(StringUtils.equalsIgnoreCase(unit, "year")) {
			timeUnit = ChronoUnit.YEARS;
		}
		
		if(timeUnit != null) {
			List<GlobalGilHistory> globalGilHistoryList = this.globalGilHistoryRepo.getGlobalGilHistoryByCalendarTimeType(timeUnit);
			Collections.sort(globalGilHistoryList);
			results = globalGilHistoryList.parallelStream().map(globalGilHistory -> new GilDateGraphEntry(globalGilHistory.getGlobal_gil_count(), globalGilHistory.getDate()))
					.collect(Collectors.toList());
		}
		
		return GenericResponse.createGenericResponseEntity(results);
		
	}
	
	@Cacheable("playerBalanceHistory")
	public LeaderboardBalanceData generatePlayerBalanceHistory(String players, int count) {
		List<String> playerList = Arrays.asList(StringUtils.split(players, ","));
		Map<String, LinkedList<BalanceHistory>> playerBalanceHistories = playerList.parallelStream()
				.collect(Collectors.toMap(Function.identity(), playerName -> new LinkedList<BalanceHistory>(this.balanceHistoryRepo.getTournamentBalanceHistoryFromPastWeek(playerName))));
		
		List<LeaderboardBalanceHistoryEntry> playerBalanceHistoryEntries = playerBalanceHistories.keySet().parallelStream().map(playerName -> new LeaderboardBalanceHistoryEntry(playerName, playerBalanceHistories.get(playerName)))
				.collect(Collectors.toList());
		
		LeaderboardBalanceData data = this.dumpReportsService.getLabelsAndSetRelevantBalanceHistories(playerBalanceHistoryEntries, count);
		
		return data;
	}
}
