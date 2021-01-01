package fft_battleground.event.model;

import fft_battleground.botland.model.BattleGroundEventType;

import lombok.Data;

@Data
public class UnownedSkillEvent extends BattleGroundEvent {
	private static final BattleGroundEventType type = BattleGroundEventType.UNOWNED_SKILL;

	private String player;
	private String skill;
	
	public UnownedSkillEvent() {
		super(type);
	}

	public UnownedSkillEvent(String player, String skill) {
		super(type);
		this.player = player;
		this.skill = skill;
	}
}
