package fft_battleground.event.detector;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.event.model.FightBeginsEvent;
import fft_battleground.model.ChatMessage;

public class FightDetector implements EventDetector {
	private static final Integer THRESHOLD = 10;
	
	private static final String SEARCH_STRING = "You may now !fight to enter the tournament! This tournament's Skill Drop is: ";
	private static final String SEARCH_STRING_2 = ". One random user using !fight (or !dontfight) will receive this skill. Alternately, you can buy the skill for 1,000G.";
	
	@Override
	public BattleGroundEvent detect(ChatMessage message) {
		FightBeginsEvent event = null;
		if(StringUtils.equals(message.getUsername(), "fftbattleground") && StringUtils.contains(message.getMessage(), SEARCH_STRING)) {
			String skillDrop = StringUtils.substringBetween(message.getMessage(), SEARCH_STRING, SEARCH_STRING_2);
			event = new FightBeginsEvent(skillDrop);
		}
		
		return event;
	}
	
	protected long getDifferenceInMinutes(Date previous, Date now) {
		long diffInMillies = Math.abs(now.getTime() - previous.getTime());
	    long diff = TimeUnit.MINUTES.convert(diffInMillies, TimeUnit.MILLISECONDS);
	    
	    return diff;
	}
	
}