package fft_battleground.scheduled.tasks.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fft_battleground.scheduled.tasks.ScheduledTask;
import fft_battleground.scheduled.tasks.daily.AllegianceDailyTask;
import fft_battleground.scheduled.tasks.daily.BadAccountsDailyTask;
import fft_battleground.scheduled.tasks.daily.BotListDailyTask;
import fft_battleground.scheduled.tasks.daily.CheckCertificateDailyTask;
import fft_battleground.scheduled.tasks.daily.ClassBonusDailyTask;
import fft_battleground.scheduled.tasks.daily.MissingPortraitCheckDailyTask;
import fft_battleground.scheduled.tasks.daily.PortraitsDailyTask;
import fft_battleground.scheduled.tasks.daily.PrestigeSkillDailyTask;
import fft_battleground.scheduled.tasks.daily.RefreshMustadioDailyTask;
import fft_battleground.scheduled.tasks.daily.SkillBonusDailyTask;
import fft_battleground.scheduled.tasks.daily.UserSkillsDailyTask;

@Component
public class DumpDailyScheduledTaskFactory {
	
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
	private RefreshMustadioDailyTask refreshMustadioDailyTask;
	
	@Autowired
	private MissingPortraitCheckDailyTask missingPortraitCheckDailyTask;
	
	public ScheduledTask[] dailyTasks() {
		return new ScheduledTask[] {
				this.badAccountsDailyTask,
				this.checkCertificateDailyTask,
				this.allegianceDailyTask, 
				this.botListDailyTask, 
				this.portraitsDailyTask,
				this.userSkillsDailyTask,
				this.prestigeSkillDailyTask,
				this.classBonusDailyTask,
				this.skillBonusDailyTask,
				this.refreshMustadioDailyTask,
				this.missingPortraitCheckDailyTask
			};
	}
}
