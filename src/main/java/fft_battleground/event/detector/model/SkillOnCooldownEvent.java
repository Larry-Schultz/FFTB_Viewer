package fft_battleground.event.detector.model;

import fft_battleground.event.model.BattleGroundEventType;
import lombok.Data;

@Data
public class SkillOnCooldownEvent extends BattleGroundEvent {
	private static final BattleGroundEventType type = BattleGroundEventType.SKILL_ON_COOLDOWN;
	
	private String player;
	private String skill;
	
	public SkillOnCooldownEvent() {
		super(type);
	}
	
	public SkillOnCooldownEvent(String player, String skill) {
		super(type);
		this.player = player;
		this.skill = skill;
	}

}
