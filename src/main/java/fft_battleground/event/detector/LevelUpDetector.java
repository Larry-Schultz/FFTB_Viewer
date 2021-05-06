package fft_battleground.event.detector;

import org.apache.commons.lang3.StringUtils;

import fft_battleground.event.EventDetector;
import fft_battleground.event.model.LevelUpEvent;
import fft_battleground.event.model.PlayerSkillEvent;
import fft_battleground.model.ChatMessage;

public class LevelUpDetector implements EventDetector<LevelUpEvent> {

	private static final String findString = "you advanced to Level ";
	private static final String endCharacter = "!";
	private static final String findString_2 = " Your Gil Floor has increased to 368! You learned the skill:";
	
	@Override
	public LevelUpEvent detect(ChatMessage message) {
		LevelUpEvent event = null;
		if(StringUtils.equals(StringUtils.lowerCase(message.getUsername()), StringUtils.lowerCase("FFTBattleground")) &&
				StringUtils.contains(message.getMessage(), findString)) {
			String username = StringUtils.substringBefore(message.getMessage(), ",");
			Short level = Short.valueOf(StringUtils.substringBetween(message.getMessage(), findString, endCharacter));
			
			PlayerSkillEvent skillEvent = null;
			if(StringUtils.contains(message.getMessage(), findString_2)) {
				String skill = StringUtils.trim(StringUtils.substringBetween(message.getMessage(), findString_2, endCharacter));
				skillEvent = new PlayerSkillEvent(username, skill);
			}
			
			event = new LevelUpEvent(username, level);
			if(skillEvent != null) {
				event.setSkill(skillEvent);
			}
		}
		
		return event;
	}

}
