package fft_battleground.event.detector;

import org.apache.commons.lang3.StringUtils;

import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.event.model.MusicEvent;
import fft_battleground.model.ChatMessage;

public class MusicDetector implements EventDetector {

	private static final String SEARCH_STRING = ", The current track is: ";
	private static final String SEARCH_STRING_2 = ". It will play for another ";
	private static final String SEARCH_STRING_3 = " seconds.";
	
	@Override
	public BattleGroundEvent detect(ChatMessage message) {
		MusicEvent event = null;
		
		if(StringUtils.equals(message.getUsername(), "fftbattleground") && StringUtils.contains(message.getMessage(), SEARCH_STRING)) {
			for(String split : StringUtils.split(message.getMessage(), ";")) {
				if(StringUtils.contains(split, SEARCH_STRING)) {
					String songName = StringUtils.substringBetween(split, SEARCH_STRING, SEARCH_STRING_2);
					songName = StringUtils.trim(songName);
					String durationString = StringUtils.substringBetween(split, SEARCH_STRING_2, SEARCH_STRING_3);
					Integer duration = Integer.valueOf(durationString);
					event = new MusicEvent(songName, duration);
					break;
				}
			}
		}
		
		return event;
	}

}
