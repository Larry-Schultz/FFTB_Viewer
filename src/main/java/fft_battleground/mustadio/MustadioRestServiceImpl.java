package fft_battleground.mustadio;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import fft_battleground.exception.MustadioApiException;
import fft_battleground.mustadio.model.MustadioClasses;
import fft_battleground.mustadio.model.MustadioItems;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MustadioRestServiceImpl implements MustadioRestService {
	private static final String mustadioItemUrl = "https://www.mustad.io/api/items?include=stats";
	private static final String mustadioClassUrl = "https://www.mustad.io/api/classes?include=stats";
	
	@Override
	public MustadioItems fetchMustadioItemsData() throws MustadioApiException {
		log.info("Calling mustadio to get item data");
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<MustadioItems> mustadioInfo;
		try {
			mustadioInfo = restTemplate.getForEntity(mustadioItemUrl, MustadioItems.class);
		} catch(Exception e) {
			log.error("Error found getting latest tournament info", e);
			throw new MustadioApiException(e);
		}
		log.info("mustadio item data fetch complete");
		
		return mustadioInfo.getBody();
	}
	@Override
	public MustadioClasses fetchMustadioClassData() throws MustadioApiException {
		log.info("Calling mustadio to get class base stats");
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<MustadioClasses> mustadioInfo;
		try {
			mustadioInfo = restTemplate.getForEntity(mustadioClassUrl, MustadioClasses.class);
		} catch(Exception e) {
			log.error("Error found getting latest tournament info", e);
			throw new MustadioApiException(e);
		}
		log.info("Mustadio class base stat fetch complete");
		
		return mustadioInfo.getBody();
	}

}
