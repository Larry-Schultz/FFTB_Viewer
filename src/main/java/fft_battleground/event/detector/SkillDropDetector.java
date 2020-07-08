package fft_battleground.event.detector;

import org.apache.commons.lang3.StringUtils;

import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.event.model.SkillDropEvent;
import fft_battleground.model.ChatMessage;

public class SkillDropDetector implements EventDetector {

	private static final String SEARCH_STRING = "The current Skill Drop is: ";
	
	@Override
	public BattleGroundEvent detect(ChatMessage message) {
		SkillDropEvent event = null;
		if(StringUtils.equalsIgnoreCase(message.getUsername(), "fftbattleground") && StringUtils.contains(message.getMessage(), SEARCH_STRING)) {
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
