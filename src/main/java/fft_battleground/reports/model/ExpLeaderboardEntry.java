package fft_battleground.reports.model;

import fft_battleground.model.BattleGroundTeam;
import lombok.Data;

@Data
public class ExpLeaderboardEntry {
	private Integer rank;
	private String player;
	private Short level;
	private Short exp;
	private String lastActive;
	private Integer prestigeLevel;
	private BattleGroundTeam team;
	
	public ExpLeaderboardEntry() {}
	
	public ExpLeaderboardEntry(int rank, String player, Short level, Short exp, Integer prestigeLevel, String lastActive, BattleGroundTeam team) {
		this.rank = rank;
		this.player = player;
		this.level = level;
		this.exp = exp;
		this.prestigeLevel = prestigeLevel;
		this.lastActive = lastActive;
		this.team = team;
	}
}
