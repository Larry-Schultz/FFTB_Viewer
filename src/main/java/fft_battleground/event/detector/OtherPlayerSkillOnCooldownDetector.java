package fft_battleground.event.detector;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import fft_battleground.event.EventDetector;
import fft_battleground.event.detector.model.OtherPlayerSkillOnCooldownEvent;
import fft_battleground.event.detector.model.SkillOnCooldownEvent;
import fft_battleground.model.ChatMessage;

public class OtherPlayerSkillOnCooldownDetector implements EventDetector<OtherPlayerSkillOnCooldownEvent> {

	private static final String SEARCH_STRING = ", cannot use ";
	private static final String SEARCH_STRING_2 = " again so soon. Use !lastskills to see which skills you have on cooldown and what the penalties are.";
	
	@Override
	public OtherPlayerSkillOnCooldownEvent detect(ChatMessage message) {
		OtherPlayerSkillOnCooldownEvent event = null;
		List<SkillOnCooldownEvent> eventList = new ArrayList<>();
		if(StringUtils.equals(StringUtils.lowerCase(message.getUsername()), StringUtils.lowerCase("FFTBattleground"))) {
			String[] splitStrings = StringUtils.split(message.getMessage(), ";");
			for(String splitString: splitStrings) {
				if(StringUtils.contains(splitString, SEARCH_STRING_2)) {
					SkillOnCooldownEvent newEvent = this.handlePossibleEvent(splitString);
					if(newEvent != null) {
						eventList.add(newEvent);
					}
				}
			}
			
		}
		
		if(eventList.size() > 0) {
			event = new OtherPlayerSkillOnCooldownEvent(eventList);
		}
		
		return event;
	}
	
	public SkillOnCooldownEvent handlePossibleEvent(String possibleMatch) {
		SkillOnCooldownEvent event = null;
		
		String player = StringUtils.trim(StringUtils.lowerCase(StringUtils.substringBefore(possibleMatch, SEARCH_STRING)));
		String skill = StringUtils.trim(StringUtils.substringBetween(possibleMatch, SEARCH_STRING, SEARCH_STRING_2));
		
		if(skill != null) {
			event = new SkillOnCooldownEvent(player, skill);
		}
		
		return event;
	}

}
