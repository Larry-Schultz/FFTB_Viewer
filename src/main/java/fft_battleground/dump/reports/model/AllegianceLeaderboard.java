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
	
	@JsonIgnore
	public String getProperTeamName() {
		String result = this.team.name();
		result = StringUtils.lowerCase(result);
		result = StringUtils.capitalize(result);
		return result;
	}
	
	@JsonIgnore
	public String getFormattedPlayerCount() {
		String result = this.commaFormat(this.totalPlayers);
		return result;
	}
	
	@JsonIgnore
	public String getFormattedTotalGil() {
		String result = this.commaFormat(this.totalGil);
		return result;
	}
	
	@JsonIgnore
	public String getFormattedGilPerPlayer() {
		DecimalFormat format = new DecimalFormat("#,###.00");
		String result= format.format(this.gilPerPlayer);
		return result;
	}
	
	@JsonIgnore
	public String getFormattedTotalLevels() {
		String result = this.commaFormat(this.totalLevels);
		return result;
	}
	
	@JsonIgnore
	public String getFormattedLevelsPerPlayer() {
		DecimalFormat format = new DecimalFormat("#,###.00");
		String result = format.format(this.totalLevelsPerPlayer);
		return result;
	}
	
	@JsonIgnore
	public String getFormattedTotalPrestiges() {
		String result = this.commaFormat(this.totalPrestiges);
		return result;
	}
	
	@JsonIgnore
	public String getFormattedBetWins() {
		String result = this.commaFormat(this.betWins);
		return result;
	}
	
	@JsonIgnore
	public String getFormattedBetLosses() {
		String result = this.commaFormat(this.betLosses);
		return result;
	}
	
	@JsonIgnore
	public String getFormattedFightWins() {
		String result = this.commaFormat(this.fightWins);
		return result;
	}
	
	@JsonIgnore
	public String getFormattedFightLosses() {
		String result = this.commaFormat(this.fightLosses);
		return result;
	}
	
	@JsonIgnore
	public String getFormattedCurrentSeasonFightWinsAsChampion() {
		String result =this.commaFormat(this.currentSeasonFightWinsAsChampion);
		return result;
	}
	
	@JsonIgnore
	public String getFormattedBetRatio() {
		DecimalFormat format = new DecimalFormat("0.00");
		String result = format.format(this.betRatio);
		return result;
	}
	
	@JsonIgnore
	public String getFormattedFightRatio() {
		DecimalFormat format = new DecimalFormat("0.00");
		String result = format.format(this.fightRatio);
		return result;
	}
	
	@JsonIgnore
	public String getFormattedBetQuantile() {
		String result = this.generateOrdinal(this.betQuantile);
		return result;
	}
	
	@JsonIgnore
	public String getFormattedFightQuantile() {
		String result = this.generateOrdinal(this.fightQuantile);
		return result;
	}
	
	@JsonIgnore
	public String getPortraitName() {
		String result = StringUtils.substringAfterLast(this.portraitUrl, "/");
		result = StringUtils.substringBefore(result, ".gif");
		return result;
	}
	
	protected String commaFormat(Integer number) {
		NumberFormat myFormat  = NumberFormat.getInstance();
		myFormat.setGroupingUsed(true);
		String result = myFormat.format(number);
		return result;
	}
	
	protected String generateOrdinal(Integer number) {
		int j = number % 10, k = number % 100;
		if (j == 1 && k != 11) {
		    return number + "st";
		}
		if (j == 2 && k != 12) {
		    return number + "nd";
		}
		if (j == 3 && k != 13) {
		    return number + "rd";
		}
		return number + "th";
	}
}
