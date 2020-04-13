package fft_battleground.event.detector;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.event.model.BuySkillEvent;
import fft_battleground.event.model.PlayerSkillEvent;
import fft_battleground.model.ChatMessage;

public class BuySkillDetector implements EventDetector {
	
	private static final String SEARCH_STRING = ", you successfully bought the ";
	
	@Override
	public BattleGroundEvent detect(ChatMessage message) {
		BuySkillEvent event = null;
		if(StringUtils.equals(message.getUsername(), "fftbattleground") && StringUtils.contains(message.getMessage(), SEARCH_STRING)) {
			for(String str: StringUtils.split(message.getMessage(), ";")) {
				if(StringUtils.contains(message.getMessage(), SEARCH_STRING)) {
					PlayerSkillEvent playerSkillEvent = this.parseSkill(str);
					if(playerSkillEvent != null) {
						if(event == null) {
							event = new BuySkillEvent(); 
							event.setSkillEvents(new ArrayList<>());
						}
						
						event.getSkillEvents().add(playerSkillEvent);
					}
				}
			}
		}
		
		return event;
	}
	
	public PlayerSkillEvent parseSkill(String str) {
		PlayerSkillEvent event = null;
		String player = StringUtils.substringBefore(str, SEARCH_STRING);
		String skill = StringUtils.substringBetween(str, SEARCH_STRING, " skill for");
		event = new PlayerSkillEvent(player, Arrays.asList(new String[] {skill}));
		return event;
	}

}
