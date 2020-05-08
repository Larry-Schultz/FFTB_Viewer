package fft_battleground.event.detector;

import org.apache.commons.lang3.StringUtils;

import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.event.model.PrestigeAscensionEvent;
import fft_battleground.model.ChatMessage;

public class PrestigeAscensionDetector implements EventDetector {

	private static final String SEARCH_STRING = ", you close your eyes and strip your flesh away, ascending to a new level of prestige. Your gil floor has been increased by 100G, and you learned the Hidden Skill: ";
	
	@Override
	public BattleGroundEvent detect(ChatMessage message) {
		PrestigeAscensionEvent event = null;
		if(StringUtils.equals(message.getUsername(), "fftbattleground") && StringUtils.contains(message.getMessage(), SEARCH_STRING)) {
			for(String split : StringUtils.split(message.getMessage(), ";")) {
				if(StringUtils.contains(split, SEARCH_STRING)) {
					String player = StringUtils.trim(StringUtils.lowerCase(StringUtils.substringBefore(split, SEARCH_STRING)));
					String skill = StringUtils.substringBetween(split, SEARCH_STRING, "!");
					event = new PrestigeAscensionEvent(player, skill);
				}
			}
		}
		
		return event;
	}

}
