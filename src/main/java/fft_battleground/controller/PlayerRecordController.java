package fft_battleground.controller;

import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import fft_battleground.controller.response.model.GilDateGraphEntry;
import fft_battleground.dump.DumpReportsService;
import fft_battleground.dump.reports.model.LeaderboardBalanceData;
import fft_battleground.dump.reports.model.LeaderboardBalanceHistoryEntry;
import fft_battleground.dump.service.BalanceHistoryServiceImpl;
import fft_battleground.exception.CacheMissException;
import fft_battleground.exception.TournamentApiException;
import fft_battleground.repo.model.BalanceHistory;
import fft_battleground.repo.model.GlobalGilHistory;
import fft_battleground.repo.model.PlayerRecord;
import fft_battleground.repo.model.PlayerSkills;
import fft_battleground.repo.model.PrestigeSkills;
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
	private DumpReportsService dumpReportsService;
	
	@Autowired
	private BalanceHistoryServiceImpl balanceHistoryUtil;
	
	@ApiIgnore
	@GetMapping("/playerRecord/{playerName}")
	@Transactional(propagation=Propagation.REQUIRED)
	public ResponseEntity<GenericResponse<PlayerRecord>> getPlayerData(@PathVariable("playerName") String playerName, HttpServletRequest request) throws TournamentApiException {
		log.info("Player api called for player {}", playerName);
		String idString = StringUtils.lowerCase(playerName);
		Optional<PlayerRecord> maybePlayer =  this.playerRecordRepo.findById(idString);
		if(maybePlayer.isPresent()) {
			//map metadata
			Hibernate.initialize(maybePlayer.get().getPlayerSkills());
			Hibernate.initialize(maybePlayer.get().getPrestigeSkills());
			for(PlayerSkills playerSkill : maybePlayer.get().getPlayerSkills()) {
				playerSkill.setMetadata(this.tournamentService.getCurrentTips().getPlayerSkillMetadata(playerSkill.getSkill()));
			}
			for(PrestigeSkills prestigeSkill : maybePlayer.get().getPrestigeSkills()) {
				prestigeSkill.setMetadata(this.tournamentService.getCurrentTips().getPlayerSkillMetadata(prestigeSkill.getSkill()));
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
		
		LeaderboardBalanceData data = this.balanceHistoryUtil.getLabelsAndSetRelevantBalanceHistories(entries, count);
		
		return GenericResponse.createGenericResponseEntity(data);
	}
	
	@ApiIgnore
	@GetMapping("/botLeaderboardBalanceHistory")
	public @ResponseBody ResponseEntity<GenericResponse<LeaderboardBalanceData>> 
	getBotBalanceHistory(@RequestParam(name="count", required=true) Integer maxHistoryEntries, 
			@RequestParam(name="bots", required=false, defaultValue="10") int maxNumberOfActiveBots) throws CacheMissException {
		LeaderboardBalanceData data = this.dumpReportsService.getBotLeaderboardBalanceHistory();
		
		return GenericResponse.createGenericResponseEntity(data);
	}
	
	@ApiIgnore
	@GetMapping("/playerLeaderboardBalanceHistory")
	public @ResponseBody ResponseEntity<GenericResponse<LeaderboardBalanceData>>
	getPlayerBalanceHistories(@RequestParam(name="players", required=true) String players, @RequestParam(name="count", required=true) int count) throws CacheMissException {
		LeaderboardBalanceData data = this.dumpReportsService.getPlayerLeaderboardBalanceHistory();
		
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
}
