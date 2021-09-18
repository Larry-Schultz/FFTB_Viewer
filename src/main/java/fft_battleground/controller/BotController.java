package fft_battleground.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import fft_battleground.dump.reports.BotlandLeaderboardReportGenerator;
import fft_battleground.dump.reports.model.BotlandLeaderboard;
import fft_battleground.dump.reports.model.BotlandLeaderboardEntry;
import fft_battleground.exception.CacheMissException;
import fft_battleground.util.GenericResponse;

@RequestMapping("/")
@Controller
public class BotController {

	@Autowired
	private BotlandLeaderboardReportGenerator botlandLeaderboardReportGenerator;
	
	@GetMapping("/botlandleaderboard")
	public ResponseEntity<GenericResponse<BotlandLeaderboard>> getBotlandLeaderboard() throws CacheMissException {
		return GenericResponse.createGenericResponseEntity(this.botlandLeaderboardReportGenerator.getReport());
	}
	
	@GetMapping("/botlandleaderboard/{botName}")
	public ResponseEntity<GenericResponse<BotlandLeaderboardEntry>> getBotlandLeaderboard(@PathVariable("botName") String botName) throws CacheMissException {
		return GenericResponse.createGenericResponseEntity(this.botlandLeaderboardReportGenerator.getReport().getLeaderboardEntryForPlayer(botName));
	}
}
