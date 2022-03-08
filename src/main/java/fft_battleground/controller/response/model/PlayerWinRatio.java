package fft_battleground.controller.response.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PlayerWinRatio {
	public String player;
	public double winRate;
	
	public PlayerWinRatio(String player, int wins, int losses) {
		this.player = player;
		this.winRate = this.calculateWinRate(wins, losses);
	}
	
	protected double calculateWinRate(int wins, int losses) {
		double winDouble = (double) wins;
		double lossDouble = (double) losses;
		double winRate = (winDouble + 1) / (winDouble + lossDouble + 1);
		return winRate;
	}
}
