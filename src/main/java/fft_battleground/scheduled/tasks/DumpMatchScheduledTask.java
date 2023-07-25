package fft_battleground.scheduled.tasks;

public abstract class DumpMatchScheduledTask extends ScheduledTask {
	
	public DumpMatchScheduledTask() {
		super();
	}

	protected abstract void task();

}
