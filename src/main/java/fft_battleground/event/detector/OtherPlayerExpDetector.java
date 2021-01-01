package fft_battleground.event.detector;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import fft_battleground.event.EventDetector;
import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.event.model.ExpEvent;
import fft_battleground.event.model.OtherPlayerExpEvent;
import fft_battleground.model.ChatMessage;

public class OtherPlayerExpDetector implements EventDetector<OtherPlayerExpEvent> {

	protected static final String SEARCH_STRING = ". You will Level Up when you gain another ";
	
	@Override
	public OtherPlayerExpEvent detect(ChatMessage message) {
		OtherPlayerExpEvent event = null;
		if(StringUtils.equals(message.getUsername(), "fftbattleground") && StringUtils.contains(message.getMessage(), SEARCH_STRING)) {
			List<ExpEvent> events = this.getEvents(message);
			if(events.size() > 0) {
				event = new OtherPlayerExpEvent(events);
			}
		}
		
		return event;
	}
	
	protected List<ExpEvent> getEvents(ChatMessage message) {
		List<ExpEvent> events = new ArrayList<>();
		String[] splitBySemicolon = StringUtils.split(message.getMessage(), ";");
		for(String splitString : splitBySemicolon) {
			ExpEvent event = this.parseEvent(splitString);
			if(event != null) {
				events.add(event);
			}
		}
		
		return events;
	}
	
	protected ExpEvent parseEvent(String splitString) {
		ExpEvent event = null;
		if(StringUtils.contains(splitString, SEARCH_STRING)) {
			String player = StringUtils.substringBefore(splitString, ",");
			String levelString = StringUtils.substringAfter(StringUtils.substringBefore(splitString, SEARCH_STRING), "Level ");
			String remainingExpString = StringUtils.substringBefore(StringUtils.substringAfter(splitString, SEARCH_STRING), " EXP.");
			if(StringUtils.isNumeric(levelString) && StringUtils.isNumeric(remainingExpString)) {
				Short level = this.parseLevel(levelString);
				Short remainingExp = this.parseRemainingExp(remainingExpString);
				event = new ExpEvent(player, level, remainingExp);
			}
		}
		
		return event;
	}
	
	protected Short parseLevel(String str) {
		Short level = Short.valueOf(str);
		return level;
	}
	
	protected Short parseRemainingExp(String str) {
		Short remainingExp = Short.valueOf(str);
		return remainingExp;
	}

}
