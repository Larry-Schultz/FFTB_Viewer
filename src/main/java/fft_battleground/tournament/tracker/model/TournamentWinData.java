package fft_battleground.tournament.tracker.model;

import java.util.ArrayList;
import java.util.List;

import fft_battleground.model.BattleGroundTeam;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class TournamentWinData {
	@Getter private List<BattleGroundTeam> wins = new ArrayList<BattleGroundTeam>();
	@Getter private List<BattleGroundTeam> losses = new ArrayList<BattleGroundTeam>();
	@Getter @Setter private Integer streak = null;
	
	public TournamentWinData(Integer streak) {
		this.streak = streak;
	}
}
