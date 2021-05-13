package fft_battleground.dump.scheduled;

import java.util.TimerTask;

import fft_battleground.dump.DumpService;
import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.event.model.fake.GlobalGilHistoryUpdateEvent;
import fft_battleground.exception.DumpException;
import fft_battleground.repo.model.GlobalGilHistory;
import fft_battleground.util.Router;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UpdateGlobalGilCount extends TimerTask {
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