package fft_battleground.botland.personality;

import java.util.Map;

import fft_battleground.botland.PersonalityModule;
import fft_battleground.model.BattleGroundTeam;

public class FactsPersonality implements PersonalityModule {

	@Override
	public String personalityString(String botName, Float leftScore, BattleGroundTeam leftTeam, Float rightScore,
			BattleGroundTeam rightTeam, Map<Integer, Integer> quantiles) {
		StringBuilder builder = new StringBuilder(botName).append(": ");
		
		
		return builder.toString();
	}

}
