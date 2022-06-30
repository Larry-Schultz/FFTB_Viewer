package fft_battleground.dump.scheduled.tournament;

import java.util.Collection;

import fft_battleground.dump.DumpScheduledTasksManagerImpl;
import fft_battleground.dump.scheduled.DumpTournamentScheduledTask;
import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.exception.DumpException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UpdateLastActiveDataTournamentTask extends DumpTournamentScheduledTask {

	public UpdateLastActiveDataTournamentTask(DumpScheduledTasksManagerImpl dumpScheduledTasks) {
		super(dumpScheduledTasks);
	}

	@Override
	protected void task() {
		try {
			Collection<BattleGroundEvent> lastActiveEvents = this.dumpServiceRef.getLastActiveUpdatesFromDumpService();
			//lastActiveEvents.stream().forEach(event -> log.info("Found event from Dump: {} with data: {}", event.getEventType().getEventStringName(), event.toString()));
			log.info("Updated {} lastActiveEvents", lastActiveEvents.size());
			this.routerRef.sendAllDataToQueues(lastActiveEvents);
		} catch(DumpException e) {
			log.error("error getting last active data from dump", e);
			this.dumpServiceRef.getErrorWebhookManager().sendException(e, "error getting last active data from dump");
		}
	}

}
