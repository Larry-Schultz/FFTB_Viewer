package fft_battleground.music;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.stereotype.Component;

import fft_battleground.music.model.Music;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@Component
public class MusicSongNameMapImpl implements MusicSongNameMap {
	private Map<String, Music> songMusicView;
	
	public MusicSongNameMapImpl() {
		this.songMusicView = new HashMap<>();
	}
	
	@Override
	public Long getMusicIdBySong(String song) {
		Long id = null;
		String cleanedMusicKey = cleanSongNameKey(song);
		Music music = this.songMusicView.get(cleanedMusicKey);
		if(music != null) {
			id = music.getId();
		} else {
			music = this.findClosestMatch(song);
			id = music != null ? music.getId() : null;
		}
		return id;
	}
	
	@Override
	public void refreshCache(Collection<Music> dumpMusicData) {
		for(Music music: dumpMusicData) {
			String cleanedMusicKey = cleanSongNameKey(music);
			if(!this.songMusicView.containsKey(cleanedMusicKey)) {
				this.songMusicView.put(cleanedMusicKey, music);
			}
		}
	}
	
	private Music findClosestMatch(String songToFindMatchFor) {
		LevenshteinDistance distanceCalculator = LevenshteinDistance.getDefaultInstance();
		Function<String, Pair<String, Integer>> songDistancePair = song -> Pair.<String, Integer>of(song, distanceCalculator.apply(songToFindMatchFor, song));
		String songKey = songMusicView.keySet().stream().map(songDistancePair)
			.sorted(Comparator.comparingInt(Pair::getRight)).findFirst().get().getLeft();
		Music music = this.songMusicView.get(songKey);
		log.info("Closest song for {} was {}", songToFindMatchFor, songKey);
		return music;
	}
	
	private static String cleanSongNameKey(Music music) {
		String name = cleanSongNameKey(music.getSongName());
		return name;
	}
	
	private static String cleanSongNameKey(String song) {
		String name = StringUtils.lowerCase(song);
		return name;
	}
}