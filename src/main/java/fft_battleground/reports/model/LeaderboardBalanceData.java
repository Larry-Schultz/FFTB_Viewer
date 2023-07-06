package fft_battleground.reports.model;

import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LeaderboardBalanceData {
	private List<Date> labels;
	private List<LeaderboardBalanceHistoryEntry> leaderboardBalanceHistories;
	private int standardSize = 0;
	
	public LeaderboardBalanceData(List<Date> labels, List<LeaderboardBalanceHistoryEntry> leaderboardBalanceHistories) {
		this.labels = labels;
		this.leaderboardBalanceHistories = leaderboardBalanceHistories;
	}
}