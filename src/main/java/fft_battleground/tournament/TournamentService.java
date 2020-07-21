package fft_battleground.tournament;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
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

import fft_battleground.dump.DumpResourceManager;
import fft_battleground.util.GambleUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@CacheConfig(cacheNames = {"tips"})
public class TournamentService {
	
	private static final String tournamentInfoApiUri = "https://fftbg.com/api/tournaments?limit=1";
	private static final String tournamentApiBaseUri = "https://fftbg.com/api/tournament/";
	
	private static final String tipsApiUrl = "https://fftbg.com/api/tips";
	private static final String raidBossUrlTemplateForTournament = "http://www.fftbattleground.com/fftbg/tournament_%s/raidboss.txt";
	private static final String tournamentPotUrlTemplate = "http://www.fftbattleground.com/fftbg/tournament_%1$s/pot%2$s.txt";
	
	@Autowired
	private DumpResourceManager dumpResourceManager;
	
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
		try(BufferedReader skillReader = this.dumpResourceManager.openDumpResource(resource)) {
			String line;
			while((line = skillReader.readLine()) != null) {
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

}
