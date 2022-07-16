package fft_battleground.event.detector;

import org.apache.commons.lang3.StringUtils;

import fft_battleground.event.EventDetector;
import fft_battleground.event.detector.model.PortraitEvent;
import fft_battleground.model.ChatMessage;

public class PortraitEventDetector implements EventDetector<PortraitEvent> {

	private static final String SEARCH_STRING = ", your Cheer Portrait was successfully set to ";
	private static final String SEARCH_STRING_2 = ", your Cheer Portrait is set to ";
	
	@Override
	public PortraitEvent detect(ChatMessage message) {
		PortraitEvent event = null;
		if(StringUtils.equals(message.getUsername(), "fftbattleground") && this.containsASearchString(message.getMessage())) {
			for(String split : StringUtils.split(message.getMessage(), ";")) {
				if(StringUtils.contains(split, SEARCH_STRING)) {
					event = this.parseEvent(split, SEARCH_STRING);
				} else if(StringUtils.contains(split, SEARCH_STRING_2)) {
					event = this.parseEvent(split, SEARCH_STRING_2);
				}
			}
		}
		
		return event;
	}
	
	private PortraitEvent parseEvent(String message, String searchString) {
		String player = StringUtils.substringBefore(message, searchString);
		player = StringUtils.lowerCase(player);
		String portrait = StringUtils.substringBetween(message, searchString, ".");
		PortraitEvent event = new PortraitEvent(player, portrait);
		return event;
	}
	
	private boolean containsASearchString(String message) {
		boolean result = StringUtils.contains(message, SEARCH_STRING) || StringUtils.contains(message, SEARCH_STRING_2);
		return result;
	}

}
