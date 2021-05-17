package fft_battleground.controller.model;

import java.util.Set;

import fft_battleground.repo.model.PlayerRecord;

import lombok.Data;

@Data
public class PlayerData {
	private PlayerRecord playerRecord;
	private String portraitUrl;
	private String fightRatio;
	private String betRatio;
	private boolean containsPrestige = false;
	private boolean bot = false;
	private int prestigeLevel = 0;
	private Integer leaderboardPosition;
	private String timezoneFormattedDateString;
	private String timezoneFormattedLastFightActiveDateString;
	private Integer expRank;
	private String percentageOfGlobalGil;
	private boolean notFound = false;
	private Integer betPercentile;
	private Integer fightPercentile;
	private Set<String> classBonuses;
	private Set<String> skillBonuses;
	
	public PlayerData() {}
}