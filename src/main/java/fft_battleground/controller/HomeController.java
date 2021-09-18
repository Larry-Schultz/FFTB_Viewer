package fft_battleground.controller;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.websocket.server.PathParam;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import fft_battleground.botland.BetBotFactory;
import fft_battleground.botland.model.BotData;
import fft_battleground.botland.personality.PersonalityModuleFactory;
import fft_battleground.controller.model.BotLeaderboardData;
import fft_battleground.controller.model.BotResponseData;
import fft_battleground.controller.model.BotlandData;
import fft_battleground.controller.model.ExpLeaderboardData;
import fft_battleground.controller.model.MusicData;
import fft_battleground.controller.model.PlayerData;
import fft_battleground.controller.model.PlayerLeaderboardData;
import fft_battleground.dump.DumpReportsService;
import fft_battleground.dump.DumpService;
import fft_battleground.dump.model.GlobalGilPageData;
import fft_battleground.dump.model.Music;
import fft_battleground.dump.reports.model.AllegianceLeaderboardWrapper;
import fft_battleground.dump.reports.model.AscensionData;
import fft_battleground.dump.reports.model.BotLeaderboard;
import fft_battleground.dump.reports.model.ExpLeaderboard;
import fft_battleground.dump.reports.model.LeaderboardData;
import fft_battleground.dump.reports.model.PlayerLeaderboard;
import fft_battleground.exception.CacheMissException;
import fft_battleground.exception.TournamentApiException;
import fft_battleground.image.ImageCache;
import fft_battleground.metrics.AccessTracker;
import fft_battleground.repo.model.BotHourlyData;
import fft_battleground.repo.model.Bots;
import fft_battleground.repo.repository.BotsHourlyDataRepo;
import fft_battleground.repo.repository.BotsRepo;
import fft_battleground.util.GenericElementOrdering;
import fft_battleground.util.GenericResponse;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/")
@Slf4j
public class HomeController {
	
	@Autowired
	private ImageCache imageCache;
	
	@Autowired
	private BotsRepo botsRepo;
	
	@Autowired
	private BotsHourlyDataRepo botsHourlyDataRepo;
	
	@Autowired
	private PersonalityModuleFactory personalityModuleFactory;
	
	@Autowired
	private DumpService dumpService;
	
	@Autowired
	private DumpReportsService dumpReportsService;
	
	@Autowired
	private BetBotFactory betBotFactory;
	
	@Autowired
	private AccessTracker accessTracker;
	
	@GetMapping(value = "/images/characters/{characterName}", produces = MediaType.IMAGE_JPEG_VALUE)
	public @ResponseBody ResponseEntity<byte[]> getImageWithMediaType(@PathVariable("characterName") String characterName) throws IOException {
		byte[] data = this.imageCache.getCharacterImage(characterName);
	    if(data == null) {
	    	return new ResponseEntity<>(new byte[] {}, HttpStatus.NOT_FOUND);
	    } else {
	    	return new ResponseEntity<>(data, HttpStatus.OK);
	    }
	}
	
	@GetMapping(value = "/images/portraits/{characterName}", produces = MediaType.IMAGE_JPEG_VALUE)
	public @ResponseBody ResponseEntity<byte[]> getPortraitImageWithMediaType(@PathVariable("characterName") String characterName) throws IOException {
		byte[] data = this.imageCache.getPortaitImage(characterName);
	    if(data == null) {
	    	return new ResponseEntity<>(new byte[] {}, HttpStatus.NOT_FOUND);
	    } else {
	    	return new ResponseEntity<>(data, HttpStatus.OK);
	    }
	}
	
	@GetMapping("/")
	public String homePage(@RequestHeader(value = "User-Agent") String userAgent, Model model, HttpServletRequest request) {
		this.logAccess("index page", userAgent, request);
		return "index.html";
	}
	
	@GetMapping("/apidocs")
	public String apiDocsPage(@RequestHeader(value = "User-Agent") String userAgent, Model Model, HttpServletRequest request) {
		this.logAccess("apidocs", userAgent, request);
		return "api.html";
	}
	
	@GetMapping({"/player", "/player/"})
	public @ResponseBody ResponseEntity<GenericResponse<PlayerData>> playerDataPage(@RequestHeader(value = "User-Agent") String userAgent, Model model, HttpServletRequest request) {
		this.logAccess("player search page", userAgent, request);
		PlayerData data = null;
		return GenericResponse.createGenericResponseEntity("No player provided", data);
	}

