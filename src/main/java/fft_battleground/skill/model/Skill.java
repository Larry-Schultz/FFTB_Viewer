package fft_battleground.skill.model;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import fft_battleground.repo.model.PlayerRecord;

public interface Skill {

	String getSkill();
	SkillType getSkillType();
	Integer getCooldown();
	PlayerRecord getPlayer_record();
	
	public static List<String> convertToListOfSkillStrings(Collection<? extends Skill> playerSkills) {
		List<String> skillStrings = playerSkills.parallelStream().map(playerSkill -> playerSkill.getSkill()).collect(Collectors.toList());
		return skillStrings;
	}
}
