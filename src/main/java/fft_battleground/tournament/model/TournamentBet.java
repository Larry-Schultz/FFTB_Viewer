package fft_battleground.tournament.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TournamentBet {
	@JsonProperty("User")
	private String user;
	@JsonProperty("Value")
	private Integer value;
	@JsonProperty("Balance")
	private Integer balance;
}
