package fft_battleground.event.detector;

import org.apache.commons.lang3.StringUtils;

import fft_battleground.event.EventDetector;
import fft_battleground.event.detector.model.MusicEvent;
import fft_battleground.model.ChatMessage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MusicDetector implements EventDetector<MusicEvent> {

	private static final String SEARCH_STRING = ", the current track is: ";
	private static final String SEARCH_STRING_2 = ". It will play for another ";
	private static final String SEARCH_STRING_3 = " seconds.";
	private static final String AUTO_SEARCH_STRING_1 = "The track is now: ";
	private static final String AUTO_SEARCH_STRING_2 = ". It will play for ";
	
	@Override
	public MusicEvent detect(ChatMessage message) {
		MusicEvent event = null;
		
		if(StringUtils.equals(message.getUsername(), "fftbattleground") && containsASearchString(message.getMessage())) {
			for(String split : StringUtils.split(message.getMessage(), ";")) {
				try {
					String songName = null;
					String durationString = null;
					boolean automatic = false;
					if(StringUtils.contains(split, SEARCH_STRING)) {
						songName = StringUtils.substringBetween(split, SEARCH_STRING, SEARCH_STRING_2);
						durationString = StringUtils.substringBetween(split, SEARCH_STRING_2, SEARCH_STRING_3);
					} else if(StringUtils.contains(split, AUTO_SEARCH_STRING_1)) {
						songName = StringUtils.substringBetween(split, AUTO_SEARCH_STRING_1, AUTO_SEARCH_STRING_2);
						durationString = StringUtils.substringBetween(split, AUTO_SEARCH_STRING_2, SEARCH_STRING_3);
						automatic = true;
					}
					if(songName != null && durationString != null) {
						songName = StringUtils.trim(songName);
						Integer duration = Integer.valueOf(durationString);
						event = new MusicEvent(songName, duration);
						event.setAutomatic(automatic);
						break;
					}
				} catch(NumberFormatException e) {
					log.warn("Number format exception reading the duration time of the current song, not creating event.  String was: {}", split, e);
				}
			}
		}
		
		return event;
	}
	
	private boolean containsASearchString(String message) {
		boolean result = StringUtils.contains(message, SEARCH_STRING) || StringUtils.contains(message, AUTO_SEARCH_STRING_1);
		return result;
	}

}
