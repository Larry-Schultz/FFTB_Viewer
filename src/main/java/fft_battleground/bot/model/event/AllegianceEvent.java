package fft_battleground.bot.model.event;

import fft_battleground.bot.model.BattleGroundEventType;
import fft_battleground.bot.model.DatabaseResultsData;
import fft_battleground.model.BattleGroundTeam;
import lombok.Data;

@Data
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
