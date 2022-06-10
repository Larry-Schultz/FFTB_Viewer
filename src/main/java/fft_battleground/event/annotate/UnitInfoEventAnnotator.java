package fft_battleground.event.annotate;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fft_battleground.event.detector.model.UnitInfoEvent;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.tournament.model.Tournament;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class UnitInfoEventAnnotator implements BattleGroundEventAnnotator<UnitInfoEvent> {
	
	@Autowired
	private TeamInfoEventAnnotator teamInfoEventAnnotator;
	
	@Getter @Setter private Tournament currentTournament;
	
	@Override
	public void annotateEvent(UnitInfoEvent event) {
		String playerName = event.getPlayer();
		String matchingName;
		if(event.getTeam() == BattleGroundTeam.CHAMPION) {
			matchingName = this.teamInfoEventAnnotator.findClosestMatchingName(playerName, this.currentTournament.getAllPlayers());
		} else {
			matchingName = this.teamInfoEventAnnotator.findClosestMatchingName(playerName, this.currentTournament.getEntrants());
		}
		if(matchingName != null) {
			event.setPlayer(matchingName);
		} else {
			event.setPlayer(playerName);
		}
		
		event.getUnit().setRaidBoss(event.getIsRaidBoss());
	}

}
