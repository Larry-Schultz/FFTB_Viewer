package fft_battleground.controller.home;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import fft_battleground.botland.BetBotFactory;
import fft_battleground.botland.model.BotData;
import fft_battleground.botland.personality.PersonalityModuleFactory;
import fft_battleground.controller.response.model.botland.BotResponseData;
import fft_battleground.metrics.AccessTracker;
import fft_battleground.repo.model.BotHourlyData;
import fft_battleground.repo.model.Bots;
import fft_battleground.repo.repository.BotsHourlyDataRepo;
import fft_battleground.repo.repository.BotsRepo;
import fft_battleground.util.GenericElementOrdering;
import fft_battleground.util.GenericResponse;
import io.swagger.annotations.ApiOperation;

@Controller
public class BotPageController extends AbstractHomeController {

	@Autowired
	private BotsRepo botsRepo;
	
	@Autowired
	private BotsHourlyDataRepo botsHourlyDataRepo;
	
	@Autowired
	private PersonalityModuleFactory personalityModuleFactory;
	
	@Autowired
	private BetBotFactory betBotFactory;
	
	@Autowired
	public BotPageController(AccessTracker accessTracker) {
		super(accessTracker);
	}
	
	@ApiOperation("returns tracked data for the listed botland bot")
	@GetMapping("/bot/{botName}")
	public @ResponseBody ResponseEntity<GenericResponse<BotResponseData>> botData(@PathVariable("botName") String botName,
			@RequestParam(name="refresh", required=false, defaultValue="false") Boolean refresh,
			@RequestHeader(value = "User-Agent", required=false, defaultValue="") String userAgent, Model model, HttpServletRequest request) {
		if(refresh != null && !refresh) {
			String botNameString = botName != null ? botName : "null bot name";
			this.logAccess("bot page for " + botNameString, userAgent, request);
		}
		
		List<Bots> botList = this.botsRepo.getBotsForToday();
		List<String> botNames = botList.parallelStream()
				.map(bots -> bots.getPlayer())
				.collect(Collectors.toList());
		boolean containsBot = botNames.contains(botName);
		if(!containsBot) {
			return GenericResponse.createGenericResponseEntity(new BotResponseData(), "Not Found", HttpStatus.NOT_FOUND);
		}
		
		
		Map<String, List<GenericElementOrdering<BotHourlyData>>> botHourlyDataMap = this.botsHourlyDataRepo.getOrderedBotHourlyDataForBots(List.of(botName));
		Map<String, String> botResponses = this.personalityModuleFactory.getLastBotPersonalityResponses();
		BotData botData = this.betBotFactory.getBotDataMap().get(botName);
		BotResponseData response = new BotResponseData(botName, botList.get(0), botData, botHourlyDataMap.get(botName), botResponses.get(botName));
		return GenericResponse.createGenericResponseEntity(response);
	}

}
