package fft_battleground.event.detector;

import org.apache.commons.lang3.StringUtils;

import fft_battleground.event.EventDetector;
import fft_battleground.event.detector.model.BattleGroundEvent;
import fft_battleground.event.detector.model.MusicEvent;
import fft_battleground.model.ChatMessage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MusicDetector implements EventDetector<MusicEvent> {

	private static final String SEARCH_STRING = ", the current track is: ";
	private static final String SEARCH_STRING_2 = ". It will play for another ";
	private static final String SEARCH_STRING_3 = " seconds.";
	
	@Override
	public MusicEvent detect(ChatMessage message) {
		MusicEvent event = null;
		
		if(StringUtils.equals(message.getUsername(), "fftbattleground") && StringUtils.contains(message.getMessage(), SEARCH_STRING)) {
			for(String split : StringUtils.split(message.getMessage(), ";")) {
				try {
					if(StringUtils.contains(split, SEARCH_STRING)) {
						String songName = StringUtils.substringBetween(split, SEARCH_STRING, SEARCH_STRING_2);
						songName = StringUtils.trim(songName);
						String durationString = StringUtils.substringBetween(split, SEARCH_STRING_2, SEARCH_STRING_3);
						Integer duration = Integer.valueOf(durationString);
						event = new MusicEvent(songName, duration);
						break;
					}
				} catch(NumberFormatException e) {
					log.warn("Number format exception reading the duration time of the current song, not creating event.  String was: {}", split, e);
				}
			}
		}
		
		return event;
	}

}
