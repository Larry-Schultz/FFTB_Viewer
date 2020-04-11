package fft_battleground.bot.model.event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fft_battleground.bot.model.BattleGroundEventType;
import fft_battleground.bot.model.DatabaseResultsData;
import lombok.Data;

@Data
public class SkillWinEvent 
extends BattleGroundEvent
implements DatabaseResultsData {
	private static final BattleGroundEventType event = BattleGroundEventType.SKILL_WIN;
	
	private List<PlayerSkillEvent> skillEvents;
	
	public SkillWinEvent() {}
	
	public SkillWinEvent(String player1, String player2, String skill) {
		super(event);
		this.skillEvents = new ArrayList<>();
		
		this.skillEvents.add(new PlayerSkillEvent(player1, Arrays.asList(new String[] {skill})));
		this.skillEvents.add(new PlayerSkillEvent(player2, Arrays.asList(new String[] {skill})));
	}

	@Override
	public String toString() {
		return "SkillWinEvent [skillEvents=" + skillEvents + "]";
	}
	

}
