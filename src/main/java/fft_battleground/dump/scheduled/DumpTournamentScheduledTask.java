package fft_battleground.dump.scheduled;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fft_battleground.dump.DumpScheduledTasksManagerImpl;
import fft_battleground.dump.DumpService;
import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.util.Router;

public abstract class DumpTournamentScheduledTask extends ScheduledTask {
	protected static final Logger log = LoggerFactory.getLogger("dataUpdate");
	
	protected Router<BattleGroundEvent> routerRef;
	protected DumpService dumpServiceRef;
	
	public DumpTournamentScheduledTask(DumpScheduledTasksManagerImpl dumpScheduledTasks) {
		super(dumpScheduledTasks, dumpScheduledTasks.getDumpService());
		
		this.routerRef = dumpScheduledTasks.getEventRouter();
		this.dumpServiceRef = dumpScheduledTasks.getDumpService();
	}

	protected abstract void task();
	
	protected void logPlayers(String logType, int count, Collection<String> players) {
		List<String> playerNamesList = new ArrayList<>(players);
		Collections.sort(playerNamesList);
		String playerNames = StringUtils.join(playerNamesList.toArray(new String[] {}),", ");
		log.info("updated {} for {} players.  The players: {}", logType, count, playerNames) ;
	}

}
