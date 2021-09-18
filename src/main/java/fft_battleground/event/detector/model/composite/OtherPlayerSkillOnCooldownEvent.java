package fft_battleground.event.detector.model.composite;

import java.util.List;

import fft_battleground.event.detector.model.SkillOnCooldownEvent;
import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.event.model.BattleGroundEventType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
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
