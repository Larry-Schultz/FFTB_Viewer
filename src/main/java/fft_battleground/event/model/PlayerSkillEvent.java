package fft_battleground.event.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import fft_battleground.botland.model.BattleGroundEventType;
import fft_battleground.botland.model.DatabaseResultsData;

import lombok.Data;

@Data
public class PlayerSkillEvent 
extends BattleGroundEvent 
implements DatabaseResultsData {
	private static final BattleGroundEventType type = BattleGroundEventType.PLAYER_SKILL;
	
	private String player;
	private List<String> skills;
	
	public PlayerSkillEvent() {}
	
	public PlayerSkillEvent(String player) {
		super(type);
		this.player = player;
		this.skills = Collections.emptyList();
	}
	
	public PlayerSkillEvent(String player, List<String> skills) {
		super(type);
		this.player = player;
		this.skills = skills;
	}
	
	public PlayerSkillEvent(BattleGroundEventType battleGroundEventtype, String player, List<String> skills) {
		super(battleGroundEventtype);
		this.player = player;
		this.skills = skills;
	}
	
	public PlayerSkillEvent(String player, String skill) {
		super(type);
		this.player = player;
		this.skills = Arrays.asList(new String[] {skill});
	}

	@Override
	public String toString() {
		return "PlayerSkillEvent [player=" + player + ", skills=" + skills + "]";
	}



}
