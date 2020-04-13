package fft_battleground.event.detector;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.event.model.ExpEvent;
import fft_battleground.model.ChatMessage;

public class ExpDetector extends OtherPlayerExpDetector implements EventDetector {
	
	private String username;
	
	public ExpDetector(String username) {
		super();
		this.username = username;
	}
	
	@Override
	public BattleGroundEvent detect(ChatMessage message) {
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


}
