package fft_battleground.tournament;

import java.util.concurrent.TimeUnit;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@CacheConfig(cacheNames = {"tips"})
public class TournamentService {
	
	private static final String tournamentInfoApiUri = "https://fftbg.com/api/tournaments?limit=1";
	private static final String tournamentApiBaseUri = "https://fftbg.com/api/tournament/";
	
	private static final String tipsApiUrl = "https://fftbg.com/api/tips";
	
	private Cache<String, Tips> tipsCache = Caffeine.newBuilder()
			  .expireAfterWrite(1, TimeUnit.HOURS)
			  .maximumSize(1)
			  .build();
	
	public Tournament getcurrentTournament() {
		Tournament currentTournament = null;
		
		TournamentInfo latestTournament = this.getLatestTournamentInfo();
		currentTournament = this.getTournamentById(latestTournament.getID());
		
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

}
