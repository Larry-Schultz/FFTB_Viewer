package fft_battleground.dump.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.math3.stat.descriptive.rank.Median;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fft_battleground.dump.cache.DumpCacheManager;
import fft_battleground.dump.cache.map.LastActiveCache;
import fft_battleground.dump.cache.map.LastFightActiveCache;
import fft_battleground.dump.model.GlobalGilPageData;
import fft_battleground.repo.model.GlobalGilHistory;
import fft_battleground.repo.repository.GlobalGilHistoryRepo;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GlobalGiServiceImpl implements GlobalGilService {

	@Autowired
	private DumpCacheManager dumpCacheManager;

	@Autowired
	private GlobalGilHistoryRepo globalGilHistoryRepo;
	
	@Autowired
	private LastActiveCache lastActiveCache;
	
	@Autowired
	private LastFightActiveCache lastFightActiveCache;
	
	@Override
	public GlobalGilPageData getGlobalGilData() {
		GlobalGilPageData data = null;
		GlobalGilHistory todaysData = this.globalGilHistoryRepo.getFirstGlobalGilHistory();
		List<GlobalGilHistory> historyByDay = this.globalGilHistoryRepo
				.getGlobalGilHistoryByCalendarTimeType(ChronoUnit.DAYS);
		List<GlobalGilHistory> historyByWeek = this.globalGilHistoryRepo
				.getGlobalGilHistoryByCalendarTimeType(ChronoUnit.WEEKS);
		List<GlobalGilHistory> historyByMonth = this.globalGilHistoryRepo
				.getGlobalGilHistoryByCalendarTimeType(ChronoUnit.MONTHS);
		List<GlobalGilHistory> historyByYear = this.globalGilHistoryRepo
				.getGlobalGilHistoryByCalendarTimeType(ChronoUnit.YEARS);

		Median medianCalculator = new Median();
		Date oneMonthAgo = Date.from(LocalDate.now().minus(30, ChronoUnit.DAYS).atStartOfDay(ZoneId.systemDefault()).toInstant());
		double[] activeSortedPlayerBalances=Stream.of(this.lastActiveCache.getMap().entrySet(), this.lastFightActiveCache.getMap().entrySet())
										.flatMap(Set::stream)
										.filter(entry -> entry.getValue().after(oneMonthAgo))
										.filter(entry -> entry.getKey() != null)
										.collect(Collectors.groupingBy(Entry<String, Date>::getKey))
										.keySet().stream()
										.mapToDouble(player -> {
											Integer balance = this.dumpCacheManager.getBalanceFromCache(player);
											return balance != null ? balance.doubleValue() : 0d;
										})
										.sorted().toArray();
		int median = (int) Math.round(medianCalculator.evaluate(activeSortedPlayerBalances));
		int todaysActiveGilCount = (int) Arrays.stream(activeSortedPlayerBalances).sum();
		
		data = new GlobalGilPageData(todaysData, historyByDay, historyByWeek, historyByMonth, historyByYear, median, todaysActiveGilCount);

		return data;
	}

	@Override
	@SneakyThrows
	public Double percentageOfGlobalGil(Integer balance) {
		Double percentage = new Double(0);
		if (balance != null) {
			GlobalGilHistory todaysData = this.globalGilHistoryRepo.getFirstGlobalGilHistory();
			percentage = ((new Double(balance) / new Double(todaysData.getGlobal_gil_count())));
		}
		return percentage;
	}
}
