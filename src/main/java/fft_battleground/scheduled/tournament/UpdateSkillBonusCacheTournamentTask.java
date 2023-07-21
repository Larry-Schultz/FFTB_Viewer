package fft_battleground.scheduled.tournament;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import fft_battleground.dump.DumpScheduledTasksManagerImpl;
import fft_battleground.event.detector.model.fake.SkillBonusEvent;
import fft_battleground.scheduled.DumpTournamentScheduledTask;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UpdateSkillBonusCacheTournamentTask extends DumpTournamentScheduledTask {

	public UpdateSkillBonusCacheTournamentTask(DumpScheduledTasksManagerImpl dumpScheduledTasks) {
		super(dumpScheduledTasks);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void task() {
		Set<String> playersWithUpdatedSkillBonus = this.dumpServiceRef.getDumpDataProvider().getRecentPlayersForSkillBonusDump()
				.parallelStream().map(playerName -> StringUtils.lowerCase(playerName)).collect(Collectors.toSet()); //lowercase output;
		AtomicInteger count = new AtomicInteger(0);
		playersWithUpdatedSkillBonus.parallelStream().forEach(player -> {
			try {
				Set<String> currentSkillBonuses = this.dumpServiceRef.getDumpDataProvider().getSkillBonus(player);
				this.dumpServiceRef.getSkillBonusCache().put(player, currentSkillBonuses);
				SkillBonusEvent skillBonus = new SkillBonusEvent(player, currentSkillBonuses);
				this.routerRef.sendDataToQueues(skillBonus);
				count.getAndIncrement();
			} catch(Exception e) {
				log.error("error getting skill bonus data from dump", e);
				this.dumpServiceRef.getErrorWebhookManager().sendException(e, "error getting skill bonus data from dump");
			}
		});
		this.logPlayers("skill bonuses", count.get(), playersWithUpdatedSkillBonus);
	}

}
