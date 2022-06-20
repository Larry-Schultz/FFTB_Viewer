package fft_battleground.event.detector.model;

import java.util.Set;

import fft_battleground.event.detector.model.fake.ClassBonusEvent;
import fft_battleground.event.detector.model.fake.SkillBonusEvent;
import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.event.model.BattleGroundEventType;
import fft_battleground.event.model.DatabaseResultsData;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class BonusEvent 
extends BattleGroundEvent
implements DatabaseResultsData {
	private static final BattleGroundEventType type = BattleGroundEventType.BONUS;
	
	private ClassBonusEvent classBonusEvent;
	private SkillBonusEvent skillBonusEvent;
	
	public BonusEvent(String player, Set<String> classes, String skill) {
		super(type);
		this.classBonusEvent = new ClassBonusEvent(player, classes);
		this.skillBonusEvent = new SkillBonusEvent(player, Set.of(skill));
	}

}
