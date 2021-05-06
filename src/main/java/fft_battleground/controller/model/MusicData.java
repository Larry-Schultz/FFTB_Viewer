package fft_battleground.controller.model;

import fft_battleground.dump.model.Music;

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
