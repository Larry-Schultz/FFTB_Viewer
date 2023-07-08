package fft_battleground.event.detector;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import fft_battleground.event.EventDetector;
import fft_battleground.event.detector.model.FightBeginsEvent;
import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.model.ChatMessage;

public class FightBeginsDetector implements EventDetector<FightBeginsEvent> {
	private static final Integer THRESHOLD = 10;
	
	private static final String SEARCH_STRING = "The !fight command can now be used to enter the upcoming tournament! This tournament's Skill Drop is: ";
	private static final String SEARCH_STRING_2 = ". At least one random entrant will receive this skill free of charge from the Skill Drop raffle, while other users can purchase it for 1,000G.";
	
	@Override
	public FightBeginsEvent detect(ChatMessage message) {
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