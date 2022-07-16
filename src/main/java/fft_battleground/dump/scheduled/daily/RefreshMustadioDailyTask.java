package fft_battleground.dump.scheduled.daily;

import fft_battleground.dump.DumpScheduledTasksManagerImpl;
import fft_battleground.dump.DumpService;
import fft_battleground.dump.scheduled.ScheduledTask;
import fft_battleground.mustadio.MustadioService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RefreshMustadioDailyTask extends ScheduledTask {
	private MustadioService mustadioServiceRef;
	
	public RefreshMustadioDailyTask(DumpScheduledTasksManagerImpl dumpScheduledTasks, DumpService dumpService) {
		super(dumpScheduledTasks, dumpService);
		this.mustadioServiceRef = dumpScheduledTasks.getMustadioService();
	}

	@Override
	protected void task() {
		log.info("Starting batch refresh of Mustadio data");
		this.mustadioServiceRef.refreshMustadioData();
		log.info("Batch refresh of Mustadio data complete");
	}

}
