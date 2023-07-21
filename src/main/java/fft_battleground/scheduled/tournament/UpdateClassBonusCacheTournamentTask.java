package fft_battleground.scheduled.tournament;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import fft_battleground.dump.DumpScheduledTasksManagerImpl;
import fft_battleground.event.detector.model.fake.ClassBonusEvent;
import fft_battleground.exception.DumpException;
import fft_battleground.repo.model.ClassBonus;
import fft_battleground.scheduled.DumpTournamentScheduledTask;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UpdateClassBonusCacheTournamentTask extends DumpTournamentScheduledTask {

	public UpdateClassBonusCacheTournamentTask(DumpScheduledTasksManagerImpl dumpScheduledTasks) {
		super(dumpScheduledTasks);
	}

	@Override
	protected void task() {
		Set<String> playersWithUpdatedClassBonus = this.dumpServiceRef.getDumpDataProvider().getRecentPlayersForClassBonusDump()
				.parallelStream().map(playerName -> StringUtils.lowerCase(playerName)).collect(Collectors.toSet()); //lowercase output
		AtomicInteger count = new AtomicInteger(0);
		playersWithUpdatedClassBonus.parallelStream().forEach(player -> {
			try {
				Set<String> currentClassBonuses = this.dumpServiceRef.getDumpDataProvider().getClassBonus(player);
				currentClassBonuses = ClassBonus.convertToBotOutput(currentClassBonuses);
				this.dumpServiceRef.getClassBonusCache().put(player, currentClassBonuses);
				ClassBonusEvent classBonus = new ClassBonusEvent(player, currentClassBonuses);
				this.routerRef.sendDataToQueues(classBonus);
				count.getAndIncrement();
			} catch(DumpException e) {
				log.error("error getting class bonus data from dump", e);
				this.dumpServiceRef.getErrorWebhookManager().sendException(e, "error getting class bonus data from dump");
			}
		});
		this.logPlayers("class bonuses", count.get(), playersWithUpdatedClassBonus);
	}

}
