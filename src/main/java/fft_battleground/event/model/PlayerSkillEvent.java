package fft_battleground.event.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import fft_battleground.botland.model.BattleGroundEventType;
import fft_battleground.botland.model.DatabaseResultsData;
import fft_battleground.repo.model.PlayerSkills;

import lombok.Data;

@Data
public class PlayerSkillEvent 
extends BattleGroundEvent 
implements DatabaseResultsData {
	private static final BattleGroundEventType type = BattleGroundEventType.PLAYER_SKILL;
	
	private String player;
	//private List<String> skills;
	private List<PlayerSkills> playerSkills;
	
	public PlayerSkillEvent() {}
	
	public PlayerSkillEvent(String player) {
		super(type);
		this.player = player;
		this.playerSkills = new ArrayList<>();
	}
	
	public PlayerSkillEvent(String player, List<String> skills) {
		super(type);
		this.player = player;
		this.playerSkills = new ArrayList<>(skills.parallelStream().map(skillName -> new PlayerSkills(skillName)).collect(Collectors.toList()));
	}
	
	public PlayerSkillEvent(BattleGroundEventType battleGroundEventtype, String player, List<String> skills) {
		super(battleGroundEventtype);
		this.player = player;
		this.playerSkills = new ArrayList<>(skills.parallelStream().map(skillName -> new PlayerSkills(skillName)).collect(Collectors.toList()));
	}
	
	public PlayerSkillEvent(String player, String skill) {
		super(type);
		this.player = player;
		this.playerSkills = new ArrayList<>(Arrays.asList(new PlayerSkills[] {new PlayerSkills(skill)}));
	}
	
	public PlayerSkillEvent(String player, String skill, int cooldown) {
		super(type);
		this.player = player;
		this.playerSkills = new ArrayList<>(Arrays.asList(new PlayerSkills[] {new PlayerSkills(skill, cooldown)}));
	}
	
	public PlayerSkillEvent(List<PlayerSkills> playerSkills, String player) {
		super(type);
		this.player = player;
		this.playerSkills = playerSkills;
	}
	
	public List<String> getSkills() {
		List<String> skills = this.playerSkills.parallelStream().map(playerSkills -> playerSkills.getSkill()).collect(Collectors.toList());
		return skills;
	}



}
