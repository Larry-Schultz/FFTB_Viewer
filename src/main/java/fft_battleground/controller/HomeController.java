package fft_battleground.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import fft_battleground.Images;
import fft_battleground.botland.BetBotFactory;
import fft_battleground.botland.model.SkillType;
import fft_battleground.controller.model.PlayerData;
import fft_battleground.dump.DumpReportsService;
import fft_battleground.dump.DumpService;
import fft_battleground.dump.model.GlobalGilPageData;
import fft_battleground.dump.model.Music;
import fft_battleground.dump.model.PrestigeTableEntry;
import fft_battleground.dump.reports.model.AllegianceLeaderboardWrapper;
import fft_battleground.dump.reports.model.BotLeaderboard;
import fft_battleground.dump.reports.model.ExpLeaderboardEntry;
import fft_battleground.dump.reports.model.LeaderboardData;
import fft_battleground.dump.reports.model.PlayerLeaderboard;
import fft_battleground.exception.CacheMissException;
import fft_battleground.repo.model.BotHourlyData;
import fft_battleground.repo.model.Bots;
import fft_battleground.repo.model.PlayerRecord;
import fft_battleground.repo.model.PlayerSkills;
import fft_battleground.repo.model.TeamInfo;
import fft_battleground.repo.repository.BotsHourlyDataRepo;
import fft_battleground.repo.repository.BotsRepo;
import fft_battleground.repo.repository.MatchRepo;
import fft_battleground.repo.repository.PlayerRecordRepo;
import fft_battleground.tournament.TournamentService;
import fft_battleground.util.GambleUtil;
import fft_battleground.util.GenericElementOrdering;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/")
@Slf4j
public class HomeController {
	
	@Autowired
	private Images images;
	
	@Autowired
	private TournamentService tournamentService;
	
	@Autowired
	private PlayerRecordRepo playerRecordRepo;
	
	@Autowired
	private BotsRepo botsRepo;
	
	@Autowired
	private BotsHourlyDataRepo botsHourlyDataRepo;
	
	@Autowired
	private MatchRepo matchRepo;
	
	@Autowired
	private DumpService dumpService;
	
	@Autowired
	private DumpReportsService dumpReportsService;
	
	@Autowired
	private BetBotFactory betBotFactory;
	
	@GetMapping(value = "/images/characters/{characterName}", produces = MediaType.IMAGE_JPEG_VALUE)
	public @ResponseBody ResponseEntity<byte[]> getImageWithMediaType(@PathVariable("characterName") String characterName) throws IOException {
		String basePath = "/static";
		String imagePath = images.getCharacterImagePath(characterName);
		String path = basePath + imagePath;
		Resource imageResource = new ClassPathResource(path);
		InputStream in = null;
		try {
			in = imageResource.getInputStream();
		} catch(FileNotFoundException e) {
			byte[] blankArray = null;
			return new ResponseEntity<byte[]>(blankArray, HttpStatus.NOT_FOUND);
		}
	    return new ResponseEntity<byte[]>(IOUtils.toByteArray(in), HttpStatus.OK);
	}
	
	@GetMapping("/")
	public String homePage(Model model, HttpServletRequest request) {
		this.logAccess("index page" , request);
		return "index.html";
	}
	
	@GetMapping("/apidocs")
	public String apiDocsPage(Model Model, HttpServletRequest request) {
		this.logAccess("apidocs" , request);
		return "api.html";
	}
	
	@GetMapping({"/player", "/player/"})
	public String playerDataPage(Model model, HttpServletRequest request) {
		this.logAccess("player search page" , request);
		return "playerRecord.html";
	}

