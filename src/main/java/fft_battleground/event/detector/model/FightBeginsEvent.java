package fft_battleground.event.detector.model;

import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.event.model.BattleGroundEventType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class FightBeginsEvent extends BattleGroundEvent {
	private static final BattleGroundEventType event = BattleGroundEventType.FIGHT_BEGINS;
	
	public String skillDrop;
	
	public FightBeginsEvent(String skillDrop) {
		super(event);
		this.skillDrop = skillDrop;
	}
	
	public SkillDropEvent generateSkillDropEvent() {
		SkillDropEvent event = new SkillDropEvent(this.skillDrop);
		return event;
	}

	@Override
	public String toString() {
		return "FightBeginsEvent [toString()=" + " getEventType()=" + getEventType()
				+ ", hashCode()=" + hashCode() + ", getClass()=" + getClass() + "]";
	}
	
}
