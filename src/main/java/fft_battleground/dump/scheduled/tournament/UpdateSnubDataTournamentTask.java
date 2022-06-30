package fft_battleground.dump.scheduled.tournament;

import java.util.Collection;

import fft_battleground.dump.DumpScheduledTasksManagerImpl;
import fft_battleground.dump.scheduled.DumpTournamentScheduledTask;
import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.exception.DumpException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UpdateSnubDataTournamentTask extends DumpTournamentScheduledTask {

	public UpdateSnubDataTournamentTask(DumpScheduledTasksManagerImpl dumpScheduledTasks) {
		super(dumpScheduledTasks);
	}

	@Override
	protected void task() {
		try {
			Collection<BattleGroundEvent> snubEvents = this.dumpServiceRef.getSnubUpdatesFromDumpService();
			log.info("Updated {} snub events", snubEvents.size());
			this.routerRef.sendAllDataToQueues(snubEvents);
		} catch(DumpException e) {
			log.error("error getting snub data from dump", e);
			this.dumpServiceRef.getErrorWebhookManager().sendException(e, "error getting snub data from dump");
		}
	}

}
