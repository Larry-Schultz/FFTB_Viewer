package fft_battleground.scheduled;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import fft_battleground.repo.model.BatchDataEntry;
import fft_battleground.repo.repository.BatchDataEntryRepo;
import fft_battleground.scheduled.tasks.DumpMatchScheduledTask;
import fft_battleground.scheduled.tasks.DumpTournamentScheduledTask;
import fft_battleground.scheduled.tasks.ScheduledTask;
import fft_battleground.scheduled.tasks.factory.DumpDailyScheduledTaskFactory;
import fft_battleground.scheduled.tasks.factory.DumpTournamentScheduledTaskFactory;
import fft_battleground.scheduled.tasks.match.UpdateGlobalGilCount;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DumpScheduledTasksManagerImpl implements DumpScheduledTasksManager {
	
	@Autowired
	private DumpDailyScheduledTaskFactory dumpDailyScheduledTaskFactory;
	
	@Autowired
	private DumpTournamentScheduledTaskFactory dumpTournamentScheduledTaskFactory;
	
	@Autowired
	private UpdateGlobalGilCount updateGlobalGilCount;
	
	@Autowired
	private BatchDataEntryRepo batchDataEntryRepo;
	
	private ExecutorService batchTimer = Executors.newFixedThreadPool(1);
	
	@Scheduled(cron = "0 0 1 * * ?")
	public void runAllUpdates() {
		ScheduledTask[] dumpScheduledTasks = this.dumpDailyScheduledTaskFactory.dailyTasks();
		for(ScheduledTask task : dumpScheduledTasks) {
			this.batchTimer.submit(task);
		}
		
	}
	
	public List<DumpTournamentScheduledTask> tournamentTasks() {
		return dumpTournamentScheduledTaskFactory.tournamentTasks();
	}
	
	public DumpMatchScheduledTask globalGilUpdateTask() {
		return this.updateGlobalGilCount;
	}
	
	@Transactional
	public void writeToBatchDataEntryRepo(BatchDataEntry batchDataEntry) {
		this.batchDataEntryRepo.saveAndFlush(batchDataEntry);
	}

}
