package fft_battleground.bot.detector;

import org.apache.commons.lang3.StringUtils;

import fft_battleground.bot.model.event.BattleGroundEvent;
import fft_battleground.bot.model.event.SkillDropEvent;
import fft_battleground.model.ChatMessage;

public class SkillDropDetector implements EventDetector {

	private static final String SEARCH_STRING = "The current Skill Drop is: ";
	
	@Override
	public BattleGroundEvent detect(ChatMessage message) {
		SkillDropEvent event = null;
		if(StringUtils.equals(message.getUsername(), "fftbattleground") && StringUtils.contains(message.getMessage(), SEARCH_STRING)) {
			for(String str : StringUtils.split(message.getMessage(), ";")) {
				if(StringUtils.contains(message.getMessage(), SEARCH_STRING)) {
					String skill = StringUtils.substringBetween(str, SEARCH_STRING, " ");
					String skillDescription = StringUtils.substringBetween(str, "(", ")");
					
					event = new SkillDropEvent(skill, skillDescription);
					break;
				}
			}
		}
		
		return event;
	}

}
