package fft_battleground.dump;

import java.util.List;

import fft_battleground.repo.model.BatchDataEntry;
import fft_battleground.scheduled.DumpMatchScheduledTask;
import fft_battleground.scheduled.DumpTournamentScheduledTask;

public interface DumpScheduledTasksManager {
	void runAllUpdates();
	List<DumpTournamentScheduledTask> tournamentTasks();
	DumpMatchScheduledTask globalGilUpdateTask();
	void writeToBatchDataEntryRepo(BatchDataEntry batchDataEntry);
}
