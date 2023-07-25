package fft_battleground.scheduled.tasks.tournament;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fft_battleground.discord.WebhookManager;
import fft_battleground.dump.DumpDataProvider;
import fft_battleground.dump.DumpService;
import fft_battleground.event.detector.model.fake.ClassBonusEvent;
import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.exception.DumpException;
import fft_battleground.repo.model.ClassBonus;
import fft_battleground.scheduled.tasks.DumpTournamentScheduledTask;
import fft_battleground.util.Router;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class UpdateClassBonusCacheTournamentTask extends DumpTournamentScheduledTask {

	@Autowired
	private Router<BattleGroundEvent> eventRouter;
	
	@Autowired
	private DumpService dumpService;
	
	@Autowired
	private DumpDataProvider dumpDataProvider;
	
	@Autowired
	private WebhookManager errorWebhookManager;
	
	public UpdateClassBonusCacheTournamentTask() {}

	@Override
	protected void task() {
		Set<String> playersWithUpdatedClassBonus = this.dumpDataProvider.getRecentPlayersForClassBonusDump()
				.parallelStream().map(playerName -> StringUtils.lowerCase(playerName)).collect(Collectors.toSet()); //lowercase output
		AtomicInteger count = new AtomicInteger(0);
		playersWithUpdatedClassBonus.parallelStream().forEach(player -> {
			try {
				Set<String> currentClassBonuses = this.dumpService.getDumpDataProvider().getClassBonus(player);
				currentClassBonuses = ClassBonus.convertToBotOutput(currentClassBonuses);
				this.dumpService.getClassBonusCache().put(player, currentClassBonuses);
				ClassBonusEvent classBonus = new ClassBonusEvent(player, currentClassBonuses);
				this.eventRouter.sendDataToQueues(classBonus);
				count.getAndIncrement();
			} catch(DumpException e) {
				log.error("error getting class bonus data from dump", e);
				this.errorWebhookManager.sendException(e, "error getting class bonus data from dump");
			}
		});
		this.logPlayers("class bonuses", count.get(), playersWithUpdatedClassBonus);
	}

}
