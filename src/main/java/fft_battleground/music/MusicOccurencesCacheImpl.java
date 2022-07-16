package fft_battleground.music;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import fft_battleground.repo.model.MusicListenCount;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@Component
class MusicOccurencesCacheImpl implements MusicOccurencesCache {
	private Date firstOccurence;
	private long totalOccurences = 0;
	private long songsWithOccurencesCount = 0;
	private List<MusicListenCount> occurences;
	private Map<Long, MusicListenCount> occurencesIdView;
	private Map<String, MusicListenCount> songNameView;
	
	public MusicOccurencesCacheImpl() {
		this.occurences = new ArrayList<>();
		this.occurencesIdView = new HashMap<>();
		this.songNameView = new HashMap<>();
	}
	
	@Override
	public MusicListenCount getOccurencesById(Long id) {
		MusicListenCount mlc = this.occurencesIdView.get(id);
		return mlc;
	}
	
	@Override
	public MusicListenCount getOccurencesbySong(String songName) {
		String cleanedSongName = cleanSongNameKey(songName);
		MusicListenCount mlc = this.songNameView.get(cleanedSongName);
		return mlc;
	}
	
	@Override
	public void addNewOccurence(List<MusicListenCount> list) {
		list.forEach(this::addNewOccurence);
		this.updateOccurenceCounts();
	}
	
	private void addNewOccurence(MusicListenCount mlc) {
		this.occurences.add(mlc);
		this.occurencesIdView.put(mlc.getSongId(), mlc);
		this.songNameView.put(mlc.getSong(), mlc);
	}
	
	@Override
	public boolean updateExistingOccurence(Long songId, Long additionalOccurences) {
		boolean success = false;
		MusicListenCount mlc = this.occurencesIdView.get(songId);
		if(mlc != null) {
			Long occurences = mlc.getOccurences() + additionalOccurences;
			mlc.setOccurences(occurences);
			mlc.setUpdateDateTime(Timestamp.from(Instant.now()));
			success = true;
		}
		
		return success;
	}
	
	@Override
	public void updateOccurenceCounts() {
		log.info("Updating music occurence counts.");
		this.firstOccurence = this.calculateFirstOccurence();
		this.totalOccurences = this.calculateTotalOccurences();
		this.songsWithOccurencesCount = this.calculateSongsWithOccurenceCount();
		log.info("Updating music occurence counts complete.");
	}
	
	@Override
	public Date getFirstOccurence() {
		return this.firstOccurence;
	}
	
	@Override
	public long getTotalOccurences() {
		return this.totalOccurences;
	}
	
	@Override
	public long songCountWithOccurences() {
		return this.songsWithOccurencesCount;
	}
	
	private static String cleanSongNameKey(String song) {
		String name = StringUtils.lowerCase(song);
		return name;
	}
	
	private Date calculateFirstOccurence() {
		log.info("Recalculating first music occurence");
		Date firstOccurence = null;
		try {
		firstOccurence = this.occurences.stream()
				.map(MusicListenCount::getCreateDateTime)
				.filter(Objects::nonNull)
				.sorted()
				.findFirst().orElseGet(() -> Timestamp.from(Instant.now()));
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm a z");
		log.info("First music occurence calculation complete, there first occurence is {}", sdf.format(firstOccurence));
		} catch(Exception e) {
			log.error("Exception found while calculating first music occurence", e);
		}
		
		return firstOccurence;
	}
	
	private long calculateTotalOccurences() {
		long result = 0;
		log.info("Recalculating total music occurences");
		try {
			result = this.occurences.stream().mapToLong(MusicListenCount::getOccurences).sum();
			log.info("Total music occurences calculation complete.  Found {} total occurences", result);
		} catch(Exception e) {
			log.error("Exception found while calculating music total occrences", e);
		}
		
		return result;
	}
	
	private long calculateSongsWithOccurenceCount() {
		log.info("Recalculating songs with occurence count");
		long result = 0;
		try {
			result = this.occurences.stream().mapToLong(MusicListenCount::getOccurences).filter(count -> count > 0).count();
			log.info("Song with occrence count calculation complete.  Found {} songs with occurences", result);
		} catch(Exception e) {
			log.error("Exception found while calculating songs with occurences count", e);
		}
		
		return result;
	}
}