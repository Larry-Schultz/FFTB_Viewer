package fft_battleground.dump.scheduled.daily;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.collect.Maps;
import com.google.common.collect.MapDifference.ValueDifference;

import fft_battleground.dump.DumpScheduledTasksManagerImpl;
import fft_battleground.dump.DumpService;
import fft_battleground.dump.scheduled.DumpDailyScheduledTask;
import fft_battleground.event.detector.model.AllegianceEvent;
import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.repo.model.BatchDataEntry;
import fft_battleground.repo.util.BatchDataEntryType;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AllegianceDailyTask extends DumpDailyScheduledTask {

	
	public AllegianceDailyTask(DumpScheduledTasksManagerImpl dumpScheduledTasks, DumpService dumpService) { 
		super(dumpScheduledTasks, dumpService);
	}
	
	protected void task() {
		this.updateAllegiances();
	}
	
	private  void updateAllegiances() {
		log.info("updating allegiances cache");
		Date startDate = new Date();
		BatchDataEntry allegiancePreviousBatchDataEntry = this.batchDataEntryRepoRef.getLastestBatchDataEntryForBatchEntryType(BatchDataEntryType.ALLEGIANCE);
		int numberOfPlayersUpdated = 0;
		final AtomicInteger numberOfPlayersAnalyzed = new AtomicInteger(0);
		Map<String, BattleGroundTeam> allegiancesFromDump = new TreeMap<>();
		Set<String> playerNamesSet = this.dumpDataProviderRef.getPlayersForAllegianceDump();
		if(!this.isCheckAllUsers() ) {
			playerNamesSet = this.filterPlayerListToActiveUsers(playerNamesSet, allegiancePreviousBatchDataEntry);
		}
		numberOfPlayersAnalyzed.set(playerNamesSet.size());
		
		final AtomicInteger count = new AtomicInteger(0);
		playerNamesSet.parallelStream().forEach(player -> {
			BattleGroundTeam team = null;
			try {
				team = this.dumpDataProviderRef.getAllegianceForPlayer(player);
			} catch (Exception e) {
				log.error("Error getting allegiance for player {}", player, e);
				this.errorWebhookManagerRef.sendException(e, "\"Error getting allegiance for player " + player);
			}
			if(team != null) {
				allegiancesFromDump.put(player, team);
			}
			
			if(count.incrementAndGet() % 20 == 0) {
				log.info("Read allegiance data for {} users out of {}", count, numberOfPlayersAnalyzed.get());
			}

		});
		
		Map<String, ValueDifference<BattleGroundTeam>> balanceDelta = Maps.difference(this.dumpServiceRef.getAllegianceCache(), allegiancesFromDump).entriesDiffering();
		List<BattleGroundEvent> allegianceEvents = new LinkedList<>();
		//find differences
		for(String key: balanceDelta.keySet()) {
			AllegianceEvent event = new AllegianceEvent(key, balanceDelta.get(key).rightValue());
			allegianceEvents.add(event);
			//update cache with new data
			this.dumpServiceRef.getAllegianceCache().put(key, balanceDelta.get(key).rightValue());
			//increment players updated
			numberOfPlayersUpdated++;
		}
		
		//add missing players
		for(String key: allegiancesFromDump.keySet()) {
			if(!this.dumpServiceRef.getAllegianceCache().containsKey(key)) {
				AllegianceEvent event = new AllegianceEvent(key, allegiancesFromDump.get(key));
				allegianceEvents.add(event);
				this.dumpServiceRef.getAllegianceCache().put(key, allegiancesFromDump.get(key));
				//increment players updated
				numberOfPlayersUpdated++;
				//increment players analyzed
				numberOfPlayersAnalyzed.getAndIncrement();
			}
		}
		
		allegianceEvents.stream().forEach(event -> log.info("Found event from Dump: {} with data: {}", event.getEventType().getEventStringName(), event.toString()));
		this.eventRouterRef.sendAllDataToQueues(allegianceEvents);
		
		Date endDate = new Date();
		BatchDataEntry newBatchDataEntry = new BatchDataEntry(BatchDataEntryType.ALLEGIANCE, numberOfPlayersAnalyzed.get(), numberOfPlayersUpdated, startDate, endDate);
		this.dumpScheduledTasksRef.writeToBatchDataEntryRepo(newBatchDataEntry);
		log.info("allegiances cache update complete.");
		
		return;
	}
}
