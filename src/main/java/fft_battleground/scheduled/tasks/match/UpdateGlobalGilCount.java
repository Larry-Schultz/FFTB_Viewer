package fft_battleground.scheduled.tasks.match;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fft_battleground.discord.WebhookManager;
import fft_battleground.dump.DumpDataProvider;
import fft_battleground.event.detector.model.fake.GlobalGilHistoryUpdateEvent;
import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.exception.DumpException;
import fft_battleground.repo.model.GlobalGilHistory;
import fft_battleground.scheduled.tasks.DumpMatchScheduledTask;
import fft_battleground.util.Router;

@Component
public class UpdateGlobalGilCount extends DumpMatchScheduledTask {
	private static final Logger log = LoggerFactory.getLogger("dataUpdate");
	
	@Autowired
	private Router<BattleGroundEvent> eventRouter;
	
	@Autowired
	private DumpDataProvider dumpDataProvider;
	
	@Autowired
	private WebhookManager errorWebhookManager;
	
	public UpdateGlobalGilCount() {}
	
	@Override
	public void task() {
		log.debug("updating global gil count");
		GlobalGilHistory globalGilCount = null;
		try {
			globalGilCount = this.recalculateGlobalGil();
		} catch (DumpException e) {
			log.error("error updating global gil count", e);
			this.errorWebhookManager.sendException(e, "error updating global gil count");
			return;
		}
		BattleGroundEvent globalGilCountEvent = new GlobalGilHistoryUpdateEvent(globalGilCount);
		log.info("Found event from Dump: {} with data: {}", globalGilCountEvent.getEventType().getEventStringName(), globalGilCount.toString());
		this.eventRouter.sendDataToQueues(globalGilCountEvent);
	}
	
	private GlobalGilHistory recalculateGlobalGil() throws DumpException {
		Pair<Integer, Long> globalGilData = this.dumpDataProvider.getHighScoreTotal();
		Long globalGilCount = globalGilData.getRight();
		Integer globalPlayerCount = globalGilData.getLeft();
		
		SimpleDateFormat sdf = new SimpleDateFormat(GlobalGilHistory.dateFormatString);
		String currentDateString = sdf.format(new Date());
		GlobalGilHistory globalGilHistory = new GlobalGilHistory(currentDateString, globalGilCount, globalPlayerCount);
		
		return globalGilHistory;
	}
	
}