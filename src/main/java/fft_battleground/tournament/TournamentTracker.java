package fft_battleground.tournament;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import fft_battleground.dump.DumpResourceManager;
import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.event.model.TeamInfoEvent;
import fft_battleground.event.model.UnitInfoEvent;
import fft_battleground.event.model.fake.TournamentStatusUpdateEvent;
import fft_battleground.exception.DumpException;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.tournament.model.Tournament;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TournamentTracker {
	private static final String winnersTxtUrlFormat = "http://www.fftbattleground.com/fftbg/tournament_%s/winner.txt";
	
	@Autowired
	private DumpResourceManager dumpResourceManager;
	
	public TournamentStatusUpdateEvent generateTournamentStatus(Tournament currentTournamentDetails) throws DumpException {
		TournamentStatusUpdateEvent tournamentStatus = null;
		Long tournamentId = currentTournamentDetails.getID();
		List<BattleGroundTeam> winners = this.getWinnersFromTournament(tournamentId);
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
		
		Map<BattleGroundTeam, String> teamDataString;
		
		return tournamentStatus;
	}
	
	protected List<BattleGroundTeam> getWinnersFromTournament(final Long id) throws DumpException {
		List<BattleGroundTeam> winners = new LinkedList<>();
		Resource resource;
		try {
			resource = new UrlResource(String.format(winnersTxtUrlFormat, id.toString()));
		} catch (MalformedURLException e) {
			log.error("malformed url", e);
			return new ArrayList<>();
		}
		try(BufferedReader raidBossReader = this.dumpResourceManager.openDumpResource(resource)) {
			String line;
			while((line = raidBossReader.readLine()) != null) {
				if(StringUtils.isNotBlank(line)) {
					winners.add(BattleGroundTeam.parse(line));
				}
			}
		} catch (IOException e) {
			log.debug("reading winner data for tournament {} has failed", id);
			return new ArrayList<>();
		}
		
		return winners;
	}
	
	protected Map<BattleGroundTeam, Boolean> getAliveTeamMap(final List<BattleGroundTeam> winners) {
		Map<BattleGroundTeam, Boolean> teamAliveMap = BattleGroundTeam.coreTeams().stream().collect(Collectors.toMap(Function.identity(), team -> new Boolean(true) ));
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
		Map<BattleGroundTeam, List<BattleGroundEvent>> tournamentEvents = BattleGroundTeam.coreTeams().stream()
				.collect(Collectors.toMap(Function.identity(), team1 -> currentTournamentDetails.getEventFromTournamentForTeam(team1)));
		
		return tournamentEvents;
	}
	
	protected Map<BattleGroundTeam, String> generateTeamDataString(Map<BattleGroundTeam, TeamInfoEvent> teamInfoEventMap) {
		Map<BattleGroundTeam, String> teamDataStringMap = new HashMap<>();
		
		return teamDataStringMap;
	}
	
	protected void matchOne(final Map<BattleGroundTeam, Boolean> aliveTeamMap, final List<BattleGroundTeam> winners) {
		int index = 0;
		BattleGroundTeam[] possibleTeamsInMatchArray = this.easyCreateBattleGroundArray(BattleGroundTeam.RED, BattleGroundTeam.BLUE);
		this.resolveMatch(aliveTeamMap, winners, index, possibleTeamsInMatchArray);
	}
	
	protected void matchTwo(final Map<BattleGroundTeam, Boolean> aliveTeamMap, final List<BattleGroundTeam> winners) {
		int index = 1;
		BattleGroundTeam[] possibleTeamsInMatchArray = this.easyCreateBattleGroundArray(BattleGroundTeam.GREEN, BattleGroundTeam.YELLOW);
		this.resolveMatch(aliveTeamMap, winners, index, possibleTeamsInMatchArray);	
	}
	
	protected void matchThree(final Map<BattleGroundTeam, Boolean> aliveTeamMap, final List<BattleGroundTeam> winners) {
		int index = 2;
		BattleGroundTeam[] possibleTeamsInMatchArray = this.easyCreateBattleGroundArray(BattleGroundTeam.WHITE, BattleGroundTeam.BLACK);
		this.resolveMatch(aliveTeamMap, winners, index, possibleTeamsInMatchArray);
	}
	
	protected void matchFour(final Map<BattleGroundTeam, Boolean> aliveTeamMap, final List<BattleGroundTeam> winners) {
		int index = 3;
		BattleGroundTeam[] possibleTeamsInMatchArray = this.easyCreateBattleGroundArray(BattleGroundTeam.PURPLE, BattleGroundTeam.BROWN);
		this.resolveMatch(aliveTeamMap, winners, index, possibleTeamsInMatchArray);
	}
	
	protected void semifinalOne(final Map<BattleGroundTeam, Boolean> aliveTeamMap, final List<BattleGroundTeam> winners) {
		int index = 4;
		BattleGroundTeam[] possibleTeamsInMatchArray = this.easyCreateBattleGroundArray(BattleGroundTeam.RED, BattleGroundTeam.BLUE, BattleGroundTeam.GREEN, BattleGroundTeam.YELLOW);
		this.resolveMatch(aliveTeamMap, winners, index, possibleTeamsInMatchArray);
	}
	
	protected void semifinalTwo(final Map<BattleGroundTeam, Boolean> aliveTeamMap, final List<BattleGroundTeam> winners) {
		int index = 5;
		BattleGroundTeam[] possibleTeamsInMatchArray = this.easyCreateBattleGroundArray(BattleGroundTeam.WHITE, BattleGroundTeam.BLACK, BattleGroundTeam.PURPLE, BattleGroundTeam.BROWN);
		this.resolveMatch(aliveTeamMap, winners, index, possibleTeamsInMatchArray);
	}
	
	protected void finalMatch(final Map<BattleGroundTeam, Boolean> aliveTeamMap, final List<BattleGroundTeam> winners) {
		int index = 6;
		BattleGroundTeam[] possibleTeamsInMatchArray = this.easyCreateBattleGroundArray(BattleGroundTeam.RED, BattleGroundTeam.BLUE, BattleGroundTeam.GREEN, BattleGroundTeam.YELLOW, 
				BattleGroundTeam.WHITE, BattleGroundTeam.BLACK, BattleGroundTeam.PURPLE, BattleGroundTeam.BROWN);
		this.resolveMatch(aliveTeamMap, winners, index, possibleTeamsInMatchArray);
	}
	
	protected void resolveMatch(final Map<BattleGroundTeam, Boolean> aliveTeamMap, final List<BattleGroundTeam> winners, final int winnerIndex, final BattleGroundTeam[] possibleTeamsInMatchArray) {
		List<BattleGroundTeam> possibleTeamsForMatch = Arrays.asList(possibleTeamsInMatchArray);
		if(possibleTeamsForMatch.contains(winners.get(winnerIndex))) {
			possibleTeamsForMatch.remove(winners.get(winnerIndex));
			possibleTeamsForMatch.stream().forEach(team -> aliveTeamMap.put(team, false));
		}
		
		return;
	}
	
	protected BattleGroundTeam[] easyCreateBattleGroundArray(BattleGroundTeam... team) {
		return team;
	}

}