	@GetMapping({"/player/{playerName}"})
	public String playerDataPage(@PathVariable(name="playerName") String playerName, @RequestParam(name="refresh", required=false, defaultValue="false") Boolean refresh, 
			Model model, TimeZone timezone, HttpServletRequest request) throws CacheMissException {
		if(!refresh) {
			this.logAccess(playerName + " search page " , request);
		}
		if(playerName != null) {
			String id = StringUtils.trim(StringUtils.lowerCase(playerName));
			Optional<PlayerRecord> maybePlayer = this.playerRecordRepo.findById(id);
			if(maybePlayer.isPresent()) {
				PlayerData playerData = new PlayerData();
				PlayerRecord record = maybePlayer.get();
				record.getPlayerSkills().stream().forEach(
						playerSkill -> playerSkill.setMetadata(StringUtils.replace(this.tournamentService.getCurrentTips().getUserSkill().get(playerSkill.getSkill()), "\"", "")));
				playerData.setPlayerRecord(record);
				
				boolean isBot = this.dumpService.getBotCache().contains(record.getPlayer());
				playerData.setBot(isBot);
				
				if(StringUtils.isNotBlank(record.getPortrait())) {
					String portrait = record.getPortrait();
					String portraitUrl = this.images.getPortraitByName(portrait, record.getAllegiance());
					playerData.setPortraitUrl(portraitUrl);
				}
				if(StringUtils.isBlank(record.getPortrait()) || playerData.getPortraitUrl() == null) {
					List<TeamInfo> playerTeamInfo = this.matchRepo.getLatestTeamInfoForPlayer(record.getPlayer(), PageRequest.of(0,1));
					if(playerTeamInfo != null && playerTeamInfo.size() > 0) {
						playerData.setPortraitUrl(this.images.getPortraitLocationByTeamInfo(playerTeamInfo.get(0), record.getAllegiance()));
					} else {
						if(playerData.isBot()) {
							playerData.setPortraitUrl(this.images.getPortraitByName("Steel Giant"));
						} else {
							playerData.setPortraitUrl(this.images.getPortraitByName("Ramza"));
						}
					}
				}
				
				DecimalFormat df = new DecimalFormat("0.00");
				Double betRatio = ((double) 1 + record.getWins())/((double)1+ record.getWins() + record.getLosses());
				Double fightRatio = ((double)1 + record.getFightWins())/((double) record.getFightWins() + record.getFightLosses());
				String betRatioString = df.format(betRatio);
				String fightRatioString = df.format(fightRatio);
				playerData.setBetRatio(betRatioString);
				playerData.setFightRatio(fightRatioString);
				Integer betPercentile = this.dumpReportsService.getBetPercentile(betRatio);
				Integer fightPercentile = this.dumpReportsService.getFightPercentile(fightRatio);
				playerData.setBetPercentile(betPercentile);
				playerData.setFightPercentile(fightPercentile);
				
				
				boolean containsPrestige = false;
				int prestigeLevel = 0;
				for(PlayerSkills skill: record.getPlayerSkills()) {
					if(skill.getSkillType() == SkillType.PRESTIGE) {
						containsPrestige = true;
						prestigeLevel++;
					}
				}
				playerData.setContainsPrestige(containsPrestige);
				playerData.setPrestigeLevel(prestigeLevel);
				playerData.setExpRank(this.dumpService.getExpRankLeaderboardByPlayer().get(record.getPlayer()));
				
				DecimalFormat format = new DecimalFormat("##.#########");
				String percentageOfTotalGil = format.format(this.dumpReportsService.percentageOfGlobalGil(record.getLastKnownAmount()) * (double)100);
				playerData.setPercentageOfGlobalGil(percentageOfTotalGil);
				
				if(record.getLastActive() != null) {
					playerData.setTimezoneFormattedDateString(this.createDateStringWithTimezone(timezone, record.getLastActive()));
				}
				
				Integer leaderboardRank = this.dumpReportsService.getLeaderboardPosition(playerName);
				playerData.setLeaderboardPosition(leaderboardRank);
				
				model.addAttribute("playerData", playerData);
			} else {
				PlayerData playerData = new PlayerData();
				playerData.setNotFound(true);
				playerData.setPlayerRecord(new PlayerRecord());
				playerData.getPlayerRecord().setPlayer(GambleUtil.cleanString(playerName));
				model.addAttribute("playerData", playerData);
			}
		}
		return "playerRecord.html";
	}
	
	@GetMapping("/music")
	public String musicPage(Model model, HttpServletRequest request) {
		this.logAccess("music search page", request);
		Collection<Music> music = this.dumpService.getPlaylist();
		model.addAttribute("playlist", music);
		
		return "musicList.html";
	}
	
	@GetMapping("/botleaderboard")
	public String botLeaderboardPage(Model model, HttpServletRequest request) throws CacheMissException {
		this.logAccess("bot leaderboard", request);
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
		model.addAttribute("leaderboard", output);
		model.addAttribute("generationDate", leaderboardData.formattedGenerationDate());
		return "botLeaderboard.html";
	}
	
	@GetMapping({"/playerLeaderboard", "/leaderboard"})
	public String playerLeaderboardPage(Model model, HttpServletRequest request) throws CacheMissException {
		this.logAccess("player leaderboard", request);
		PlayerLeaderboard leaderboard = this.dumpReportsService.getLeaderboard();
		model.addAttribute("leaderboard", leaderboard);
		model.addAttribute("topPlayersCommaSplit", StringUtils.join(leaderboard.getHighestPlayers().stream().map(highestPlayer -> highestPlayer.getName()).collect(Collectors.toList()), ','));
		
		return "playerLeaderboard.html";
	}
	
