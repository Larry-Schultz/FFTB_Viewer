package fft_battleground.scheduled.tasks.daily;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;
import com.google.common.collect.MapDifference.ValueDifference;

import fft_battleground.discord.WebhookManager;
import fft_battleground.dump.DumpDataProvider;
import fft_battleground.dump.cache.map.AllegianceCache;
import fft_battleground.dump.cache.map.LastActiveCache;
import fft_battleground.dump.cache.map.LastFightActiveCache;
import fft_battleground.event.detector.model.AllegianceEvent;
import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.repo.model.BatchDataEntry;
import fft_battleground.repo.repository.BatchDataEntryRepo;
import fft_battleground.repo.util.BatchDataEntryType;
import fft_battleground.scheduled.DumpScheduledTasksManager;
import fft_battleground.scheduled.tasks.DumpDailyScheduledTask;
import fft_battleground.util.Router;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AllegianceDailyTask extends DumpDailyScheduledTask {

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
	
	@Autowired
	private AllegianceCache allegianceCache;
	
	public AllegianceDailyTask(@Autowired LastActiveCache lastActiveCache, 
			@Autowired LastFightActiveCache lastFightActiveCache) { 
		super(lastActiveCache, lastFightActiveCache);
	}
	
	protected void task() {
		this.updateAllegiances();
	}
	
	private  void updateAllegiances() {
		log.info("updating allegiances cache");
		Date startDate = new Date();
		BatchDataEntry allegiancePreviousBatchDataEntry = this.batchDataEntryRepo.getLastestBatchDataEntryForBatchEntryType(BatchDataEntryType.ALLEGIANCE);
		int numberOfPlayersUpdated = 0;
		final AtomicInteger numberOfPlayersAnalyzed = new AtomicInteger(0);
		Map<String, BattleGroundTeam> allegiancesFromDump = new TreeMap<>();
		Set<String> playerNamesSet = this.dumpDataProvider.getPlayersForAllegianceDump();
		if(!this.isCheckAllUsers() ) {
			playerNamesSet = this.filterPlayerListToActiveUsers(playerNamesSet, allegiancePreviousBatchDataEntry);
		}
		numberOfPlayersAnalyzed.set(playerNamesSet.size());
		
		final AtomicInteger count = new AtomicInteger(0);
		playerNamesSet.parallelStream().forEach(player -> {
			BattleGroundTeam team = null;
			try {
				team = this.dumpDataProvider.getAllegianceForPlayer(player);
			} catch (Exception e) {
				log.error("Error getting allegiance for player {}", player, e);
				this.errorWebhookManager.sendException(e, "\"Error getting allegiance for player " + player);
			}
			if(team != null) {
				allegiancesFromDump.put(player, team);
			}
			
			if(count.incrementAndGet() % 20 == 0) {
				log.info("Read allegiance data for {} users out of {}", count, numberOfPlayersAnalyzed.get());
			}

		});
		
		Map<String, ValueDifference<BattleGroundTeam>> balanceDelta = Maps.difference(this.allegianceCache.getMap(), allegiancesFromDump).entriesDiffering();
		List<BattleGroundEvent> allegianceEvents = new LinkedList<>();
		//find differences
		for(String key: balanceDelta.keySet()) {
			AllegianceEvent event = new AllegianceEvent(key, balanceDelta.get(key).rightValue());
			allegianceEvents.add(event);
			//update cache with new data
			this.allegianceCache.put(key, balanceDelta.get(key).rightValue());
			//increment players updated
			numberOfPlayersUpdated++;
		}
		
		//add missing players
		for(String key: allegiancesFromDump.keySet()) {
			if(!this.allegianceCache.containsKey(key)) {
				AllegianceEvent event = new AllegianceEvent(key, allegiancesFromDump.get(key));
				allegianceEvents.add(event);
				this.allegianceCache.put(key, allegiancesFromDump.get(key));
				//increment players updated
				numberOfPlayersUpdated++;
				//increment players analyzed
				numberOfPlayersAnalyzed.getAndIncrement();
			}
		}
		
		allegianceEvents.stream().forEach(event -> log.info("Found event from Dump: {} with data: {}", event.getEventType().getEventStringName(), event.toString()));
		this.eventRouter.sendAllDataToQueues(allegianceEvents);
		
		Date endDate = new Date();
		BatchDataEntry newBatchDataEntry = new BatchDataEntry(BatchDataEntryType.ALLEGIANCE, numberOfPlayersAnalyzed.get(), numberOfPlayersUpdated, startDate, endDate);
		this.dumpScheduledTaskManager.writeToBatchDataEntryRepo(newBatchDataEntry);
		log.info("allegiances cache update complete.");
		
		return;
	}
}
