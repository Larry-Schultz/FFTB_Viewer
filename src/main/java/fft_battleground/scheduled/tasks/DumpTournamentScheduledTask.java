package fft_battleground.scheduled.tasks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DumpTournamentScheduledTask extends ScheduledTask {
	protected static final Logger log = LoggerFactory.getLogger("dataUpdate");
	
	public DumpTournamentScheduledTask() {}

	protected abstract void task();
	
	protected void logPlayers(String logType, int count, Collection<String> players) {
		List<String> playerNamesList = new ArrayList<>(players);
		Collections.sort(playerNamesList);
		String playerNames = StringUtils.join(playerNamesList.toArray(new String[] {}),", ");
		log.info("updated {} for {} players.  The players: {}", logType, count, playerNames) ;
	}

}
