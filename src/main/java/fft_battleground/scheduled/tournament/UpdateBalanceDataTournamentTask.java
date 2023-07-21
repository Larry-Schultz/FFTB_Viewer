package fft_battleground.scheduled.tournament;

import java.util.Collection;

import fft_battleground.dump.DumpScheduledTasksManagerImpl;
import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.exception.DumpException;
import fft_battleground.scheduled.DumpTournamentScheduledTask;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UpdateBalanceDataTournamentTask extends DumpTournamentScheduledTask {

	public UpdateBalanceDataTournamentTask(DumpScheduledTasksManagerImpl dumpScheduledTasks) {
		super(dumpScheduledTasks);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void task() {
		try {
			Collection<BattleGroundEvent> balanceEvents = this.dumpServiceRef.getBalanceUpdatesFromDumpService();
			balanceEvents.stream().forEach(event -> log.info("Found event from Dump: {} with data: {}", event.getEventType().getEventStringName(), event.toString()));
			this.routerRef.sendAllDataToQueues(balanceEvents);
		} catch(DumpException e) {
			log.error("error getting balance data from dump", e);
			this.dumpServiceRef.getErrorWebhookManager().sendException(e, "error getting balance data from dump");
		}
	}

}
