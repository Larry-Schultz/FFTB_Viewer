package fft_battleground.scheduled.tasks;

import java.util.TimerTask;

public abstract class ScheduledTask extends TimerTask {
	
	public void run() {
		this.task();
	}
	
	protected abstract void task();
}