package fft_battleground.event.detector.model;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.event.model.BattleGroundEventType;
import fft_battleground.model.BattleGroundTeam;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class MatchInfoEvent extends BattleGroundEvent {

	private static final BattleGroundEventType type = BattleGroundEventType.MATCH_INFO;
	
	private BattleGroundTeam team1;
	private BattleGroundTeam team2;
	private Integer mapNumber;
	private String mapName;
	private List<String> memeTournamentSettings;

	public MatchInfoEvent(Pair<BattleGroundTeam, BattleGroundTeam> teamPair, Pair<Integer, String> mapData, 
			List<String> memeTournamentSettings) {
		super(type);
		this.team1 = teamPair.getLeft();
		this.team2 = teamPair.getRight();
		this.mapNumber = mapData.getLeft();
		this.mapName = mapData.getRight();
		this.memeTournamentSettings = memeTournamentSettings;
	}


	@Override
	public String toString() {
		return "MatchInfoEvent [team1=" + team1 + ", team2=" + team2 + ", mapNumber=" + mapNumber + ", mapName="
				+ mapName + "]";
	}
}
