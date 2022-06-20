package fft_battleground.event.annotate;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fft_battleground.event.detector.model.SkillDropEvent;
import fft_battleground.exception.TournamentApiException;
import fft_battleground.tournament.TournamentService;
import fft_battleground.tournament.tips.Tips;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SkillDropEventAnnotator implements BattleGroundEventAnnotator<SkillDropEvent> {
	
	@Autowired
	private TournamentService tournamentService;
	
	@Override
	public void annotateEvent(SkillDropEvent event) {
		if(event != null) {
			try {
				Tips tipFromTournamentService = this.tournamentService.getCurrentTips();
				String description = tipFromTournamentService.getUserSkill().get(event.getSkill());
				description = StringUtils.replace(description, "\"", "");
				event.setSkillDescription(description);
				return;
			} catch (TournamentApiException e) {
				log.error("Could not annotate skillDropEvent {} due to error communicating with tournament API", event.toString(), e);
				return;
			}
			
		}
	}

}
