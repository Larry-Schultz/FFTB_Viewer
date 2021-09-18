package fft_battleground.event.detector.model.fake;

import java.util.Set;

import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.event.model.BattleGroundEventType;
import fft_battleground.event.model.DatabaseResultsData;
import lombok.Data;

@Data
public class SkillBonusEvent 
extends BattleGroundEvent 
implements DatabaseResultsData {
	private static final BattleGroundEventType type = BattleGroundEventType.SKILL_BONUS;

	private String player;
	private Set<String> skillBonuses;
	
	public SkillBonusEvent() {
		super(type);
	}
	
	public SkillBonusEvent(String player, Set<String> skillBonuses) {
		super(type);
		this.player = player;
		this.skillBonuses = skillBonuses;
	}
}
