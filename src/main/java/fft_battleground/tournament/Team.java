package fft_battleground.tournament;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class Team {
	@JsonProperty("Player")
	private String Player;
	@JsonProperty("Name")
	private String Name;
	@JsonProperty("Palettes")
	private String Palettes;
	@JsonProperty("Units")
	private List<Unit> Units;
	
	public Team() {}
	
}
