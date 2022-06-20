package fft_battleground.dump.scheduled.task;

import fft_battleground.dump.DumpScheduledTasks;
import fft_battleground.dump.scheduled.DumpScheduledTask;
import fft_battleground.mustadio.MustadioService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RefreshMustadioTask extends DumpScheduledTask {
	private MustadioService mustadioServiceRef;
	
	public RefreshMustadioTask(DumpScheduledTasks dumpScheduledTasks) {
		super(dumpScheduledTasks);
		this.mustadioServiceRef = dumpScheduledTasks.getMustadioService();
	}

	@Override
	protected void task() {
		log.info("Starting batch refresh of Mustadio data");
		this.mustadioServiceRef.refreshMustadioData();
		log.info("Batch refresh of Mustadio data complete");
	}

}
