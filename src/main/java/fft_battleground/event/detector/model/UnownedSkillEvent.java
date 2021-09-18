package fft_battleground.event.detector.model;

import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.event.model.BattleGroundEventType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
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
