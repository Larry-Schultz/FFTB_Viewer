package fft_battleground.event.detector;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

import fft_battleground.event.EventDetector;
import fft_battleground.event.detector.model.BuySkillRandomEvent;
import fft_battleground.event.detector.model.PlayerSkillEvent;
import fft_battleground.model.ChatMessage;

public class BuySkillRandomDetector implements EventDetector<BuySkillRandomEvent> {

	private static final String SEARCH_STRING = ", you rolled the dice and bought the ";
	private static final String SEARCH_STRING_2 = " skill for ";
	private static final String SEARCH_STRING_3 = ". Your new balance is ";
	
	@Override
	public BuySkillRandomEvent detect(ChatMessage message) {
		BuySkillRandomEvent event = null;
		if(StringUtils.equals(message.getUsername(), "fftbattleground") && StringUtils.contains(message.getMessage(), SEARCH_STRING)) {
			for(String str: StringUtils.split(message.getMessage(), ";")) {
				if(StringUtils.contains(message.getMessage(), SEARCH_STRING)) {
					PlayerSkillEvent playerSkillEvent = this.parseSkill(str);
					String costStr = StringUtils.substringBetween(str, SEARCH_STRING_2, SEARCH_STRING_3);
					costStr = StringUtils.replace(costStr, ",", "");
					costStr = StringUtils.replace(costStr, "G", "");
					Integer cost = Integer.valueOf(costStr);
					
					event = new BuySkillRandomEvent(playerSkillEvent, cost);
				}
			}
		}
		
		return event;
	}
	
	public PlayerSkillEvent parseSkill(String str) {
		PlayerSkillEvent event = null;
		String player = StringUtils.substringBefore(str, SEARCH_STRING);
		String skill = StringUtils.substringBetween(str, SEARCH_STRING, SEARCH_STRING_2);
		event = new PlayerSkillEvent(player, Arrays.asList(new String[] {skill}));
		return event;
	}

}
