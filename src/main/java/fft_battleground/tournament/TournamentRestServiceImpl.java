package fft_battleground.tournament;

import java.net.MalformedURLException;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import fft_battleground.exception.TournamentApiException;
import fft_battleground.tournament.model.Tournament;
import fft_battleground.tournament.model.TournamentInfo;
import fft_battleground.tournament.tips.Tips;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TournamentRestServiceImpl implements TournamentRestService {
	private static final String tournamentInfoApiUri = "https://fftbg.com/api/tournaments?limit=1";
	private static final String tournamentApiBaseUri = "https://fftbg.com/api/tournament/";
	private static final String tipsApiUrl = "https://fftbg.com/api/tips";
	
	@Override
	public Tips getTips() throws TournamentApiException {
		Resource resource;
		try {
			resource = new UrlResource(tipsApiUrl);
		} catch (MalformedURLException e) {
			log.error("Error found getting latest tournament info", e);
			throw new TournamentApiException(e);
		}
		Tips tips = new Tips(resource);
		
		return tips;
	}
	
	@Override
	public TournamentInfo getLatestTournamentInfo() throws TournamentApiException {
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
	
	@Override
	public Tournament getTournamentById(Long id) throws TournamentApiException {
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
}
