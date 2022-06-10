package fft_battleground.tournament.model;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import fft_battleground.model.BattleGroundTeam;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Palette {
	private Pair<BattleGroundTeam, BattleGroundTeam> palettes;
	
	public Palette(BattleGroundTeam palette1, BattleGroundTeam palette2) {
		this.palettes = new ImmutablePair<>(palette1, palette2);
	}
}