package fft_battleground.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.apache.commons.compress.utils.IOUtils;
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
import org.springframework.web.bind.annotation.ResponseBody;

import fft_battleground.botland.model.SkillType;
import fft_battleground.dump.DumpReportsService;
import fft_battleground.dump.DumpService;
import fft_battleground.dump.model.LeaderboardData;
import fft_battleground.dump.model.Music;
import fft_battleground.dump.model.PlayerLeaderboard;
import fft_battleground.model.Images;
import fft_battleground.repo.MatchRepo;
import fft_battleground.repo.PlayerRecordRepo;
import fft_battleground.repo.model.PlayerRecord;
import fft_battleground.repo.model.PlayerSkills;
import fft_battleground.repo.model.TeamInfo;
import fft_battleground.tournament.TournamentService;
import lombok.Data;

@Controller
@RequestMapping("/")
public class HomeController {
	
	@Autowired
	private Images images;
	
	@Autowired
	private TournamentService tournamentService;
	
	@Autowired
	private PlayerRecordRepo playerRecordRepo;
	
	@Autowired
	private MatchRepo matchRepo;
	
	@Autowired
	private DumpService dumpService;
	
	@Autowired
	private DumpReportsService dumpReportsService;
	
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
	public String homePage(Model model) {
		return "index.html";
	}
	
	@GetMapping("/apidocs")
	public String apiDocsPage(Model Model) {
		return "api.html";
	}
	
	@GetMapping({"/player", "/player/"})
	public String playerDataPage(Model model) {
		return "playerRecord.html";
	}

	@GetMapping({"/player/{playerName}"})
	public String playerDataPage(@PathVariable(name="playerName") String playerName, Model model, TimeZone timezone) {
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
						playerData.setPortraitUrl(this.images.getPortraitLocationByTeamInfo(playerTeamInfo.get(0)));
					} else {
						if(playerData.isBot()) {
							playerData.setPortraitUrl(this.images.getPortraitByName("Steel Giant"));
						} else {
							playerData.setPortraitUrl(this.images.getPortraitByName("Ramza"));
						}
					}
				}
				
				DecimalFormat df = new DecimalFormat("0.00");
				String betRatio = df.format(((double) 1 + record.getWins())/((double)1+ record.getWins() + record.getLosses()));
				String fightRatio = df.format(((double)1 + record.getFightWins())/((double) record.getFightWins() + record.getFightLosses()));
				playerData.setBetRatio(betRatio);
				playerData.setFightRatio(fightRatio);
				
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
				
				if(record.getLastActive() != null) {
					playerData.setTimezoneFormattedDateString(this.createDateStringWithTimezone(timezone, record.getLastActive()));
				}
				
				Integer leaderboardRank = this.dumpReportsService.getLeaderboardPosition(playerName);
				playerData.setLeaderboardPosition(leaderboardRank);
				
				model.addAttribute("playerData", playerData);
			}
		}
		return "playerRecord.html";
	}
	
	@GetMapping("/music")
	public String musicPage(Model model) {
		Collection<Music> music = this.dumpService.getPlaylist();
		model.addAttribute("playlist", music);
		
		return "musicList.html";
	}
	
	@GetMapping("/botleaderboard")
	public String botLeaderboardPage(Model model) {
		Map<String, Integer> botLeaderboard = this.dumpReportsService.getBotLeaderboard();
		NumberFormat myFormat = NumberFormat.getInstance();
		myFormat.setGroupingUsed(true);
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
		
		List<LeaderboardData> output = botLeaderboard.keySet().stream().map(
				botName -> { 
					String player = botName;
					String gil = myFormat.format(botLeaderboard.get(botName));
					String activeDate = dateFormat.format(this.dumpService.getLastActiveDateFromCache(botName));
					return new LeaderboardData(player, gil, activeDate); 
				}).sorted().collect(Collectors.toList());
		Collections.reverse(output);
		for(int i = 0; i < output.size(); i++) { 
			output.get(i).setRank(i + 1); 
		}
		model.addAttribute("leaderboard", output);
		return "botLeaderboard.html";
	}
	
	@GetMapping({"/playerLeaderboard", "/leaderboard"})
	public String playerLeaderboardPage(Model model) {
		PlayerLeaderboard leaderboard = this.dumpReportsService.getLeaderboard();
		model.addAttribute("leaderboard", leaderboard);
		model.addAttribute("topPlayersCommaSplit", StringUtils.join(leaderboard.getHighestPlayers().stream().map(highestPlayer -> highestPlayer.getName()).collect(Collectors.toList()), ','));
		
		return "playerLeaderboard.html";
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
	
}

	

@Data
class PlayerData {
	private PlayerRecord playerRecord;
	private String portraitUrl;
	private String fightRatio;
	private String betRatio;
	private boolean containsPrestige = false;
	private boolean bot = false;
	private int prestigeLevel = 0;
	private Integer leaderboardPosition;
	private String timezoneFormattedDateString;
	
	public PlayerData() {}
}
