package fft_battleground.botland.personality;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import fft_battleground.model.BattleGroundTeam;

public class FactsPersonality extends PersonalityModule {

	@Override
	public String personalityString(String botName, Float leftScore, BattleGroundTeam leftTeam, Float rightScore,
			BattleGroundTeam rightTeam, Map<Integer, Integer> percentiles, Integer percentile) {
		Integer roundedLeftScore = (int) leftScore.floatValue();
		Integer roundedRightScore = (int) rightScore.floatValue();
		StringBuilder builder = new StringBuilder(botName).append(": ");
		if(leftScore > rightScore) {
			builder.append("Betting on ").append(StringUtils.capitalize(leftTeam.getProperName())).append(". ");
		} else {
			builder.append("Betting on ").append(rightTeam.getProperName()).append(". ");
		}
		builder.append(StringUtils.capitalize(leftTeam.getProperName())).append(" has a score of ").append(roundedLeftScore.toString());
		builder.append(" and ");
		builder.append(StringUtils.capitalize(rightTeam.getProperName())).append(" has a score of ").append(roundedRightScore.toString());
		builder.append(".");
		
		return builder.toString();
	}

}
