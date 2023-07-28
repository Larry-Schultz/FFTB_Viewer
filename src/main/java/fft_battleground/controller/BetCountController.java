package fft_battleground.controller;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import fft_battleground.controller.request.model.PlayerListRequest;
import fft_battleground.controller.request.model.TournamentListRequest;
import fft_battleground.controller.response.model.PlayerList;
import fft_battleground.controller.response.model.PlayerWinRatio;
import fft_battleground.dump.cache.set.BotCache;
import fft_battleground.repo.model.BotBetData;
import fft_battleground.repo.repository.BotBetDataRepo;
import fft_battleground.repo.repository.PlayerRecordRepo;
import fft_battleground.util.GenericResponse;
import springfox.documentation.annotations.ApiIgnore;

@Controller
@RequestMapping("/betcount")
public class BetCountController {

	@Autowired
	private BotBetDataRepo botBetDataRepo;
	
	@Autowired
	private PlayerRecordRepo playerRecordRepo;
	
	@Autowired
	private BotCache botCache;
	
	@ApiIgnore
	@GetMapping("/first")
	public @ResponseBody ResponseEntity<GenericResponse<BotBetData>> getFirstTournamentTracked() {
		return GenericResponse.createGenericResponseEntity(this.botBetDataRepo.getFirstTournament());
	}
	
	@ApiIgnore
	@PostMapping("/")
	public @ResponseBody ResponseEntity<GenericResponse<List<BotBetData>>> 
	getBetCountsForTournaments(@RequestBody TournamentListRequest tournamentListRequest) 
	{
		List<BotBetData> botBetDataForTournaments = this.botBetDataRepo.getBotBetDataForTournaments(tournamentListRequest.getTournamentIds());
		return GenericResponse.createGenericResponseEntity(botBetDataForTournaments);
	}
	
	@ApiIgnore
	@PostMapping("/playerBetWinRatios")
	public @ResponseBody ResponseEntity<GenericResponse<List<PlayerWinRatio>>>
	getBetWinRatiosForPlayers(@RequestBody PlayerListRequest playerListRequest) 
	{
		Date oneHourBack = this.oneHourBack(playerListRequest.getActiveDate());
		List<PlayerWinRatio> winRatios = this.playerRecordRepo.getPlayerBetWinRatios(oneHourBack);
		return GenericResponse.createGenericResponseEntity(winRatios);
	}
	
	@ApiIgnore
	@PostMapping("/playerFightWinRatios")
	public @ResponseBody ResponseEntity<GenericResponse<List<PlayerWinRatio>>>
	getFightWinRatiosForPlayers(@RequestBody PlayerListRequest playerListRequest) 
	{
		Date oneHourBack = this.oneHourBack(playerListRequest.getActiveDate());
		List<PlayerWinRatio> winRatios = this.playerRecordRepo.getPlayerFightWinRatios(oneHourBack);
		return GenericResponse.createGenericResponseEntity(winRatios);
	}
	
	@ApiIgnore
	@PostMapping("/playerSubscribers")
	public @ResponseBody ResponseEntity<GenericResponse<PlayerList>> 
	getSubscribers(@RequestBody PlayerListRequest playerListRequest) 
	{
		Date oneHourBack = this.oneHourBack(playerListRequest.getActiveDate());
		List<String> subscribers = this.playerRecordRepo.getSubscribers(oneHourBack);
		PlayerList data = new PlayerList(subscribers);
		return GenericResponse.createGenericResponseEntity(data);
	}
	
	@ApiIgnore
	@GetMapping("/bots")
	public @ResponseBody ResponseEntity<GenericResponse<PlayerList>> 
	getBots() {
		PlayerList data = new PlayerList(new ArrayList<String>(this.botCache.getSet()));
		return GenericResponse.createGenericResponseEntity(data);
	}
	
	private Date oneHourBack(Date activeDate) {
		Calendar cal = Calendar.getInstance();
		// remove next line if you're always using the current time.
		cal.setTime(activeDate);
		cal.add(Calendar.HOUR, -1);
		Date oneHourBack = cal.getTime();
		return oneHourBack;
	}
}
