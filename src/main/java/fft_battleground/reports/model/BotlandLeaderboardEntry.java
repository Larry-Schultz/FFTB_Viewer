package fft_battleground.reports.model;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BotlandLeaderboardEntry {
	private String botName;
	private Long endOfDayHighScoreVictoryCount;
	private Long daysparticipating;
	private Date creationDate;
	private Long totalBetWins;
	private Long totalBetLosses;
	private Integer highestRecordedBalance;
	private Double averageBalance;
	private Double averagePeakBalance;
	private Double averageWinsPerDay;
	private Double averageLossesPerDay;
	private Double averageWinRatePerDay;
}
