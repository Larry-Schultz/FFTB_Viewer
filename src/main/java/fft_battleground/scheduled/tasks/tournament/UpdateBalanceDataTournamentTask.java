package fft_battleground.scheduled.tasks.tournament;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fft_battleground.discord.WebhookManager;
import fft_battleground.dump.cache.DumpCacheManager;
import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.exception.DumpException;
import fft_battleground.scheduled.tasks.DumpTournamentScheduledTask;
import fft_battleground.util.Router;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class UpdateBalanceDataTournamentTask extends DumpTournamentScheduledTask {

	@Autowired
	private Router<BattleGroundEvent> eventRouter;
	
	@Autowired
	private WebhookManager errorWebhookManager;
	
	@Autowired
	private DumpCacheManager dumpCacheManager;
	
	public UpdateBalanceDataTournamentTask() {}

	@Override
	protected void task() {
		try {
			Collection<BattleGroundEvent> balanceEvents = this.dumpCacheManager.getBalanceUpdatesFromDumpService();
			balanceEvents.stream().forEach(event -> log.info("Found event from Dump: {} with data: {}", event.getEventType().getEventStringName(), event.toString()));
			this.eventRouter.sendAllDataToQueues(balanceEvents);
		} catch(DumpException e) {
			log.error("error getting balance data from dump", e);
			this.errorWebhookManager.sendException(e, "error getting balance data from dump");
		}
	}

}
