package fft_battleground.bot.detector;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import fft_battleground.bot.model.event.BattleGroundEvent;
import fft_battleground.bot.model.event.PlayerSkillEvent;
import fft_battleground.model.ChatMessage;

public class PlayerSkillDetector implements EventDetector {

	private static final String SEARCH_STRING = ", your skills: ";
	
	@Override
	public BattleGroundEvent detect(ChatMessage message) {
		PlayerSkillEvent event = null;
		
		if(StringUtils.equals(message.getUsername(), "fftbattleground") && StringUtils.contains(message.getMessage(), SEARCH_STRING)) {
			for(String str : StringUtils.split(message.getMessage(), ";")) {
				if(StringUtils.contains(message.getMessage(), SEARCH_STRING)) {
					String player = StringUtils.substringBefore(str, SEARCH_STRING);
					String skillsString = this.getSkillsString(str);
					List<String> skills = new LinkedList<String>();
					for(String singleSkillString : StringUtils.split(skillsString, ",")) {
						skills.add(StringUtils.trim(StringUtils.replace(singleSkillString, ".", "")));
					}
					
					if(skills.size() > 0) {
						event = new PlayerSkillEvent(player, skills);
					}
				}
				
			}
		}
		
		return event;
	}
	
	protected String getSkillsString(String str) {
		String skillsString = StringUtils.substringBetween(str, SEARCH_STRING, "(");
		if(skillsString == null) {
			skillsString = StringUtils.substringAfter(str, SEARCH_STRING);
		}
		
		return skillsString;
	}

}
