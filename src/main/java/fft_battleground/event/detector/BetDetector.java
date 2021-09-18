package fft_battleground.event.detector;

import org.apache.commons.lang3.StringUtils;

import fft_battleground.botland.model.BetType;
import fft_battleground.event.EventDetector;
import fft_battleground.event.detector.model.BetEvent;
import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.model.ChatMessage;

public class BetDetector implements EventDetector<BetEvent>
{
	private static final String ALLINBUT_FLAG_SEARCH_STRING = "allbut"; 
	
	@Override
	public BetEvent detect(ChatMessage message) {
		BetEvent event = null;
		String messageText = message.getMessage();
		if(StringUtils.contains(messageText, "!betf")) {
			String amount = "0"; 
			String teamName = null;
			boolean allinbutFlag = false;
			//if the flag is there, remove the text entirely and set the flag.  This allows the rest of the code to work without change
			if(StringUtils.contains(messageText, ALLINBUT_FLAG_SEARCH_STRING)) {
				allinbutFlag = true;
				StringUtils.replace(messageText, ALLINBUT_FLAG_SEARCH_STRING +" ", "");
			}
			String[] textSplit = StringUtils.split(messageText, ' ');
			if(textSplit.length > 1) {
				teamName = textSplit[1];
				amount="floor";
			}
			String betText = amount;
			BattleGroundTeam team = BattleGroundTeam.parse(teamName);
			
			if(this.validateBet(amount)) {
				event = new BetEvent(message.getUsername(), team, amount, betText, BetType.FLOOR, message.getIsSubscriber());
				event.setAllinbutFlag(allinbutFlag);
			} else {
				event = null;
			}
		} else if(StringUtils.contains(messageText, "!bet") || StringUtils.startsWithIgnoreCase(messageText, "!" + ALLINBUT_FLAG_SEARCH_STRING)) {
			String amount = "0";
			String betText = "";
			String teamName = null;
			
			boolean allinbutFlag = false;
			
			if(StringUtils.startsWithIgnoreCase(messageText, "!" + ALLINBUT_FLAG_SEARCH_STRING)) {
				allinbutFlag = true;
			}
			
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
				if(this.validateBet(amount)) {
					event = new BetEvent(message.getUsername(), team, amount, betText, type, message.getIsSubscriber());
					event.setAllinbutFlag(allinbutFlag);
				} else {
					event = null;
				}
				return event;
			} else {
				return event;
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
				event = new BetEvent(message.getUsername(), team, amount, betText, BetType.ALLIN, message.getIsSubscriber());
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