package fft_battleground.scheduled.tasks.daily;

import java.util.Date;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fft_battleground.discord.WebhookManager;
import fft_battleground.dump.DumpDataProvider;
import fft_battleground.dump.DumpService;
import fft_battleground.event.detector.model.fake.SkillBonusEvent;
import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.exception.DumpException;
import fft_battleground.repo.model.BatchDataEntry;
import fft_battleground.repo.repository.BatchDataEntryRepo;
import fft_battleground.repo.util.BatchDataEntryType;
import fft_battleground.scheduled.DumpScheduledTasksManager;
import fft_battleground.scheduled.tasks.DumpDailyScheduledTask;
import fft_battleground.util.Router;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component 
public class SkillBonusDailyTask extends DumpDailyScheduledTask {
	
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
	
	public SkillBonusDailyTask(@Autowired DumpService dumpService) {
		super(dumpService);
	}
	
	protected void task() {
		this.updateAllSkillBonuses();
	}
	
	private void updateAllSkillBonuses() {
		Date startDate = new Date();
		BatchDataEntry previousBatchDataEntry = this.batchDataEntryRepo.getLastestBatchDataEntryForBatchEntryType(BatchDataEntryType.SKILL_BONUS);
		int playersAnalyzed = 0;
		int playersUpdated = 0;
		try {
			log.info("updating skill bonuses caches");
			Set<String> skillBonusPlayers = this.dumpDataProvider.getPlayersForSkillBonusDump();
			
			if(!this.isCheckAllUsers() ) {
				skillBonusPlayers = this.filterPlayerListToActiveUsers(skillBonusPlayers, previousBatchDataEntry);
			}
			playersAnalyzed = skillBonusPlayers.size();
			
			int count = 0;
			for(String player: skillBonusPlayers) {
				Set<String> currentSkillBonuses = this.dumpDataProvider.getSkillBonus(player);
				this.dumpServiceRef.getSkillBonusCache().put(player, currentSkillBonuses);
				SkillBonusEvent eventToSendToRepo = new SkillBonusEvent(player, currentSkillBonuses);
				this.eventRouter.sendDataToQueues(eventToSendToRepo);
				
				playersUpdated++; count++;
				if(count % 20 == 0) {
					log.info("Read skill bonus data for {} users out of {}", count, playersAnalyzed);
				}
			}
		} catch(DumpException e) {
			log.error("Error updating skill bonus", e);
			this.errorWebhookManager.sendException(e, "Error updating skill bonus");
			return;
		} catch(Exception e) {
			log.error("Error updating class bonus", e);
			this.errorWebhookManager.sendException(e, "Error updating class bonus");
			return;
		}
		log.info("updating skill bonuses cache successful");
		Date endDate = new Date();
		BatchDataEntry newBatchDataEntry = new BatchDataEntry(BatchDataEntryType.SKILL_BONUS, playersAnalyzed, playersUpdated, startDate, endDate);
		this.dumpScheduledTaskManager.writeToBatchDataEntryRepo(newBatchDataEntry);
	}
	
}
