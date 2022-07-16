package fft_battleground.dump.scheduled;

import java.util.TimerTask;

import fft_battleground.dump.DumpScheduledTasksManagerImpl;
import fft_battleground.dump.DumpService;

public abstract class ScheduledTask extends TimerTask {
	protected DumpScheduledTasksManagerImpl dumpScheduledTasksRef;
	protected DumpService dumpServiceRef;
	
	public ScheduledTask(DumpScheduledTasksManagerImpl dumpScheduledTasks, DumpService dumpService) {
		this.dumpScheduledTasksRef = dumpScheduledTasks;
		this.dumpServiceRef = dumpService;
	}
	
	public void run() {
		this.task();
	}
	
	protected abstract void task();
}