package fft_battleground.botland.personality;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import fft_battleground.model.BattleGroundTeam;

public class SidePersonality extends PersonalityModule {

	private String side;
	
	public SidePersonality(String side) {
		this.side = side;
	}

	@Override
	public String personalityString(String botName, Float leftScore, BattleGroundTeam leftTeam, Float rightScore,
			BattleGroundTeam rightTeam, Map<Integer, Integer> percentiles, Integer percentile) {
		StringBuilder builder = new StringBuilder(botName).append(": ").append("Take my energy ").append(StringUtils.capitalize(side)).append(" team!");
		
		return builder.toString();
	}
}
