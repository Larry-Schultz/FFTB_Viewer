package fft_battleground.event.model;

import fft_battleground.botland.model.BattleGroundEventType;

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
