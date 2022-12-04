package fft_battleground.dump.service;

import java.util.Date;
import java.util.List;

import fft_battleground.dump.reports.model.LeaderboardBalanceData;
import fft_battleground.dump.reports.model.LeaderboardBalanceHistoryEntry;

public interface BalanceHistoryService {

	boolean isPlayerActiveInLastMonth(String player);
	boolean isPlayerActiveInLastMonth(Date lastActiveDate);

	// this time let's take it hour by hour, and then use my original date slice
	// algorithm
	LeaderboardBalanceData getLabelsAndSetRelevantBalanceHistories(List<LeaderboardBalanceHistoryEntry> entries,
			Integer count);

}