package fft_battleground.dump;

import java.util.List;

import fft_battleground.dump.scheduled.DumpMatchScheduledTask;
import fft_battleground.dump.scheduled.DumpTournamentScheduledTask;
import fft_battleground.repo.model.BatchDataEntry;

public interface DumpScheduledTasksManager {
	void runAllUpdates();
	List<DumpTournamentScheduledTask> getTournamentTasks();
	DumpMatchScheduledTask getGlobalGilUpdateTask();
	void writeToBatchDataEntryRepo(BatchDataEntry batchDataEntry);
}
