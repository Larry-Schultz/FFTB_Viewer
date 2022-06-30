package fft_battleground.dump.scheduled.tournament;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import fft_battleground.dump.DumpScheduledTasksManagerImpl;
import fft_battleground.dump.scheduled.DumpTournamentScheduledTask;
import fft_battleground.event.detector.model.PrestigeSkillsEvent;
import fft_battleground.repo.model.PrestigeSkills;
import fft_battleground.skill.model.Skill;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UpdatePrestigeSkillsTournamentTask extends DumpTournamentScheduledTask {

	public UpdatePrestigeSkillsTournamentTask(DumpScheduledTasksManagerImpl dumpScheduledTasks) {
		super(dumpScheduledTasks);
	}

	@Override
	protected void task() {
		Set<String> playersWithUpdatedPrestigeSkills = this.dumpServiceRef.getDumpDataProvider().getRecentPlayersForPrestigeSkillsDump()
				.parallelStream().map(playerName -> StringUtils.lowerCase(playerName)).collect(Collectors.toSet()); //lowercase output;
		AtomicInteger count = new AtomicInteger(0);
		playersWithUpdatedPrestigeSkills.parallelStream().forEach(player -> {
			try {
				List<PrestigeSkills> currentUserPrestigeSkills = this.dumpServiceRef.getDumpDataProvider().getSkillsForPlayer(player)
																	.stream().map(PrestigeSkills::new).collect(Collectors.toList());
				List<String> skills = Skill.convertToListOfSkillStrings(currentUserPrestigeSkills);
				this.dumpServiceRef.getPrestigeSkillsCache().put(player, skills);
				PrestigeSkillsEvent prestigeSkillsEvent = new PrestigeSkillsEvent(currentUserPrestigeSkills, player);
				this.routerRef.sendDataToQueues(prestigeSkillsEvent);
				count.getAndIncrement();
			} catch (Exception e) {
				log.error("error getting prestige skill data from dump", e);
				this.dumpServiceRef.getErrorWebhookManager().sendException(e, "error getting prestige skill data from dump");
			}
		});
		this.logPlayers("prestige skills", count.get(), playersWithUpdatedPrestigeSkills);
	}

}
