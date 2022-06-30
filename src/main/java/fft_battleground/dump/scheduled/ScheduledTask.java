package fft_battleground.dump.scheduled;

import java.util.TimerTask;

import fft_battleground.dump.DumpScheduledTasksManagerImpl;

public abstract class ScheduledTask extends TimerTask {
	protected DumpScheduledTasksManagerImpl dumpScheduledTasksRef;
	
	public ScheduledTask(DumpScheduledTasksManagerImpl dumpScheduledTasks) {
		this.dumpScheduledTasksRef = dumpScheduledTasks;
	}
	
	public void run() {
		this.task();
	}
	
	protected abstract void task();
}