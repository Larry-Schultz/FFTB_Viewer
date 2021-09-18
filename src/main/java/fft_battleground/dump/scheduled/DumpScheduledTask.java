package fft_battleground.dump.scheduled;

import java.util.TimerTask;

import fft_battleground.dump.DumpScheduledTasks;

public abstract class DumpScheduledTask extends TimerTask {
	protected DumpScheduledTasks dumpScheduledTasksRef;
	
	public DumpScheduledTask(DumpScheduledTasks dumpScheduledTasks) {
		this.dumpScheduledTasksRef = dumpScheduledTasks;
	}
	
	public void run() {
		this.task();
	}
	
	protected abstract void task();
}