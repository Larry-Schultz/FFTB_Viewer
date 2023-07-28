package fft_battleground.scheduled.tasks.tournament;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fft_battleground.discord.WebhookManager;
import fft_battleground.dump.DumpDataProvider;
import fft_battleground.dump.cache.map.SkillBonusCache;
import fft_battleground.event.detector.model.fake.SkillBonusEvent;
import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.scheduled.tasks.DumpTournamentScheduledTask;
import fft_battleground.util.Router;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class UpdateSkillBonusCacheTournamentTask extends DumpTournamentScheduledTask {

	@Autowired
	private Router<BattleGroundEvent> eventRouter;

	@Autowired
	private DumpDataProvider dumpDataProvider;
	
	@Autowired
	private WebhookManager errorWebhookManager;
	
	@Autowired
	private SkillBonusCache skillBonusCache;
	
	public UpdateSkillBonusCacheTournamentTask() {}

	@Override
	protected void task() {
		Set<String> playersWithUpdatedSkillBonus = this.dumpDataProvider.getRecentPlayersForSkillBonusDump()
				.parallelStream().map(playerName -> StringUtils.lowerCase(playerName)).collect(Collectors.toSet()); //lowercase output;
		AtomicInteger count = new AtomicInteger(0);
		playersWithUpdatedSkillBonus.parallelStream().forEach(player -> {
			try {
				Set<String> currentSkillBonuses = this.dumpDataProvider.getSkillBonus(player);
				this.skillBonusCache.put(player, currentSkillBonuses);
				SkillBonusEvent skillBonus = new SkillBonusEvent(player, currentSkillBonuses);
				this.eventRouter.sendDataToQueues(skillBonus);
				count.getAndIncrement();
			} catch(Exception e) {
				log.error("error getting skill bonus data from dump", e);
				this.errorWebhookManager.sendException(e, "error getting skill bonus data from dump");
			}
		});
		this.logPlayers("skill bonuses", count.get(), playersWithUpdatedSkillBonus);
	}

}
