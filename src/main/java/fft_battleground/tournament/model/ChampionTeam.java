package fft_battleground.tournament.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChampionTeam {
	@JsonProperty("Player")
	private String player;
	@JsonProperty("Name")
	private String name;
	@JsonProperty("Palettes")
	private String palettes;
	@JsonProperty("Units")
	private List<Unit> Units;
}
