package fft_battleground.event.model;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import fft_battleground.bot.model.BattleGroundEventType;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.repo.model.PlayerRecord;
import lombok.Data;

@Data
public class TeamInfoEvent extends BattleGroundEvent {

	private static final BattleGroundEventType type = BattleGroundEventType.TEAM_INFO;
	
	public BattleGroundTeam team;
	public List<Pair<String, String>> playerUnitPairs;
	
	public List<PlayerRecord> metaData;
	
	public TeamInfoEvent(BattleGroundTeam team, List<Pair<String, String>> unitData) {
		super(type);
		this.team = team;
		this.playerUnitPairs = unitData;
	}

	@Override
	public String toString() {
		return "TeamInfoEvent [team=" + team + ", playerUnitPairs=" + playerUnitPairs + "]";
	}

	
}
