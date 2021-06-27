package fft_battleground.botland.personality;

import java.util.Map;

import fft_battleground.model.BattleGroundTeam;

public class FollowerPersonality implements PersonalityModule {

	@Override
	public String personalityString(String botName, Float leftScore, BattleGroundTeam leftTeam, Float rightScore,
			BattleGroundTeam rightTeam, Map<Integer, Integer> percentiles) {
		StringBuilder builder = new StringBuilder(botName).append(": ");
		
		Integer roundedLeftScore = (int) leftScore.floatValue();
		Integer roundedRightScore = (int) rightScore.floatValue();
		
		if(leftScore >= rightScore) {
			builder.append("Betting on ").append(leftTeam.getProperName()).append(" as it is more popular. ")
			.append(roundedLeftScore).append(" vs ").append(roundedRightScore).append(".");
		} else {
			builder.append("Betting on ").append(rightTeam.getProperName()).append(" as it is more popular. ")
			.append(roundedRightScore).append(" vs ").append(roundedLeftScore).append(".");
		}
		
		return builder.toString();
	}

}
