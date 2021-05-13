package fft_battleground.dump.scheduled;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import fft_battleground.dump.DumpDataProvider;
import fft_battleground.dump.DumpService;
import fft_battleground.event.PlayerSkillRefresh;
import fft_battleground.event.model.PlayerSkillEvent;
import fft_battleground.event.model.PrestigeSkillsEvent;
import fft_battleground.exception.AscensionException;
import fft_battleground.exception.DumpException;
import fft_battleground.repo.model.PlayerSkills;
import fft_battleground.util.BattlegroundRetryState;

@Component
public class AscensionRefreshRetry {
	private static final Logger log = LoggerFactory.getLogger("AscensionPrestigeLogger");

	@Autowired
	private DumpService dumpService;
	
	@Autowired
	private DumpDataProvider dumpDataProvider;
	
	@Retryable( value = AscensionException.class, maxAttempts = 5, backoff = @Backoff(delay = 20*1000, multiplier=2))
	public PlayerSkillRefresh forcePlayerSkillRefreshForAscension(String player, int prestigeSkillsBeforeCount, final BattlegroundRetryState state) throws AscensionException {
		state.incrementCount();
		log.info("Attempt {} to update prestige skills for player {}", state.getRetryCount(), player);
		log.info("Player {} has {} prestige skills currently", player, prestigeSkillsBeforeCount);
		
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
			
			if(prestigeSkills != null && prestigeSkills.size() > 0) {
				//store prestige skills
				this.dumpService.getPrestigeSkillsCache().remove(player);
				this.dumpService.getPrestigeSkillsCache().put(player, prestigeSkills);
				PrestigeSkillsEvent prestigeEvent = new PrestigeSkillsEvent(player, prestigeSkills);
				refresh.setPrestigeSkillEvent(prestigeEvent);
				log.info("According to the Dump {} has the prestige skills: {}", player, prestigeSkills.toString());
			}
		
			if(prestigeSkills == null || prestigeSkills.size() == 0) {
				throw new NullPointerException("prestige skills is null");
			}
			Assert.assertNotEquals(prestigeSkills.size(), prestigeSkillsBeforeCount);
		} catch(AssertionError e) {
			final String messageFormat = "Problem with Ascension: Skill not updated.  Prestige before %1$o, Prestige after %2$o.  Retry #%3$o";
			String message = String.format(messageFormat, prestigeSkillsBeforeCount, prestigeSkills.size(), state.getRetryCount());
			log.error(message, e);
			throw new AscensionException(e, message);
		} catch(DumpException|NullPointerException e) {
			final String messageFormat = "Problem with Ascension: Prestige Skill data not retrieved.  Prestige before %1$o, Prestige after %2$o.  Retry #%3$o";
			String message = String.format(messageFormat, prestigeSkillsBeforeCount, 0, state.getRetryCount());
			log.error(message, e);
			throw new AscensionException(e, message);		
		}
		
		return refresh;
	}
	
	/**
	 * We tried to update the prestige data.  If we are getting anything from the Dump let's blindly trust what's there and do some kind of prestige update.
	 * 
	 * @param e
	 * @param player
	 * @param prestigeSkillsBeforeCount
	 * @param state
	 * @return
	 */
	@Recover
	public PlayerSkillRefresh recoverFromFailedSkillRefreshFromAscension(AscensionException e, String player, int prestigeSkillsBeforeCount, final BattlegroundRetryState state) {
		log.warn("Forcing an update with what we have for player {} after {} retries", player, state.getRetryCount());
		PlayerSkillRefresh refresh = new PlayerSkillRefresh(player);
		//delete all skills from cache
		this.dumpService.getUserSkillsCache().remove(player);
		
		List<String> userSkills = Collections.emptyList();
		
		this.dumpService.getUserSkillsCache().put(player, userSkills);
		PlayerSkillEvent userSkillsEvent = new PlayerSkillEvent(player, userSkills);
		refresh.setPlayerSkillEvent(userSkillsEvent);
	
		List<String> prestigeSkills = null;
		//attempt to get prestige skills, this is allowed to fail meaning this player has no prestige
		try {
			prestigeSkills = this.dumpDataProvider.getPrestigeSkillsForPlayer(player);
		} catch (DumpException e1) {
			log.warn("no prestige data found for player {} in Recover function", player);
			prestigeSkills = null;
		}
		
		if(prestigeSkills != null && prestigeSkills.size() > 0) {
			//store prestige skills
			log.info("Maybe we were wrong about the number of prestige levels {} had", player);
			this.dumpService.getPrestigeSkillsCache().remove(player);
			this.dumpService.getPrestigeSkillsCache().put(player, prestigeSkills);
			PrestigeSkillsEvent prestigeEvent = new PrestigeSkillsEvent(player, prestigeSkills);
			refresh.setPrestigeSkillEvent(prestigeEvent);
			log.info("According to the Dump {} has the prestige skills: {}", player, prestigeSkills.toString());
		} else {
			refresh = null;
		}
		
		return refresh;
	}
}
