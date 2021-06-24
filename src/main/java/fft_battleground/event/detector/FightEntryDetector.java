package fft_battleground.event.detector;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import fft_battleground.event.EventDetector;
import fft_battleground.event.detector.model.FightEntryEvent;
import fft_battleground.model.ChatMessage;
import fft_battleground.model.Gender;

public class FightEntryDetector implements EventDetector<FightEntryEvent> {
	private static final String SEARCH_STRING = "!fight";
			
	public FightEntryDetector() {
		
	}
	
	@Override
	public FightEntryEvent detect(ChatMessage message) {
		FightEntryEvent event = null;
		String stringCommand = message.getMessage();
		if(StringUtils.contains(stringCommand, SEARCH_STRING) && StringUtils.startsWith(stringCommand, SEARCH_STRING)) {
			String player = StringUtils.lowerCase(message.getUsername());
			event = this.parseEvent(stringCommand, player);
		}
		
		return event;
	}
	
	/*
	 * Possible command string combinations
	 * !fight
	 * !fight UltimaDemon
	 * !fight UltimateDemon ExpBoost
	 * !fight Squire
	 * !fight Squire Female
	 * !fight Squire Female Dance
	 * !fight Squire Female Dance - PunchArt 
	 * !fight Squire Female Dance -PunchArt
	 * 
	 *  lets not check correctness, let the stream bot handle that
	 *  
	 *  first token is always !fight
	 *  second token is always class
	 */
	protected FightEntryEvent parseEvent(String command, String player) {
		FightEntryEvent event = null;
		
		List<String> commandTokens = Arrays.asList(StringUtils.split(command, " "));
		
		String className = this.getClassName(commandTokens);
		Gender gender = this.getGender(commandTokens); //we can use this, because the only time we would ever get a gender error is during !fight
		String skill = this.getSkill(commandTokens, gender);
		String exclusionSkill = this.getExclusionSkill(commandTokens, gender);
		
		event = new FightEntryEvent(command, player, className, gender, skill, exclusionSkill);
		
		return event;
	}
	
	protected String getClassName(List<String> tokens) {
		if(tokens.size() < 2) {
			return null;
		}
		
		String className = tokens.get(1);
		
		return className;
	}
	
	protected Gender getGender(List<String> tokens) {
		Gender result = null;
		if(tokens.size() < 3) {
			result = null;
		} else {
			String possibleGenderString = tokens.get(2);
			if(Gender.isGender(possibleGenderString)) {
				result = Gender.getGenderFromString(possibleGenderString);
			}
		}
		
		return result;
	}
	
	protected String getSkill(List<String> tokens, Gender gender) {
		String skill = null;
		
		//try the 3rd and 4th string for skills
		if(tokens.size() < 3) {
			skill = null;
		} else if(tokens.size() >= 3 && (gender == Gender.MONSTER || gender == null)) {
			//if there is no gender, the skill should be in the 3rd slot
			//(!fight, monster, skill, -, exclusion)
			//  0         1      2     3   4
			skill = tokens.get(2);
			
		} else if(tokens.size() >= 4 && (gender == Gender.MALE || gender == Gender.FEMALE)) {
			//if there is a gender, the skill should be in the 4th slot
			//(!fight, class, gender, skill, -, exclusion)
			skill = tokens.get(3);
		} else {
			skill = null;
		}
		
		return skill;
	}
	
	protected String getExclusionSkill(List<String> tokens, Gender gender) {
		/*
		 * combinations that get us here
		 * (!fight, monster, skill, -, exclusion)
		 *   0      1        2      3      4
		 *               OR
		 * (!fight, class, gender, skill, -, exclusion)              
		 *    0       1      2       3    4    5
		 *    			 OR
		 * (!fight, class, gender, skill, -exclusion)
		 *    0     1      2       3      4
		 */
		String exclusion = null;
		String lastToken = tokens.get(tokens.size() - 1);
		if(StringUtils.startsWith(lastToken, "-")) {
			exclusion = StringUtils.remove(lastToken, "-");
		}
		if(tokens.size() < 5) {
			exclusion = null;
		} else if(tokens.size() == 5 && (gender == null || gender == Gender.MONSTER)) {
			exclusion = tokens.get(4);
		} else if(tokens.size() == 6 && (gender == Gender.MALE || gender == Gender.FEMALE)) {
			exclusion = tokens.get(5);
		}
		
		return exclusion;
	}


}
