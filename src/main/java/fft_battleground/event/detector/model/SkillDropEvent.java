package fft_battleground.event.detector.model;

import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.event.model.BattleGroundEventType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class SkillDropEvent extends BattleGroundEvent {
	private static final BattleGroundEventType type = BattleGroundEventType.SKILL_DROP;
	
	private String skill;
	private String skillDescription;
	
	public SkillDropEvent(String skill, String skillDescription) {
		super(type);
		this.skill = skill;
		this.skillDescription = skillDescription;
	}
	
	public SkillDropEvent(String skillDrop) {
		super(type);
		this.skill = skillDrop;
	}

	@Override
	public String toString() {
		return "SkilldropEvent [skill=" + skill + ", skillDescription=" + skillDescription + "]";
	}

	
}
