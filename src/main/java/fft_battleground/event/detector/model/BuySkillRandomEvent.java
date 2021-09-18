package fft_battleground.event.detector.model;

import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.event.model.BattleGroundEventType;
import fft_battleground.event.model.DatabaseResultsData;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class BuySkillRandomEvent 
extends BattleGroundEvent 
implements DatabaseResultsData {
	private static final BattleGroundEventType type = BattleGroundEventType.BUY_SKILL_RANDOM;
	
	public Integer skillBuyBalanceUpdate;
	
	private PlayerSkillEvent skillEvent;

	public BuySkillRandomEvent() {
		super(type);
	}
	
	public BuySkillRandomEvent(PlayerSkillEvent skillEvent, Integer cost) {
		super(type);
		this.skillEvent = skillEvent;
		this.skillBuyBalanceUpdate = (-1) * cost;
	}
	
	public String getPlayer() {
		return this.getSkillEvent().getPlayer();
	}

}
