package fft_battleground.tournament.tracker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import fft_battleground.model.BattleGroundTeam;

public class TournamentSurvivorCalculator {

	public Map<BattleGroundTeam, Boolean> getAliveTeamMap(final List<BattleGroundTeam> winners) {
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
	
	public List<BattleGroundTeam> trackedTeams() {
		List<BattleGroundTeam> teams = new ArrayList<>(BattleGroundTeam.coreTeams());
		teams.add(BattleGroundTeam.CHAMPION);
		return teams;
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

}
