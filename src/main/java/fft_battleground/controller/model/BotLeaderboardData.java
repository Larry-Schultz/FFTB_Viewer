package fft_battleground.controller.model;

import java.util.List;

import fft_battleground.dump.reports.model.LeaderboardData;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BotLeaderboardData {
	private List<LeaderboardData> botLeaderboard;
	private String generationDateString;
}
