package fft_battleground.bot.detector;

import org.apache.commons.lang3.StringUtils;

import fft_battleground.bot.model.event.BattleGroundEvent;
import fft_battleground.bot.model.event.LevelUpEvent;
import fft_battleground.model.ChatMessage;

public class LevelUpDetector implements EventDetector {

	private static final String findString = "you advanced to Level ";
	private static final String endCharacter = "!";
	
	@Override
	public BattleGroundEvent detect(ChatMessage message) {
		LevelUpEvent event = null;
		if(StringUtils.equals(StringUtils.lowerCase(message.getUsername()), StringUtils.lowerCase("FFTBattleground")) &&
				StringUtils.contains(message.getMessage(), findString)) {
			String username = StringUtils.substringBefore(message.getMessage(), ",");
			Short level = Short.valueOf(StringUtils.substringBetween(message.getMessage(), findString, endCharacter));
			
			event = new LevelUpEvent(username, level);
		}
		
		return event;
	}

}
