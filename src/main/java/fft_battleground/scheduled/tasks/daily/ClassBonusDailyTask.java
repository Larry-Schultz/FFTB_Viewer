package fft_battleground.scheduled.tasks.daily;

import java.util.Date;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fft_battleground.discord.WebhookManager;
import fft_battleground.dump.DumpDataProvider;
import fft_battleground.dump.cache.map.ClassBonusCache;
import fft_battleground.dump.cache.map.LastActiveCache;
import fft_battleground.dump.cache.map.LastFightActiveCache;
import fft_battleground.event.detector.model.fake.ClassBonusEvent;
import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.exception.DumpException;
import fft_battleground.repo.model.BatchDataEntry;
import fft_battleground.repo.model.ClassBonus;
import fft_battleground.repo.repository.BatchDataEntryRepo;
import fft_battleground.repo.util.BatchDataEntryType;
import fft_battleground.scheduled.DumpScheduledTasksManager;
import fft_battleground.scheduled.tasks.DumpDailyScheduledTask;
import fft_battleground.util.Router;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ClassBonusDailyTask extends DumpDailyScheduledTask {
	
	@Autowired
	private DumpDataProvider dumpDataProvider;
	
	@Autowired
	private Router<BattleGroundEvent> eventRouter;
	
	@Autowired
	private WebhookManager errorWebhookManager;
	
	@Autowired
	private BatchDataEntryRepo batchDataEntryRepo;
	
	@Autowired
	private DumpScheduledTasksManager dumpScheduledTaskManager;
	
	@Autowired
	private ClassBonusCache classBonusCache;
	
	@Getter @Setter private boolean checkAllUsers = false;
	
	public ClassBonusDailyTask(@Autowired LastActiveCache lastActiveCache, 
			@Autowired LastFightActiveCache lastFightActiveCache) { 
		super(lastActiveCache, lastFightActiveCache);
	}
	
	protected void task() {
		this.updateAllClassBonuses();
	}
	
	private void updateAllClassBonuses() {
		Date startDate = new Date();
		BatchDataEntry previousBatchDataEntry = this.batchDataEntryRepo.getLastestBatchDataEntryForBatchEntryType(BatchDataEntryType.CLASS_BONUS);
		int playersAnalyzed = 0;
		int playersUpdated = 0;
		try {
			log.info("updating class bonuses caches");
			Set<String> classBonusPlayers = this.dumpDataProvider.getPlayersForClassBonusDump();

			if(!this.checkAllUsers) {
				classBonusPlayers = this.filterPlayerListToActiveUsers(classBonusPlayers, previousBatchDataEntry);
			}
			playersAnalyzed = classBonusPlayers.size();
			
			int count = 0;
			for(String player: classBonusPlayers) {
				Set<String> currentClassBonuses = this.dumpDataProvider.getClassBonus(player);
				currentClassBonuses = ClassBonus.convertToBotOutput(currentClassBonuses); //convert to bot output
				this.classBonusCache.put(player, currentClassBonuses);
				ClassBonusEvent eventToSendToRepo = new ClassBonusEvent(player, currentClassBonuses);
				this.eventRouter.sendDataToQueues(eventToSendToRepo);
				
				playersUpdated++; count++;
				if(count % 20 == 0) {
					log.info("Read class bonus data for {} users out of {}", count, playersAnalyzed);
				}
			}
		} catch(DumpException e) {
			log.error("Error updating class bonus", e);
			this.errorWebhookManager.sendException(e, "Error updating class bonus");
			return;
		} catch(Exception e) {
			log.error("Error updating class bonus", e);
			this.errorWebhookManager.sendException(e, "Error updating class bonus");
			return;
		}
		log.info("updating class bonuses caches successful");
		Date endDate = new Date();
		BatchDataEntry newBatchDataEntry = new BatchDataEntry(BatchDataEntryType.CLASS_BONUS, playersAnalyzed, playersUpdated, startDate, endDate);
		this.dumpScheduledTaskManager.writeToBatchDataEntryRepo(newBatchDataEntry);
	}
}
