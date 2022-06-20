package fft_battleground.event.detector;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import fft_battleground.event.EventDetector;
import fft_battleground.event.detector.model.BonusEvent;
import fft_battleground.model.ChatMessage;

public class BonusDetector implements EventDetector<BonusEvent> {
	
	private static final String SEARCH_STRING = " bonus for entering as a random unit, or one of these classes: ";
	private static final String CLASS_BONUS_SEARCH_STRING = ". Also, you'll earn a ";
	private static final String SKILL_BONUS_SEARCH_STRING = " EXP bonus for using this skill: ";
	private static final String PLAYER_SEARCH_STRING = ", you'll earn a ";
	
	@Override
	public BonusEvent detect(ChatMessage message) {
		BonusEvent event = null;
		if(StringUtils.equals(message.getUsername(), "fftbattleground") && StringUtils.contains(message.getMessage(), SEARCH_STRING)) {
			for(String str: StringUtils.split(message.getMessage(), ";")) {
				if(StringUtils.contains(message.getMessage(), SEARCH_STRING)) {
					event = this.parseBonusEvent(message.getMessage());
				}
			}
		}
		
		return event;
	}
	
	private BonusEvent parseBonusEvent(String message) {
		BonusEvent event = null;
		String player = StringUtils.trim(StringUtils.substringBefore(message, PLAYER_SEARCH_STRING));
		String classes = StringUtils.trim(StringUtils.substringBetween(message, SEARCH_STRING, CLASS_BONUS_SEARCH_STRING));
		String skillBonus = StringUtils.trim(StringUtils.substringAfter(message, SKILL_BONUS_SEARCH_STRING));
		Set<String> classList = this.parseClassBonus(classes);
		event = new BonusEvent(player, classList, skillBonus);
		return event;
	}
	
	private Set<String> parseClassBonus(String classList) {
		String[] classStrings = StringUtils.split(classList, ", ");
		Set<String> classStringList = null;
		if(classStrings.length >= 3) {
			classStringList = Set.of(classStrings);
		}
		
		return classStringList;
	}
}
