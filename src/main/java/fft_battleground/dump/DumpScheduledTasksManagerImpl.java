package fft_battleground.dump;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import fft_battleground.discord.WebhookManager;
import fft_battleground.dump.scheduled.DumpMatchScheduledTask;
import fft_battleground.dump.scheduled.DumpTournamentScheduledTask;
import fft_battleground.dump.scheduled.ScheduledTask;
import fft_battleground.dump.scheduled.daily.AllegianceDailyTask;
import fft_battleground.dump.scheduled.daily.BadAccountsDailyTask;
import fft_battleground.dump.scheduled.daily.BotListDailyTask;
import fft_battleground.dump.scheduled.daily.CheckCertificateDailyTask;
import fft_battleground.dump.scheduled.daily.ClassBonusDailyTask;
import fft_battleground.dump.scheduled.daily.MissingPortraitCheckDailyTask;
import fft_battleground.dump.scheduled.daily.PortraitsDailyTask;
import fft_battleground.dump.scheduled.daily.PrestigeSkillDailyTask;
import fft_battleground.dump.scheduled.daily.RefreshMustadioDailyTask;
import fft_battleground.dump.scheduled.daily.SkillBonusDailyTask;
import fft_battleground.dump.scheduled.daily.UserSkillsDailyTask;
import fft_battleground.dump.scheduled.match.UpdateGlobalGilCount;
import fft_battleground.dump.scheduled.tournament.UpdateBalanceDataTournamentTask;
import fft_battleground.dump.scheduled.tournament.UpdateClassBonusCacheTournamentTask;
import fft_battleground.dump.scheduled.tournament.UpdateDetectorAuditTableTournamentTask;
import fft_battleground.dump.scheduled.tournament.UpdateExperienceDataTournamentTask;
import fft_battleground.dump.scheduled.tournament.UpdateLastActiveDataTournamentTask;
import fft_battleground.dump.scheduled.tournament.UpdateMusicDataTournamentTask;
import fft_battleground.dump.scheduled.tournament.UpdateMusicOccurenceHistoryTournamentTask;
import fft_battleground.dump.scheduled.tournament.UpdatePrestigeSkillsTournamentTask;
import fft_battleground.dump.scheduled.tournament.UpdateSkillBonusCacheTournamentTask;
import fft_battleground.dump.scheduled.tournament.UpdateUserSkillsTournamentTask;
import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.event.model.DatabaseResultsData;
import fft_battleground.image.ImageCacheService;
import fft_battleground.image.ImageDumpDataProvider;
import fft_battleground.mustadio.MustadioService;
import fft_battleground.repo.model.BatchDataEntry;
import fft_battleground.repo.repository.BatchDataEntryRepo;
import fft_battleground.repo.repository.MusicListenCountHistoryRepo;
import fft_battleground.repo.repository.MusicListenCountRepo;
import fft_battleground.skill.SkillUtils;
import fft_battleground.util.Router;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DumpScheduledTasksManagerImpl implements DumpScheduledTasksManager {
	
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
	
	@Autowired
	@Getter private MusicListenCountRepo musicListenCountRepo;
	
	@Autowired
	@Getter private MusicListenCountHistoryRepo musicListenCountHistoryRepo;
	
	@Autowired
	@Getter private ImageDumpDataProvider imageDumpDataProvider;
	
	@Autowired
	@Getter private ImageCacheService imageCacheService;
	
	@Value("${server.ssl.key-store-password}")
	@Getter private String keyStorePass;
	
	private ExecutorService batchTimer = Executors.newFixedThreadPool(1);
	
	@Scheduled(cron = "0 0 1 * * ?")
	public void runAllUpdates() {
		ScheduledTask[] dumpScheduledTasks = new ScheduledTask[] {
				new BadAccountsDailyTask(this, this.dumpService),
				new CheckCertificateDailyTask(this, this.dumpService),
				new AllegianceDailyTask(this, this.dumpService), 
				new BotListDailyTask(this, this.dumpService), 
				new PortraitsDailyTask(this, this.dumpService),
				new UserSkillsDailyTask(this, this.dumpService),
				new PrestigeSkillDailyTask(this, this.dumpService),
				new ClassBonusDailyTask(this, this.dumpService),
				new SkillBonusDailyTask(this, this.dumpService),
				new RefreshMustadioDailyTask(this, this.dumpService),
				new MissingPortraitCheckDailyTask(this, this.dumpService)
			};
		for(ScheduledTask task : dumpScheduledTasks) {
			this.batchTimer.submit(task);
		}
		
	}
	
	public List<DumpTournamentScheduledTask> tournamentTasks() {
		List<DumpTournamentScheduledTask> tasks = List.of(
				new UpdateBalanceDataTournamentTask(this),
				new UpdateExperienceDataTournamentTask(this),
				new UpdateLastActiveDataTournamentTask(this),
				new UpdateClassBonusCacheTournamentTask(this),
				new UpdateSkillBonusCacheTournamentTask(this),
				new UpdateUserSkillsTournamentTask(this),
				new UpdatePrestigeSkillsTournamentTask(this),
				new UpdateMusicDataTournamentTask(this),
				new UpdateMusicOccurenceHistoryTournamentTask(this),
				new UpdateDetectorAuditTableTournamentTask(this)
				);
		return tasks;
	}
	
	public DumpMatchScheduledTask globalGilUpdateTask() {
		return new UpdateGlobalGilCount(this.eventRouter, this);
	}
	
	@Transactional
	public void writeToBatchDataEntryRepo(BatchDataEntry batchDataEntry) {
		this.batchDataEntryRepo.saveAndFlush(batchDataEntry);
	}

}
