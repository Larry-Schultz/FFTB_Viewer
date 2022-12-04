package fft_battleground.music;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.stereotype.Component;

import fft_battleground.repo.model.MusicListenCount;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@Component
class MusicOccurencesCacheImpl implements MusicOccurencesCache {
	private static final int DISTANCE_LIMIT = 2;
	private Date firstOccurence;
	private long totalOccurences = 0;
	private long songsWithOccurencesCount = 0;
	private List<MusicListenCount> occurences;
	private Map<String, MusicListenCount> songNameView;
	
	public MusicOccurencesCacheImpl() {
		this.occurences = new ArrayList<>();
		this.songNameView = new HashMap<>();
	}
	
	@Override
	public MusicListenCount getOccurencesbySong(String songName) {
		MusicListenCount mlc = this.songNameView.get(songName);
		return mlc;
	}
	
	@Override
	public MusicListenCount getOccurencesByClosestSong(String songName) {
		MusicListenCount mlc = this.getOccurencesbySong(songName);
		if(mlc == null) {
			mlc = this.findClosestMatch(songName);
		}
		
		return mlc;
	}
	
	@Override
	public void addNewOccurence(List<MusicListenCount> list) {
		list.forEach(this::addNewOccurence);
		this.updateOccurenceCounts();
	}
	
	private void addNewOccurence(MusicListenCount mlc) {
		this.occurences.add(mlc);
		this.songNameView.put(mlc.getSong(), mlc);
	}
	
	@Override
	public boolean updateExistingOccurence(String songName, Long additionalOccurences) {
		boolean success = false;
		MusicListenCount mlc = this.getOccurencesbySong(songName);
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
	public long totalOccurences() {
		long count = this.occurences.stream().mapToLong(occurence -> occurence.getOccurences()).sum();
		return count;
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
	
	private MusicListenCount findClosestMatch(String songToFindMatchFor) {
		LevenshteinDistance distanceCalculator = LevenshteinDistance.getDefaultInstance();
		Function<String, Pair<String, Integer>> songDistancePair = song -> Pair.<String, Integer>of(song, distanceCalculator.apply(songToFindMatchFor, song));
		Optional<Pair<String, Integer>> maybeSongDistance = songNameView.keySet().stream().map(songDistancePair)
			.sorted(Comparator.comparingInt(Pair::getRight))
			.filter(pair -> pair.getRight() <= DISTANCE_LIMIT)
			.findFirst();
		MusicListenCount musicListenCount = null;
		if(maybeSongDistance.isPresent()) {
			String songKey = maybeSongDistance.get().getKey();
			musicListenCount = this.songNameView.get(songKey);
			log.info("Closest song for {} was {}", songToFindMatchFor, songKey);
		} else {
			log.warn("No song match found for \"{}\" in occurences cache", songToFindMatchFor);
		}
		
		return musicListenCount;
	}

}