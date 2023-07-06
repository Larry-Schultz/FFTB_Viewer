package fft_battleground.controller.response.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import fft_battleground.reports.model.LeaderboardData;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class BotLeaderboardData {
	public static final int ACTIVE_BOT_CUTOFF_IN_MONTHS = 3;
	
	private List<LeaderboardData> botLeaderboard;
	private List<LeaderboardData> inactiveBots;
	private String generationDateString;
	
	public BotLeaderboardData() {}
	
	public BotLeaderboardData(List<LeaderboardData> botLeaderboard, String generationDateString) {
		Date activeTimeCutoff = this.calculateActiveTimeCutoff();
		Map<Boolean, List<LeaderboardData>> isLeaderboardEntryActiveMap = botLeaderboard.stream()
				.collect(Collectors.partitioningBy(leaderboardEntry -> {
					boolean isAfter = false;
					try {
						Date entryActiveDate = this.parseActiveDate(leaderboardEntry.getLastActiveDate());
						isAfter = activeTimeCutoff.before(entryActiveDate);
					} catch(Exception e) {
						log.error("exception partitioning bot leaderboard", e);
					}
					return isAfter;
				}));
		this.botLeaderboard = isLeaderboardEntryActiveMap.get(true);
		this.rerankLeaderboard(this.botLeaderboard);
		this.inactiveBots = isLeaderboardEntryActiveMap.get(false);
		this.rerankLeaderboard(this.inactiveBots);
	}
	
	private Date calculateActiveTimeCutoff() {
		Calendar c = Calendar.getInstance(); 
		c.add(Calendar.MONTH, (-1) * ACTIVE_BOT_CUTOFF_IN_MONTHS);
		return c.getTime();
	}
	
	private Date parseActiveDate(String activeDate) {
		SimpleDateFormat sdf = new SimpleDateFormat(LeaderboardData.LEADERBOARD_ACTIVE_PLAYER_DATE_FORMAT);
		Date date = null;
		try {
			date = sdf.parse(activeDate);
		} catch (ParseException e) {
			log.warn("failing to parse active dates when generating bot leaderboard");
		}
		return date;
	}
	
	private void rerankLeaderboard(List<LeaderboardData> leaderboard) {
		for(int i = 0; i < leaderboard.size(); i++) { 
			leaderboard.get(i).setRank(i + 1); 
		}
	}
}
