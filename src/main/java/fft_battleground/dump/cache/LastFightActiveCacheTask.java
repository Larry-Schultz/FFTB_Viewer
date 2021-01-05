package fft_battleground.dump.cache;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import fft_battleground.repo.model.PlayerRecord;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LastFightActiveCacheTask
extends CacheTask
implements Callable<Map<String, Date>> {
	public static final String dateActiveFormatString = "EEE MMM dd HH:mm:ss z yyyy";
	
	public LastFightActiveCacheTask(List<PlayerRecord> playerRecords) {
		super(playerRecords);
	}

	@Override
	public Map<String, Date> call() throws Exception {
		Map<String, Date> lastFightActiveCache;
		
		log.info("started loading last fight active cache");
		playerRecords.parallelStream().filter(playerRecord -> playerRecord.getLastFightActive() == null).forEach(playerRecord -> {
			try {
				SimpleDateFormat dateFormatter = new SimpleDateFormat(dateActiveFormatString);
				playerRecord.setLastFightActive(dateFormatter.parse("Wed Jan 01 00:00:00 EDT 2020"));
			} catch (ParseException e) {
				log.error("error parsing date for lastActive", e);
			}
		});
		lastFightActiveCache = playerRecords.parallelStream().collect(Collectors.toMap(PlayerRecord::getPlayer, PlayerRecord::getLastFightActive));
		log.info("finished loading last fight active cache");
		
		return lastFightActiveCache;
	}
	
}