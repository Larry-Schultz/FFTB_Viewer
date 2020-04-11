package fft_battleground.bot.detector;

import org.apache.commons.lang3.StringUtils;

import fft_battleground.bot.model.event.BattleGroundEvent;
import fft_battleground.bot.model.event.BettingBeginsEvent;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.model.ChatMessage;

public class BettingBeginsDetector implements EventDetector {

	@Override
	public BattleGroundEvent detect(ChatMessage message) {
		if(StringUtils.equals(StringUtils.lowerCase(message.getUsername()), StringUtils.lowerCase("FFTBattleground")) &&
				StringUtils.contains(message.getMessage(), "Betting is open for")) {
			String removeBeginningText = StringUtils.substringAfter(message.getMessage(), "Betting is open for ");
			String removeEndText = StringUtils.substringBefore(removeBeginningText, ". Use !bet [amount] [team] to place a wager!");
			String[] splitRemainingText = StringUtils.split(removeEndText, ' ');
			
			String team1Text = splitRemainingText[0];
			BattleGroundTeam team1 = BattleGroundTeam.parse(team1Text);
			String team2Text = splitRemainingText[2];
			BattleGroundTeam team2 = BattleGroundTeam.parse(team2Text);
			
			return new BettingBeginsEvent(team1, team2);
		}
		
		return null;
	}
	
}