	@GetMapping("/expLeaderboard")
	@SneakyThrows
	public String expLeaderboard(Model model, HttpServletRequest request) {
		this.logAccess("exp leaderboard", request);
		List<ExpLeaderboardEntry> leaderboardEntries = this.dumpReportsService.generateExpLeaderboardData();
		List<PrestigeTableEntry> prestigeEntries = this.dumpReportsService.generatePrestigeTable();
		
		Date generationDate = new Date();
		String generationDateFormatString = "yyyy-MM-dd hh:mm:ss aa zzz";
		SimpleDateFormat sdf = new SimpleDateFormat(generationDateFormatString);
		String generationDateString = sdf.format(generationDate);
		
		model.addAttribute("leaderboard", leaderboardEntries);
		model.addAttribute("prestigeTable", prestigeEntries);
		model.addAttribute("generationDate", generationDateString);
		
		return "expLeaderboard.html";
	}
	
	@GetMapping("/gilCount")
	public String gilCountPage(Model model, HttpServletRequest request) {
		this.logAccess("global gil count", request);
		GlobalGilPageData data = this.dumpReportsService.getGlobalGilData();
		model.addAttribute("globalGilData", data);
		return "globalGil.html";
		
	}
	
	@GetMapping("/botland")
	public String botland(@RequestParam(name="refresh", required=false, defaultValue="false") Boolean refresh, Model model, HttpServletRequest request) {
		if(!refresh) {
			this.logAccess("botland", request);
		}
		List<Bots> botData = this.botsRepo.getBotsForToday();
		Collections.sort(botData, Collections.reverseOrder());
		model.addAttribute("botData", botData);
		model.addAttribute("primaryBotAccountName", this.betBotFactory.getIrcName());
		model.addAttribute("primaryBotName", this.betBotFactory.getPrimaryBotName());
		model.addAttribute("botConfigData", this.betBotFactory.getBotDataMap());
		
		List<String> botNames = botData.parallelStream().map(bots -> bots.getPlayer()).collect(Collectors.toList());
		Map<String, List<GenericElementOrdering<BotHourlyData>>> botHourlyDataMap = this.botsHourlyDataRepo.getOrderedBotHourlyDataForBots(botNames);
		model.addAttribute("botHourlyDataMap", botHourlyDataMap);
		return "botland.html";
	}
	
	@GetMapping("/allegianceLeaderboard")
	public String allegianceLeaderboard(Model model, HttpServletRequest request) throws CacheMissException {
		this.logAccess("allegiance leaderboard", request);
		AllegianceLeaderboardWrapper leaderboard = this.dumpReportsService.getAllegianceData();
		model.addAttribute("allegianceLeaderboardWrapper", leaderboard);
		return "allegianceLeaderboard.html";
	}
	
	@GetMapping("/robots.txt")
	public ResponseEntity<String> robots() {
		String response = null;
		StringBuilder robotsBuilder = new StringBuilder();
		robotsBuilder.append("User-agent: *\n");
		
		//hardcoded pages
		String allowPrefix = "Allow: ";
		String[] sites = new String[] {"/", "/botland", "/gilCount", "/expLeaderboard", "/playerLeaderboard", "/leaderboard", "/botleaderboard", "/music", "/player", "/apidocs"};
		Arrays.asList(sites).stream().forEach(site -> robotsBuilder.append(allowPrefix + site + " \n"));
		
		this.dumpService.getBalanceCache().keySet().stream().forEach(player -> robotsBuilder.append(allowPrefix + "/player/" + player + " \n"));
		
		response = robotsBuilder.toString();
		return new ResponseEntity<String>(response, HttpStatus.OK);
	}
	
	protected String createDateStringWithTimezone(TimeZone zone, Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		SimpleDateFormat sdf = new SimpleDateFormat("MMM dd yyyy");

		//Here you say to java the initial timezone. This is the secret
		sdf.setTimeZone(zone);
		//Will print in UTC
		String result = sdf.format(calendar.getTime());    

		return result;
	}
	
	@SneakyThrows
	protected void logAccess(String pageName, HttpServletRequest request) {
		InetAddress addr = InetAddress.getByName(request.getRemoteAddr());
		String host = addr.getHostName();
		log.info("{} page accessed from user: {} with hostname {}", pageName, request.getRemoteAddr(), host);
	}
	
}
