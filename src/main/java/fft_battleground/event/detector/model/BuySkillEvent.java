package fft_battleground.event.detector.model;

import java.util.List;

import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.event.model.BattleGroundEventType;
import fft_battleground.event.model.DatabaseResultsData;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class BuySkillEvent 
extends BattleGroundEvent
implements DatabaseResultsData {
	private static final BattleGroundEventType type = BattleGroundEventType.BUY_SKILL;
	
	public static final Integer skillBuyBalanceUpdate = -1000;
	
	private List<PlayerSkillEvent> skillEvents;

	public BuySkillEvent() {
		super(type);
	}
	
	public BuySkillEvent(List<PlayerSkillEvent> skillEvents) {
		super(type);
		this.skillEvents = skillEvents;
	}

	@Override
	public String toString() {
		return "BuySkillEvent [skillEvents=" + skillEvents + "]";
	}

}
