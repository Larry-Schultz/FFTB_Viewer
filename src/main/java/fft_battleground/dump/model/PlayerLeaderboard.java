package fft_battleground.dump.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PlayerLeaderboard {
	private List<LeaderboardData> highestPlayers;
	private List<LeaderboardData> topPlayers;
}