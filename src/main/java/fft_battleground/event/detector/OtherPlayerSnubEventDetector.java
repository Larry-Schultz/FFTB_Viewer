package fft_battleground.event.detector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import fft_battleground.event.EventDetector;
import fft_battleground.event.detector.model.SnubEvent;
import fft_battleground.event.detector.model.composite.OtherPlayerSnubEvent;
import fft_battleground.model.ChatMessage;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OtherPlayerSnubEventDetector implements EventDetector<OtherPlayerSnubEvent> {

	private static final String SEARCH_STRING_1 = ", your current snub streak is: ";
	private static final String SEARCH_STRING_2 = ". You're entered into the current tournament at ";
	
	@Override
	public OtherPlayerSnubEvent detect(ChatMessage message) {
		OtherPlayerSnubEvent event = null;
		if(StringUtils.equals(message.getUsername(), "fftbattleground") && StringUtils.contains(message.getMessage(), SEARCH_STRING_1)) {
			List<String> splitStrings = Arrays.stream(StringUtils.split(message.getMessage(), ";")).filter(str -> StringUtils.contains(str, SEARCH_STRING_1)).collect(Collectors.toList());
			List<SnubEvent> snubEvents = new ArrayList<>();
			for(String str: splitStrings) {
				try {
					if(StringUtils.contains(str, SEARCH_STRING_2)) {
						String player = StringUtils.substringBefore(str, SEARCH_STRING_1);
						String snubString = StringUtils.trim(StringUtils.substringBetween(str, SEARCH_STRING_1, SEARCH_STRING_2));
						snubString = StringUtils.replace(snubString, ".", "");
						if(!StringUtils.isNumeric(snubString) ) {
							throw new NumberFormatException("input string: " + snubString + " full string: " + str);
						}
						Integer snub = Integer.valueOf(snubString);
						if(player != null && snub != null) {
							snubEvents.add(new SnubEvent(player, snub));
						}
					} else {
						String player = StringUtils.substringBefore(str, SEARCH_STRING_1);
						String snubString = StringUtils.substringBetween(str, SEARCH_STRING_1, ".");
						snubString = StringUtils.replace(snubString, ".", "");
						if(!StringUtils.isNumeric(snubString) ) {
							throw new NumberFormatException("input string: " + snubString + " full string: " + str);
						}
						Integer snub = Integer.valueOf(snubString);
						if(player != null && snub != null) {
							snubEvents.add(new SnubEvent(player, snub));
						}
					}
				}catch(NumberFormatException e) {
					log.error("Number format exception with parsing snub string", e);
					event = null;
				} 
			}
			
			if(snubEvents != null && snubEvents.size() > 0) {
				event = new OtherPlayerSnubEvent(snubEvents);
			}
		}
		
		return event;
	}

}
