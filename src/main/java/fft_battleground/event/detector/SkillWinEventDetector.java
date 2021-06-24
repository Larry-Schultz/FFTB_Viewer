package fft_battleground.event.detector;

import org.apache.commons.lang3.StringUtils;

import fft_battleground.event.EventDetector;
import fft_battleground.event.detector.model.SkillWinEvent;
import fft_battleground.model.ChatMessage;

public class SkillWinEventDetector implements EventDetector<SkillWinEvent> {

	private static final String SEARCH_STRING = "! You have been bestowed the ";
	private static final String SEARCH_STRING_2 = " skill free of charge! Additionally, ";
	private static final String SEARCH_STRING_3 = " received it from the subscriber-only pool!";
	
	
	@Override
	public SkillWinEvent detect(ChatMessage message) {
		SkillWinEvent event = null;
		if(StringUtils.equals(message.getUsername(), "fftbattleground") && StringUtils.contains(message.getMessage(), SEARCH_STRING)
				&& StringUtils.contains(message.getMessage(), SEARCH_STRING_2) && StringUtils.contains(message.getMessage(), SEARCH_STRING_3)) {
			for(String split : StringUtils.split(message.getMessage(), ";")) {
				if(StringUtils.contains(split, SEARCH_STRING)
						&& StringUtils.contains(split, SEARCH_STRING_2) && StringUtils.contains(split, SEARCH_STRING_3)) {
					String player1 = StringUtils.substringBetween(split, "Congratulations, ", SEARCH_STRING);
					String skill = StringUtils.substringBetween(split, SEARCH_STRING, SEARCH_STRING_2);
					String player2 = StringUtils.substringBetween(split, SEARCH_STRING_2, SEARCH_STRING_3);
					event = new SkillWinEvent(player1, player2, skill);
				}
			}
		}
		
		return event;
	}

}
