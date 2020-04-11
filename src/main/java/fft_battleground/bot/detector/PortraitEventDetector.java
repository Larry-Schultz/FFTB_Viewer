package fft_battleground.bot.detector;

import org.apache.commons.lang3.StringUtils;

import fft_battleground.bot.model.event.BattleGroundEvent;
import fft_battleground.bot.model.event.PortraitEvent;
import fft_battleground.model.ChatMessage;

public class PortraitEventDetector implements EventDetector {

	private static final String SEARCH_STRING = ", your Cheer Portrait was successfully set to ";
	
	@Override
	public BattleGroundEvent detect(ChatMessage message) {
		PortraitEvent event = null;
		if(StringUtils.equals(message.getUsername(), "fftbattleground") && StringUtils.contains(message.getMessage(), SEARCH_STRING)) {
			for(String split : StringUtils.split(message.getMessage(), ";")) {
				if(StringUtils.contains(split, SEARCH_STRING)) {
					String player = StringUtils.substringBefore(split, SEARCH_STRING);
					String portrait = StringUtils.substringBetween(split, SEARCH_STRING, ".");
					event = new PortraitEvent(player, portrait);
				}
			}
		}
		return event;
	}

}
