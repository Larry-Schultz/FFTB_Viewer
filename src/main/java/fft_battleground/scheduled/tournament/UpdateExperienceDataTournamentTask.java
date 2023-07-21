package fft_battleground.scheduled.tournament;

import java.util.Collection;

import fft_battleground.dump.DumpScheduledTasksManagerImpl;
import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.exception.DumpException;
import fft_battleground.scheduled.DumpTournamentScheduledTask;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UpdateExperienceDataTournamentTask extends DumpTournamentScheduledTask {

	public UpdateExperienceDataTournamentTask(DumpScheduledTasksManagerImpl dumpScheduledTasks) {
		super(dumpScheduledTasks);
	}

	@Override
	protected void task() {
		try {
			Collection<BattleGroundEvent> expEvents = this.dumpServiceRef.getExpUpdatesFromDumpService();
			expEvents.stream().forEach(event -> log.info("Found event from Dump: {} with data: {}", event.getEventType().getEventStringName(), event.toString()));
			this.routerRef.sendAllDataToQueues(expEvents);
		} catch(DumpException e) {
			log.error("error getting exp data from dump", e);
			this.dumpServiceRef.getErrorWebhookManager().sendException(e, "error getting exp data from dump");
		}
	}

}
