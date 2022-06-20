package fft_battleground.dump;

import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.Timer;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import fft_battleground.discord.WebhookManager;
import fft_battleground.dump.scheduled.DumpScheduledTask;
import fft_battleground.dump.scheduled.task.AllegianceTask;
import fft_battleground.dump.scheduled.task.BadAccountsTask;
import fft_battleground.dump.scheduled.task.BotListTask;
import fft_battleground.dump.scheduled.task.CheckCertificateTask;
import fft_battleground.dump.scheduled.task.ClassBonusTask;
import fft_battleground.dump.scheduled.task.PortraitsTask;
import fft_battleground.dump.scheduled.task.RefreshMustadioTask;
import fft_battleground.dump.scheduled.task.SkillBonusTask;
import fft_battleground.dump.scheduled.task.UserSkillsTask;
import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.event.model.DatabaseResultsData;
import fft_battleground.mustadio.MustadioService;
import fft_battleground.repo.model.BatchDataEntry;
import fft_battleground.repo.repository.BatchDataEntryRepo;
import fft_battleground.skill.SkillUtils;
import fft_battleground.util.Router;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DumpScheduledTasks {
	
	@Autowired
	@Getter private Router<BattleGroundEvent> eventRouter;
	
	@Autowired
	@Getter private DumpService dumpService;
	
	@Autowired
	@Getter private DumpDataProvider dumpDataProvider;
	
	@Autowired
	@Getter private SkillUtils monsterUtils;
	
	@Autowired
	@Getter private Router<DatabaseResultsData> betResultsRouter;
	
	@Autowired
	@Getter private BatchDataEntryRepo batchDataEntryRepo;
	
	@Autowired
	@Getter private WebhookManager errorWebhookManager;
	
	@Autowired
	@Getter private MustadioService mustadioService;
	
	@Value("${server.ssl.key-store-password}")
	@Getter private String keyStorePass;
	
	private Timer batchTimer = new Timer();
	private Timer forceTimer = new Timer();
	
	@Scheduled(cron = "0 0 1 * * ?")
	public void runAllUpdates() {
		DumpScheduledTask[] dumpScheduledTasks = new DumpScheduledTask[] {
				new BadAccountsTask(this),
				new CheckCertificateTask(this),
				new AllegianceTask(this), 
				new BotListTask(this), 
				new PortraitsTask(this),
				new UserSkillsTask(this, this.dumpService),
				new ClassBonusTask(this),
				new RefreshMustadioTask(this),
			};
		for(DumpScheduledTask task : dumpScheduledTasks) {
			this.batchTimer.schedule(task, 0);
		}
		
	}
	
	public void forceCertificateCheck() {
		this.forceSchedule(new CheckCertificateTask(this));
	}
	
	public void forceScheduleAllegianceBatch() {
		this.forceSchedule(new AllegianceTask(this));
	}
	
	public void forceScheduleBotListTask() {
		this.forceSchedule(new BotListTask(this));
	}
	
	public void forceSchedulePortraitsBatch() {
		this.forceSchedule(new PortraitsTask(this));
	}
	
	public void forceScheduleUserSkillsTask(DumpService dumpService, boolean runAll) {
		UserSkillsTask task = new UserSkillsTask(this, dumpService);
		task.setCheckAllUsers(runAll);
		this.forceSchedule(task);
	}
	
	public void forceScheduleClassBonusTask() {
		this.forceSchedule(new ClassBonusTask(this));
	}
	
	public void forceScheduleSkillBonusTask() {
		this.forceSchedule(new SkillBonusTask(this));
	}
	
	public void forceScheduledBadAccountsTask() {
		this.forceSchedule(new BadAccountsTask(this));
	}
	
	protected void forceSchedule(DumpScheduledTask task) {
		this.forceTimer.schedule(task, 0);
	}
	
	@Transactional
	public void writeToBatchDataEntryRepo(BatchDataEntry batchDataEntry) {
		this.batchDataEntryRepo.saveAndFlush(batchDataEntry);
	}
	
	public Set<String> filterPlayerListToActiveUsers(Set<String> players, BatchDataEntry batchDataEntry) {
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
			.filter(player -> this.dumpService.getLastActiveCache().containsKey(player) || this.dumpService.getLastFightActiveCache().containsKey(player))
			.filter(player -> this.dumpService.getLastActiveCache().get(player) != null || this.dumpService.getLastFightActiveCache().get(player) != null)
			.filter(player -> {
				Date lastActiveDate = this.dumpService.getLastActiveCache().get(player);
				Date lastFightActiveDate = this.dumpService.getLastFightActiveCache().get(player);
				
				boolean lastActiveBeforeLastSuccessfulRun = false;
				if(this.dumpService.getLastActiveCache().get(player) != null) {
					lastActiveBeforeLastSuccessfulRun = lastActiveDate.compareTo(compareToPerviousUpdateComplete) > 0 || compareToPerviousUpdateComplete == null;
				}
				
				boolean lastFightActiveBeforeLastSuccessfulRun = false;
				if(this.dumpService.getLastFightActiveCache().get(player) != null) {
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
