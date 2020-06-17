package fft_battleground.event.model;

import fft_battleground.botland.model.BattleGroundEventType;
import lombok.Data;

@Data
public class MusicEvent  extends BattleGroundEvent {
	private static final BattleGroundEventType EVENT_TYPE = BattleGroundEventType.MUSIC;
	
	private String songName;
	private Integer durationInSeconds;
	private String id;
	
	public MusicEvent(String name, Integer durationInSeconds) {
		super(EVENT_TYPE);
		this.songName = name;
		this.durationInSeconds = durationInSeconds;
	}

	@Override
	public String toString() {
		return "MusicEvent [songName=" + songName + ", durationInSeconds=" + durationInSeconds
				+ ", id=" + id + "]";
	}
}
