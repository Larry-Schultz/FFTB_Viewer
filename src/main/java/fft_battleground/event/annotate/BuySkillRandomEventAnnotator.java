package fft_battleground.event.annotate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fft_battleground.event.model.BuySkillRandomEvent;
import fft_battleground.event.model.PlayerSkillEvent;
import fft_battleground.exception.TournamentApiException;
import fft_battleground.tournament.MonsterUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class BuySkillRandomEventAnnotator implements BattleGroundEventAnnotator<BuySkillRandomEvent> {

	@Autowired
	private MonsterUtils monsterUtils;
	
	@Override
	public void annotateEvent(BuySkillRandomEvent event) {
		PlayerSkillEvent playerSkillEvent = event.getSkillEvent();
		try {
			this.monsterUtils.categorizeSkillsList(playerSkillEvent.getPlayerSkills());
		} catch (TournamentApiException e) {
			log.error("Error categorizing skills for buy skill event for player {}", playerSkillEvent.getPlayer(), e);
		}
	}

}
