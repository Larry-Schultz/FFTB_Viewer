package fft_battleground.tournament;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import fft_battleground.model.BattleGroundTeam;
import fft_battleground.tournament.model.Champion;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ChampionServiceImpl implements ChampionService {

	private static final String championApiUrl = "https://fftbg.com/api/champions/";
	
	private static final String champDataKey = "CHAMPION_DATA_KEY";
	
	private Cache<String, List<Champion>> champDataCache = Caffeine.newBuilder()
			  .expireAfterWrite(5, TimeUnit.MINUTES)
			  .maximumSize(1)
			  .build();
	
	@Override
	public Map<BattleGroundTeam, Integer> getSeasonChampWinsByAllegiance() {
		Map<BattleGroundTeam, Integer> seasonChampWinsByAllegiance = new HashMap<>();
		BattleGroundTeam.coreTeams().forEach(team -> seasonChampWinsByAllegiance.put(team, new Integer(0)));
		List<Champion> championList = this.getChampionData();
		for(Champion champ : championList) {
			Integer previousValue = seasonChampWinsByAllegiance.get(champ.getTeam()).intValue();
			Integer newValue = previousValue + champ.getStreak();
			seasonChampWinsByAllegiance.put(champ.getTeam(), newValue);
		}
		return seasonChampWinsByAllegiance;
		
	}
	
	protected List<Champion> getChampionData() {
		if(this.champDataCache.getIfPresent(champDataKey) == null) {
			log.debug("Champion cache busted, calling champion API");
			List<Champion> championData = this.callChampionApi();
			this.champDataCache.put(champDataKey, championData);
		} 
		
		List<Champion> result = this.champDataCache.getIfPresent(champDataKey);
		return result;
	}
	
	@SneakyThrows
	protected List<Champion> callChampionApi() {
		RestTemplate restTemplate = new RestTemplate();
		String championDataString = restTemplate.getForObject(championApiUrl, String.class);
		ObjectMapper objectMapper = new ObjectMapper();
		Champion[] championData = objectMapper.readValue(championDataString, Champion[].class);
		List<Champion> championList = Arrays.asList(championData);
		return championList;
	}
	
}
