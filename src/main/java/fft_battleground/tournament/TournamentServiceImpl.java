package fft_battleground.tournament;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import fft_battleground.dump.DumpDataProvider;
import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.exception.DumpException;
import fft_battleground.exception.TournamentApiException;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.tournament.model.Tournament;
import fft_battleground.tournament.model.TournamentInfo;
import fft_battleground.tournament.tips.Tips;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TournamentServiceImpl implements TournamentService {
	private static final String tournamentFolderUrlTemplate = "http://www.fftbattleground.com/fftbg/tournament_%s/";
	
	@Autowired
	private TournamentRestService tournamentRestService;
	
	@Autowired
	private TournamentDumpService tournamentDumpService;
	
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
	
	private AtomicReference<Tournament> currentTournamentReference = new AtomicReference<>();
	
	@Override
	public Tournament getCurrentTournament() {
		return this.currentTournamentReference.get();
	}
	
	@Override
	public Tournament createNewCurrentTournament() throws DumpException, TournamentApiException {
		Tournament currentTournament = null;
		
		TournamentInfo latestTournament = this.tournamentRestService.getLatestTournamentInfo();
		currentTournament = this.tournamentRestService.getTournamentById(latestTournament.getID());
		List<String> raidbosses = this.tournamentDumpService.getRaidBossesFromTournament(currentTournament.getID());
		currentTournament.setRaidbosses(raidbosses);
		currentTournament.setEntrants(new ArrayList<>(this.tournamentDumpService.parseEntrantFile(latestTournament.getID()))); //override what comes from the tournament API since I don't trust it
		
		Set<String> allPlayers = this.dumpDataProvider.getHighScoreDump().keySet();
		currentTournament.setAllPlayers(allPlayers);
		
		Map<BattleGroundTeam, Integer> teamValue = this.tournamentDumpService.parseTeamValueFile(latestTournament.getID());
		currentTournament.setTeamValue(teamValue);
		
		Integer streak = this.tournamentDumpService.getChampionStreak(latestTournament.getID());
		currentTournament.setChampionStreak(streak);
		
		List<String> memeTournamentSettings = this.tournamentDumpService.getMemeTournamentSettings(latestTournament.getID());
		currentTournament.setMemeTournamentSettings(memeTournamentSettings);
		
		this.currentTournamentReference.set(currentTournament);
		
		return this.currentTournamentReference.get();
	}
	
	@Override
	public Tips getCurrentTips() throws TournamentApiException {
		Tips currentTip;
		synchronized(tipsCacheLock) {
			currentTip = tipsCache.getIfPresent(tipsCacheKey);
			if(currentTip == null) {
				Tips tips = this.tournamentRestService.getTips();
				this.tipsCache.put(tipsCacheKey, tips);
				currentTip = tips;
			}
		}
		
		return currentTip;
	}
	
	@Override
	public List<BattleGroundEvent> getRealBetInfoFromLatestPotFile(Long tournamentId) throws DumpException {
		List<BattleGroundEvent> result = new LinkedList<>();
		
		Set<String> filenamesInTournamentFolder = this.filenamesInTournamentFolder(tournamentId);
		Integer potId = this.getIdOfLatestPotFile(filenamesInTournamentFolder);
		result = this.tournamentDumpService.parsePotFile(tournamentId, potId);
		
		return result;
	}
	
	@Override
	public Set<String> getPrestigeSkills() throws DumpException {
		synchronized(this.prestigeSkillSetCacheLock) {
			Set<String> prestigeSkills = this.prestigeSkillSetCache.getIfPresent(prestigeSkillSetKey);
			if(prestigeSkills == null) {
				prestigeSkills = this.tournamentDumpService.getPrestigeSkillList();
				this.prestigeSkillSetCache.put(prestigeSkillSetKey, prestigeSkills);
			}
			
			return prestigeSkills;
		}
		
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

}
