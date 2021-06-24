package fft_battleground.event.detector;

import org.apache.commons.lang3.StringUtils;

import fft_battleground.event.EventDetector;
import fft_battleground.event.detector.model.ExpEvent;
import fft_battleground.model.ChatMessage;

@SuppressWarnings("rawtypes")
public class ExpDetector implements EventDetector<ExpEvent> {
	protected static final String SEARCH_STRING = ". You will Level Up when you gain another ";
	
	private String username;
	
	public ExpDetector(String username) {
		super();
		this.username = username;
	}
	
	@Override
	public ExpEvent detect(ChatMessage message) {
		ExpEvent event = null;
		if(StringUtils.equals(message.getUsername(), "fftbattleground") && StringUtils.contains(message.getMessage(),  this.username) 
				&& StringUtils.contains(message.getMessage(), SEARCH_STRING)) {
			String[] splitBySemicolon = StringUtils.split(message.getMessage(), ";");
			for(String splitString : splitBySemicolon) {
				event = this.parseEvent(splitString);
				if(event != null) {
					break;
				}
			}
		}
		
		return event;
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
