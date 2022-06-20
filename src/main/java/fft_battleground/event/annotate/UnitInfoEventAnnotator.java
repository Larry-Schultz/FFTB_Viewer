package fft_battleground.event.annotate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fft_battleground.event.detector.model.UnitInfoEvent;
import fft_battleground.exception.MissingTipException;
import fft_battleground.exception.TournamentApiException;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.mustadio.MustadioService;
import fft_battleground.tournament.TournamentService;
import fft_battleground.tournament.model.Tournament;
import fft_battleground.tournament.tips.Tips;
import fft_battleground.tournament.tips.UnitStats;
import fft_battleground.tournament.tips.UnitTipInfo;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class UnitInfoEventAnnotator implements BattleGroundEventAnnotator<UnitInfoEvent> {
	
	@Autowired
	private TeamInfoEventAnnotator teamInfoEventAnnotator;
	
	@Getter @Setter private Tournament currentTournament;
	
	@Autowired
	private TournamentService tournamentService;
	
	@Autowired
	private MustadioService mustadioService;
	
	@Override
	public void annotateEvent(UnitInfoEvent event) {
		this.setClosestPlayerName(event);
		this.setUnitRaidBossFlag(event);
		this.addRelevantTips(event);
		this.addUnitStats(event);
	}
	
	private void setClosestPlayerName(UnitInfoEvent event) {
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
	}
	
	private void setUnitRaidBossFlag(UnitInfoEvent event) {
		event.getUnit().setRaidBoss(event.getIsRaidBoss());
	}
	
	private void addRelevantTips(UnitInfoEvent event) {
		try {
			Tips tips = this.tournamentService.getCurrentTips();
			//UnitStats unitStats = new UnitStats(tips.getClassMap().get(event.getUnit().getClassName()));
			UnitTipInfo unitTipInfo = new UnitTipInfo(event.getUnit(), tips);
			event.setUnitTipInfo(unitTipInfo);
		} catch (TournamentApiException e) {
			log.error("Failed to load tips", e);
		} catch (MissingTipException e) {
			log.error("Error generating unit tips", e);
		} catch(Exception e) {
			log.error("Error annotating unit info event", e);
		}
	}
	
	private void addUnitStats(UnitInfoEvent event) {
		UnitStats stats = this.mustadioService.getUnitStats(event);
		event.setUnitStats(stats);
	}

}