	@GetMapping({"/player/{playerName}"})
	public @ResponseBody ResponseEntity<GenericResponse<PlayerData>> playerDataPage(@PathVariable(name="playerName") String playerName, @RequestParam(name="refresh", required=false, defaultValue="false") Boolean refresh, 
			@RequestHeader(value = "User-Agent") String userAgent, Model model, TimeZone timezone, HttpServletRequest request) throws CacheMissException, TournamentApiException {
		if(!refresh) {
			this.logAccess(playerName + " search page ", userAgent, request);
		}
		
		PlayerData playerData = this.dumpService.getDataForPlayerPage(playerName, timezone);
		return GenericResponse.createGenericResponseEntity(playerData);
	}
	
	@GetMapping("/music")
	public @ResponseBody ResponseEntity<GenericResponse<Collection<MusicData>>> musicPage(@RequestHeader(value = "User-Agent") String userAgent, Model model, HttpServletRequest request) {
		this.logAccess("music search page", userAgent, request);
		Collection<Music> music = this.dumpService.getPlaylist();
		Collection<MusicData> data = music.parallelStream().map(musicEntry -> new MusicData(musicEntry)).collect(Collectors.toList());
		
		return GenericResponse.createGenericResponseEntity(data);
	}
	
	@GetMapping("/botleaderboard")
	public @ResponseBody ResponseEntity<GenericResponse<BotLeaderboardData>> botLeaderboardPage(@RequestHeader(value = "User-Agent") String userAgent, 
			HttpServletRequest request) throws CacheMissException {
		this.logAccess("bot leaderboard", userAgent, request);
		BotLeaderboard leaderboardData = this.dumpReportsService.getBotLeaderboard();
		Map<String, Integer> botLeaderboard = leaderboardData.getBotLeaderboard();
		NumberFormat myFormat = NumberFormat.getInstance();
		myFormat.setGroupingUsed(true);
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
		DecimalFormat decimalformat = new DecimalFormat("##.#########");
		
		List<LeaderboardData> output = botLeaderboard.keySet().stream().map(
				botName -> { 
					String player = botName;
					String gil = myFormat.format(botLeaderboard.get(botName));
					String activeDate = dateFormat.format(this.dumpService.getLastActiveDateFromCache(botName));
					String percentageOfGlobalGil = decimalformat.format(this.dumpReportsService.percentageOfGlobalGil(botLeaderboard.get(botName)) * (double)100);
					LeaderboardData data = new LeaderboardData(player, gil, activeDate); 
					data.setPercentageOfGlobalGil(percentageOfGlobalGil);
					return data;
				}).sorted().collect(Collectors.toList());
		Collections.reverse(output);
		for(int i = 0; i < output.size(); i++) { 
			output.get(i).setRank(i + 1); 
		}

		BotLeaderboardData data = new BotLeaderboardData(output, leaderboardData.formattedGenerationDate());
		return GenericResponse.createGenericResponseEntity(data);
	}
	
	@GetMapping({"/playerLeaderboard", "/leaderboard"})
	public @ResponseBody ResponseEntity<GenericResponse<PlayerLeaderboardData>> playerLeaderboardPage(@RequestHeader(value = "User-Agent") String userAgent, Model model, 
			HttpServletRequest request) throws CacheMissException {
		this.logAccess("player leaderboard", userAgent, request);
		PlayerLeaderboard leaderboard = this.dumpReportsService.getLeaderboard();
		model.addAttribute("leaderboard", leaderboard);
		String commaDelimitedTopPlayersString = StringUtils.join(leaderboard.getHighestPlayers().stream().map(highestPlayer -> highestPlayer.getName()).collect(Collectors.toList()), ',');
		
		PlayerLeaderboardData data = new PlayerLeaderboardData(leaderboard, leaderboard.formattedGenerationDate(), commaDelimitedTopPlayersString);
		
		return GenericResponse.createGenericResponseEntity(data);
	}
	
	@GetMapping("/expLeaderboard")
	@SneakyThrows
	public @ResponseBody ResponseEntity<GenericResponse<ExpLeaderboardData>> expLeaderboard(@RequestHeader(value = "User-Agent") String userAgent, Model model, HttpServletRequest request) {
		this.logAccess("exp leaderboard", userAgent, request);
		ExpLeaderboard leaderboard = this.dumpReportsService.getExpLeaderboard();
		
		Date generationDate = new Date();
		String generationDateFormatString = "yyyy-MM-dd hh:mm:ss aa zzz";
		SimpleDateFormat sdf = new SimpleDateFormat(generationDateFormatString);
		String generationDateString = sdf.format(generationDate);
		
		ExpLeaderboardData data = new ExpLeaderboardData(leaderboard, generationDateString);
		
		return GenericResponse.createGenericResponseEntity(data);
	}
	
