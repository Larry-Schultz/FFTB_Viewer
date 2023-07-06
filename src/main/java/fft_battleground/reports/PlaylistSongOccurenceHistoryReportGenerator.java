package fft_battleground.reports;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import fft_battleground.discord.WebhookManager;
import fft_battleground.exception.CacheBuildException;
import fft_battleground.repo.model.MusicListenCountHistory;
import fft_battleground.repo.repository.BattleGroundCacheEntryRepo;
import fft_battleground.repo.repository.MusicListenCountHistoryRepo;
import fft_battleground.repo.util.BattleGroundCacheEntryKey;
import fft_battleground.reports.model.LeaderboardBalanceData;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PlaylistSongOccurenceHistoryReportGenerator extends AbstractPlaylistHistoryReportGenerator {

	@Autowired
	private MusicListenCountHistoryRepo musicListenCountHistoryRepo;
	
	private static final BattleGroundCacheEntryKey KEY = BattleGroundCacheEntryKey.PLAYLIST_SONG_OCCURENCE_COUNT_HISTORY;
	private static final String REPORT_NAME	= "Playlist Song Occurence Count History";
	
	@Autowired
	public PlaylistSongOccurenceHistoryReportGenerator(BattleGroundCacheEntryRepo battleGroundCacheEntryRepo, 
			WebhookManager errorWebhookManager, Timer battleGroundCacheTimer) {
		super(KEY, REPORT_NAME, battleGroundCacheEntryRepo, errorWebhookManager, battleGroundCacheTimer);
	}

	@Override
	public LeaderboardBalanceData generateReport() throws CacheBuildException {
		List<MusicListenCountHistory> musicListenCountHistory = this.fetchAllMusicListenCountHistory();
		PlayerListHistoryReportDTO dto = this.generateDTO(musicListenCountHistory);
		LeaderboardBalanceData data = super.createLeaderboardBalanceData(REPORT_NAME, dto);
		return data;
	}
	
	@Transactional
	private List<MusicListenCountHistory> fetchAllMusicListenCountHistory() {
		List<MusicListenCountHistory> musicListenCountHistory = this.musicListenCountHistoryRepo.findAll();
		return musicListenCountHistory;
	}
	
	private PlayerListHistoryReportDTO generateDTO(List<MusicListenCountHistory> musicListenCountHistory) {
		Function<MusicListenCountHistory, LocalDate> convertCreationDateToLocalDate = 
				(mlch) -> mlch.getCreateDateTime().toLocalDateTime().toLocalDate();
		Map<LocalDate, Long> dateOccurenceCountMap = musicListenCountHistory.stream()
				.collect(Collectors.toMap(convertCreationDateToLocalDate, MusicListenCountHistory::getOccurences));
		PlayerListHistoryReportDTO dto = new PlayerListHistoryReportDTO(dateOccurenceCountMap);
		return dto;
	}

}
