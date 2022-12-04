package fft_battleground.repo.repository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import fft_battleground.repo.model.MusicListenCountHistory;
import lombok.SneakyThrows;

@Repository
public interface MusicListenCountHistoryRepo extends JpaRepository<MusicListenCountHistory, String> {

	@Transactional
	public default void updateMusicListenCountHistory(String dateString, long occurences) {
		Optional<MusicListenCountHistory> maybeEntry = this.findById(dateString);
		MusicListenCountHistory entry = null;
		if(maybeEntry.isPresent()) {
			entry = maybeEntry.get();
			entry.setOccurences(occurences);
		} else {
			entry = new MusicListenCountHistory(dateString, occurences);
		}
		
		this.saveAndFlush(entry);
	}
	
	public default void updateTodayMusicListenCountHistory(long occurences) {
		Date todayDate = new Date();
		String todayDateString = this.displayDateWithWebFormat(todayDate);
		this.updateMusicListenCountHistory(todayDateString, occurences);
	}
	
	@SneakyThrows
	public default String displayDateWithWebFormat(Date date) {
		SimpleDateFormat pageFormat = new SimpleDateFormat(MusicListenCountHistory.DISPLAY_FORMAT);
		String newFormat = pageFormat.format(date);
		return newFormat;
	}
}
