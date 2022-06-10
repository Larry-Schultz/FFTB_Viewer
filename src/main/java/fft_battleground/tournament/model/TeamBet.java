package fft_battleground.tournament.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import fft_battleground.model.BattleGroundTeam;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TeamBet {
	@JsonProperty("Team")
	private BattleGroundTeam team;
	@JsonProperty("Odds")
	private Double odds;
	@JsonProperty("Bets")
	private List<TournamentBet> Bets;
	
	public int potTotal() {
		return this.Bets.stream().mapToInt(TournamentBet::getValue).sum();
	}
}