	@GetMapping("/ascension")
	@SneakyThrows
	public @ResponseBody ResponseEntity<GenericResponse<AscensionData>> ascension(@RequestHeader(value = "User-Agent") String userAgent, Model model, HttpServletRequest request) {
		this.logAccess("ascension", userAgent, request);
		AscensionData prestigeEntries = this.dumpReportsService.generatePrestigeTable();
		
		Date generationDate = new Date();
		String generationDateFormatString = "yyyy-MM-dd hh:mm:ss aa zzz";
		SimpleDateFormat sdf = new SimpleDateFormat(generationDateFormatString);
		String generationDateString = sdf.format(generationDate);
		
		prestigeEntries.setGenerationDateString(generationDateString);
		
		return GenericResponse.createGenericResponseEntity(prestigeEntries);
	}
	
	@GetMapping("/gilCount")
	public @ResponseBody ResponseEntity<GenericResponse<GlobalGilPageData>> gilCountPage(@RequestHeader(value = "User-Agent") String userAgent, Model model, HttpServletRequest request) {
		this.logAccess("global gil count", userAgent, request);
		GlobalGilPageData data = this.dumpReportsService.getGlobalGilData();
		model.addAttribute("globalGilData", data);
		return GenericResponse.createGenericResponseEntity(data);
		
	}
	
	@GetMapping("/botland")
	public @ResponseBody ResponseEntity<GenericResponse<BotlandData>> botland(@RequestParam(name="refresh", required=false, defaultValue="false") Boolean refresh, 
			@RequestHeader(value = "User-Agent") String userAgent, Model model, HttpServletRequest request) {
		if(!refresh) {
			this.logAccess("botland", userAgent, request);
		}
		List<Bots> botData = this.botsRepo.getBotsForToday();
		Collections.sort(botData, Collections.reverseOrder());
		
		List<String> botNames = botData.parallelStream().map(bots -> bots.getPlayer()).collect(Collectors.toList());
		Map<String, List<GenericElementOrdering<BotHourlyData>>> botHourlyDataMap = this.botsHourlyDataRepo.getOrderedBotHourlyDataForBots(botNames);
		
		Map<String, String> botResponses = this.personalityModuleFactory.getLastBotPersonalityResponses();
		
		BotlandData data = new BotlandData(botData, this.betBotFactory.getIrcName(), this.betBotFactory.getPrimaryBotName(), this.betBotFactory.getBotDataMap(), 
				botHourlyDataMap, botResponses);
		return GenericResponse.createGenericResponseEntity(data);
	}
	
	@GetMapping("/bot/{botName}")
	public @ResponseBody ResponseEntity<GenericResponse<BotResponseData>> botData(@PathVariable("botName") String botName,
			@RequestParam(name="refresh", required=false, defaultValue="false") Boolean refresh,
			@RequestHeader(value = "User-Agent") String userAgent, Model model, HttpServletRequest request) {
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
	
	@GetMapping("/allegianceLeaderboard")
	public @ResponseBody ResponseEntity<GenericResponse<AllegianceLeaderboardWrapper>> allegianceLeaderboard(@RequestHeader(value = "User-Agent") String userAgent, HttpServletRequest request) throws CacheMissException {
		this.logAccess("allegiance leaderboard", userAgent, request);
		AllegianceLeaderboardWrapper leaderboard = this.dumpReportsService.getAllegianceData();
		return GenericResponse.createGenericResponseEntity(leaderboard);
	}
	
	@GetMapping("/robots.txt")
	public ResponseEntity<String> robots(@RequestHeader(value = "User-Agent") String userAgent, HttpServletRequest request) {
		this.logAccess("robots.txt", userAgent, request);
		String response = null;
		StringBuilder robotsBuilder = new StringBuilder();
		robotsBuilder.append("User-agent: *\n");
		
		//hardcoded pages
		String allowPrefix = "Allow: ";
		String[] sites = new String[] {"/", "/botland", "/gilCount", "/expLeaderboard", "/playerLeaderboard", "/leaderboard", "/botleaderboard", "/music", "/player", "/apidocs", "/allegianceLeaderboard", "/ascension"};
		Arrays.asList(sites).stream().forEach(site -> robotsBuilder.append(allowPrefix + site + " \n"));
		
		this.dumpService.getBalanceCache().keySet().stream().forEach(player -> robotsBuilder.append(allowPrefix + "/player/" + player + " \n"));
		
		response = robotsBuilder.toString();
		return new ResponseEntity<String>(response, HttpStatus.OK);
	}
	
	@SneakyThrows
	protected void logAccess(String pageName, String userAgent, HttpServletRequest request) {
		this.accessTracker.addAccessEntry(pageName, userAgent, request);
	}
	
}
