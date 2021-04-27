package fft_battleground.dump.model;

import java.util.List;

import fft_battleground.repo.model.GlobalGilHistory;

import lombok.Data;

@Data
public class GlobalGilPageData {
	public static final String DATE_FORMAT = "MM-dd-yyyy";
	
	public GlobalGilPageData(GlobalGilHistory todaysData, List<GlobalGilHistory> historyByDay,
			List<GlobalGilHistory> historyByWeek, List<GlobalGilHistory> historyByMonth, List<GlobalGilHistory> historyByYear) {
		this.todaysCount = todaysData;
		this.countByDay = historyByDay;
		this.countByWeek = historyByWeek;
		this.countByMonth = historyByMonth;
		this.countByYear = historyByYear;
	}
	
	private GlobalGilHistory todaysCount;
	private List<GlobalGilHistory> countByDay;
	private List<GlobalGilHistory> countByWeek;
	private List<GlobalGilHistory> countByMonth;
	private List<GlobalGilHistory> countByYear;
}
