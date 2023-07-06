package fft_battleground.reports.model;

import java.util.List;

import fft_battleground.repo.model.BalanceHistory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LeaderboardBalanceHistoryEntry {
	private String playerName;
	private List<BalanceHistory> balanceHistory;
}