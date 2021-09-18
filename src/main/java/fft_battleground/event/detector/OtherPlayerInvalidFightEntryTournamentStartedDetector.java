package fft_battleground.event.detector;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import fft_battleground.event.EventDetector;
import fft_battleground.event.detector.model.InvalidFightEntryTournamentStarted;
import fft_battleground.event.detector.model.composite.OtherPlayerInvalidFightEntryTournamentStartedEvent;
import fft_battleground.model.ChatMessage;

public class OtherPlayerInvalidFightEntryTournamentStartedDetector
implements EventDetector<OtherPlayerInvalidFightEntryTournamentStartedEvent> {

	private static final String SEARCH_STRING = ", ";
	private static final String SEARCH_STRING_2 = " this tournament already started.";
	
	@Override
	public OtherPlayerInvalidFightEntryTournamentStartedEvent detect(ChatMessage message) {
		OtherPlayerInvalidFightEntryTournamentStartedEvent event = null;
		List<InvalidFightEntryTournamentStarted> eventList = new ArrayList<>();
		if(StringUtils.equals(StringUtils.lowerCase(message.getUsername()), StringUtils.lowerCase("FFTBattleground"))) {
			String[] splitStrings = StringUtils.split(message.getMessage(), ";");
			for(String splitString: splitStrings) {
				if(StringUtils.contains(splitString, SEARCH_STRING_2)) {
					InvalidFightEntryTournamentStarted newEvent = this.handlePossibleEvent(splitString);
					if(newEvent != null) {
						eventList.add(newEvent);
					}
				}
			}
			
		}
		
		if(eventList.size() > 0) {
			event = new OtherPlayerInvalidFightEntryTournamentStartedEvent(eventList);
		}
		
		return event;
	}
	
	public InvalidFightEntryTournamentStarted handlePossibleEvent(String possibleMatch) {
		InvalidFightEntryTournamentStarted event = null;
		
		String player = StringUtils.trim(StringUtils.lowerCase(StringUtils.substringBefore(possibleMatch, SEARCH_STRING)));
		event = new InvalidFightEntryTournamentStarted(player);
		
		return event;
	}
}
