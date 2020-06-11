package fft_battleground.dump.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import fft_battleground.repo.model.GlobalGilHistory;
import lombok.Data;
import lombok.SneakyThrows;

@Data
public class GlobalGilPageData {
	public static final String DATE_FORMAT = "MM-dd-yyyy";
	
	public GlobalGilPageData(GlobalGilHistory todaysData, List<GlobalGilHistory> historyByDay,
			List<GlobalGilHistory> historyByWeek, List<GlobalGilHistory> historyByMonth) {
		this.todaysCount = todaysData;
		this.countByDay = historyByDay;
		this.countByWeek = historyByWeek;
		this.countByMonth = historyByMonth;
	}
	
	private GlobalGilHistory todaysCount;
	private List<GlobalGilHistory> countByDay;
	private List<GlobalGilHistory> countByWeek;
	private List<GlobalGilHistory> countByMonth;
	
}
