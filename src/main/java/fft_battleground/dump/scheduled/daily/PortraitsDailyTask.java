package fft_battleground.dump.scheduled.daily;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;

import fft_battleground.dump.DumpScheduledTasksManagerImpl;
import fft_battleground.dump.scheduled.DumpDailyScheduledTask;
import fft_battleground.event.detector.model.PortraitEvent;
import fft_battleground.repo.model.BatchDataEntry;
import fft_battleground.repo.util.BatchDataEntryType;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PortraitsDailyTask extends DumpDailyScheduledTask {
	
	public PortraitsDailyTask(DumpScheduledTasksManagerImpl dumpScheduledTasks) {
		super(dumpScheduledTasks);
	}
	
	protected void task() {
		this.updatePortraits();
	}
	
	protected void updatePortraits() {
		log.info("updating portrait cache");
		Date startDate = new Date();
		BatchDataEntry portraitPreviousBatchDataEntry = this.batchDataEntryRepoRef.getLastestBatchDataEntryForBatchEntryType(BatchDataEntryType.PORTRAIT);
		final AtomicInteger playersAnalyzed = new AtomicInteger(0);
		int playersUpdated = 0;
		Map<String, String> portraitsFromDump = new HashMap<>();
		Set<String> playerNamesSet = this.dumpDataProviderRef.getPlayersForPortraitDump();
		if(!this.isCheckAllUsers()) {
			playerNamesSet = this.filterPlayerListToActiveUsers(playerNamesSet, portraitPreviousBatchDataEntry);
		}
		playersAnalyzed.set(playerNamesSet.size());
		
		final AtomicInteger count = new AtomicInteger(0);
		playerNamesSet.parallelStream().forEach(player -> {
			String portrait;
			PortraitEvent event = null;
			try {
				portrait = this.dumpDataProviderRef.getPortraitForPlayer(player);
				
				if(!StringUtils.isBlank(portrait)) {
					event = new PortraitEvent(player, portrait);
					log.info("Found event from Dump: {} with data: {}", event.getEventType().getEventStringName(), event.toString());
					this.eventRouterRef.sendDataToQueues(event);
				}
				
				if(count.incrementAndGet() % 20 == 0) {
					log.info("Read portrait data for {} users out of {}", count, playersAnalyzed);
				}
			} catch (Exception e) {
				log.error("Error updating portrait for player {}", player, e);
				this.errorWebhookManagerRef.sendException(e, "Error updating portrait for player " + player);
			}
		});
		
		
		//generate new BatchDataEntry for this batch run
		Date endDate = new Date();
		BatchDataEntry newBatchDataEntry = new BatchDataEntry(BatchDataEntryType.PORTRAIT, playersAnalyzed.get(), playersUpdated, startDate, endDate);
		this.dumpScheduledTasksRef.writeToBatchDataEntryRepo(newBatchDataEntry);
		log.info("portrait cache update complete");

		return;
	}
}
