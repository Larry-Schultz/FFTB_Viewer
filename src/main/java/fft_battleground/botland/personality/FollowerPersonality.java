package fft_battleground.botland.personality;

import java.util.Map;

import fft_battleground.model.BattleGroundTeam;

public class FollowerPersonality extends PersonalityModule {

	public static String BETS = "bets";
	public static String MONEY = "money";
	
	private String mode;
	
	public FollowerPersonality() {
		this.mode = BETS;
	}
	
	public FollowerPersonality(String mode) {
		this.mode = mode;
	}
	
	@Override
	public String personalityString(String botName, Float leftScore, BattleGroundTeam leftTeam, Float rightScore,
			BattleGroundTeam rightTeam, Map<Integer, Integer> percentiles, Integer percentile) {
		StringBuilder builder = new StringBuilder(botName).append(": ");
		
		Integer roundedLeftScore = (int) leftScore.floatValue();
		Integer roundedRightScore = (int) rightScore.floatValue();
		
		if(leftScore > rightScore) {
			builder.append("Betting on ").append(leftTeam.getProperName()).append(this.getModeString())
			.append(roundedLeftScore).append(" vs ").append(roundedRightScore).append(" ").append(this.scoretype())
			.append(".");
		} else if(leftScore < rightScore) {
			builder.append("Betting on ").append(rightTeam.getProperName()).append(this.getModeString())
			.append(roundedRightScore).append(" vs ").append(roundedLeftScore).append(" ").append(this.scoretype())
			.append(".");
		} else {
			builder.append("Betting on ").append(leftTeam.getProperName()).append(" as there is a tie in regards to ")
			.append(this.scoretype()).append(" ").append(roundedLeftScore).append(" vs ").append(roundedRightScore)
			.append(".");
		}
		
		return builder.toString();
	}
	
	public String getModeString() {
		String result = null;
		if(this.mode == BETS) {
			result = " as it is more popular. ";
		} else if(this.mode == MONEY) {
			result = " since it has more gil. ";
		}
		
		return result;
	}
	
	public String scoretype() {
		String result = null;
		if(this.mode == BETS) {
			result = "bets";
		} else if(this.mode == MONEY) {
			result = "gil";
		}
		
		return result;
	}

}
