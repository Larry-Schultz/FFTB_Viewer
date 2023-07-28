package fft_battleground.scheduled.tasks.daily;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fft_battleground.discord.WebhookManager;
import fft_battleground.dump.DumpDataProvider;
import fft_battleground.dump.cache.map.LastActiveCache;
import fft_battleground.dump.cache.map.LastFightActiveCache;
import fft_battleground.event.detector.model.PortraitEvent;
import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.repo.model.BatchDataEntry;
import fft_battleground.repo.repository.BatchDataEntryRepo;
import fft_battleground.repo.util.BatchDataEntryType;
import fft_battleground.scheduled.DumpScheduledTasksManager;
import fft_battleground.scheduled.tasks.DumpDailyScheduledTask;
import fft_battleground.util.Router;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PortraitsDailyTask extends DumpDailyScheduledTask {
	
	@Autowired
	private DumpDataProvider dumpDataProvider;
	
	@Autowired
	private BatchDataEntryRepo batchDataEntryRepo;
	
	@Autowired
	private WebhookManager errorWebhookManager;
	
	@Autowired
	private Router<BattleGroundEvent> eventRouter;
	
	@Autowired
	private DumpScheduledTasksManager dumpScheduledTaskManager;
	
	public PortraitsDailyTask(@Autowired LastActiveCache lastActiveCache, 
			@Autowired LastFightActiveCache lastFightActiveCache) { 
		super(lastActiveCache, lastFightActiveCache);
	}
	
	protected void task() {
		this.updatePortraits();
	}
	
	protected void updatePortraits() {
		log.info("updating portrait cache");
		Date startDate = new Date();
		BatchDataEntry portraitPreviousBatchDataEntry = this.batchDataEntryRepo.getLastestBatchDataEntryForBatchEntryType(BatchDataEntryType.PORTRAIT);
		final AtomicInteger playersAnalyzed = new AtomicInteger(0);
		int playersUpdated = 0;
		Map<String, String> portraitsFromDump = new HashMap<>();
		Set<String> playerNamesSet = this.dumpDataProvider.getPlayersForPortraitDump();
		if(!this.isCheckAllUsers()) {
			playerNamesSet = this.filterPlayerListToActiveUsers(playerNamesSet, portraitPreviousBatchDataEntry);
		}
		playersAnalyzed.set(playerNamesSet.size());
		
		final AtomicInteger count = new AtomicInteger(0);
		playerNamesSet.parallelStream().forEach(player -> {
			String portrait;
			PortraitEvent event = null;
			try {
				portrait = this.dumpDataProvider.getPortraitForPlayer(player);
				
				if(!StringUtils.isBlank(portrait)) {
					event = new PortraitEvent(player, portrait);
					log.info("Found event from Dump: {} with data: {}", event.getEventType().getEventStringName(), event.toString());
					this.eventRouter.sendDataToQueues(event);
				}
				
				if(count.incrementAndGet() % 20 == 0) {
					log.info("Read portrait data for {} users out of {}", count, playersAnalyzed);
				}
			} catch (Exception e) {
				log.error("Error updating portrait for player {}", player, e);
				this.errorWebhookManager.sendException(e, "Error updating portrait for player " + player);
			}
		});
		
		
		//generate new BatchDataEntry for this batch run
		Date endDate = new Date();
		BatchDataEntry newBatchDataEntry = new BatchDataEntry(BatchDataEntryType.PORTRAIT, playersAnalyzed.get(), playersUpdated, startDate, endDate);
		this.dumpScheduledTaskManager.writeToBatchDataEntryRepo(newBatchDataEntry);
		log.info("portrait cache update complete");

		return;
	}
}
