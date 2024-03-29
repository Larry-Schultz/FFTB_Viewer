package fft_battleground.event.detector.model;

import fft_battleground.event.model.BattleGroundEventType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class ExpEvent extends LevelUpEvent {
	public static final BattleGroundEventType type = BattleGroundEventType.EXP;
	
	private Short level;
	private Short remainingExp;
	
	public ExpEvent(String player, Short level, Short remainingExp) {
		super(type, player, level);
		this.level = level;
		this.remainingExp = remainingExp;
	}

	@Override
	public String toString() {
		return "ExpEvent [level=" + level + ", remainingExp=" + remainingExp + ", player=" + getPlayer() + "]";
	}


}
