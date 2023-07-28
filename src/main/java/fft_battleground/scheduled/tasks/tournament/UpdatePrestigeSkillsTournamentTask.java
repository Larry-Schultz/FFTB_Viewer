package fft_battleground.scheduled.tasks.tournament;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fft_battleground.discord.WebhookManager;
import fft_battleground.dump.DumpDataProvider;
import fft_battleground.dump.cache.map.PrestigeSkillsCache;
import fft_battleground.event.detector.model.PrestigeSkillsEvent;
import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.repo.model.PrestigeSkills;
import fft_battleground.scheduled.tasks.DumpTournamentScheduledTask;
import fft_battleground.skill.model.Skill;
import fft_battleground.util.Router;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class UpdatePrestigeSkillsTournamentTask extends DumpTournamentScheduledTask {

	@Autowired
	private Router<BattleGroundEvent> eventRouter;
	
	@Autowired
	private DumpDataProvider dumpDataProvider;
	
	@Autowired
	private WebhookManager errorWebhookManager;
	
	@Autowired
	private PrestigeSkillsCache prestigeSkillsCache;
	
	public UpdatePrestigeSkillsTournamentTask() {}

	@Override
	protected void task() {
		Set<String> playersWithUpdatedPrestigeSkills = this.dumpDataProvider.getRecentPlayersForPrestigeSkillsDump()
				.parallelStream().map(playerName -> StringUtils.lowerCase(playerName)).collect(Collectors.toSet()); //lowercase output;
		AtomicInteger count = new AtomicInteger(0);
		playersWithUpdatedPrestigeSkills.parallelStream().forEach(player -> {
			try {
				List<PrestigeSkills> currentUserPrestigeSkills = this.dumpDataProvider.getSkillsForPlayer(player)
																	.stream().map(PrestigeSkills::new).collect(Collectors.toList());
				List<String> skills = Skill.convertToListOfSkillStrings(currentUserPrestigeSkills);
				this.prestigeSkillsCache.put(player, skills);
				PrestigeSkillsEvent prestigeSkillsEvent = new PrestigeSkillsEvent(currentUserPrestigeSkills, player);
				this.eventRouter.sendDataToQueues(prestigeSkillsEvent);
				count.getAndIncrement();
			} catch (Exception e) {
				log.error("error getting prestige skill data from dump", e);
				this.errorWebhookManager.sendException(e, "error getting prestige skill data from dump");
			}
		});
		this.logPlayers("prestige skills", count.get(), playersWithUpdatedPrestigeSkills);
	}

}
