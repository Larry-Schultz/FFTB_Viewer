package fft_battleground.bot.model;

import fft_battleground.model.BattleGroundTeam;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Bet {
	private BattleGroundTeam team;
	private Integer amount;

}
