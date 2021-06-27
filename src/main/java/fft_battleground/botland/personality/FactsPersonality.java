package fft_battleground.botland.personality;

import java.util.Map;

import fft_battleground.model.BattleGroundTeam;

public class FactsPersonality implements PersonalityModule {

	@Override
	public String personalityString(String botName, Float leftScore, BattleGroundTeam leftTeam, Float rightScore,
			BattleGroundTeam rightTeam, Map<Integer, Integer> percentiles) {
		Integer roundedLeftScore = (int) leftScore.floatValue();
		Integer roundedRightScore = (int) rightScore.floatValue();
		StringBuilder builder = new StringBuilder(botName).append(": ");
		builder.append(leftTeam.getProperName()).append(" has a score of ").append(roundedLeftScore.toString());
		builder.append(" and ");
		builder.append(rightTeam.getProperName()).append(" has a score of ").append(roundedRightScore.toString());
		builder.append(".");
		
		return builder.toString();
	}

}
