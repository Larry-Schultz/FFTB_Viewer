package fft_battleground.dump.scheduled.tournament;

import fft_battleground.dump.DumpScheduledTasksManagerImpl;
import fft_battleground.dump.scheduled.DumpTournamentScheduledTask;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UpdateDetectorAuditTableTournamentTask extends DumpTournamentScheduledTask {

	public UpdateDetectorAuditTableTournamentTask(DumpScheduledTasksManagerImpl dumpScheduledTasks) {
		super(dumpScheduledTasks);
	}

	@Override
	protected void task() {
		this.dumpServiceRef.getDetectorAuditManager().updateDatabaseAndClearCache();
	}

}
