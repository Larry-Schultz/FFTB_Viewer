package fft_battleground.event.model.fake;

import java.util.Map;

import fft_battleground.botland.model.BattleGroundEventType;
import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.event.model.TeamInfoEvent;
import fft_battleground.event.model.UnitInfoEvent;
import fft_battleground.model.BattleGroundTeam;

import lombok.Data;

@Data
public class TournamentStatusUpdateEvent extends BattleGroundEvent {
	private static final BattleGroundEventType type = BattleGroundEventType.TOURNAMENT_STATUS_UPDATE_EVENT;
	
	private Map<BattleGroundTeam, Boolean> teamStillActiveMap;
	private Map<BattleGroundTeam, TeamInfoEvent> teamInfoMap;
	private Map<String, UnitInfoEvent> playerUnitInfoMap;
	
	public TournamentStatusUpdateEvent() {
		super(type);
	}
	
	public TournamentStatusUpdateEvent(Map<BattleGroundTeam, Boolean> teamStillActiveMap, Map<BattleGroundTeam, TeamInfoEvent> teamInfoMap, Map<String, UnitInfoEvent> unitInfoEventMap) {
		super(type);
		this.teamStillActiveMap = teamStillActiveMap;
		this.teamInfoMap = teamInfoMap;
		this.playerUnitInfoMap = unitInfoEventMap;
	}

}
