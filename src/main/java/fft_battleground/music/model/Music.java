package fft_battleground.music.model;

import lombok.Data;

@Data
public class Music implements Comparable<Music> {
	private String songName;
	private Long id;
	private String minutes;
	private String seconds;
	private Long occurences;
	
	public Music() {}

	public Music(String name, String id, String duration) {
		this.songName = name;
		this.id = Long.valueOf(id);
		
		Integer durationValue = Integer.valueOf(duration);
		if(durationValue != 0) {
			this.minutes = String.format("%02d", durationValue / 60);
			this.seconds = String.format("%02d",durationValue % 60);
		} else {
			this.minutes = String.format("%02d", 0);
			this.seconds = String.format("%02d", 0);
		}
		
	}

	@Override
	public int compareTo(Music arg0) {
		return this.songName.compareTo(arg0.getSongName());
	}
}
