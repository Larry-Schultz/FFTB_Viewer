package fft_battleground.tournament;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import fft_battleground.dump.DumpDataProvider;
import fft_battleground.dump.DumpResourceManager;
import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.event.model.BetInfoEvent;
import fft_battleground.event.model.UnitInfoEvent;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.repo.PlayerRecordRepo;
import fft_battleground.repo.model.PlayerRecord;
import fft_battleground.util.GambleUtil;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TournamentService {
	
	private static final String tournamentInfoApiUri = "https://fftbg.com/api/tournaments?limit=1";
	private static final String tournamentApiBaseUri = "https://fftbg.com/api/tournament/";
	
	private static final String tipsApiUrl = "https://fftbg.com/api/tips";
	private static final String tournamentFolderUrlTemplate = "http://www.fftbattleground.com/fftbg/tournament_%s/";
	private static final String raidBossUrlTemplateForTournament = "http://www.fftbattleground.com/fftbg/tournament_%s/raidboss.txt";
	private static final String tournamentPotUrlTemplate = "http://www.fftbattleground.com/fftbg/tournament_%1$s/pot%2$s.txt";
	
	@Autowired
	private PlayerRecordRepo playerRecordRepo;
	
	@Autowired
	private DumpResourceManager dumpResourceManager;
	
	@Autowired
	private DumpDataProvider dumpDataProvider;
	
	private Cache<String, Tips> tipsCache = Caffeine.newBuilder()
			  .expireAfterWrite(1, TimeUnit.HOURS)
			  .maximumSize(1)
			  .build();
	
	public Tournament getcurrentTournament() {
		Tournament currentTournament = null;
		
		TournamentInfo latestTournament = this.getLatestTournamentInfo();
		currentTournament = this.getTournamentById(latestTournament.getID());
		List<String> raidbosses = this.getRaidBossesFromTournament(currentTournament.getID());
		currentTournament.setRaidbosses(raidbosses);
		
		return currentTournament;
	}
	
	@SneakyThrows
	@Cacheable("tips")
	public Tips getCurrentTips() {
		Tips currentTip = tipsCache.getIfPresent("tips");
		if(currentTip == null) {
			Resource resource = new UrlResource(tipsApiUrl);
			Tips tips = new Tips(resource);
			this.tipsCache.put("tips", tips);
			currentTip = tips;
		}
		
		return currentTip;
	}
	
	protected TournamentInfo getLatestTournamentInfo() {
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<TournamentInfo[]> tournamentInfo = restTemplate.getForEntity(tournamentInfoApiUri, TournamentInfo[].class);
		//TournamentInfo tournamentInfo = restTemplate.getForObject(tournamentInfoApiUri, TournamentInfo.class);
		return tournamentInfo.getBody()[0];
	}
	
	protected Tournament getTournamentById(Long id) {
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<Tournament> latestTournament = restTemplate.getForEntity(tournamentApiBaseUri + id.toString(), Tournament.class);
		return latestTournament.getBody();
	}
	
	protected List<String> getRaidBossesFromTournament(Long id) {
		List<String> raidbosses = new LinkedList<>();
		Resource resource;
		try {
			resource = new UrlResource(String.format(raidBossUrlTemplateForTournament, id.toString()));
		} catch (MalformedURLException e) {
			log.error("malformed url", e);
			return new ArrayList<>();
		}
		try(BufferedReader raidBossReader = this.dumpResourceManager.openDumpResource(resource)) {
			String line;
			while((line = raidBossReader.readLine()) != null) {
				if(StringUtils.isNotBlank(line)) {
					raidbosses.add(GambleUtil.cleanString(line));
				}
			}
		} catch (IOException e) {
			log.debug("reading raidboss data for tournament {} has failed", id);
			return new ArrayList<>();
		}
		
		return raidbosses;
	}
	
	public List<BattleGroundEvent> getRealBetInfoFromLatestPotFile(Long tournamentId) {
		List<BattleGroundEvent> result = new LinkedList<>();
		
		Set<String> filenamesInTournamentFolder = this.filenamesInTournamentFolder(tournamentId);
		Integer potId = this.getIdOfLatestPotFile(filenamesInTournamentFolder);
		result = this.parsePotFile(tournamentId, potId);
		
		return result;
	}
	
	@SneakyThrows
	protected Set<String> filenamesInTournamentFolder(Long id) {
		Set<String> files = this.dumpDataProvider.getPlayerList(String.format(tournamentFolderUrlTemplate, id.toString()));
		return files;
	}
	
	protected Integer getIdOfLatestPotFile(Set<String> files) {
		int result = 0;
		
		for(String file : files) {
			if(StringUtils.contains(file, "pot")) {
				String currentIdString = StringUtils.substringAfter(file, "pot");
				int currentId = 0;
				try {
					currentId = Integer.valueOf(currentIdString);
				} catch(NumberFormatException e) {}
				if(currentId != 0) {
					if(currentId > result) {
						result = currentId;
					}
				}
			}
		}
		
		return result;
	}
	
	@SneakyThrows
	protected List<BattleGroundEvent> parsePotFile(Long tournamentId, Integer potId) {
		List<BattleGroundEvent> result = new LinkedList<>();
		
		Resource resource;
		try {
			resource = new UrlResource(String.format(tournamentPotUrlTemplate, tournamentId.toString(), potId.toString()));
		} catch (MalformedURLException e) {
			log.error("malformed url", e);
			return new ArrayList<>();
		}
		try(BufferedReader potFileReader = this.dumpResourceManager.openDumpResource(resource)) {
			potFileReader.readLine(); //read the summary line
			String line = null;
			String player = null;
			BattleGroundTeam currentTeam = null;
			while((line = potFileReader.readLine()) != null) {
				if(StringUtils.isNotBlank(line) && StringUtils.contains(line, " bets:")) { //if it contains bets:, it has the team name
					String teamString = StringUtils.substringBefore(line, " bets:");
					currentTeam = BattleGroundTeam.parse(teamString);
				} else if(StringUtils.isNotBlank(line) && !StringUtils.contains(line, " bets:")) { //otherwise it should be a player entry
					try {
						player = StringUtils.substringBefore(line, ":");
						player = GambleUtil.cleanString(player);
						String betAmountString = StringUtils.substringBetween(line, ": ", "G");
						betAmountString = StringUtils.replace(betAmountString, ",", "");
						Integer betAmount = Integer.valueOf(betAmountString);
						BetInfoEvent newBetInfoEvent = new BetInfoEvent(player, betAmount, currentTeam);
						result.add(newBetInfoEvent);
					} catch(NumberFormatException e) {
						//don't use data if the parsing fails
						log.warn("bet amount parsing failed for player {} in pot file {} for tournament {}", player, "pot" + potId.toString() + ".txt", tournamentId.toString());
					} 
				} 
			}
		}
		
		return result;
	}
	
	protected void cleanUnitInfoEventPlayerName(UnitInfoEvent event) {
		final String[] trainerPrefixes = {"Trainer", "Leader", "Rival", "Ranger", "NinjaKid", "Blackbelt", "Lass", "Officer", "Rocket"};
		String playerName = GambleUtil.cleanString(event.getPlayer());
		String likePlayerNameString = StringUtils.replace(playerName, " ", "%"); 
		List<PlayerRecord> records = this.playerRecordRepo.findLikePlayer(likePlayerNameString);
	}

}
