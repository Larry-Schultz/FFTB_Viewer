package fft_battleground.controller.home;

import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import fft_battleground.controller.response.model.PlayerLeaderboardData;
import fft_battleground.exception.CacheMissException;
import fft_battleground.metrics.AccessTracker;
import fft_battleground.reports.PlayerLeaderboardBalanceHistoryReportGenerator;
import fft_battleground.reports.PlayerLeaderboardReportGenerator;
import fft_battleground.reports.model.LeaderboardBalanceData;
import fft_battleground.reports.model.PlayerLeaderboard;
import fft_battleground.util.GenericResponse;
import io.swagger.annotations.ApiOperation;
import springfox.documentation.annotations.ApiIgnore;

@Controller
public class PlayerLeaderboardController extends AbstractHomeController {

	@Autowired
	private PlayerLeaderboardReportGenerator playerLeaderboardReportGenerator;
	
	@Autowired
	private PlayerLeaderboardBalanceHistoryReportGenerator playerLeaderboardBalanceHistoryReportGenerator;
	
	@Autowired
	public PlayerLeaderboardController(AccessTracker accessTracker) {
		super(accessTracker);
		// TODO Auto-generated constructor stub
	}
	
	@ApiIgnore
	@ApiOperation("returns player leaderboard in json form")
	@GetMapping({"/playerLeaderboard", "/leaderboard"})
	public @ResponseBody ResponseEntity<GenericResponse<PlayerLeaderboardData>> playerLeaderboardPage(@RequestHeader(value = "User-Agent") String userAgent, Model model, 
			HttpServletRequest request) throws CacheMissException {
		this.logAccess("player leaderboard", userAgent, request);
		PlayerLeaderboard leaderboard = this.playerLeaderboardReportGenerator.getReport();
		model.addAttribute("leaderboard", leaderboard);
		String commaDelimitedTopPlayersString = StringUtils.join(leaderboard.getHighestPlayers().stream().map(highestPlayer -> highestPlayer.getName()).collect(Collectors.toList()), ',');
		
		PlayerLeaderboardData data = new PlayerLeaderboardData(leaderboard, leaderboard.formattedGenerationDate(), commaDelimitedTopPlayersString);
		
		return GenericResponse.createGenericResponseEntity(data);
	}
	
	@ApiIgnore
	@GetMapping("/playerLeaderboardBalanceHistory")
	public @ResponseBody ResponseEntity<GenericResponse<LeaderboardBalanceData>>
	getPlayerBalanceHistories() throws CacheMissException {
		LeaderboardBalanceData data = this.playerLeaderboardBalanceHistoryReportGenerator.getReport();
		
		return GenericResponse.createGenericResponseEntity(data);
	}

}
