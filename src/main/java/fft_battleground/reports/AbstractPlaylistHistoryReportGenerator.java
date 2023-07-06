package fft_battleground.reports;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.tuple.Pair;

import com.fasterxml.jackson.databind.ObjectMapper;

import fft_battleground.discord.WebhookManager;
import fft_battleground.exception.CacheBuildException;
import fft_battleground.repo.model.BalanceHistory;
import fft_battleground.repo.repository.BattleGroundCacheEntryRepo;
import fft_battleground.repo.util.BattleGroundCacheEntryKey;
import fft_battleground.reports.model.LeaderboardBalanceData;
import fft_battleground.reports.model.LeaderboardBalanceHistoryEntry;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;

public abstract class AbstractPlaylistHistoryReportGenerator extends AbstractReportGenerator<LeaderboardBalanceData> {

	public static final String LABEL_DATE_FORMAT = "dd-M-yyyy hh";
	public static final int STANDARD_SIZE = 10;
	
	public AbstractPlaylistHistoryReportGenerator(BattleGroundCacheEntryKey key, String reportName,
			BattleGroundCacheEntryRepo battleGroundCacheEntryRepo, WebhookManager errorWebhookManager,
			Timer battleGroundCacheTimer) {
		super(key, reportName, battleGroundCacheEntryRepo, errorWebhookManager, battleGroundCacheTimer);
		// TODO Auto-generated constructor stub
	}

	@Override
	public abstract LeaderboardBalanceData generateReport() throws CacheBuildException;

	@Override
	@SneakyThrows
	public LeaderboardBalanceData deserializeJson(String json) {
		ObjectMapper mapper = new ObjectMapper();
		LeaderboardBalanceData ascensionData = mapper.readValue(json, LeaderboardBalanceData.class);
		return ascensionData;
	}
	
	protected LeaderboardBalanceData createLeaderboardBalanceData(String reportName, PlayerListHistoryReportDTO playerListHistoryReportDTO) {
		List<Pair<LocalDate, Long>> equallySpacedData = this.getEquallySpacedDates(playerListHistoryReportDTO.getDateCounts());
		List<Date> dates = equallySpacedData.stream().map(Pair<LocalDate, Long>::getLeft)
				.map(AbstractPlaylistHistoryReportGenerator::convertLocalDateToDate)
				.collect(Collectors.toList());
		List<BalanceHistory> balanceHistory = equallySpacedData.stream()
				.map(pair -> new BalanceHistory(reportName, pair.getRight().intValue(), convertLocalDateToDate(pair.getLeft())))
				.collect(Collectors.toList());
		List<LeaderboardBalanceHistoryEntry> entry = Collections.singletonList(new LeaderboardBalanceHistoryEntry(reportName, balanceHistory));
		
		int size = balanceHistory.size();
		LeaderboardBalanceData data = new LeaderboardBalanceData(dates, entry, size);
		return data;
	}
	
	private List<Pair<LocalDate, Long>> getEquallySpacedDates(Map<LocalDate, Long> dateCounts) {
		if(dateCounts.keySet().size() <= STANDARD_SIZE) {
			List<Pair<LocalDate, Long>> result = dateCounts.keySet().stream().sorted()
					.map(key -> Pair.of(key, dateCounts.get(key)))
					.collect(Collectors.toList());
			return result;
		}
		
	    // Get the earliest and latest dates from the map
	    LocalDate earliest = dateCounts.keySet().stream().min(LocalDate::compareTo).get();
	    LocalDate latest = dateCounts.keySet().stream().max(LocalDate::compareTo).get();

	    // Calculate the number of days between the earliest and latest dates
	    long numDays = earliest.until(latest, ChronoUnit.DAYS);

	    // Calculate the interval between the dates we want to include in the result
	    long interval = numDays / 10;

	    // Create a list of the 10 equally spaced dates
	    List<LocalDate> dates = IntStream.range(0, 10)
	        .mapToObj(i -> earliest.plusDays(i * interval))
	        .collect(Collectors.toList());

	    // Create a list of pairs containing each date and its corresponding count
	    List<Pair<LocalDate, Long>> result = dates.stream()
	        .map(date -> {
	            // If the date exists in the map, return its count
	            if (dateCounts.containsKey(date)) {
	                return Pair.of(date, dateCounts.get(date));
	            }

	            // If the date doesn't exist in the map, look for the previous day with a count
	            // If no such day is found (i.e. if the date is the earliest date), use 0 as the count
	            LocalDate previousDay = date.minusDays(1);
	            while (!dateCounts.containsKey(previousDay) && !previousDay.equals(earliest)) {
	                previousDay = previousDay.minusDays(1);
	            }
	            long count = dateCounts.containsKey(previousDay) ? dateCounts.get(previousDay) : 0;
	            return Pair.of(date, count);
	        })
	        .collect(Collectors.toList());

	    return result;
	}
	
	private static Date convertLocalDateToDate(LocalDate localDate) {
		Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
		return date;
	}
	
	@Data
	@AllArgsConstructor
	protected static class PlayerListHistoryReportDTO {
		private final Map<LocalDate, Long> dateCounts;
	}

}
