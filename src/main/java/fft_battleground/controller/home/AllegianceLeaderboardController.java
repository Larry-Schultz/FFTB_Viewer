package fft_battleground.controller.home;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;

import fft_battleground.exception.CacheMissException;
import fft_battleground.metrics.AccessTracker;
import fft_battleground.reports.AllegianceReportGenerator;
import fft_battleground.reports.model.AllegianceLeaderboardWrapper;
import fft_battleground.util.GenericResponse;
import springfox.documentation.annotations.ApiIgnore;

@Controller
public class AllegianceLeaderboardController extends AbstractHomeController {

	@Autowired
	private AllegianceReportGenerator allegianceReportGenerator;
	
	@Autowired
	public AllegianceLeaderboardController(AccessTracker accessTracker) {
		super(accessTracker);
	}
	
	@ApiIgnore
	@GetMapping("/allegianceLeaderboard")
	public @ResponseBody ResponseEntity<GenericResponse<AllegianceLeaderboardWrapper>> allegianceLeaderboard(@RequestHeader(value = "User-Agent", required=false, defaultValue="") String userAgent, 
			HttpServletRequest request) throws CacheMissException {
		this.logAccess("allegiance leaderboard", userAgent, request);
		AllegianceLeaderboardWrapper leaderboard = this.allegianceReportGenerator.getReport();
		return GenericResponse.createGenericResponseEntity(leaderboard);
	}

}
