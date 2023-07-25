package fft_battleground.scheduled.tasks.tournament;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fft_battleground.metrics.DetectorAuditManager;
import fft_battleground.scheduled.tasks.DumpTournamentScheduledTask;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class UpdateDetectorAuditTableTournamentTask extends DumpTournamentScheduledTask {

	@Autowired
	private DetectorAuditManager detectorAuditManager;
	
	public UpdateDetectorAuditTableTournamentTask() {}

	@Override
	protected void task() {
		this.detectorAuditManager.updateDatabaseAndClearCache();
	}

}
