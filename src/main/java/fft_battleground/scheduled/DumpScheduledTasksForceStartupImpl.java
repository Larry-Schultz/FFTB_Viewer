package fft_battleground.scheduled;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fft_battleground.dump.DumpService;
import fft_battleground.scheduled.tasks.ScheduledTask;
import fft_battleground.scheduled.tasks.daily.AllegianceDailyTask;
import fft_battleground.scheduled.tasks.daily.BadAccountsDailyTask;
import fft_battleground.scheduled.tasks.daily.BotListDailyTask;
import fft_battleground.scheduled.tasks.daily.CheckCertificateDailyTask;
import fft_battleground.scheduled.tasks.daily.ClassBonusDailyTask;
import fft_battleground.scheduled.tasks.daily.MissingPortraitCheckDailyTask;
import fft_battleground.scheduled.tasks.daily.PortraitsDailyTask;
import fft_battleground.scheduled.tasks.daily.PrestigeSkillDailyTask;
import fft_battleground.scheduled.tasks.daily.SkillBonusDailyTask;
import fft_battleground.scheduled.tasks.daily.UserSkillsDailyTask;

@Component
public class DumpScheduledTasksForceStartupImpl implements DumpScheduledTasksForceStartup {

	@Autowired
	private BadAccountsDailyTask badAccountsDailyTask;
	
	@Autowired
	private CheckCertificateDailyTask checkCertificateDailyTask;
	
	@Autowired
	private AllegianceDailyTask allegianceDailyTask;
	
	@Autowired
	private BotListDailyTask botListDailyTask;
	
	@Autowired
	private PortraitsDailyTask portraitsDailyTask;
	
	@Autowired
	private UserSkillsDailyTask userSkillsDailyTask;
	
	@Autowired
	private PrestigeSkillDailyTask prestigeSkillDailyTask;
	
	@Autowired
	private ClassBonusDailyTask classBonusDailyTask;
	
	@Autowired
	private SkillBonusDailyTask skillBonusDailyTask;
	
	@Autowired
	private MissingPortraitCheckDailyTask missingPortraitCheckDailyTask;
	
	private ExecutorService threadPool = Executors.newFixedThreadPool(1);
	
	@Override
	public void forceSpecificDailyTasks() {
		this.forceCertificateCheck();
		this.forceScheduledPrestigeSkillTask();
		this.forceScheduledMissingPortraitCheck();	
	}
	
	protected void forceCertificateCheck() {
		this.forceSchedule(this.checkCertificateDailyTask);
	}
	
	protected void forceScheduleAllegianceBatch() {
		this.forceSchedule(this.allegianceDailyTask);
	}
	
	protected void forceScheduleBotListTask() {
		this.forceSchedule(this.botListDailyTask);
	}
	
	protected void forceSchedulePortraitsBatch() {
		this.forceSchedule(this.portraitsDailyTask);
	}
	
	protected void forceScheduleUserSkillsTask(boolean runAll) {
		UserSkillsDailyTask task = this.userSkillsDailyTask;
		task.setCheckAllUsers(runAll);
		this.forceSchedule(task);
	}
	
	protected void forceScheduleClassBonusTask() {
		this.forceSchedule(this.classBonusDailyTask);
	}
	
	protected void forceScheduleSkillBonusTask() {
		this.forceSchedule(this.skillBonusDailyTask);
	}
	
	protected void forceScheduledBadAccountsTask() {
		this.forceSchedule(this.badAccountsDailyTask);
	}
	
	protected void forceScheduledPrestigeSkillTask() {
		this.forceSchedule(this.prestigeSkillDailyTask);
	}
	
	protected void forceScheduledMissingPortraitCheck() {
		this.forceSchedule(this.missingPortraitCheckDailyTask);
	}
	
	private void forceSchedule(ScheduledTask task) {
		this.threadPool.submit(task);
	}
}
