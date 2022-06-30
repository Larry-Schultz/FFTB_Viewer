package fft_battleground.dump.scheduled;

import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

import fft_battleground.discord.WebhookManager;
import fft_battleground.dump.DumpDataProvider;
import fft_battleground.dump.DumpScheduledTasksManagerImpl;
import fft_battleground.dump.DumpService;
import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.event.model.DatabaseResultsData;
import fft_battleground.repo.model.BatchDataEntry;
import fft_battleground.repo.repository.BatchDataEntryRepo;
import fft_battleground.skill.SkillUtils;
import fft_battleground.util.Router;
import lombok.Getter;
import lombok.Setter;

public abstract class DumpDailyScheduledTask extends ScheduledTask {

	protected DumpService dumpServiceRef;
	protected BatchDataEntryRepo batchDataEntryRepoRef;
	protected DumpDataProvider dumpDataProviderRef;
	protected WebhookManager errorWebhookManagerRef;
	protected SkillUtils monsterUtilsRef;
	protected Router<DatabaseResultsData> betResultsRouterRef;
	protected Router<BattleGroundEvent> eventRouterRef;
	
	@Getter @Setter private boolean checkAllUsers = false;
	
	public DumpDailyScheduledTask(DumpScheduledTasksManagerImpl dumpScheduledTasks) {
		super(dumpScheduledTasks);
		
		this.batchDataEntryRepoRef = this.dumpScheduledTasksRef.getBatchDataEntryRepo();
		this.dumpDataProviderRef = this.dumpScheduledTasksRef.getDumpDataProvider();
		this.errorWebhookManagerRef = this.dumpScheduledTasksRef.getErrorWebhookManager();
		this.monsterUtilsRef = this.dumpScheduledTasksRef.getMonsterUtils();
		this.betResultsRouterRef = this.dumpScheduledTasksRef.getBetResultsRouter();
		this.eventRouterRef = dumpScheduledTasks.getEventRouter();
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
			.filter(player -> this.dumpServiceRef.getLastActiveCache().containsKey(player) || this.dumpServiceRef.getLastFightActiveCache().containsKey(player))
			.filter(player -> this.dumpServiceRef.getLastActiveCache().get(player) != null || this.dumpServiceRef.getLastFightActiveCache().get(player) != null)
			.filter(player -> {
				Date lastActiveDate = this.dumpServiceRef.getLastActiveCache().get(player);
				Date lastFightActiveDate = this.dumpServiceRef.getLastFightActiveCache().get(player);
				
				boolean lastActiveBeforeLastSuccessfulRun = false;
				if(this.dumpServiceRef.getLastActiveCache().get(player) != null) {
					lastActiveBeforeLastSuccessfulRun = lastActiveDate.compareTo(compareToPerviousUpdateComplete) > 0 || compareToPerviousUpdateComplete == null;
				}
				
				boolean lastFightActiveBeforeLastSuccessfulRun = false;
				if(this.dumpServiceRef.getLastFightActiveCache().get(player) != null) {
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
