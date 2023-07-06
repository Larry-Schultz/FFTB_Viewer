package fft_battleground.controller.home;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import fft_battleground.controller.response.model.BotLeaderboardData;
import fft_battleground.dump.DumpService;
import fft_battleground.dump.service.GlobalGilService;
import fft_battleground.exception.CacheMissException;
import fft_battleground.metrics.AccessTracker;
import fft_battleground.reports.BotLeaderboardBalanceHistoryReportGenerator;
import fft_battleground.reports.BotLeaderboardReportGenerator;
import fft_battleground.reports.model.BotLeaderboard;
import fft_battleground.reports.model.LeaderboardBalanceData;
import fft_battleground.reports.model.LeaderboardData;
import fft_battleground.util.GenericResponse;
import io.swagger.annotations.ApiOperation;
import springfox.documentation.annotations.ApiIgnore;

@Controller
public class BotLeaderboardController extends AbstractHomeController {

	@Autowired
	private BotLeaderboardReportGenerator botLeaderboardReportGenerator;
	
	@Autowired
	private DumpService dumpService;
	
	@Autowired
	private GlobalGilService globalGilService;
	
	@Autowired
	private BotLeaderboardBalanceHistoryReportGenerator botLeaderboardBalanceHistoryReportGenerator;
	
	@Autowired
	public BotLeaderboardController(AccessTracker accessTracker) {
		super(accessTracker);
		// TODO Auto-generated constructor stub
	}
	
	@ApiIgnore
	@ApiOperation("returns bot leaderboard in json form")
	@GetMapping("/botleaderboard")
	public @ResponseBody ResponseEntity<GenericResponse<BotLeaderboardData>> botLeaderboardPage(@RequestHeader(value = "User-Agent") String userAgent, 
			HttpServletRequest request) throws CacheMissException {
		this.logAccess("bot leaderboard", userAgent, request);
		BotLeaderboard leaderboardData = botLeaderboardReportGenerator.getReport();
		Map<String, Integer> botLeaderboard = leaderboardData.getBotLeaderboard();
		NumberFormat myFormat = NumberFormat.getInstance();
		myFormat.setGroupingUsed(true);
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
		DecimalFormat decimalformat = new DecimalFormat("##.#########");
		
		List<LeaderboardData> completeLeaderboard = botLeaderboard.keySet().stream().map(
				botName -> { 
					String player = botName;
					String gil = myFormat.format(botLeaderboard.get(botName));
					String activeDate = dateFormat.format(this.dumpService.getLastActiveDateFromCache(botName));
					String percentageOfGlobalGil = decimalformat.format(this.globalGilService.percentageOfGlobalGil(botLeaderboard.get(botName)) * (double)100);
					LeaderboardData data = new LeaderboardData(player, gil, activeDate); 
					data.setPercentageOfGlobalGil(percentageOfGlobalGil);
					return data;
				}).sorted().collect(Collectors.toList());
		Collections.reverse(completeLeaderboard);
		for(int i = 0; i < completeLeaderboard.size(); i++) { 
			completeLeaderboard.get(i).setRank(i + 1); 
		}

		BotLeaderboardData data = new BotLeaderboardData(completeLeaderboard, leaderboardData.formattedGenerationDate());
		return GenericResponse.createGenericResponseEntity(data);
	}
	
	@ApiIgnore
	@GetMapping("/botLeaderboardBalanceHistory")
	public @ResponseBody ResponseEntity<GenericResponse<LeaderboardBalanceData>> 
	getBotBalanceHistory() throws CacheMissException {
		LeaderboardBalanceData data = botLeaderboardBalanceHistoryReportGenerator.getReport();
		
		return GenericResponse.createGenericResponseEntity(data);
	}

}
