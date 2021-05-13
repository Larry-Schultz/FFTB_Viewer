package fft_battleground.event.model.fake;

import java.util.Set;

import fft_battleground.botland.model.BattleGroundEventType;
import fft_battleground.botland.model.DatabaseResultsData;
import fft_battleground.event.model.BattleGroundEvent;
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
