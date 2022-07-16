package fft_battleground.event.detector.model;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.event.model.BattleGroundEventType;
import lombok.Data;

@Data
public class MusicEvent extends BattleGroundEvent {
	private static final BattleGroundEventType EVENT_TYPE = BattleGroundEventType.MUSIC;
	
	private String songName;
	private Integer durationInSeconds;
	private String id;
	private boolean automatic = false;
	
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

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof MusicEvent)) {
			return false;
		}
		MusicEvent otherEvent = (MusicEvent) obj;
		boolean result = StringUtils.equalsIgnoreCase(this.getSongName(), otherEvent.getSongName());
		return result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(durationInSeconds, id, songName);
		return result;
	}
	
	
}
