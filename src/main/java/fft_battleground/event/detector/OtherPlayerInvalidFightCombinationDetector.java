package fft_battleground.event.detector;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import fft_battleground.event.EventDetector;
import fft_battleground.event.detector.model.InvalidFightEntryCombinationEvent;
import fft_battleground.event.detector.model.composite.OtherPlayerInvalidFightCombinationEvent;
import fft_battleground.model.ChatMessage;

public class OtherPlayerInvalidFightCombinationDetector implements EventDetector<OtherPlayerInvalidFightCombinationEvent> {

	private static final String SEARCH_STRING = ", invalid combination! Can only have one skill and one excluded skill, and exclusions must be prefixed with -.";
	
	@Override
	public OtherPlayerInvalidFightCombinationEvent detect(ChatMessage message) {
		OtherPlayerInvalidFightCombinationEvent event = null;
		List<InvalidFightEntryCombinationEvent> eventList = new ArrayList<>();
		if(StringUtils.equals(StringUtils.lowerCase(message.getUsername()), StringUtils.lowerCase("FFTBattleground"))) {
			String[] splitStrings = StringUtils.split(message.getMessage(), ";");
			for(String splitString: splitStrings) {
				if(StringUtils.contains(splitString, SEARCH_STRING)) {
					InvalidFightEntryCombinationEvent newEvent = this.handlePossibleEvent(splitString);
					if(newEvent != null) {
						eventList.add(newEvent);
					}
				}
			}
			
		}
		
		if(eventList.size() > 0) {
			event = new OtherPlayerInvalidFightCombinationEvent(eventList);
		}
		return event;
	}
	
	public InvalidFightEntryCombinationEvent handlePossibleEvent(String possibleMatch) {
		InvalidFightEntryCombinationEvent event = null;
		
		String player = StringUtils.trim(StringUtils.lowerCase(StringUtils.substringBefore(possibleMatch, SEARCH_STRING)));
		
		event = new InvalidFightEntryCombinationEvent(player);
		
		return event;
	}

}
