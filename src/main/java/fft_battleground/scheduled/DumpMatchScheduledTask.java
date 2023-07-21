package fft_battleground.scheduled;

import fft_battleground.dump.DumpScheduledTasksManagerImpl;
import fft_battleground.dump.DumpService;

public abstract class DumpMatchScheduledTask extends ScheduledTask {

	protected DumpService dumpServiceRef;
	
	public DumpMatchScheduledTask(DumpScheduledTasksManagerImpl dumpScheduledTasks) {
		super(dumpScheduledTasks, dumpScheduledTasks.getDumpService());
		this.dumpServiceRef = dumpScheduledTasks.getDumpService();
	}

	protected abstract void task();

}
