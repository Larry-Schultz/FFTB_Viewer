package fft_battleground.botland.personality;

import java.util.Map;

import fft_battleground.model.BattleGroundTeam;

public class InversePercentilePersonality extends PersonalityModule {

	@Override
	protected String personalityString(String botName, Float leftScore, BattleGroundTeam leftTeam, Float rightScore,
			BattleGroundTeam rightTeam, Map<Integer, Integer> percentiles, Integer percentile) {
		Integer leftScoreFloor = (int) leftScore.floatValue();
		Integer rightScoreFloor = (int) rightScore.floatValue();
		StringBuilder builder = new StringBuilder("inverse: ");
		if(leftScore >= rightScore) {
			builder.append("picking ").append(rightTeam.getProperName()).append(" as they have the lowest score, ").append(leftScoreFloor.toString())
			.append(" vs ").append(rightScoreFloor.toString());
		} else {
			builder.append("picking ").append(leftTeam.getProperName()).append(" as they have the lowest score, ").append(leftScoreFloor.toString())
			.append(" vs ").append(rightScoreFloor.toString());
		}
		builder.append(". Percentile: ").append(percentile.toString()).append("%.");
		
		return builder.toString();
	}

}
