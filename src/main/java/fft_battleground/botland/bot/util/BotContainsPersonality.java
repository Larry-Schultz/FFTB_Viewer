package fft_battleground.botland.bot.util;

import fft_battleground.botland.bot.exception.BotConfigException;

public interface BotContainsPersonality {
	public static final String PERSONALITY_PARAM = "personality";
	public static final String MISSING_PERSONALITY_PARAM_ERROR = "Missing personality";
	
	public default String getPersonalityParam() {
		return PERSONALITY_PARAM;
	}
	
	public default String readPersonalityParam(BotParameterReader reader) throws BotConfigException {
		String result = reader.readRequiredStringParam(PERSONALITY_PARAM, MISSING_PERSONALITY_PARAM_ERROR);
		return result;
	}
	
	public String getPersonalityName();
}
