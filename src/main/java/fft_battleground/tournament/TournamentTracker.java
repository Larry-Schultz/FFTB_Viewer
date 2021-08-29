package fft_battleground.tournament;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fft_battleground.event.annotate.TeamInfoEventAnnotator;
import fft_battleground.event.annotate.UnitInfoEventAnnotator;
import fft_battleground.event.detector.model.BattleGroundEvent;
import fft_battleground.event.detector.model.BettingBeginsEvent;
import fft_battleground.event.detector.model.TeamInfoEvent;
import fft_battleground.event.detector.model.UnitInfoEvent;
import fft_battleground.event.detector.model.fake.TournamentStatusUpdateEvent;
import fft_battleground.exception.DumpException;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.tournament.model.Tournament;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TournamentTracker {

	@Autowired
	private TeamInfoEventAnnotator teamInfoEventAnnotator;
	
	@Autowired
	private UnitInfoEventAnnotator unitInfoEventAnnotator;
	
	@Autowired
	private TournamentService tournamentService;
	
	public TournamentStatusUpdateEvent generateTournamentStatus(BettingBeginsEvent bettingBeginsEvent, Tournament currentTournamentDetails) throws DumpException {
		log.info("Creating tournament tracker for current match");
		TournamentStatusUpdateEvent tournamentStatus = null;
		Long tournamentId = currentTournamentDetails.getID();
		List<BattleGroundTeam> winners = new ArrayList<>();
		//if teams aren't red and blue lookup the winners, otherwise winners should be empty.  The winners.txt is only created after the first match, and does not exist for RED vs BLUE.
		if( !(bettingBeginsEvent.getTeam1() == BattleGroundTeam.RED && bettingBeginsEvent.getTeam2() == BattleGroundTeam.BLUE) ) {
			winners = this.tournamentService.getWinnersFromTournament(tournamentId);
		}
		
		Map<BattleGroundTeam, Boolean> aliveTeamMap = this.getAliveTeamMap(winners);
		
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
			.filter(team -> team != BattleGroundTeam.CHAMPION)
			.map(key -> unitInfoMap.get(key))
			.forEach(
				unitInfoEventList -> unitInfoEventList.parallelStream().forEach(event -> this.unitInfoEventAnnotator.annotateEvent(event))
			);
		
		tournamentStatus = new TournamentStatusUpdateEvent(aliveTeamMap, teamInfoMap, unitInfoMap);
		
		log.info("Tournament tracker creation complete");
		
		return tournamentStatus;
	}
	
	protected Map<BattleGroundTeam, Boolean> getAliveTeamMap(final List<BattleGroundTeam> winners) {
		Map<BattleGroundTeam, Boolean> teamAliveMap = this.trackedTeams().stream().collect(Collectors.toMap(Function.identity(), team -> true ));
		if(winners.size() > 0) {
			this.matchOne(teamAliveMap, winners);
		}
		if(winners.size() > 1) {
			this.matchTwo(teamAliveMap, winners);
		}
		if(winners.size() > 2) {
			this.matchThree(teamAliveMap, winners);
		}
		if(winners.size() > 3) {
			this.matchFour(teamAliveMap, winners);
		}
		if(winners.size() > 4) {
			this.semifinalOne(teamAliveMap, winners);
		}
		if(winners.size() > 5) {
			this.semifinalTwo(teamAliveMap, winners);
		}
		if(winners.size() > 6) {
			this.finalMatch(teamAliveMap, winners);
		}
		
		return teamAliveMap;
	}
	
	protected Map<BattleGroundTeam, List<BattleGroundEvent>> getEventsFromCurrentTournament(final Tournament currentTournamentDetails) {
		Map<BattleGroundTeam, List<BattleGroundEvent>> tournamentEvents = this.trackedTeams().stream()
				.collect(Collectors.toMap(Function.identity(), team1 -> currentTournamentDetails.getEventFromTournamentForTeam(team1)));
		
		return tournamentEvents;
	}
	
	protected void matchOne(final Map<BattleGroundTeam, Boolean> aliveTeamMap, final List<BattleGroundTeam> winners) {
		int index = 0;
		List<BattleGroundTeam> possibleTeamsInMatchArray = this.easyCreateBattleGroundArray(BattleGroundTeam.RED, BattleGroundTeam.BLUE);
		this.resolveMatch(aliveTeamMap, winners, index, possibleTeamsInMatchArray);
	}
	
	protected void matchTwo(final Map<BattleGroundTeam, Boolean> aliveTeamMap, final List<BattleGroundTeam> winners) {
		int index = 1;
		List<BattleGroundTeam> possibleTeamsInMatchArray = this.easyCreateBattleGroundArray(BattleGroundTeam.GREEN, BattleGroundTeam.YELLOW);
		this.resolveMatch(aliveTeamMap, winners, index, possibleTeamsInMatchArray);	
	}
	
	protected void matchThree(final Map<BattleGroundTeam, Boolean> aliveTeamMap, final List<BattleGroundTeam> winners) {
		int index = 2;
		List<BattleGroundTeam> possibleTeamsInMatchArray = this.easyCreateBattleGroundArray(BattleGroundTeam.WHITE, BattleGroundTeam.BLACK);
		this.resolveMatch(aliveTeamMap, winners, index, possibleTeamsInMatchArray);
	}
	
	protected void matchFour(final Map<BattleGroundTeam, Boolean> aliveTeamMap, final List<BattleGroundTeam> winners) {
		int index = 3;
		List<BattleGroundTeam> possibleTeamsInMatchArray = this.easyCreateBattleGroundArray(BattleGroundTeam.PURPLE, BattleGroundTeam.BROWN);
		this.resolveMatch(aliveTeamMap, winners, index, possibleTeamsInMatchArray);
	}
	
	protected void semifinalOne(final Map<BattleGroundTeam, Boolean> aliveTeamMap, final List<BattleGroundTeam> winners) {
		int index = 4;
		List<BattleGroundTeam> possibleTeamsInMatchArray = this.easyCreateBattleGroundArray(BattleGroundTeam.RED, BattleGroundTeam.BLUE, BattleGroundTeam.GREEN, BattleGroundTeam.YELLOW);
		this.resolveMatch(aliveTeamMap, winners, index, possibleTeamsInMatchArray);
	}
	
	protected void semifinalTwo(final Map<BattleGroundTeam, Boolean> aliveTeamMap, final List<BattleGroundTeam> winners) {
		int index = 5;
		List<BattleGroundTeam> possibleTeamsInMatchArray = this.easyCreateBattleGroundArray(BattleGroundTeam.WHITE, BattleGroundTeam.BLACK, BattleGroundTeam.PURPLE, BattleGroundTeam.BROWN);
		this.resolveMatch(aliveTeamMap, winners, index, possibleTeamsInMatchArray);
	}
	
	protected void finalMatch(final Map<BattleGroundTeam, Boolean> aliveTeamMap, final List<BattleGroundTeam> winners) {
		int index = 6;
		List<BattleGroundTeam> possibleTeamsInMatchArray = this.easyCreateBattleGroundArray(BattleGroundTeam.RED, BattleGroundTeam.BLUE, BattleGroundTeam.GREEN, BattleGroundTeam.YELLOW, 
				BattleGroundTeam.WHITE, BattleGroundTeam.BLACK, BattleGroundTeam.PURPLE, BattleGroundTeam.BROWN);
		this.resolveMatch(aliveTeamMap, winners, index, possibleTeamsInMatchArray);
	}
	
	protected void resolveMatch(final Map<BattleGroundTeam, Boolean> aliveTeamMap, final List<BattleGroundTeam> winners, final int winnerIndex, final List<BattleGroundTeam> possibleTeamsInMatchArray) {
		List<BattleGroundTeam> possibleTeamsForMatch = possibleTeamsInMatchArray;
		if(possibleTeamsForMatch.contains(winners.get(winnerIndex))) {
			
			/* removes the winning team from the possible teams.  All other teams must have lost, and the aliveTeamMap will reflect this.
			 * It is okay to remove, because this is created per match function.
			 */
			possibleTeamsForMatch.remove(winners.get(winnerIndex));  
			possibleTeamsForMatch.stream().forEach(team -> aliveTeamMap.put(team, false));
		}
		
		return;
	}
	
	protected List<BattleGroundTeam> easyCreateBattleGroundArray(BattleGroundTeam... team) {
		List<BattleGroundTeam> battleGroundArrayList = new ArrayList<BattleGroundTeam>(Arrays.asList(team));
		return battleGroundArrayList;
	}
	
	protected List<BattleGroundTeam> trackedTeams() {
		List<BattleGroundTeam> teams = new ArrayList<>(BattleGroundTeam.coreTeams());
		teams.add(BattleGroundTeam.CHAMPION);
		return teams;
	}

}
