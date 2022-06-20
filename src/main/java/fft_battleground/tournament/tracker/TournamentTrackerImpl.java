package fft_battleground.tournament.tracker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fft_battleground.event.annotate.TeamInfoEventAnnotator;
import fft_battleground.event.annotate.UnitInfoEventAnnotator;
import fft_battleground.event.detector.model.BettingBeginsEvent;
import fft_battleground.event.detector.model.TeamInfoEvent;
import fft_battleground.event.detector.model.UnitInfoEvent;
import fft_battleground.event.detector.model.fake.TournamentStatusUpdateEvent;
import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.exception.DumpException;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.tournament.TournamentDumpService;
import fft_battleground.tournament.model.Tournament;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TournamentTrackerImpl implements TournamentTracker {

	@Autowired
	private TeamInfoEventAnnotator teamInfoEventAnnotator;
	
	@Autowired
	private UnitInfoEventAnnotator unitInfoEventAnnotator;
	
	@Autowired
	private TournamentDumpService tournamentDumpService;
	
	private TournamentSurvivorCalculator tournamentSurvivorCalculator = new TournamentSurvivorCalculator();
	
	@Override
	public TournamentStatusUpdateEvent generateTournamentStatus(BettingBeginsEvent bettingBeginsEvent, Tournament currentTournamentDetails) throws DumpException {
		log.info("Creating tournament tracker for current match");
		TournamentStatusUpdateEvent tournamentStatus = null;
		Long tournamentId = currentTournamentDetails.getID();
		List<BattleGroundTeam> winners = new ArrayList<>();
		//if teams aren't red and blue lookup the winners, otherwise winners should be empty.  The winners.txt is only created after the first match, and does not exist for RED vs BLUE.
		if( !(bettingBeginsEvent.getTeam1() == BattleGroundTeam.RED && bettingBeginsEvent.getTeam2() == BattleGroundTeam.BLUE) ) {
			winners = this.tournamentDumpService.getWinnersFromTournament(tournamentId);
		}
		
		Map<BattleGroundTeam, Boolean> aliveTeamMap = this.tournamentSurvivorCalculator.getAliveTeamMap(winners);
		
		Map<BattleGroundTeam, List<BattleGroundEvent>> tournamentEventMap = this.getEventsFromCurrentTournament(currentTournamentDetails);
		Map<BattleGroundTeam, TeamInfoEvent> teamInfoMap = new HashMap<>();
		Map<BattleGroundTeam, List<UnitInfoEvent>> unitInfoMap = tournamentEventMap.keySet().stream().collect(Collectors.toMap(Function.identity(), team -> new ArrayList<UnitInfoEvent>()));
		for(BattleGroundTeam team: tournamentEventMap.keySet()) {
			for(BattleGroundEvent event : tournamentEventMap.get(team)) {
				if(event instanceof TeamInfoEvent) {
					teamInfoMap.put(team, (TeamInfoEvent)event);
				} else if(event instanceof UnitInfoEvent) {
					unitInfoMap.get(team).add((UnitInfoEvent) event);
				}
			}
		}
		
		//annotate events
		this.teamInfoEventAnnotator.setCurrentTournament(currentTournamentDetails);
		this.unitInfoEventAnnotator.setCurrentTournament(currentTournamentDetails);
		teamInfoMap.keySet().parallelStream()
			//.filter(team -> team != BattleGroundTeam.CHAMPION)
			.map(key -> teamInfoMap.get(key))
			.forEach(event -> this.teamInfoEventAnnotator.annotateEvent(event));
		
		unitInfoMap.keySet().parallelStream()
			.map(key -> unitInfoMap.get(key))
			.forEach(
				unitInfoEventList -> unitInfoEventList.parallelStream().forEach(event -> this.unitInfoEventAnnotator.annotateEvent(event))
			);
		
		tournamentStatus = new TournamentStatusUpdateEvent(aliveTeamMap, teamInfoMap, unitInfoMap, currentTournamentDetails.getTournamentWinTracker(), currentTournamentDetails.getChampionStreak());
		
		log.info("Tournament tracker creation complete");
		
		return tournamentStatus;
	}
	
	protected Map<BattleGroundTeam, List<BattleGroundEvent>> getEventsFromCurrentTournament(final Tournament currentTournamentDetails) {
		Map<BattleGroundTeam, List<BattleGroundEvent>> tournamentEvents = this.tournamentSurvivorCalculator.trackedTeams().stream()
				.collect(Collectors.toMap(Function.identity(), team1 -> currentTournamentDetails.getEventFromTournamentForTeam(team1)));
		
		return tournamentEvents;
	}
}
