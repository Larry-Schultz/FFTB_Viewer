package fft_battleground.event.detector;

import org.apache.commons.lang3.StringUtils;

import fft_battleground.event.EventDetector;
import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.event.model.GiftSkillEvent;
import fft_battleground.model.ChatMessage;

public class GiftSkillDetector implements EventDetector<GiftSkillEvent> {

	private static final String SEARCH_STRING = "Due to a generous donation from ";
	private static final String SEARCH_STRING_2 = " has been bestowed the ";
	private static final String SEARCH_STRING_3 = " skill free of charge!";
	
	@Override
	public GiftSkillEvent detect(ChatMessage message) {
		GiftSkillEvent event = null;
		if(StringUtils.equals(StringUtils.lowerCase(message.getUsername()), StringUtils.lowerCase("FFTBattleground")) &&
				StringUtils.contains(message.getMessage(), SEARCH_STRING)) 
		{
			String[] splitStr = StringUtils.split(message.getMessage(), ";");
			for(String split : splitStr) {
				if(StringUtils.contains(split, SEARCH_STRING)) {
					String givingPlayer = StringUtils.lowerCase(StringUtils.substringBetween(split, SEARCH_STRING, ", "));
					String receivingPlayer = StringUtils.lowerCase(StringUtils.substringBetween(split, ", ", SEARCH_STRING_2));
					String receivedSkill = StringUtils.substringBetween(split, SEARCH_STRING_2, SEARCH_STRING_3);
					if(event == null) {
						event = new GiftSkillEvent(givingPlayer, receivedSkill, receivingPlayer);
					} else {
						event.addAdditionalGift(givingPlayer, receivedSkill, receivingPlayer);
					}
				}
			}
		}
		
		return event;
	}

}
