package fft_battleground.dump.scheduled.daily;

import java.util.Date;
import java.util.Set;

import fft_battleground.discord.WebhookManager;
import fft_battleground.dump.DumpDataProvider;
import fft_battleground.dump.DumpScheduledTasksManagerImpl;
import fft_battleground.dump.DumpService;
import fft_battleground.dump.scheduled.ScheduledTask;
import fft_battleground.repo.model.BatchDataEntry;
import fft_battleground.repo.repository.BatchDataEntryRepo;
import fft_battleground.repo.util.BatchDataEntryType;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BotListDailyTask extends ScheduledTask {
	private DumpService dumpServiceRef;
	private DumpDataProvider dumpDataProviderRef;
	private WebhookManager errorWebhookManagerRef;
	private BatchDataEntryRepo batchDataEntryRepoRef;
	
	public BotListDailyTask(DumpScheduledTasksManagerImpl dumpScheduledTasks) {
		super(dumpScheduledTasks);
		this.dumpServiceRef = dumpScheduledTasks.getDumpService();
		this.dumpDataProviderRef = dumpScheduledTasks.getDumpDataProvider();
		this.errorWebhookManagerRef = dumpScheduledTasks.getErrorWebhookManager();
		this.batchDataEntryRepoRef = dumpScheduledTasks.getBatchDataEntryRepo();
	}
	
	protected void task() {
		this.updateBotList();
	}
	
	private Set<String> updateBotList() {
		log.info("updating bot list");
		Date startDate = new Date();
		BatchDataEntry previousBatchDataEntry = this.batchDataEntryRepoRef.getLastestBatchDataEntryForBatchEntryType(BatchDataEntryType.BOT);
		int numberOfPlayersAnalyzed = 0;
		int numberOfPlayersUpdated = 0;
		try {
			Set<String> dumpBots = this.dumpDataProviderRef.getBots();
			dumpBots.stream().forEach(botName -> this.dumpServiceRef.getBotCache().add(botName));
			numberOfPlayersAnalyzed = dumpBots.size();
			numberOfPlayersUpdated = dumpBots.size();
		} catch(Exception e) {
			Date endDate = new Date();
			BatchDataEntry newBatchDataEntry = new BatchDataEntry(BatchDataEntryType.BOT, numberOfPlayersAnalyzed, numberOfPlayersUpdated, 
					startDate, endDate, e.getClass().toString(), e.getStackTrace()[0].getLineNumber());
			this.dumpScheduledTasksRef.writeToBatchDataEntryRepo(newBatchDataEntry);
			this.errorWebhookManagerRef.sendException(e, "Error in Update Bot List batch job");
			return null;
		}
		Date endDate = new Date();
		BatchDataEntry newBatchDataEntry = new BatchDataEntry(BatchDataEntryType.BOT, numberOfPlayersAnalyzed, numberOfPlayersUpdated, startDate, endDate);
		this.dumpScheduledTasksRef.writeToBatchDataEntryRepo(newBatchDataEntry);
		log.info("bot list update complete");
		Set<String> result = this.dumpServiceRef.getBotCache();
		return result;
	}
	
}
