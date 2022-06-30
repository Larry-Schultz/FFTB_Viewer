package fft_battleground.dump.scheduled.tournament;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import fft_battleground.dump.DumpScheduledTasksManagerImpl;
import fft_battleground.dump.scheduled.DumpTournamentScheduledTask;
import fft_battleground.event.detector.model.PlayerSkillEvent;
import fft_battleground.repo.model.PlayerSkills;
import fft_battleground.skill.model.Skill;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UpdateUserSkillsTournamentTask extends DumpTournamentScheduledTask {

	public UpdateUserSkillsTournamentTask(DumpScheduledTasksManagerImpl dumpScheduledTasks) {
		super(dumpScheduledTasks);
	}

	@Override
	protected void task() {
		Set<String> playersWithUpdatedUserSkills = this.dumpServiceRef.getDumpDataProvider().getRecentPlayersForUserSkillsDump()
				.parallelStream().map(playerName -> StringUtils.lowerCase(playerName)).collect(Collectors.toSet()); //lowercase output;
		AtomicInteger count = new AtomicInteger(0);
		playersWithUpdatedUserSkills.parallelStream().forEach(player -> {
			try {
				List<PlayerSkills> currentUserSkills = this.dumpServiceRef.getDumpDataProvider().getSkillsForPlayer(player);
				this.dumpServiceRef.getMonsterUtils().categorizeSkillsList(currentUserSkills);
				this.dumpServiceRef.getMonsterUtils().regulateMonsterSkillCooldowns(currentUserSkills);
				List<String> skills = Skill.convertToListOfSkillStrings(currentUserSkills);
				this.dumpServiceRef.getUserSkillsCache().put(player, skills);
				PlayerSkillEvent playerSkillEvent = new PlayerSkillEvent(currentUserSkills, player);
				this.routerRef.sendDataToQueues(playerSkillEvent);
				count.getAndIncrement();
			} catch (Exception e) {
				log.error("error getting user skill data from dump", e);
				this.dumpServiceRef.getErrorWebhookManager().sendException(e, "error getting user skill data from dump");
			}
		});
		this.logPlayers("user skills", count.get(), playersWithUpdatedUserSkills);
	}

}
