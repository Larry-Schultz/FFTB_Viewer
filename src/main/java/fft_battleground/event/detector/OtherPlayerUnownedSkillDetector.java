package fft_battleground.event.detector;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import fft_battleground.event.EventDetector;
import fft_battleground.event.model.OtherPlayerUnownedSkillEvent;
import fft_battleground.event.model.UnownedSkillEvent;
import fft_battleground.model.ChatMessage;

public class OtherPlayerUnownedSkillDetector implements EventDetector<OtherPlayerUnownedSkillEvent> {

	private static final String SEARCH_STRING = ", you don't own the skill ";
	private static final String SEARCH_STRING_2 = "! Type !skills to see your list of skills.";
	
	@Override
	public OtherPlayerUnownedSkillEvent detect(ChatMessage message) {
		OtherPlayerUnownedSkillEvent event = null;
		List<UnownedSkillEvent> eventList = new ArrayList<>();
		if(StringUtils.equals(StringUtils.lowerCase(message.getUsername()), StringUtils.lowerCase("FFTBattleground"))) {
			String[] splitStrings = StringUtils.split(message.getMessage(), ";");
			for(String splitString: splitStrings) {
				if(StringUtils.contains(splitString, SEARCH_STRING)) {
					UnownedSkillEvent newEvent = this.handlePossibleEvent(splitString);
					if(newEvent != null) {
						eventList.add(newEvent);
					}
				}
			}
			
		}
		
		if(eventList.size() > 0) {
			event = new OtherPlayerUnownedSkillEvent(eventList);
		}
		
		return event;
	}
	
	public UnownedSkillEvent handlePossibleEvent(String possibleMatch) {
		UnownedSkillEvent event = null;
		
		String player = StringUtils.trim(StringUtils.lowerCase(StringUtils.substringBefore(possibleMatch, SEARCH_STRING)));
		String skill = StringUtils.trim(StringUtils.substringBetween(possibleMatch, SEARCH_STRING, SEARCH_STRING_2));
		
		event = new UnownedSkillEvent(player, skill);
		
		return event;
	}

}
