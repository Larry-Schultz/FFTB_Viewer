package fft_battleground.event.model;

import fft_battleground.botland.model.BattleGroundEventType;
import lombok.Data;

@Data
public class MusicEvent  extends BattleGroundEvent {
	public BattleGroundEventType type = BattleGroundEventType.MUSIC;
	
	private String songName;
	private Integer durationInSeconds;
	private String id;
	
	public MusicEvent(String name, Integer durationInSeconds) {
		this.songName = name;
		this.durationInSeconds = durationInSeconds;
	}

	@Override
	public String toString() {
		return "MusicEvent [type=" + type + ", songName=" + songName + ", durationInSeconds=" + durationInSeconds
				+ ", id=" + id + "]";
	}
}
