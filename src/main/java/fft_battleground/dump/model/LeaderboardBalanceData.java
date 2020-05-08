package fft_battleground.dump.model;

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
}