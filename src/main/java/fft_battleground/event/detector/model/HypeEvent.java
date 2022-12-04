package fft_battleground.event.detector.model;

import java.util.List;

import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.event.model.BattleGroundEventType;
import fft_battleground.event.model.Hype;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class HypeEvent extends BattleGroundEvent {
	private static final BattleGroundEventType type = BattleGroundEventType.HYPE;
	
	private String player;
	private List<Hype> hypeEmotes;
	
	public HypeEvent(String player, List<Hype> hypeEmotes) {
		super(type);
		
		this.player = player;
		this.hypeEmotes = hypeEmotes;
	}
}
