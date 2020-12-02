package fft_battleground.dump.reports.model;

import lombok.Data;

@Data
public class ExpLeaderboardEntry {
	private Integer rank;
	private String player;
	private Short level;
	private Short exp;
	private String lastActive;
	private Integer prestigeLevel;
	
	public ExpLeaderboardEntry() {}
	
	public ExpLeaderboardEntry(int rank, String player, Short level, Short exp, Integer prestigeLevel, String lastActive) {
		this.rank = rank;
		this.player = player;
		this.level = level;
		this.exp = exp;
		this.prestigeLevel = prestigeLevel;
		this.lastActive = lastActive;
	}
}
