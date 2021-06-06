package fft_battleground.event.annotate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fft_battleground.event.GiftSkill;
import fft_battleground.event.model.GiftSkillEvent;
import fft_battleground.event.model.PlayerSkillEvent;
import fft_battleground.exception.TournamentApiException;
import fft_battleground.tournament.MonsterUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class GiftSkillEventAnnotator implements BattleGroundEventAnnotator<GiftSkillEvent> {

	@Autowired
	private MonsterUtils monsterUtils;
	
	@Override
	public void annotateEvent(GiftSkillEvent event) {
		for(GiftSkill giftSkill: event.getGiftSkills()) {
			PlayerSkillEvent playerSkillEvent = giftSkill.getPlayerSkillEvent();
			try {
				this.monsterUtils.categorizeSkillsList(playerSkillEvent.getPlayerSkills());
			} catch (TournamentApiException e) {
				log.error("Error categorizing skills for gift skill event with giving player {} and receiving player {}", giftSkill.getGivingPlayer(), playerSkillEvent.getPlayer(), e);
			}
		}
	}

}
