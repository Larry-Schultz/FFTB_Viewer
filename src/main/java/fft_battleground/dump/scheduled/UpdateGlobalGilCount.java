package fft_battleground.dump.scheduled;

import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fft_battleground.dump.DumpService;
import fft_battleground.event.detector.model.BattleGroundEvent;
import fft_battleground.event.detector.model.fake.GlobalGilHistoryUpdateEvent;
import fft_battleground.exception.DumpException;
import fft_battleground.repo.model.GlobalGilHistory;
import fft_battleground.util.Router;

public class UpdateGlobalGilCount extends TimerTask {
	Logger log = LoggerFactory.getLogger("dataUpdate");
	
	private Router<BattleGroundEvent> routerRef;
	private DumpService dumpServiceRef;
	
	public UpdateGlobalGilCount(Router<BattleGroundEvent> routerRef, DumpService dumpServiceRef) {
		this.routerRef = routerRef;
		this.dumpServiceRef = dumpServiceRef;
	}
	
	@Override
	public void run() {
		log.debug("updating global gil count");
		GlobalGilHistory globalGilCount = null;
		try {
			globalGilCount = this.dumpServiceRef.recalculateGlobalGil();
		} catch (DumpException e) {
			log.error("error updating global gil count", e);
			this.dumpServiceRef.getErrorWebhookManager().sendException(e, "error updating global gil count");
			return;
		}
		BattleGroundEvent globalGilCountEvent = new GlobalGilHistoryUpdateEvent(globalGilCount);
		log.info("Found event from Dump: {} with data: {}", globalGilCountEvent.getEventType().getEventStringName(), globalGilCount.toString());
		this.routerRef.sendDataToQueues(globalGilCountEvent);
	}
}