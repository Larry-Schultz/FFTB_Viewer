package fft_battleground.repo.util;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public enum BattleGroundCacheEntryKey {
	LEADERBOARD("leaderboard", TimeUnit.HOURS, Calendar.HOUR, 8),
	BOT_LEADERBOARD("botleaderboard", TimeUnit.HOURS, Calendar.HOUR, 8),
	BET_PERCENTILES("betpercentiles", TimeUnit.HOURS, Calendar.HOUR, 8),
	FIGHT_PERCENTILES("fightpercentiles", TimeUnit.HOURS, Calendar.HOUR, 8),
	ALLEGIANCE_LEADERBOARD("allegianceleaderboard", TimeUnit.HOURS, Calendar.HOUR, 8), 
	EXPERIENCE_LEADERBOARD("experienceLeaderboard", TimeUnit.HOURS, Calendar.HOUR, 8);
	
	BattleGroundCacheEntryKey(String key, TimeUnit timeUnit, Integer calendarUnit, Integer timeValue) {
		this.key = key;
		this.timeUnit = timeUnit;
		this.calendarUnit = calendarUnit;
		this.timeValue = timeValue;
	}

	private String key;
	private TimeUnit timeUnit;
	private Integer calendarUnit;
	private Integer timeValue;
	
	public String getKey() {
		return this.key;
	}
	public TimeUnit getTimeUnit() {
		return this.timeUnit;
	}
	public Integer getCalendarUnit() {
		return this.calendarUnit;
	}
	public Integer getTimeValue() {
		return this.timeValue;
	}
	
	public String getCronString() {
		return null;
	}
}
