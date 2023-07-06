package fft_battleground.reports;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import fft_battleground.discord.WebhookManager;
import fft_battleground.exception.CacheBuildException;
import fft_battleground.repo.model.MusicListenCount;
import fft_battleground.repo.model.MusicListenCountHistory;
import fft_battleground.repo.repository.BattleGroundCacheEntryRepo;
import fft_battleground.repo.repository.MusicListenCountRepo;
import fft_battleground.repo.util.BattleGroundCacheEntryKey;
import fft_battleground.reports.model.LeaderboardBalanceData;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PlaylistSongCountHistoryReportGenerator extends AbstractPlaylistHistoryReportGenerator {
	
	private static final BattleGroundCacheEntryKey KEY = BattleGroundCacheEntryKey.PLAYLIST_SONG_COUNT_HISTORY;
	private static final String REPORT_NAME = "Playlist Song Count History";
	
	@Autowired
	private MusicListenCountRepo musicListenCountRepo;
	
	@Autowired
	public PlaylistSongCountHistoryReportGenerator(BattleGroundCacheEntryRepo battleGroundCacheEntryRepo, 
			WebhookManager errorWebhookManager, Timer battleGroundCacheTimer) {
		super(KEY, REPORT_NAME, battleGroundCacheEntryRepo, errorWebhookManager, battleGroundCacheTimer);
		// TODO Auto-generated constructor stub
	}

	@Override
	
	public LeaderboardBalanceData generateReport() throws CacheBuildException {
		List<MusicListenCount> mlc = this.fetchMusicListenCounts();
		Map<LocalDate, Long> dateCountMap = this.generateDateCountMap(mlc);
		PlayerListHistoryReportDTO dto = this.generatePlayerListHistoryReportDTO(dateCountMap);
		LeaderboardBalanceData data = super.createLeaderboardBalanceData(REPORT_NAME, dto);
		return data;
	}
	
	@SneakyThrows
	public String displayDateWithWebFormat(Date date) {
		SimpleDateFormat pageFormat = new SimpleDateFormat(MusicListenCountHistory.DISPLAY_FORMAT);
		String newFormat = pageFormat.format(date);
		return newFormat;
	}
	
	@Transactional
	private List<MusicListenCount> fetchMusicListenCounts() {
		List<MusicListenCount> musicListenCounts = this.musicListenCountRepo.findAll();
		return musicListenCounts;
	}
	
	private Map<LocalDate, Long> generateDateCountMap(List<MusicListenCount> musicListenCounts) {
		Function<MusicListenCount, LocalDate> creationDateStringFunction = mlc -> mlc.getCreateDateTime().toLocalDateTime().toLocalDate();
		Map<LocalDate, Long> createDateStringSongCountMap = musicListenCounts.stream()
				.collect(Collectors.groupingBy(creationDateStringFunction, Collectors.counting()));
		return createDateStringSongCountMap;
	}
	
	private PlayerListHistoryReportDTO generatePlayerListHistoryReportDTO(Map<LocalDate, Long> countPerDayMap) {
		List<LocalDate> orderedDates = countPerDayMap.keySet().stream().sorted().collect(Collectors.toList());
		Map<LocalDate, Long> totalCountUpToDateMap = new TreeMap<>();
		long currentCount = 0;
		for(int i = 0; i < orderedDates.size(); i++) {
			LocalDate currentDate = orderedDates.get(i);
			currentCount += countPerDayMap.get(currentDate);
			totalCountUpToDateMap.put(currentDate, currentCount);
		}
		
		PlayerListHistoryReportDTO dto = new PlayerListHistoryReportDTO(totalCountUpToDateMap);
		
		return dto;
	}

}
