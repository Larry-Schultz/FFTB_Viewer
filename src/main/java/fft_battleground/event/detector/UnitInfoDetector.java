package fft_battleground.event.detector;

import org.apache.commons.lang3.StringUtils;

import fft_battleground.event.EventDetector;
import fft_battleground.event.model.UnitInfoEvent;
import fft_battleground.model.ChatMessage;

public class UnitInfoDetector implements EventDetector<UnitInfoEvent> {

	private static final String ANTI_SEARCH_STRING = "your last three skill";
	
	@Override
	public UnitInfoEvent detect(ChatMessage message) {
		UnitInfoEvent event = null;
		if(StringUtils.contains(message.getUsername(), "fftbattleground") && !StringUtils.contains(message.getMessage(), ANTI_SEARCH_STRING) && 
				StringUtils.countMatches(message.getMessage(), "-") >= 4) {
			String username = StringUtils.substringBefore(message.getMessage(), " -");
			String unitInfoString = message.getMessage();
			event = new UnitInfoEvent(username, unitInfoString);
		}
		// TODO Auto-generated method stub
		return event;
	}

}
