package fft_battleground.botland.personality;

import java.util.Map;

import fft_battleground.model.BattleGroundTeam;

public interface PersonalityModule {

	public String personalityString(String botName, Float leftScore, BattleGroundTeam leftTeam, Float rightScore, BattleGroundTeam rightTeam, 
			Map<Integer, Integer> percentiles);
}
