package fft_battleground.event.detector;

import org.apache.commons.lang3.StringUtils;

import fft_battleground.event.EventDetector;
import fft_battleground.event.detector.model.BettingEndsEvent;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.model.ChatMessage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BettingEndsDetector implements EventDetector<BettingEndsEvent> {

	protected static final String findString = "Betting is closed. The final bets were: ";
	
	@Override
	public BettingEndsEvent detect(ChatMessage message) {
		if(StringUtils.equals(StringUtils.lowerCase(message.getUsername()), StringUtils.lowerCase("FFTBattleground"))
				&& StringUtils.contains(message.getMessage(), findString)) {
			try {
			String removeStart = StringUtils.substringAfter(message.getMessage(), findString);
			String[] twoPieces = StringUtils.splitByWholeSeparator(removeStart, "), ");
			
			String[] partOneWhitespaceSplit = StringUtils.split(twoPieces[0], ' ');
			String team1Name = partOneWhitespaceSplit[0];
			BattleGroundTeam team1 = BattleGroundTeam.parse(team1Name);
			Integer team1Bets = Integer.valueOf(partOneWhitespaceSplit[2]);
			Integer team1Amount = this.parseTeamAmount(partOneWhitespaceSplit[5]);
			
			String[] partTwoWhitespaceSplit = StringUtils.split(twoPieces[1], ' ');
			String team2Name = partTwoWhitespaceSplit[0];
			BattleGroundTeam team2 = BattleGroundTeam.parse(team2Name);
			Integer team2Bets = Integer.valueOf(partTwoWhitespaceSplit[2]);
			Integer team2Amount = this.parseTeamAmount(partTwoWhitespaceSplit[5]);
			
			return new BettingEndsEvent(team1, team1Bets, team1Amount, team2, team2Bets, team2Amount);
			} catch(NumberFormatException e) {
				log.error("Error formatting team amount from BettingEndsEvent", e);
			}
		}
		
		return null;
	}
	
	private Integer parseTeamAmount(String str) throws NumberFormatException {
		String temp = str;
		temp = StringUtils.replace(str, "G", "");
		temp = StringUtils.replace(temp, ",", "");
		temp = StringUtils.remove(temp, '.');
		Integer amount = Integer.valueOf(temp);
		return amount;
	}
	
}