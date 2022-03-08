package fft_battleground.controller.response.model;

import fft_battleground.dump.reports.model.ExpLeaderboard;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ExpLeaderboardData {
	private ExpLeaderboard expLeaderboard;
	private String generationDateString;
}
