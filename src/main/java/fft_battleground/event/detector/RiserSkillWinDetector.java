package fft_battleground.event.detector;

import org.apache.commons.lang3.StringUtils;

import fft_battleground.event.EventDetector;
import fft_battleground.event.detector.model.RiserSkillWinEvent;
import fft_battleground.model.ChatMessage;
import fft_battleground.util.GambleUtil;

public class RiserSkillWinDetector implements EventDetector<RiserSkillWinEvent> {

	private static final String SEARCH_STRING = ", you learned the skill: ";
	
	@Override
	public RiserSkillWinEvent detect(ChatMessage message) {
		RiserSkillWinEvent event = null;
		if(StringUtils.equalsIgnoreCase(message.getUsername(), "fftbattleground") && StringUtils.contains(message.getMessage(), SEARCH_STRING)) {
			for(String str : StringUtils.split(message.getMessage(), ";")) {
				if(StringUtils.contains(message.getMessage(), SEARCH_STRING)) {
					String name = StringUtils.substringBefore(str, SEARCH_STRING);
					name = GambleUtil.cleanString(name);
					String skill = StringUtils.substringBetween(str, SEARCH_STRING, "!");
					event = new RiserSkillWinEvent(name, skill);
				}
			}
		}
		
		return event;
	}

}
