package fft_battleground.tournament.model;

import fft_battleground.model.BattleGroundTeam;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Palette {
	private BattleGroundTeam primary;
	private BattleGroundTeam secondary;
	
	public Palette(BattleGroundTeam primary, BattleGroundTeam secondary) {
		this.primary = primary;
		this.secondary = secondary;
	}
}