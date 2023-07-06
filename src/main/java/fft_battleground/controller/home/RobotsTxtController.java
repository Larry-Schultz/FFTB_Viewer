package fft_battleground.controller.home;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import fft_battleground.dump.DumpService;
import fft_battleground.metrics.AccessTracker;
import springfox.documentation.annotations.ApiIgnore;

@Controller
public class RobotsTxtController extends AbstractHomeController {

	private static final String[] sites = new String[] {"/", "/botland", "/gilCount", "/expLeaderboard", "/playerLeaderboard", "/leaderboard", "/botleaderboard", "/music", 
			"/player", "/apidocs", "/allegianceLeaderboard", "/ascension", "/options", "/portraits", "/maps"};
	
	@Autowired
	private DumpService dumpService;
	
	@Autowired
	public RobotsTxtController(AccessTracker accessTracker) {
		super(accessTracker);
		// TODO Auto-generated constructor stub
	}

	@ApiIgnore
	@GetMapping("/robots.txt")
	public ResponseEntity<String> robots(@RequestHeader(value = "User-Agent") String userAgent, HttpServletRequest request) {
		this.logAccess("robots.txt", userAgent, request);
		String response = null;
		StringBuilder robotsBuilder = new StringBuilder();
		robotsBuilder.append("User-agent: *\n");
		
		//hardcoded pages
		String allowPrefix = "Allow: ";
		
		Arrays.asList(sites).stream().forEach(site -> robotsBuilder.append(allowPrefix + site + " \n"));
		
		this.dumpService.getBalanceCache().keySet().stream().forEach(player -> robotsBuilder.append(allowPrefix + "/player/" + player + " \n"));
		
		response = robotsBuilder.toString();
		return new ResponseEntity<String>(response, HttpStatus.OK);
	}
}
