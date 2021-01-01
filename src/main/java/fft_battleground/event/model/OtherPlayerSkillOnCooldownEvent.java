package fft_battleground.event.model;

import java.util.List;

import fft_battleground.botland.model.BattleGroundEventType;
import lombok.Data;

@Data
public class OtherPlayerSkillOnCooldownEvent extends BattleGroundEvent {
	private static final BattleGroundEventType type = BattleGroundEventType.OTHER_PLAYER_SKILL_ON_COOLDOWN;
	
	private List<SkillOnCooldownEvent> events;
	
	public OtherPlayerSkillOnCooldownEvent() {
		super(type);
	}
	
	public OtherPlayerSkillOnCooldownEvent(List<SkillOnCooldownEvent> events) {
		super(type);
		this.events = events;
	}
}
