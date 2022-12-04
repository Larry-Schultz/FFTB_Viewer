package fft_battleground.controller.response.model;

import java.util.Date;

import fft_battleground.music.model.Music;
import lombok.Data;

@Data
public class MusicData {
	private String songName;
	private String duration;
	private long occurences;
	private Date mostRecentOccurence;
	private Date dateAdded;
	
	public MusicData(Music music) {
		this.songName = music.getSongName();
		this.duration = this.buildDurationString(music.getMinutes(), music.getSeconds());
		this.occurences = music.getOccurences();
		this.mostRecentOccurence = music.getMostRecentOccurence();
		this.dateAdded = music.getDateAdded();
	}
	
	protected String buildDurationString(String minutes, String seconds) {
		String result = minutes + ":" + seconds;
		return result;
	}
}
