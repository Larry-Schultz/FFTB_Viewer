package fft_battleground.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
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
import fft_battleground.dump.DumpService;
import fft_battleground.dump.Music;
import fft_battleground.model.Images;
import fft_battleground.repo.MatchRepo;
import fft_battleground.repo.PlayerRecordRepo;
import fft_battleground.repo.model.PlayerRecord;
import fft_battleground.repo.model.PlayerSkills;
import fft_battleground.repo.model.TeamInfo;
import fft_battleground.tournament.Tips;
import fft_battleground.tournament.TournamentService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;

@Controller
@RequestMapping("/")
public class HomeController {

	private static final int HIGHEST_PLAYERS = 10;
	private static final int TOP_PLAYERS = 100;
	
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
	public String playerDataPage(@PathVariable(name="playerName") String playerName, Model model) {
		if(playerName != null) {
			String id = StringUtils.trim(StringUtils.lowerCase(playerName));
			Optional<PlayerRecord> maybePlayer = this.playerRecordRepo.findById(id);
			if(maybePlayer.isPresent()) {
				PlayerData playerData = new PlayerData();
				PlayerRecord record = maybePlayer.get();
				record.getPlayerSkills().stream().forEach(
						playerSkill -> playerSkill.setMetadata(StringUtils.replace(this.tournamentService.getCurrentTips().getUserSkill().get(playerSkill.getSkill()), "\"", "")));
				playerData.setPlayerRecord(record);
				
				if(StringUtils.isNotBlank(record.getPortrait())) {
					String portrait = record.getPortrait();
					String portraitUrl = this.images.getPortraitByName(portrait);
					playerData.setPortraitUrl(portraitUrl);
				}
				if(StringUtils.isBlank(record.getPortrait()) || playerData.getPortraitUrl() == null) {
					List<TeamInfo> playerTeamInfo = this.matchRepo.getLatestTeamInfoForPlayer(record.getPlayer(), PageRequest.of(0,1));
					if(playerTeamInfo != null && playerTeamInfo.size() > 0) {
						playerData.setPortraitUrl(this.images.getPortraitLocationByTeamInfo(playerTeamInfo.get(0)));
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
				
				Integer leaderboardRank = this.dumpService.getLeaderboardPosition(playerName);
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
		Map<String, Integer> botLeaderboard = this.dumpService.getBotLeaderboard();
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
		Map<String, Integer> topPlayers = this.dumpService.getTopPlayers(TOP_PLAYERS);
		List<LeaderboardData> allPlayers =  topPlayers.keySet().parallelStream().map(player-> this.collectPlayerLeaderboardData(player)).filter(result -> result != null).sorted().collect(Collectors.toList());
		Collections.reverse(allPlayers);
		for(int i = 0; i < allPlayers.size(); i++) { 
			allPlayers.get(i).setRank(i + 1); 
		}
		
		List<LeaderboardData> highestPlayers = allPlayers.parallelStream().filter(leaderboardData -> leaderboardData.getRank() <= HIGHEST_PLAYERS).collect(Collectors.toList());
		List<LeaderboardData> topPlayersList = allPlayers.parallelStream().filter(leaderboardData -> leaderboardData.getRank() > HIGHEST_PLAYERS && leaderboardData.getRank() <= TOP_PLAYERS).collect(Collectors.toList());
		PlayerLeaderboard leaderboard = new PlayerLeaderboard(highestPlayers, topPlayersList);
		model.addAttribute("leaderboard", leaderboard);
		model.addAttribute("topPlayersCommaSplit", StringUtils.join(highestPlayers.stream().map(highestPlayer -> highestPlayer.getName()).collect(Collectors.toList()), ','));
		
		return "playerLeaderboard.html";
	}
	
	protected LeaderboardData collectPlayerLeaderboardData(String player) {
		NumberFormat myFormat = NumberFormat.getInstance();
		myFormat.setGroupingUsed(true);
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
		
		Optional<PlayerRecord> maybePlayer = this.playerRecordRepo.findById(StringUtils.lowerCase(player));
		if(maybePlayer.isPresent()) {
			PlayerRecord record = maybePlayer.get();
			String gil = null;
			String activeDate = null;
			if(record.getLastKnownAmount() != null) {
				gil = myFormat.format(record.getLastKnownAmount());
			}
			if(record.getLastActive() != null) {
				activeDate = dateFormat.format(record.getLastActive());
			}
			return new LeaderboardData(player, gil, activeDate);
		} else {
			return null;
		}
	}
}

	

@Data
class PlayerData {
	private PlayerRecord playerRecord;
	private String portraitUrl;
	private String fightRatio;
	private String betRatio;
	private boolean containsPrestige = false;
	private int prestigeLevel = 0;
	private Integer leaderboardPosition;
	
	public PlayerData() {}
}

@Data
@AllArgsConstructor
class PlayerLeaderboard {
	private List<LeaderboardData> highestPlayers;
	private List<LeaderboardData> topPlayers;
}

@Data
@AllArgsConstructor
class LeaderboardData implements Comparable<LeaderboardData> {
	private String name;
	private Integer rank;
	private String gil;
	private String lastActiveDate;
	
	public LeaderboardData() {}
	public LeaderboardData(String name, String gil, String lastActiveDate) {
		this.name = name;
		this.gil = gil;
		this.lastActiveDate = lastActiveDate;
	}
	@Override
	@SneakyThrows
	public int compareTo(LeaderboardData arg0) {
		NumberFormat myFormat  = NumberFormat.getInstance();
		myFormat.setGroupingUsed(true);
		Integer thisGil = myFormat.parse(this.gil).intValue();
		Integer arg0Gil = myFormat.parse(arg0.getGil()).intValue();
		return thisGil.compareTo(arg0Gil);
		
	}
}
