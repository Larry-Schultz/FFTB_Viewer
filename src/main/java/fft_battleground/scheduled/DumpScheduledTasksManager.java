package fft_battleground.scheduled;

import java.util.List;

import fft_battleground.repo.model.BatchDataEntry;
import fft_battleground.scheduled.tasks.DumpMatchScheduledTask;
import fft_battleground.scheduled.tasks.DumpTournamentScheduledTask;

public interface DumpScheduledTasksManager {
	void runAllUpdates();
	List<DumpTournamentScheduledTask> tournamentTasks();
	DumpMatchScheduledTask globalGilUpdateTask();
	void writeToBatchDataEntryRepo(BatchDataEntry batchDataEntry);
}
