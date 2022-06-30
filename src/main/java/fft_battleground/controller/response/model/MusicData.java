package fft_battleground.controller.response.model;

import fft_battleground.music.model.Music;
import lombok.Data;

@Data
public class MusicData {
	private String songName;
	private String duration;
	
	public MusicData(Music music) {
		this.songName = music.getSongName();
		this.duration = this.buildDurationString(music.getMinutes(), music.getSeconds());
	}
	
	protected String buildDurationString(String minutes, String seconds) {
		String result = minutes + ":" + seconds;
		return result;
	}
}
