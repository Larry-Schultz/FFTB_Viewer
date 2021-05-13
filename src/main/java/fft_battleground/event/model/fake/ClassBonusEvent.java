package fft_battleground.event.model.fake;

import java.util.Set;

import fft_battleground.botland.model.BattleGroundEventType;
import fft_battleground.botland.model.DatabaseResultsData;
import fft_battleground.event.model.BattleGroundEvent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class ClassBonusEvent 
extends BattleGroundEvent
implements DatabaseResultsData {
	private static final BattleGroundEventType type = BattleGroundEventType.CLASS_BONUS;
	
	private String player;
	private Set<String> classBonuses;
	
	public ClassBonusEvent() {
		super(type);
	}
	
	public ClassBonusEvent(String player, Set<String> classBonuses) {
		super(type);
		this.player = player;
		this.classBonuses = classBonuses;
	}
}
