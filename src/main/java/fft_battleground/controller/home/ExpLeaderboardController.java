package fft_battleground.controller.home;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;

import fft_battleground.controller.response.model.ExpLeaderboardData;
import fft_battleground.metrics.AccessTracker;
import fft_battleground.reports.ExperienceLeaderboardReportGenerator;
import fft_battleground.reports.model.ExpLeaderboard;
import fft_battleground.util.GenericResponse;
import io.swagger.annotations.ApiOperation;
import lombok.SneakyThrows;
import springfox.documentation.annotations.ApiIgnore;

@Controller
public class ExpLeaderboardController extends AbstractHomeController {

	@Autowired
	private ExperienceLeaderboardReportGenerator expLeaderboardGenerator;
	
	@Autowired
	public ExpLeaderboardController(AccessTracker accessTracker) {
		super(accessTracker);
	}
	
	@ApiIgnore
	@ApiOperation("returns experience leaderboard in json form")
	@GetMapping("/expLeaderboard")
	@SneakyThrows
	public @ResponseBody ResponseEntity<GenericResponse<ExpLeaderboardData>> expLeaderboard(@RequestHeader(value = "User-Agent") String userAgent, Model model, HttpServletRequest request) {
		this.logAccess("exp leaderboard", userAgent, request);
		ExpLeaderboard leaderboard = this.expLeaderboardGenerator.getReport();
		
		Date generationDate = new Date();
		String generationDateFormatString = "yyyy-MM-dd hh:mm:ss aa zzz";
		SimpleDateFormat sdf = new SimpleDateFormat(generationDateFormatString);
		String generationDateString = sdf.format(generationDate);
		
		ExpLeaderboardData data = new ExpLeaderboardData(leaderboard, generationDateString);
		
		return GenericResponse.createGenericResponseEntity(data);
	}

}
