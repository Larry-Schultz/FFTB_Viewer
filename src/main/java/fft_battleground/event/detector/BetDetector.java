package fft_battleground.event.detector;

import org.apache.commons.lang3.StringUtils;

import fft_battleground.botland.model.BetType;
import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.event.model.BetEvent;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.model.ChatMessage;

public class BetDetector implements EventDetector
{

	@Override
	public BattleGroundEvent detect(ChatMessage message) {
		BetEvent event = null;
		String messageText = message.getMessage();
		if(StringUtils.contains(messageText, "!betf")) {
			String amount = "0"; 
			String teamName = null;
			String[] textSplit = StringUtils.split(messageText, ' ');
			if(textSplit.length > 1) {
				teamName = textSplit[1];
				amount="floor";
			}
			String betText = amount;
			BattleGroundTeam team = BattleGroundTeam.parse(teamName);
			
			if(this.validateBet(amount)) {
				event = new BetEvent(message.getUsername(), team, amount, betText, BetType.FLOOR);
			} else {
				event = null;
			}
		} else if(StringUtils.contains(messageText, "!bet")) {
			String amount = "0";
			String betText = "";
			String teamName = null;
			String[] textSplit = StringUtils.split(messageText, ' ');
			BetType type = null;
			if(textSplit.length > 2) {
				if(BattleGroundTeam.isBattleGroundTeamname(textSplit[1])) {
					teamName = textSplit[1];
					amount = textSplit[2];
					betText = amount;
					type = this.determineBetType(amount);
				} else {
					teamName = textSplit[2];
					amount = textSplit[1];
					betText = amount;
					type = this.determineBetType(amount); 
				}
			}
			BattleGroundTeam team = BattleGroundTeam.parse(teamName);
			if(type != null) {
				return new BetEvent(message.getUsername(), team, amount, betText, type);
			} else {
				return null;
			}
		} else if(StringUtils.contains(messageText, "!allin")) {
			String amount = "0"; 
			String teamName = null;
			String[] textSplit = StringUtils.split(messageText, ' ');
			if(textSplit.length > 1) {
				teamName = textSplit[1];
				amount="allin";
			}
			String betText = amount;
			BattleGroundTeam team = BattleGroundTeam.parse(teamName);
			
			if(this.validateBet(amount)) {
				event = new BetEvent(message.getUsername(), team, amount, betText, BetType.ALLIN);
			} else {
				event = null;
			}
		}
		
		return event;
	}
		
	protected BetType determineBetType(String bet) {
		BetType type = null;
		if(StringUtils.contains(bet, "all")) {
			type = BetType.ALLIN;
		} else if(StringUtils.contains(bet, "%")) {
			type = BetType.PERCENTAGE;
		} else if(StringUtils.isNumeric(bet)) {
			type = BetType.VALUE;
		} else if (StringUtils.contains(bet, "half")) {
			type=BetType.HALF;
		} else if(StringUtils.contains(bet, "floor")) {
			type = BetType.FLOOR;
		}
	
		return type;
	}
	
	//try to parse the amount if its a number, to throw out dummy amounts that cause exceptions
	protected boolean validateBet(String bet) {
		if(StringUtils.isNumeric(bet)) {
			try {
				Integer.valueOf(bet);
			} catch(NumberFormatException e) {
				return false;
			}
			
			return true;
		}
		
		return true;
	}
	
}