package fft_battleground.controller.model;

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
	private Integer expRank;
	private String percentageOfGlobalGil;
	private boolean notFound = false;
	private Integer betPercentile;
	private Integer fightPercentile;
	
	public PlayerData() {}
}