package fft_battleground.event.detector.model;

import java.util.List;
import java.util.stream.Collectors;

import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.event.model.BattleGroundEventType;
import fft_battleground.event.model.DatabaseResultsData;
import fft_battleground.repo.model.PrestigeSkills;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class PrestigeSkillsEvent
extends BattleGroundEvent
implements DatabaseResultsData {
	private static final BattleGroundEventType type = BattleGroundEventType.PRESTIGE_SKILLS;
	
	private String player;
	private List<PrestigeSkills> playerSkills;

	public PrestigeSkillsEvent(String player, List<String> skills) {
		super(type);
		this.player = player;
		this.playerSkills = skills.stream().map(PrestigeSkills::new).collect(Collectors.toList());
	}
	
	public PrestigeSkillsEvent(String player, String skill) {
		super(type);
		this.player = player;
		this.playerSkills = List.of(new PrestigeSkills(skill));
	}
	
	public List<String> getSkills() {
		List<String> skills = this.playerSkills.parallelStream().map(playerSkills -> playerSkills.getSkill()).collect(Collectors.toList());
		return skills;
	}
}
