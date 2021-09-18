package fft_battleground.event.detector.model.composite;

import java.util.List;

import fft_battleground.event.detector.model.InvalidFightEntryTournamentStarted;
import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.event.model.BattleGroundEventType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class OtherPlayerInvalidFightEntryTournamentStartedEvent extends BattleGroundEvent {
	private static final BattleGroundEventType type = BattleGroundEventType.OTHER_PLAYER_INVALID_FIGHT_ENTRY_TOURNAMENT_STARTED;
	
	private List<InvalidFightEntryTournamentStarted> events;
	
	public OtherPlayerInvalidFightEntryTournamentStartedEvent() {
		super(type);
	}
	
	public OtherPlayerInvalidFightEntryTournamentStartedEvent(List<InvalidFightEntryTournamentStarted> events) {
		super(type);
		this.events = events;
	}
}
