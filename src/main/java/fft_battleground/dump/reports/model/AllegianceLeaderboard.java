package fft_battleground.dump.reports.model;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

import fft_battleground.model.BattleGroundTeam;

import lombok.Data;

@Data
public class AllegianceLeaderboard implements Comparable<AllegianceLeaderboard> {
	private int position;
	
	private BattleGroundTeam team;
	
	private Integer totalGil;
	private Integer totalPlayers;
	private Integer gilPerPlayer;
	
	private Integer totalLevels;
	private Double totalLevelsPerPlayer;
	private Integer totalPrestiges = 0;
	
	private String portraitUrl;
	
	private Integer betWins;
	private Integer betLosses;
	private Integer fightWins;
	private Integer fightLosses;
	private Integer currentSeasonFightWinsAsChampion = 0;
	
	private Double betRatio;
	private Double fightRatio;
	
	private Integer betQuantile;
	private Integer fightQuantile;
	
	private boolean betWinUnderdog = false;
	private boolean fightWinUnderdog = false;
	private boolean gilUnderdog = false;
	
	private List<AllegianceLeaderboardEntry> leaderboard;
	
	public AllegianceLeaderboard() {}
	
	public AllegianceLeaderboard(BattleGroundTeam team, String portraitUrl, List<AllegianceLeaderboardEntry> top5Players) {
		this.team = team;
		this.portraitUrl = portraitUrl;
		this.leaderboard = top5Players;
	}

	@Override
	public int compareTo(AllegianceLeaderboard arg0) {
		int compareTotalGil = this.totalGil.compareTo(arg0.getTotalGil());
		return compareTotalGil;
	}
}
