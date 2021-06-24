package fft_battleground.event.detector;

import org.apache.commons.lang3.StringUtils;

import fft_battleground.event.EventDetector;
import fft_battleground.event.detector.model.DontFightEvent;
import fft_battleground.model.ChatMessage;

public class DontFightDetector implements EventDetector<DontFightEvent> {
	private static final String SEARCH_STRING = "!dontfight";
	
	@Override
	public DontFightEvent detect(ChatMessage message) {
		DontFightEvent event = null;
		String stringCommand = message.getMessage();
		if(StringUtils.contains(stringCommand, SEARCH_STRING)) {
			String player = StringUtils.lowerCase(message.getUsername());
			String command = message.getMessage();
			event = new DontFightEvent(player, command);
		}
		
		return event;
	}

}
