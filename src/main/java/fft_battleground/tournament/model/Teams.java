package fft_battleground.tournament.model;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.fasterxml.jackson.annotation.JsonProperty;

import fft_battleground.event.model.TeamInfoEvent;
import fft_battleground.event.model.UnitInfoEvent;
import fft_battleground.model.BattleGroundTeam;

import lombok.Data;

@Data
public class Teams {
	@JsonProperty("black")
	private Team black;
	@JsonProperty("blue")
	private Team blue;
	@JsonProperty("brown")
	private Team brown;
	@JsonProperty("champion")
	private Team champion;
	@JsonProperty("green")
	private Team green;
	@JsonProperty("purple")
	private Team purple;
	@JsonProperty("red")
	private Team red;
	@JsonProperty("white")
	private Team white;
	@JsonProperty("yellow")
	private Team yellow;
	
	public Teams() {}
	
	public TeamInfoEvent getTeamInfoEventByBattleGroundTeam(BattleGroundTeam battleGroundTeam) {
		List<Pair<String, String>> unitDataPairs = this.getTeamByBattleGroundTeam(battleGroundTeam)
				.getUnits().stream()
				.map(unit -> new ImmutablePair<String, String>(unit.getName(), unit.getClassName()))
				.collect(Collectors.toList());
		TeamInfoEvent event = new TeamInfoEvent(battleGroundTeam, unitDataPairs);
		return event;
	}
	
	public List<UnitInfoEvent> getUnitInfoEventByBattleGroundTeam(BattleGroundTeam battleGroundTeam) {
		List<UnitInfoEvent> events = this.getTeamByBattleGroundTeam(battleGroundTeam).getUnits().stream()
				.map(unit -> unit.createUnitInfoEvent()).collect(Collectors.toList());
		return events;
	}
	
	public Team getTeamByBattleGroundTeam(BattleGroundTeam battleGroundTeam) {
		switch(battleGroundTeam) {
		case BLACK:
			return this.black;
		case BLUE:
			return this.blue;
		case BROWN:
			return this.brown;
		case CHAMPION:
			return this.champion;
		case GREEN:
			return this.green;
		case PURPLE:
			return this.purple;
		case RED:
			return this.red;
		case WHITE:
			return this.white;
		case YELLOW:
			return this.yellow;
		default:
			return null;
		}
	}
}
