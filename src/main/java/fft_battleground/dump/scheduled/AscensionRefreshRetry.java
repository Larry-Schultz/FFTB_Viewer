package fft_battleground.dump.scheduled;

import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import fft_battleground.dump.DumpDataProvider;
import fft_battleground.dump.DumpService;
import fft_battleground.event.PlayerSkillRefresh;
import fft_battleground.event.model.PlayerSkillEvent;
import fft_battleground.event.model.PrestigeSkillsEvent;
import fft_battleground.exception.AscensionException;
import fft_battleground.util.BattlegroundRetryState;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AscensionRefreshRetry {

	@Autowired
	private DumpService dumpService;
	
	@Autowired
	private DumpDataProvider dumpDataProvider;
	
	@Retryable( value = AscensionException.class, maxAttempts = 3, backoff = @Backoff(delay = 2000, multiplier=2))
	public PlayerSkillRefresh forcePlayerSkillRefreshForAscension(String player, int prestigeSkillsBeforeCount, final BattlegroundRetryState state) throws AscensionException {
		state.incrementCount();
		
		PlayerSkillRefresh refresh = new PlayerSkillRefresh(player);
		//delete all skills from cache
		this.dumpService.getUserSkillsCache().remove(player);
		
		List<String> userSkills = Collections.emptyList();
		
		this.dumpService.getUserSkillsCache().put(player, userSkills);
		PlayerSkillEvent userSkillsEvent = new PlayerSkillEvent(player, userSkills);
		refresh.setPlayerSkillEvent(userSkillsEvent);
	
		List<String> prestigeSkills = null;
		try {
			//attempt to get prestige skills, this is allowed to fail meaning this player has no prestige
			prestigeSkills = this.dumpDataProvider.getPrestigeSkillsForPlayer(player);
		} catch(Exception e) {
			log.warn("Player {} does not have prestige", player);
		}
		
		if(prestigeSkills != null && prestigeSkills.size() > 0) {
			//store prestige skills
			this.dumpService.getPrestigeSkillsCache().remove(player);
			this.dumpService.getPrestigeSkillsCache().put(player, prestigeSkills);
			PrestigeSkillsEvent prestigeEvent = new PrestigeSkillsEvent(player, prestigeSkills);
			refresh.setPrestigeSkillEvent(prestigeEvent);
		}
		
		try {
			Assert.assertNotNull(prestigeSkills);
			Assert.assertNotEquals(prestigeSkills.size(), prestigeSkillsBeforeCount);
		} catch(AssertionError e) {
			final String messageFormat = "Problem with Ascension: Skill not updated.  Prestige before %1$o, Prestige after %2$o.  Retry #%3$o";
			String message = String.format(messageFormat, prestigeSkillsBeforeCount, prestigeSkills.size(), state.getRetryCount());
			throw new AscensionException(e, message);
		}
		
		return refresh;
	}
}
