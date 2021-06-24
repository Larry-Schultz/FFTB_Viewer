package fft_battleground.event.detector.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fft_battleground.event.model.BattleGroundEventType;
import fft_battleground.event.model.DatabaseResultsData;
import lombok.Data;

@Data
public class SkillWinEvent 
extends BattleGroundEvent
implements DatabaseResultsData {
	private static final BattleGroundEventType event = BattleGroundEventType.SKILL_WIN;
	
	private List<PlayerSkillEvent> skillEvents;
	
	public SkillWinEvent() {}
	
	public SkillWinEvent(BattleGroundEventType type) {
		super(type);
	}
	
	public SkillWinEvent(String player1, String skill, BattleGroundEventType type) {
		super(type);
		this.skillEvents = new ArrayList<>();
		this.skillEvents.add(this.createEvent(player1, skill));
	}
	
	public SkillWinEvent(String player1, String player2, String skill) {
		super(event);
		this.skillEvents = new ArrayList<>();
		
		this.skillEvents.add(this.createEvent(player1, skill));
		this.skillEvents.add(this.createEvent(player2, skill));
	}
	
	protected PlayerSkillEvent createEvent(String player, String skill) {
		PlayerSkillEvent event = new PlayerSkillEvent(player, Arrays.asList(new String[] {skill}));
		return event;
	}

	@Override
	public String toString() {
		return "SkillWinEvent [skillEvents=" + skillEvents + "]";
	}
	

}
