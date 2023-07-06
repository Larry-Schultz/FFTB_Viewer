package fft_battleground.event.detector;

import org.apache.commons.lang3.StringUtils;

import fft_battleground.event.EventDetector;
import fft_battleground.event.detector.model.BettingBeginsEvent;
import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.model.ChatMessage;

public class BettingBeginsDetector implements EventDetector<BettingBeginsEvent> {

	@Override
	public BettingBeginsEvent detect(ChatMessage message) {
		if(StringUtils.equals(StringUtils.lowerCase(message.getUsername()), StringUtils.lowerCase("FFTBattleground")) &&
				StringUtils.contains(message.getMessage(), "Betting is open for")) {
			String removeBeginningText = StringUtils.substringAfter(message.getMessage(), "Betting is open for ");
			String removeEndText = StringUtils.substringBefore(removeBeginningText, ". Use the !bet command to place a wager!");
			String[] splitRemainingText = StringUtils.split(removeEndText, " vs ");
			
			String team1Text = splitRemainingText[0];
			BattleGroundTeam team1 = BattleGroundTeam.parse(team1Text);
			String team2Text = splitRemainingText[1];
			BattleGroundTeam team2 = BattleGroundTeam.parse(team2Text);
			
			return new BettingBeginsEvent(team1, team2);
		}
		
		return null;
	}
	
}