package fft_battleground.tournament.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import fft_battleground.model.BattleGroundTeam;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Champion {
	@JsonProperty("ID")
	private Integer id;
	@JsonProperty("Rank")
	private Integer rank;
	@JsonProperty("Team")
	private ChampionTeam team;
	@JsonProperty("Streak")
	private Integer streak;
	@JsonProperty("Defeat")
	private Date defeat;
	@JsonProperty("DefeatTime")
	private String defaultTimeString;
	@JsonProperty("LastUpdated")
	private String lastUpdatedString;
	@JsonProperty("Season")
	private Integer season;
	@JsonProperty("Color")
	private String color;
	
	public BattleGroundTeam getTeam() {
		BattleGroundTeam team = BattleGroundTeam.parse(this.color);
		return team;
	}
}
