package fft_battleground.dump.scheduled.daily;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.util.StringUtils;

import fft_battleground.discord.WebhookManager;
import fft_battleground.dump.DumpScheduledTasksManagerImpl;
import fft_battleground.dump.DumpService;
import fft_battleground.dump.scheduled.DumpDailyScheduledTask;
import fft_battleground.exception.DumpException;
import fft_battleground.exception.MissingPortraitsException;
import fft_battleground.image.ImageCacheService;
import fft_battleground.image.ImageDumpDataProvider;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MissingPortraitCheckDailyTask extends DumpDailyScheduledTask {

	private ImageDumpDataProvider imageDumpDataProvider;
	private ImageCacheService imageCacheService;
	private WebhookManager errorWebhookManager;
	
	public MissingPortraitCheckDailyTask(DumpScheduledTasksManagerImpl dumpScheduledTasks, DumpService dumpService) {
		super(dumpScheduledTasks, dumpService);
		this.imageDumpDataProvider = this.dumpScheduledTasksRef.getImageDumpDataProvider();
		this.imageCacheService = this.dumpScheduledTasksRef.getImageCacheService();
		this.errorWebhookManager = this.dumpServiceRef.getErrorWebhookManager();	
	}

	@Override
	protected void task() {
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
		
	}

}
