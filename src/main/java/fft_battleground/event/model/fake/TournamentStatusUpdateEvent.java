package fft_battleground.event.model.fake;

import java.util.List;
import java.util.Map;

import fft_battleground.botland.model.BattleGroundEventType;
import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.event.model.TeamInfoEvent;
import fft_battleground.event.model.UnitInfoEvent;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.util.GenericPairing;

import lombok.Data;

@Data
public class TournamentStatusUpdateEvent extends BattleGroundEvent {
	private static final BattleGroundEventType type = BattleGroundEventType.TOURNAMENT_STATUS_UPDATE_EVENT;
	
	private List<GenericPairing<BattleGroundTeam, Boolean>> teamStillActiveMap;
	private List<GenericPairing<BattleGroundTeam, TeamInfoEvent>> teamInfoMap;
	private List<GenericPairing<BattleGroundTeam, List<UnitInfoEvent>>> playerUnitInfoMap;
	
	public TournamentStatusUpdateEvent() {
		super(type);
	}
	
	public TournamentStatusUpdateEvent(Map<BattleGroundTeam, Boolean> teamStillActiveMap, Map<BattleGroundTeam, TeamInfoEvent> teamInfoMap, Map<BattleGroundTeam, List<UnitInfoEvent>> unitInfoEventMap) {
		super(type);
		this.teamStillActiveMap = GenericPairing.convertMapToGenericPairList(teamStillActiveMap);
		this.teamInfoMap = GenericPairing.convertMapToGenericPairList(teamInfoMap);
		this.playerUnitInfoMap = GenericPairing.convertMapToGenericPairList(unitInfoEventMap);
	}

}
