package fft_battleground.botland.personality;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import fft_battleground.model.BattleGroundTeam;

public class YoloPersonality extends PersonalityModule {

	@Override
	public String personalityString(String botName, Float leftScore, BattleGroundTeam leftTeam, Float rightScore,
			BattleGroundTeam rightTeam, Map<Integer, Integer> percentiles, Integer percentile) {
		StringBuilder builder = new StringBuilder(botName).append(": ");
		builder.append("YOLO, betting on ");
		if(leftScore >= rightScore) {
			builder.append(this.getNameFromTeam(leftTeam));
		} else {
			builder.append(this.getNameFromTeam(rightTeam));
		}
		builder.append("!");
		
		return builder.toString();
	}
	
	public String getNameFromTeam(BattleGroundTeam team) {
		String result = "";
		if(team == BattleGroundTeam.CHAMPION) {
			result = "the Champs";
		} else {
			StringBuilder teamBuilder = new StringBuilder("the ");
			teamBuilder.append(StringUtils.capitalize(team.getProperName())).append(" ").append(StringUtils.capitalize(team.getInformalName()));
			result = teamBuilder.toString();
		}
		
		return result;
	}

}
