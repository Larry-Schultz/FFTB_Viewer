package fft_battleground.scheduled.tasks.daily;

import java.util.Date;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fft_battleground.discord.WebhookManager;
import fft_battleground.dump.DumpDataProvider;
import fft_battleground.dump.DumpService;
import fft_battleground.repo.model.BatchDataEntry;
import fft_battleground.repo.repository.BatchDataEntryRepo;
import fft_battleground.repo.util.BatchDataEntryType;
import fft_battleground.scheduled.DumpScheduledTasksManager;
import fft_battleground.scheduled.tasks.DumpDailyScheduledTask;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class BotListDailyTask extends DumpDailyScheduledTask {
	
	@Autowired
	private DumpDataProvider dumpDataProvider;
	
	@Autowired
	private WebhookManager errorWebhookManager;
	
	@Autowired
	private BatchDataEntryRepo batchDataEntryRepo;
	
	@Autowired
	private DumpScheduledTasksManager dumpScheduledTaskManager;
	
	public BotListDailyTask(@Autowired DumpService dumpService) {
		super(dumpService);
	}
	
	protected void task() {
		this.updateBotList();
	}
	
	private Set<String> updateBotList() {
		log.info("updating bot list");
		Date startDate = new Date();
		BatchDataEntry previousBatchDataEntry = this.batchDataEntryRepo.getLastestBatchDataEntryForBatchEntryType(BatchDataEntryType.BOT);
		int numberOfPlayersAnalyzed = 0;
		int numberOfPlayersUpdated = 0;
		try {
			Set<String> dumpBots = this.dumpDataProvider.getBots();
			dumpBots.stream().forEach(botName -> this.dumpServiceRef.getBotCache().add(botName));
			numberOfPlayersAnalyzed = dumpBots.size();
			numberOfPlayersUpdated = dumpBots.size();
		} catch(Exception e) {
			Date endDate = new Date();
			BatchDataEntry newBatchDataEntry = new BatchDataEntry(BatchDataEntryType.BOT, numberOfPlayersAnalyzed, numberOfPlayersUpdated, 
					startDate, endDate, e.getClass().toString(), e.getStackTrace()[0].getLineNumber());
			this.dumpScheduledTaskManager.writeToBatchDataEntryRepo(newBatchDataEntry);
			this.errorWebhookManager.sendException(e, "Error in Update Bot List batch job");
			return null;
		}
		Date endDate = new Date();
		BatchDataEntry newBatchDataEntry = new BatchDataEntry(BatchDataEntryType.BOT, numberOfPlayersAnalyzed, numberOfPlayersUpdated, startDate, endDate);
		this.dumpScheduledTaskManager.writeToBatchDataEntryRepo(newBatchDataEntry);
		log.info("bot list update complete");
		Set<String> result = this.dumpServiceRef.getBotCache();
		return result;
	}
	
}
