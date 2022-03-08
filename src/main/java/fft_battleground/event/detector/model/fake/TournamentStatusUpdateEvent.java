package fft_battleground.event.detector.model.fake;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import fft_battleground.event.detector.model.TeamInfoEvent;
import fft_battleground.event.detector.model.UnitInfoEvent;
import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.event.model.BattleGroundEventType;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.tournament.tracker.model.TournamentWinData;
import fft_battleground.util.GenericPairing;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class TournamentStatusUpdateEvent extends BattleGroundEvent {
	private static final BattleGroundEventType type = BattleGroundEventType.TOURNAMENT_STATUS_UPDATE_EVENT;
	
	private List<GenericPairing<BattleGroundTeam, Boolean>> teamStillActiveMap;
	private List<GenericPairing<BattleGroundTeam, TeamInfoEvent>> teamInfoMap;
	private List<GenericPairing<BattleGroundTeam, List<UnitInfoEvent>>> playerUnitInfoMap;
	private List<GenericPairing<BattleGroundTeam, TournamentWinData>> tournamentWinMap;
	
	public TournamentStatusUpdateEvent() {
		super(type);
	}
	
	public TournamentStatusUpdateEvent(Map<BattleGroundTeam, Boolean> teamStillActiveMap, Map<BattleGroundTeam, 
			TeamInfoEvent> teamInfoMap, Map<BattleGroundTeam, List<UnitInfoEvent>> unitInfoEventMap, 
			Map<BattleGroundTeam, TournamentWinData> tournamentWinMap, Integer streak) {
		super(type);
		this.teamStillActiveMap = GenericPairing.convertMapToGenericPairList(teamStillActiveMap);
		this.teamInfoMap = GenericPairing.convertMapToGenericPairList(teamInfoMap);
		this.playerUnitInfoMap = GenericPairing.convertMapToGenericPairList(unitInfoEventMap);
		
		Map<BattleGroundTeam, TournamentWinData> tournamentWinMapCopy = tournamentWinMap.keySet().stream().collect(Collectors.toMap(Function.identity(), key -> tournamentWinMap.get(key)));
		tournamentWinMapCopy.put(BattleGroundTeam.CHAMPION, new TournamentWinData(streak));
		this.tournamentWinMap = GenericPairing.convertMapToGenericPairList(tournamentWinMapCopy);
	}

}
