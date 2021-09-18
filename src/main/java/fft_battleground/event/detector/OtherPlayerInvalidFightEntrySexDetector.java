package fft_battleground.event.detector;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import fft_battleground.event.EventDetector;
import fft_battleground.event.detector.model.InvalidFightEntrySexEvent;
import fft_battleground.event.detector.model.composite.OtherPlayerInvalidFightEntrySexEvent;
import fft_battleground.model.ChatMessage;

public class OtherPlayerInvalidFightEntrySexDetector implements EventDetector<OtherPlayerInvalidFightEntrySexEvent> {

	private static final String SEARCH_STRING = ", ";
	private static final String SEARCH_STRING_2 = " is not a valid sex.";
	
	@Override
	public OtherPlayerInvalidFightEntrySexEvent detect(ChatMessage message) {
		OtherPlayerInvalidFightEntrySexEvent event = null;
		List<InvalidFightEntrySexEvent> eventList = new ArrayList<>();
		if(StringUtils.equals(StringUtils.lowerCase(message.getUsername()), StringUtils.lowerCase("FFTBattleground"))) {
			String[] splitStrings = StringUtils.split(message.getMessage(), ";");
			for(String splitString: splitStrings) {
				if(StringUtils.contains(splitString, SEARCH_STRING_2)) {
					InvalidFightEntrySexEvent newEvent = this.handlePossibleEvent(splitString);
					if(newEvent != null) {
						eventList.add(newEvent);
					}
				}
			}
			
		}
		
		if(eventList.size() > 0) {
			event = new OtherPlayerInvalidFightEntrySexEvent(eventList);
		}
		
		return event;
	}
	
	public InvalidFightEntrySexEvent handlePossibleEvent(String possibleMatch) {
		InvalidFightEntrySexEvent event = null;
		
		String player = StringUtils.trim(StringUtils.lowerCase(StringUtils.substringBefore(possibleMatch, SEARCH_STRING)));
		String sexName = StringUtils.trim(StringUtils.substringBetween(possibleMatch, SEARCH_STRING, SEARCH_STRING_2));
		
		if(sexName != null) {
			event = new InvalidFightEntrySexEvent(player, sexName);
		}
		
		return event;
	}
}
