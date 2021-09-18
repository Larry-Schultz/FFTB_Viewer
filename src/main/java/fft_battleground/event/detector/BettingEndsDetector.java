package fft_battleground.event.detector;

import org.apache.commons.lang3.StringUtils;

import fft_battleground.event.EventDetector;
import fft_battleground.event.detector.model.BettingEndsEvent;
import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.model.ChatMessage;

public class BettingEndsDetector implements EventDetector<BettingEndsEvent> {

	protected static final String findString = "Betting is closed. Final Bets:";
	
	@Override
	public BettingEndsEvent detect(ChatMessage message) {
		if(StringUtils.equals(StringUtils.lowerCase(message.getUsername()), StringUtils.lowerCase("FFTBattleground"))
				&& StringUtils.contains(message.getMessage(), findString)) {
			String removeStart = StringUtils.substringAfter(message.getMessage(), findString);
			String removeEnd = StringUtils.substringBefore(removeStart, "... Good luck!");
			String[] twoPieces = StringUtils.split(removeEnd, ';');
			
			String[] partOneWhitespaceSplit = StringUtils.split(twoPieces[0], ' ');
			String team1Name = partOneWhitespaceSplit[0];
			BattleGroundTeam team1 = BattleGroundTeam.parse(team1Name);
			Integer team1Bets = Integer.valueOf(partOneWhitespaceSplit[2]);
			Integer team1Amount = Integer.valueOf(StringUtils.replace(StringUtils.replace(partOneWhitespaceSplit[5], "G", ""), ",", ""));
			
			String[] partTwoWhitespaceSplit = StringUtils.split(twoPieces[1], ' ');
			String team2Name = partTwoWhitespaceSplit[0];
			BattleGroundTeam team2 = BattleGroundTeam.parse(team2Name);
			Integer team2Bets = Integer.valueOf(partTwoWhitespaceSplit[2]);
			Integer team2Amount = Integer.valueOf(StringUtils.replace(StringUtils.replace(partTwoWhitespaceSplit[5], "G", ""), ",", ""));
			
			return new BettingEndsEvent(team1, team1Bets, team1Amount, team2, team2Bets, team2Amount);
		}
		
		return null;
	}
	
}