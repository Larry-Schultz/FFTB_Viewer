package fft_battleground.event.detector;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import fft_battleground.event.EventDetector;
import fft_battleground.event.detector.model.BadBetEvent;
import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.model.ChatMessage;

public class BadBetDetector implements EventDetector<BadBetEvent> {

	protected static final String SEARCH_STRING = ", you don't have enough gil to make this bet!";
	protected static final String SEARCH_STRING_2 = ", betting has closed, sorry!";
	
	@Override
	public BadBetEvent detect(ChatMessage message) {
		BadBetEvent event = null;
		List<String> players = new ArrayList<>();
		if(StringUtils.equals(message.getUsername(), "fftbattleground") 
				&& (StringUtils.contains(message.getMessage(), SEARCH_STRING) || StringUtils.contains(message.getMessage(), SEARCH_STRING_2))) {
			String[] splitStr = StringUtils.split(message.getMessage(), ";");
			for(String split : splitStr) {
				if(StringUtils.contains(split, SEARCH_STRING)) {
					String player = StringUtils.trim(StringUtils.substringBefore(split, SEARCH_STRING));
					players.add(player);
				} else if(StringUtils.contains(split, SEARCH_STRING_2)) {
					String player = StringUtils.trim(StringUtils.substringBefore(split, SEARCH_STRING_2));
					players.add(player);
				}
			}
			
			if(players.size() > 0) {
				event = new BadBetEvent(players);
			}
		}
		
		return event;
	}

}
