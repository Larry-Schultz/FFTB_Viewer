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
import fft_battleground.dump.cache.map.UserSkillsCache;
import fft_battleground.event.detector.model.PlayerSkillEvent;
import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.repo.model.PlayerSkills;
import fft_battleground.scheduled.tasks.DumpTournamentScheduledTask;
import fft_battleground.skill.SkillUtils;
import fft_battleground.skill.model.Skill;
import fft_battleground.util.Router;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class UpdateUserSkillsTournamentTask extends DumpTournamentScheduledTask {

	@Autowired
	private Router<BattleGroundEvent> eventRouter;

	@Autowired
	private DumpDataProvider dumpDataProvider;
	
	@Autowired
	private WebhookManager errorWebhookManager;
	
	@Autowired
	private SkillUtils monsterUtils;
	
	@Autowired
	private UserSkillsCache userSkillsCache;
	
	public UpdateUserSkillsTournamentTask() {}

	@Override
	protected void task() {
		log.info("Starting skills tournament update task ");
		Set<String> playersWithUpdatedUserSkills = this.dumpDataProvider.getRecentPlayersForUserSkillsDump()
				.parallelStream().map(playerName -> StringUtils.lowerCase(playerName)).collect(Collectors.toSet()); //lowercase output;
		AtomicInteger count = new AtomicInteger(0);
		playersWithUpdatedUserSkills.parallelStream().forEach(player -> {
			try {
				List<PlayerSkills> currentUserSkills = this.dumpDataProvider.getSkillsForPlayer(player);
				this.monsterUtils.categorizeSkillsList(currentUserSkills);
				this.monsterUtils.regulateMonsterSkillCooldowns(currentUserSkills);
				List<String> skills = Skill.convertToListOfSkillStrings(currentUserSkills);
				this.userSkillsCache.put(player, skills);
				PlayerSkillEvent playerSkillEvent = new PlayerSkillEvent(currentUserSkills, player);
				this.eventRouter.sendDataToQueues(playerSkillEvent);
				count.getAndIncrement();
			} catch (Exception e) {
				log.error("error getting user skill data from dump", e);
				this.errorWebhookManager.sendException(e, "error getting user skill data from dump");
			}
		});
		this.logPlayers("user skills", count.get(), playersWithUpdatedUserSkills);
	}

}
