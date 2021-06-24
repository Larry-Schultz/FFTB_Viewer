package fft_battleground.event.detector;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import fft_battleground.event.EventDetector;
import fft_battleground.event.detector.model.InvalidFightEntryClassEvent;
import fft_battleground.event.detector.model.OtherPlayerInvalidFightEntryClassEvent;
import fft_battleground.model.ChatMessage;

public class OtherPlayerInvalidFightEntryClassDetector implements EventDetector<OtherPlayerInvalidFightEntryClassEvent> {

	private static final String SEARCH_STRING = ", ";
	private static final String SEARCH_STRING_2 = " is not a valid Human or Monster class. Use !classes for a list.";
	
	@Override
	public OtherPlayerInvalidFightEntryClassEvent detect(ChatMessage message) {
		OtherPlayerInvalidFightEntryClassEvent event = null;
		List<InvalidFightEntryClassEvent> eventList = new ArrayList<>();
		if(StringUtils.equals(StringUtils.lowerCase(message.getUsername()), StringUtils.lowerCase("FFTBattleground"))) {
			String[] splitStrings = StringUtils.split(message.getMessage(), ";");
			for(String splitString: splitStrings) {
				if(StringUtils.contains(splitString, SEARCH_STRING_2)) {
					InvalidFightEntryClassEvent newEvent = this.handlePossibleEvent(splitString);
					if(newEvent != null) {
						eventList.add(newEvent);
					}
				}
			}
			
		}
		
		if(eventList.size() > 0) {
			event = new OtherPlayerInvalidFightEntryClassEvent(eventList);
		}
		
		return event;
	}
	
	public InvalidFightEntryClassEvent handlePossibleEvent(String possibleMatch) {
		InvalidFightEntryClassEvent event = null;
		
		String player = StringUtils.trim(StringUtils.lowerCase(StringUtils.substringBefore(possibleMatch, SEARCH_STRING)));
		String className = StringUtils.trim(StringUtils.substringBetween(possibleMatch, SEARCH_STRING, SEARCH_STRING_2));
		
		if(className != null) {
			event = new InvalidFightEntryClassEvent(player, className);
		}
		
		return event;
	}
}
