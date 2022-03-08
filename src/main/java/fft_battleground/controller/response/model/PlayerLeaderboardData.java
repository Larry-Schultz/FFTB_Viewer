package fft_battleground.controller.response.model;

import fft_battleground.dump.reports.model.PlayerLeaderboard;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PlayerLeaderboardData {
	private PlayerLeaderboard playerLeaderboard;
	private String generationDateString;
	private String topPlayerCommaSplitString;
	
}
