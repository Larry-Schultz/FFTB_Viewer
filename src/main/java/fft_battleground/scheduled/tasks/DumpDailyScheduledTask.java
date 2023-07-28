package fft_battleground.scheduled.tasks;

import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

import fft_battleground.dump.cache.map.LastActiveCache;
import fft_battleground.dump.cache.map.LastFightActiveCache;
import fft_battleground.repo.model.BatchDataEntry;
import lombok.Getter;
import lombok.Setter;

public abstract class DumpDailyScheduledTask extends ScheduledTask {
	
	@Getter @Setter private boolean checkAllUsers = false;
	
	protected LastActiveCache lastActiveCache;
	protected LastFightActiveCache lastFightActiveCache;
	
	public DumpDailyScheduledTask(LastActiveCache lastActiveCache, LastFightActiveCache lastFightActiveCache) {
		this.lastActiveCache = lastActiveCache;
		this.lastFightActiveCache = lastFightActiveCache;
	}

	protected abstract void task();
	
	protected Set<String> filterPlayerListToActiveUsers(Set<String> players, BatchDataEntry batchDataEntry) {
		Date previousUpdateComplete = batchDataEntry != null && (batchDataEntry.getSuccessfulRun() != null && batchDataEntry.getSuccessfulRun()) ? batchDataEntry.getUpdateStarted() : null;
		Set<String> result;
		
		//if value is null, just use yesterday
		if(previousUpdateComplete == null) {
			Calendar today = Calendar.getInstance();
			today.add(Calendar.DAY_OF_WEEK, -1);
			previousUpdateComplete = today.getTime();
		} else {
			//set time to day before last update date.
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(previousUpdateComplete);
			calendar.add(Calendar.DAY_OF_WEEK, -1);
			previousUpdateComplete = calendar.getTime();
		}
		final Date compareToPerviousUpdateComplete = previousUpdateComplete; //because the compile complains that previousUpdateComplete was not a final variable
		result = players.parallelStream()
			.filter(player -> this.lastActiveCache.containsKey(player) || this.lastFightActiveCache.containsKey(player))
			.filter(player -> this.lastActiveCache.get(player) != null || this.lastFightActiveCache.get(player) != null)
			.filter(player -> {
				Date lastActiveDate = this.lastActiveCache.get(player);
				Date lastFightActiveDate = this.lastFightActiveCache.get(player);
				
				boolean lastActiveBeforeLastSuccessfulRun = false;
				if(this.lastActiveCache.get(player) != null) {
					lastActiveBeforeLastSuccessfulRun = lastActiveDate.compareTo(compareToPerviousUpdateComplete) > 0 || compareToPerviousUpdateComplete == null;
				}
				
				boolean lastFightActiveBeforeLastSuccessfulRun = false;
				if(this.lastFightActiveCache.get(player) != null) {
					lastFightActiveBeforeLastSuccessfulRun = lastFightActiveDate.compareTo(compareToPerviousUpdateComplete) > 0 || compareToPerviousUpdateComplete == null;
				}
				
				boolean beforeSuccessfulRun = false;
				if(lastFightActiveDate != null && lastActiveDate != null) {
					int compareResult = lastActiveDate.compareTo(lastFightActiveDate); //greater than 0 means lastActive is after lastFightActive
					
					if(compareResult == 0) {
						beforeSuccessfulRun = lastActiveBeforeLastSuccessfulRun; //if equal somehow just use the last active
					} else if(compareResult == -1) {
						beforeSuccessfulRun = lastFightActiveBeforeLastSuccessfulRun; //this means fight active is more recent
					} else {
						beforeSuccessfulRun = lastActiveBeforeLastSuccessfulRun; //this means last active is more recent than fight active
					}
				} else if(lastActiveDate != null) {
					beforeSuccessfulRun = lastActiveBeforeLastSuccessfulRun;
				} else if(lastFightActiveDate != null) {
					beforeSuccessfulRun = lastFightActiveBeforeLastSuccessfulRun;
				} else {
					beforeSuccessfulRun = false;
				}
				
				return beforeSuccessfulRun;
			})
			.collect(Collectors.toSet());
		return result;
	}

}
