package fft_battleground.dump.scheduled.task;

import java.util.Date;
import java.util.Set;

import fft_battleground.discord.WebhookManager;
import fft_battleground.dump.DumpDataProvider;
import fft_battleground.dump.DumpScheduledTasks;
import fft_battleground.dump.DumpService;
import fft_battleground.dump.scheduled.DumpScheduledTask;
import fft_battleground.event.detector.model.fake.SkillBonusEvent;
import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.exception.DumpException;
import fft_battleground.repo.model.BatchDataEntry;
import fft_battleground.repo.repository.BatchDataEntryRepo;
import fft_battleground.repo.util.BatchDataEntryType;
import fft_battleground.util.Router;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SkillBonusTask extends DumpScheduledTask {
	private DumpService dumpServiceRef;
	private DumpDataProvider dumpDataProviderRef;
	private Router<BattleGroundEvent> eventRouterRef;
	private WebhookManager errorWebhookManagerRef;
	private BatchDataEntryRepo batchDataEntryRepoRef;
	
	public SkillBonusTask(DumpScheduledTasks dumpScheduledTasks) {
		super(dumpScheduledTasks);
		
		this.dumpServiceRef = dumpScheduledTasks.getDumpService();
		this.dumpDataProviderRef = dumpScheduledTasks.getDumpDataProvider();
		this.eventRouterRef = dumpScheduledTasks.getEventRouter();
		this.errorWebhookManagerRef = dumpScheduledTasks.getErrorWebhookManager();
		this.batchDataEntryRepoRef = dumpScheduledTasks.getBatchDataEntryRepo();
	}
	
	protected void task() {
		this.updateAllSkillBonuses();
	}
	
	private void updateAllSkillBonuses() {
		Date startDate = new Date();
		BatchDataEntry previousBatchDataEntry = this.batchDataEntryRepoRef.getLastestBatchDataEntryForBatchEntryType(BatchDataEntryType.SKILL_BONUS);
		int playersAnalyzed = 0;
		int playersUpdated = 0;
		try {
			log.info("updating skill bonuses caches");
			Set<String> skillBonusPlayers = this.dumpDataProviderRef.getPlayersForSkillBonusDump();
			
			skillBonusPlayers = this.dumpScheduledTasksRef.filterPlayerListToActiveUsers(skillBonusPlayers, previousBatchDataEntry);
			playersAnalyzed = skillBonusPlayers.size();
			
			int count = 0;
			for(String player: skillBonusPlayers) {
				Set<String> currentSkillBonuses = this.dumpDataProviderRef.getSkillBonus(player);
				this.dumpServiceRef.getSkillBonusCache().put(player, currentSkillBonuses);
				SkillBonusEvent eventToSendToRepo = new SkillBonusEvent(player, currentSkillBonuses);
				this.eventRouterRef.sendDataToQueues(eventToSendToRepo);
				
				playersUpdated++; count++;
				if(count % 20 == 0) {
					log.info("Read skill bonus data for {} users out of {}", count, playersAnalyzed);
				}
			}
		} catch(DumpException e) {
			log.error("Error updating skill bonus", e);
			this.errorWebhookManagerRef.sendException(e, "Error updating skill bonus");
			return;
		} catch(Exception e) {
			log.error("Error updating class bonus", e);
			this.errorWebhookManagerRef.sendException(e, "Error updating class bonus");
			return;
		}
		log.info("updating skill bonuses cache successful");
		Date endDate = new Date();
		BatchDataEntry newBatchDataEntry = new BatchDataEntry(BatchDataEntryType.SKILL_BONUS, playersAnalyzed, playersUpdated, startDate, endDate);
		this.dumpScheduledTasksRef.writeToBatchDataEntryRepo(newBatchDataEntry);
	}
	
}
