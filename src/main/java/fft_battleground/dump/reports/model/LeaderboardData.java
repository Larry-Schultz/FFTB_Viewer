package fft_battleground.dump.reports.model;

import java.text.NumberFormat;

import fft_battleground.model.BattleGroundTeam;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;

@Data
@AllArgsConstructor
public class LeaderboardData implements Comparable<LeaderboardData> {
	public static final String LEADERBOARD_ACTIVE_PLAYER_DATE_FORMAT = "MM-dd-yyyy";
	private String name;
	private Integer rank;
	private String gil;
	private String lastActiveDate;
	private String percentageOfGlobalGil;
	private BattleGroundTeam allegiance;
	
	public LeaderboardData() {}
	public LeaderboardData(String name, String gil, String lastActiveDate) {
		this.name = name;
		this.gil = gil;
		this.lastActiveDate = lastActiveDate;
	}
	@Override
	@SneakyThrows
	public int compareTo(LeaderboardData arg0) {
		NumberFormat myFormat  = NumberFormat.getInstance();
		myFormat.setGroupingUsed(true);
		Integer thisGil = myFormat.parse(this.gil).intValue();
		Integer arg0Gil = myFormat.parse(arg0.getGil()).intValue();
		return thisGil.compareTo(arg0Gil);
		
	}
}