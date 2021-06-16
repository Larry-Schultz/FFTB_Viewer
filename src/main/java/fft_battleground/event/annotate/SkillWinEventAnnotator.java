package fft_battleground.event.annotate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fft_battleground.event.model.PlayerSkillEvent;
import fft_battleground.event.model.RiserSkillWinEvent;
import fft_battleground.event.model.SkillWinEvent;
import fft_battleground.exception.TournamentApiException;
import fft_battleground.tournament.MonsterUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SkillWinEventAnnotator implements BattleGroundEventAnnotator<SkillWinEvent> {

	@Autowired
	private MonsterUtils monsterUtils;
	
	@Override
	public void annotateEvent(SkillWinEvent event) {
		for(PlayerSkillEvent playerSkillEvent: event.getSkillEvents()) {
			try {
				this.monsterUtils.categorizeSkillsList(playerSkillEvent.getPlayerSkills());
			} catch (TournamentApiException e) {
				if(event instanceof RiserSkillWinEvent) {
					log.error("Error categorizing riser skill win event for player {}", playerSkillEvent.getPlayer(), e);
				} else {
					log.error("Error categorizing skill win event for player {}", playerSkillEvent.getPlayer(), e);
				}
			}
		}
		
	}

}