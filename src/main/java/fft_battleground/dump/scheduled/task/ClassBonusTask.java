package fft_battleground.dump.scheduled.task;

import java.util.Date;
import java.util.Set;

import fft_battleground.discord.WebhookManager;
import fft_battleground.dump.DumpDataProvider;
import fft_battleground.dump.DumpScheduledTasks;
import fft_battleground.dump.DumpService;
import fft_battleground.dump.scheduled.DumpScheduledTask;
import fft_battleground.event.detector.model.fake.ClassBonusEvent;
import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.exception.DumpException;
import fft_battleground.repo.model.BatchDataEntry;
import fft_battleground.repo.model.ClassBonus;
import fft_battleground.repo.repository.BatchDataEntryRepo;
import fft_battleground.repo.util.BatchDataEntryType;
import fft_battleground.util.Router;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClassBonusTask extends DumpScheduledTask {
	private DumpService dumpServiceRef;
	private DumpDataProvider dumpDataProviderRef;
	private Router<BattleGroundEvent> eventRouterRef;
	private WebhookManager errorWebhookManagerRef;
	private BatchDataEntryRepo batchDataEntryRepoRef;
	
	public ClassBonusTask(DumpScheduledTasks dumpScheduledTasks) {
		super(dumpScheduledTasks);
		
		this.dumpServiceRef = dumpScheduledTasks.getDumpService();
		this.dumpDataProviderRef = dumpScheduledTasks.getDumpDataProvider();
		this.eventRouterRef = dumpScheduledTasks.getEventRouter();
		this.errorWebhookManagerRef = dumpScheduledTasks.getErrorWebhookManager();
		this.batchDataEntryRepoRef = dumpScheduledTasks.getBatchDataEntryRepo();
	}
	
	protected void task() {
		this.updateAllClassBonuses();
	}
	
	private void updateAllClassBonuses() {
		Date startDate = new Date();
		BatchDataEntry previousBatchDataEntry = this.batchDataEntryRepoRef.getLastestBatchDataEntryForBatchEntryType(BatchDataEntryType.CLASS_BONUS);
		int playersAnalyzed = 0;
		int playersUpdated = 0;
		try {
			log.info("updating class bonuses caches");
			Set<String> classBonusPlayers = this.dumpDataProviderRef.getPlayersForClassBonusDump();

			classBonusPlayers = this.dumpScheduledTasksRef.filterPlayerListToActiveUsers(classBonusPlayers, previousBatchDataEntry);
			playersAnalyzed = classBonusPlayers.size();
			
			int count = 0;
			for(String player: classBonusPlayers) {
				Set<String> currentClassBonuses = this.dumpDataProviderRef.getClassBonus(player);
				currentClassBonuses = ClassBonus.convertToBotOutput(currentClassBonuses); //convert to bot output
				this.dumpServiceRef.getClassBonusCache().put(player, currentClassBonuses);
				ClassBonusEvent eventToSendToRepo = new ClassBonusEvent(player, currentClassBonuses);
				this.eventRouterRef.sendDataToQueues(eventToSendToRepo);
				
				playersUpdated++; count++;
				if(count % 20 == 0) {
					log.info("Read class bonus data for {} users out of {}", count, playersAnalyzed);
				}
			}
		} catch(DumpException e) {
			log.error("Error updating class bonus", e);
			this.errorWebhookManagerRef.sendException(e, "Error updating class bonus");
			return;
		} catch(Exception e) {
			log.error("Error updating class bonus", e);
			this.errorWebhookManagerRef.sendException(e, "Error updating class bonus");
			return;
		}
		log.info("updating class bonuses caches successful");
		Date endDate = new Date();
		BatchDataEntry newBatchDataEntry = new BatchDataEntry(BatchDataEntryType.CLASS_BONUS, playersAnalyzed, playersUpdated, startDate, endDate);
		this.dumpScheduledTasksRef.writeToBatchDataEntryRepo(newBatchDataEntry);
	}
}
