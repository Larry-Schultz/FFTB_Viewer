package fft_battleground.repo;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public enum BattleGroundCacheEntryKey {
	LEADERBOARD("leaderboard", TimeUnit.HOURS, Calendar.HOUR, 4),
	BOT_LEADERBOARD("botleaderboard", TimeUnit.HOURS, Calendar.HOUR, 4),
	BET_PERCENTILES("betpercentiles", TimeUnit.HOURS, Calendar.HOUR, 4),
	FIGHT_PERCENTILES("fightpercentiles", TimeUnit.HOURS, Calendar.HOUR, 4),
	ALLEGIANCE_LEADERBOARD("allegianceleaderboard", TimeUnit.HOURS, Calendar.HOUR, 4),
	BATCH_DATA("batchdata", TimeUnit.DAYS, Calendar.DAY_OF_WEEK, 7);
	
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
