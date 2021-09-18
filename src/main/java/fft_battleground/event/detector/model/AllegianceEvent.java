package fft_battleground.event.detector.model;

import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.event.model.BattleGroundEventType;
import fft_battleground.event.model.DatabaseResultsData;
import fft_battleground.model.BattleGroundTeam;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class AllegianceEvent 
extends BattleGroundEvent 
implements DatabaseResultsData {
	public static final BattleGroundEventType type = BattleGroundEventType.ALLEGIANCE;
	
	public String player;
	public BattleGroundTeam team;

	public AllegianceEvent(String player, BattleGroundTeam team) {
		super(type);
		this.player = player;
		this.team = team;
	}

	@Override
	public String toString() {
		return "AllegianceEvent [player=" + player + ", team=" + team + "]";
	}
}
