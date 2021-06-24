package fft_battleground.event.detector;

import org.apache.commons.lang3.StringUtils;

import fft_battleground.event.EventDetector;
import fft_battleground.event.detector.model.BattleGroundEvent;
import fft_battleground.event.detector.model.BetInfoEvent;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.model.ChatMessage;

public class BetInfoEventDetector implements EventDetector<BetInfoEvent> {

	private static final String SEARCH_STRING = " share of your team's winnings, and stand to win ";
	
	@Override
	public BetInfoEvent detect(ChatMessage message) {
		BetInfoEvent event = null;
		if(StringUtils.equals(message.getUsername(), "fftbattleground") && StringUtils.contains(message.getMessage(), SEARCH_STRING)) {
			for(String split : StringUtils.split(message.getMessage(), ";")) {
				if(StringUtils.contains(split, SEARCH_STRING)) {
					String player = StringUtils.trim(StringUtils.lowerCase(StringUtils.substringBefore(split, ", your bet is ")));
					Integer betAmount = Integer.valueOf(StringUtils.replace(StringUtils.substringBetween(split, ", your bet is ", "G on "), ",", ""));
					BattleGroundTeam team = BattleGroundTeam.parse(StringUtils.substringBetween(split, "G on ", ". You hold a "));
					String percentage =  StringUtils.substringBetween(split, ". You hold a ", "% share of your team's winnings, and stand to win ");
					Integer possibleEarnings = Integer.valueOf(StringUtils.replace(StringUtils.substringBetween(split, "% share of your team's winnings, and stand to win ", "G if you win."), ",", ""));
					event = new BetInfoEvent(player, betAmount, team, percentage, possibleEarnings);
					break;
				}
			}
		}
		
		return event;
	}

}
