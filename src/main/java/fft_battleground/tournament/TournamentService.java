package fft_battleground.tournament;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
import fft_battleground.event.detector.model.BattleGroundEvent;
import fft_battleground.event.detector.model.BetInfoEvent;
import fft_battleground.event.detector.model.TeamInfoEvent;
import fft_battleground.event.detector.model.UnitInfoEvent;
import fft_battleground.exception.DumpException;
import fft_battleground.exception.TournamentApiException;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.repo.repository.PlayerRecordRepo;
import fft_battleground.tournament.model.Tournament;
import fft_battleground.tournament.model.TournamentInfo;
import fft_battleground.util.GambleUtil;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TournamentService {
	
	private static final String tournamentInfoApiUri = "https://fftbg.com/api/tournaments?limit=1";
	private static final String tournamentApiBaseUri = "https://fftbg.com/api/tournament/";
	private static final String prestigeSkillsMasterListUri = "http://www.fftbattleground.com/fftbg/PrestigeSkills.txt";
	
	private static final String tipsApiUrl = "https://fftbg.com/api/tips";
	private static final String tournamentFolderUrlTemplate = "http://www.fftbattleground.com/fftbg/tournament_%s/";
	private static final String raidBossUrlTemplateForTournament = "http://www.fftbattleground.com/fftbg/tournament_%s/raidboss.txt";
	private static final String tournamentPotUrlTemplate = "http://www.fftbattleground.com/fftbg/tournament_%1$s/pot%2$s.txt";
	private static final String entrantUrlTemplateForTournament = "http://www.fftbattleground.com/fftbg/tournament_%s/entrants.txt";
	private static final String winnersTxtUrlFormat = "http://www.fftbattleground.com/fftbg/tournament_%s/winner.txt";
	
	@Autowired
	private DumpResourceManager dumpResourceManager;
	
	@Autowired
	private DumpDataProvider dumpDataProvider;
	
	private static final String tipsCacheKey = "tips";
	private Cache<String, Tips> tipsCache = Caffeine.newBuilder()
			  .expireAfterWrite(24, TimeUnit.HOURS)
			  .maximumSize(1)
			  .build();
	private Object tipsCacheLock = new Object();
	
	private static final String prestigeSkillSetKey = "PRESTIGESKILLSET";
	private Cache<String, Set<String>> prestigeSkillSetCache = Caffeine.newBuilder()
			  .expireAfterWrite(24, TimeUnit.HOURS)
			  .maximumSize(1)
			  .build();
	private Object prestigeSkillSetCacheLock = new Object();
	
	public Tournament getcurrentTournament() throws DumpException, TournamentApiException {
		Tournament currentTournament = null;
		
		TournamentInfo latestTournament = this.getLatestTournamentInfo();
		currentTournament = this.getTournamentById(latestTournament.getID());
		List<String> raidbosses = this.getRaidBossesFromTournament(currentTournament.getID());
		currentTournament.setRaidbosses(raidbosses);
		currentTournament.setEntrants(new ArrayList<>(this.parseEntrantFile(latestTournament.getID()))); //override what comes from the tournament API since I don't trust it
		
		Set<String> allPlayers = this.dumpDataProvider.getHighScoreDump().keySet();
		currentTournament.setAllPlayers(allPlayers);
		
		return currentTournament;
	}
	
	@Cacheable("tips")
	public Tips getCurrentTips() throws TournamentApiException {
		Tips currentTip;
		synchronized(tipsCacheLock) {
			currentTip = tipsCache.getIfPresent(tipsCacheKey);
			if(currentTip == null) {
				Resource resource;
				try {
					resource = new UrlResource(tipsApiUrl);
				} catch (MalformedURLException e) {
					log.error("Error found getting latest tournament info", e);
					throw new TournamentApiException(e);
				}
				Tips tips = new Tips(resource);
				this.tipsCache.put(tipsCacheKey, tips);
				currentTip = tips;
			}
		}
		
		return currentTip;
	}
	
	public List<BattleGroundEvent> getRealBetInfoFromLatestPotFile(Long tournamentId) throws DumpException {
		List<BattleGroundEvent> result = new LinkedList<>();
		
		Set<String> filenamesInTournamentFolder = this.filenamesInTournamentFolder(tournamentId);
		Integer potId = this.getIdOfLatestPotFile(filenamesInTournamentFolder);
		result = this.parsePotFile(tournamentId, potId);
		
		return result;
	}
	
	public Set<String> getPrestigeSkills() throws DumpException {
		synchronized(this.prestigeSkillSetCacheLock) {
			Set<String> prestigeSkills = this.prestigeSkillSetCache.getIfPresent(prestigeSkillSetKey);
			if(prestigeSkills == null) {
				prestigeSkills = this.getPrestigeSkillList();
				this.prestigeSkillSetCache.put(prestigeSkillSetKey, prestigeSkills);
			}
			
			return prestigeSkills;
		}
		
	}
	
	public List<BattleGroundTeam> getWinnersFromTournament(final Long id) throws DumpException {
		List<BattleGroundTeam> winners = new LinkedList<>();
		Resource resource;
		try {
			resource = new UrlResource(String.format(winnersTxtUrlFormat, id.toString()));
		} catch (MalformedURLException e) {
			log.error("malformed url", e);
			return new ArrayList<>();
		}
		try(BufferedReader raidBossReader = this.dumpResourceManager.openDumpResource(resource)) {
			String line;
			while((line = raidBossReader.readLine()) != null) {
				if(StringUtils.isNotBlank(line)) {
					winners.add(BattleGroundTeam.parse(line));
				}
			}
		} catch (IOException e) {
			log.debug("reading winner data for tournament {} has failed", id);
			return new ArrayList<>();
		}
		
		return winners;
	}
	

	protected TournamentInfo getLatestTournamentInfo() throws TournamentApiException {
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<TournamentInfo[]> tournamentInfo;
		try {
			tournamentInfo = restTemplate.getForEntity(tournamentInfoApiUri, TournamentInfo[].class);
		} catch(Exception e) {
			log.error("Error found getting latest tournament info", e);
			throw new TournamentApiException(e);
		}
		//TournamentInfo tournamentInfo = restTemplate.getForObject(tournamentInfoApiUri, TournamentInfo.class);
		return tournamentInfo.getBody()[0];
	}
	
	protected Tournament getTournamentById(Long id) throws TournamentApiException {
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<Tournament> latestTournament;
		try {
			latestTournament = restTemplate.getForEntity(tournamentApiBaseUri + id.toString(), Tournament.class);
		} catch(Exception e) {
			log.error("Error found getting latest tournament info", e);
			throw new TournamentApiException(e);
		}
		return latestTournament.getBody();
	}
	
	protected List<String> getRaidBossesFromTournament(Long id) throws DumpException {
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
	
	protected List<BattleGroundEvent> parsePotFile(Long tournamentId, Integer potId) throws DumpException {
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
		} catch (IOException e1) {
			throw new DumpException(e1);
		}
		
		return result;
	}
	
	protected Set<String> parseEntrantFile(Long tournamentId) throws DumpException {
		Set<String> entrants = new HashSet<>();
		Resource resource;
		try {
			resource = new UrlResource(String.format(entrantUrlTemplateForTournament, tournamentId.toString()));
		} catch (MalformedURLException e1) {
			throw new DumpException(e1);
		}
		try(BufferedReader botReader = this.dumpResourceManager.openDumpResource(resource)) {
			String line;
			while((line = botReader.readLine()) != null) {
				String cleanedString = line;
				cleanedString = StringUtils.replace(cleanedString, ",", "");
				cleanedString = StringUtils.trim(cleanedString);
				entrants.add(cleanedString);
			}
		} catch (IOException e) {
			throw new DumpException(e);
		}
		
		return entrants;
	}
	
	private Set<String> getPrestigeSkillList() throws DumpException {
		Set<String> entrants = new HashSet<>();
		Resource resource;
		try {
			resource = new UrlResource(prestigeSkillsMasterListUri);
		} catch (MalformedURLException e1) {
			throw new DumpException(e1);
		}
		try(BufferedReader botReader = this.dumpResourceManager.openDumpResource(resource)) {
			String line;
			while((line = botReader.readLine()) != null) {
				String cleanedString = GambleUtil.cleanString(line);
				entrants.add(cleanedString);
			}
		} catch (IOException e) {
			throw new DumpException(e);
		}
		
		return entrants;
	}
	
	protected void cleanTeamInfoEventPlayerNames(Collection<TeamInfoEvent> event) {
		
	}
	
	protected void cleanUnitInfoEventPlayerName(Collection<UnitInfoEvent> event) {
		final String[] trainerPrefixes = {"Trainer", "Leader", "Rival", "Ranger", "NinjaKid", "Blackbelt", "Lass", "Officer", "Rocket"};
		
	}

}
