package fft_battleground.scheduled.tasks.daily;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import fft_battleground.discord.WebhookManager;
import fft_battleground.dump.cache.map.LastActiveCache;
import fft_battleground.dump.cache.map.LastFightActiveCache;
import fft_battleground.exception.DumpException;
import fft_battleground.exception.MissingPortraitsException;
import fft_battleground.image.ImageCacheService;
import fft_battleground.image.ImageDumpDataProvider;
import fft_battleground.scheduled.tasks.DumpDailyScheduledTask;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class MissingPortraitCheckDailyTask extends DumpDailyScheduledTask {

	@Autowired
	private ImageDumpDataProvider imageDumpDataProvider;
	
	@Autowired
	private ImageCacheService imageCacheService;
	
	@Autowired
	private WebhookManager errorWebhookManager;
	
	public MissingPortraitCheckDailyTask(@Autowired LastActiveCache lastActiveCache, 
			@Autowired LastFightActiveCache lastFightActiveCache) { 
		super(lastActiveCache, lastFightActiveCache);
	}

	@Override
	protected void task() {
		log.info("Starting MissingPortraitCheck");
		List<String> activePortraits = List.of();
		try {
			activePortraits = this.imageDumpDataProvider.getActivePortraits();
		} catch (DumpException e) {
			String errorMessage = "Error calling MissingPortraitCheckDailyTask: " + e.getMessage();
			log.error(errorMessage, e);
			this.errorWebhookManager.sendException(e, errorMessage);
		}
		
		List<String> missingPortraits = activePortraits.stream()
			.map(activePortrait -> StringUtils.replace(activePortrait, ".gif", ""))
			.filter(activePortrait -> this.imageCacheService.getPortaitImage(activePortrait) == null)
			.collect(Collectors.toList());
		
		if(missingPortraits.size() > 0) {
			String warningMessage = "Missing portraits: " + missingPortraits.stream().collect(Collectors.joining(", "));
			String exceptionMessage = "Missing portrait count: " + missingPortraits.size();
			this.errorWebhookManager.sendWarningException(new MissingPortraitsException(exceptionMessage), warningMessage);		
		}
		log.info("MissingPortraitCheck complete");
	}

}
