package fft_battleground.scheduled.tasks.factory;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fft_battleground.scheduled.tasks.DumpTournamentScheduledTask;
import fft_battleground.scheduled.tasks.tournament.UpdateBalanceDataTournamentTask;
import fft_battleground.scheduled.tasks.tournament.UpdateClassBonusCacheTournamentTask;
import fft_battleground.scheduled.tasks.tournament.UpdateDetectorAuditTableTournamentTask;
import fft_battleground.scheduled.tasks.tournament.UpdateExperienceDataTournamentTask;
import fft_battleground.scheduled.tasks.tournament.UpdateLastActiveDataTournamentTask;
import fft_battleground.scheduled.tasks.tournament.UpdateMusicDataTournamentTask;
import fft_battleground.scheduled.tasks.tournament.UpdateMusicOccurenceHistoryTournamentTask;
import fft_battleground.scheduled.tasks.tournament.UpdatePrestigeSkillsTournamentTask;
import fft_battleground.scheduled.tasks.tournament.UpdateSkillBonusCacheTournamentTask;
import fft_battleground.scheduled.tasks.tournament.UpdateUserSkillsTournamentTask;

@Component
public class DumpTournamentScheduledTaskFactory {

	@Autowired
	private UpdateBalanceDataTournamentTask updateBalanceDataTournamentTask;
	
	@Autowired
	private UpdateExperienceDataTournamentTask updateExperienceDataTournamentTask;
	
	@Autowired
	private UpdateLastActiveDataTournamentTask updateLastActiveDataTournamentTask;
	
	@Autowired
	private UpdateClassBonusCacheTournamentTask updateClassBonusCacheTournamentTask;
	
	@Autowired
	private UpdateSkillBonusCacheTournamentTask updateSkillBonusCacheTournamentTask;
	
	@Autowired
	private UpdateUserSkillsTournamentTask updateUserSkillsTournamentTask;
	
	@Autowired
	private UpdatePrestigeSkillsTournamentTask updatePrestigeSkillsTournamentTask;
	
	@Autowired
	private UpdateMusicDataTournamentTask updateMusicDataTournamentTask;
	
	@Autowired
	private UpdateMusicOccurenceHistoryTournamentTask updateMusicOccurenceHistoryTournamentTask;
	
	@Autowired
	private UpdateDetectorAuditTableTournamentTask updateDetectorAuditTableTournamentTask;
	
	public List<DumpTournamentScheduledTask> tournamentTasks() {
		return List.of(
				this.updateBalanceDataTournamentTask,
				this.updateExperienceDataTournamentTask,
				this.updateLastActiveDataTournamentTask,
				this.updateClassBonusCacheTournamentTask,
				this.updateSkillBonusCacheTournamentTask,
				this.updateUserSkillsTournamentTask,
				this.updatePrestigeSkillsTournamentTask,
				this.updateMusicDataTournamentTask,
				this.updateMusicOccurenceHistoryTournamentTask,
				this.updateDetectorAuditTableTournamentTask
				);
	}
}
