package fft_battleground.event.detector.model;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import fft_battleground.event.model.BattleGroundEventType;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.repo.model.PlayerRecord;
import fft_battleground.util.GenericPairing;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class TeamInfoEvent extends BattleGroundEvent {

	private static final BattleGroundEventType type = BattleGroundEventType.TEAM_INFO;
	
	private BattleGroundTeam team;
	
	@JsonIgnore
	private List<Pair<String, String>> playerUnitPairs;
	
	private List<PlayerRecord> metaData;
	private Integer teamValue;
	
	
	public TeamInfoEvent(BattleGroundTeam team, List<Pair<String, String>> unitData) {
		super(type);
		this.team = team;
		this.playerUnitPairs = unitData;
	}
	
	@JsonProperty("playerUnitPairs")
	public List<GenericPairing<String, String>> getPlayerUnitGenericPairList() {
		List<GenericPairing<String, String>> result = GenericPairing.convertPairToGenericPair(this.playerUnitPairs);
		return result;
	}

	@Override
	public String toString() {
		return "TeamInfoEvent [team=" + team + ", playerUnitPairs=" + playerUnitPairs + "]";
	}

	
}
