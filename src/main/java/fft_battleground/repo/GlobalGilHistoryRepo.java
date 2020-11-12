package fft_battleground.repo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import fft_battleground.repo.model.GlobalGilHistory;
import lombok.SneakyThrows;

public interface GlobalGilHistoryRepo extends JpaRepository<GlobalGilHistory, String> {
	
	public static final int RESULTS_COUNT = 10;

	@Query(nativeQuery=true, value="select sum(amounts.amount) " + 
			"from ( " + 
			"	select sum(left_team_amount) as amount " + 
			"	from real_bet_information " + 
			"	where create_date_time is not null and create_date_time > date_trunc('week', CURRENT_TIMESTAMP - interval '1 week') " + 
			"	union " + 
			"	select sum(right_team_amount) as amount " + 
			"	from real_bet_information " + 
			"	where create_date_time is not null and create_date_time > date_trunc('week', CURRENT_TIMESTAMP - interval '1 week') " + 
			") amounts ")
	public Long getLiquidityFromLastWeek();
	
	@Query("SELECT globalGilHistory FROM GlobalGilHistory globalGilHistory WHERE globalGilHistory.date_string = :dateString")
	public GlobalGilHistory getGlobalGilHistoryByDateString(@Param("dateString") String dateString);
	
	@SneakyThrows
	public default GlobalGilHistory getFirstGlobalGilHistory() {
		LocalDate today = LocalDate.now();
		DateTimeFormatter sdf = DateTimeFormatter.ofPattern(GlobalGilHistory.dateFormatString);
		String dateString = sdf.format(today);
		GlobalGilHistory todaysHistory = this.getGlobalGilHistoryByDateString(dateString);
		
		//Looks like we haven't recalculated the history yet, likely just after midnight.  In this case use yesterday's values
		int i = -1;
		while(todaysHistory == null) {
			LocalDate yesterday = today.plus(i, ChronoUnit.DAYS);
			dateString = sdf.format(yesterday);
			todaysHistory = this.getGlobalGilHistoryByDateString(dateString);
			i--;
		}
		
		return todaysHistory;
	}
	
	@Query("SELECT globalGilHistory FROM GlobalGilHistory globalGilHistory WHERE globalGilHistory.date_string IN :dateStringList")
	public List<GlobalGilHistory> getGlobalGilMatchingDateStrings(@Param("dateStringList") List<String> dateStringList);
	
	@SneakyThrows
	public default List<GlobalGilHistory> getGlobalGilHistoryByCalendarTimeType(ChronoUnit timeUnit) {
		List<GlobalGilHistory> histories = new ArrayList<>();
		LocalDate today = LocalDate.now();
		DateTimeFormatter sdf = DateTimeFormatter.ofPattern(GlobalGilHistory.dateFormatString);
		List<String> dateStrings = new ArrayList<>();
		int i = 0;
		do {
			String dateString = sdf.format(today);
			dateStrings.add(dateString);
			today = today.plus(-1, timeUnit);
			i++;
		} while(i < RESULTS_COUNT);
		
		histories = this.getGlobalGilMatchingDateStrings(dateStrings);
		Collections.sort(histories, Collections.reverseOrder());
		return histories;
	}
}
