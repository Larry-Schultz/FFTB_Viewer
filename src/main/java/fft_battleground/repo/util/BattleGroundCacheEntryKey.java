package fft_battleground.repo.util;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import java.time.Duration;
import java.time.temporal.TemporalUnit;
import java.time.temporal.ChronoUnit;

public enum BattleGroundCacheEntryKey {
	LEADERBOARD("leaderboard", ChronoUnit.HOURS, 8),
	BOT_LEADERBOARD("botleaderboard", ChronoUnit.HOURS, 8),
	BET_PERCENTILES("betpercentiles", ChronoUnit.HOURS, 8),
	FIGHT_PERCENTILES("fightpercentiles", ChronoUnit.HOURS, 8),
	ALLEGIANCE_LEADERBOARD("allegianceleaderboard", ChronoUnit.HOURS, 8), 
	EXPERIENCE_LEADERBOARD("experienceLeaderboard", ChronoUnit.HOURS, 1),
	PRESTIGE_TABLE("prestigetable", ChronoUnit.HOURS, 1),
	BOTLAND_LEADERBOARD("botlandleaderboard", ChronoUnit.HOURS, 8),
	PLAYER_LEADERBOARD_BALANCE_HISTORY("playerleaderboardbalancehistory", ChronoUnit.HOURS, 8),
	BOT_LEADERBOARD_BALANCE_HISTORY("botleaderboardbalancehistory", ChronoUnit.HOURS, 8),
	PLAYLIST_SONG_COUNT_HISTORY("playlistsongcounthistory", ChronoUnit.HOURS, 8),
	PLAYLIST_SONG_OCCURENCE_COUNT_HISTORY("playlistsongoccurencecounthistory", ChronoUnit.HOURS, 8);
	
	BattleGroundCacheEntryKey(String key, TemporalUnit timeUnit, Integer timeValue) {
		this.key = key;
		this.timeUnit = timeUnit;
		this.timeValue = timeValue;
	}

	private String key;
	private TemporalUnit timeUnit;
	private Integer timeValue;
	
	public String getKey() {
		return this.key;
	}
	public TemporalUnit getTemporalUnit() {
		return this.timeUnit;
	}
	
	public Integer getTimeValue() {
		return this.timeValue;
	}
	
	public int getCalendarUnit() {
		ChronoUnit chronoUnit = (ChronoUnit) this.getTemporalUnit();
		switch(chronoUnit) {
		case HOURS:
			return Calendar.HOUR;
		case MINUTES:
			return Calendar.MINUTE;
		default:
			return Calendar.HOUR;
		}
	}
	
	public TimeUnit getTimeUnit() {
		ChronoUnit chronoUnit = (ChronoUnit) this.getTemporalUnit();
		switch(chronoUnit) {
		case HOURS:
			return TimeUnit.HOURS;
		case MINUTES:
			return TimeUnit.MINUTES;
		default:
			return TimeUnit.HOURS;
		}
	}
	
	public long millis() {
		Duration time = Duration.of(this.getTimeValue(), this.getTemporalUnit());
		long millis = time.toMillis();
		return millis;
	}
}
