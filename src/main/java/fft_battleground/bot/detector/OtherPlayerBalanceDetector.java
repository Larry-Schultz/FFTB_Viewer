package fft_battleground.bot.detector;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import fft_battleground.bot.model.BattleGroundEventType;
import fft_battleground.bot.model.event.BalanceEvent;
import fft_battleground.bot.model.event.BattleGroundEvent;
import fft_battleground.bot.model.event.OtherPlayerBalanceEvent;
import fft_battleground.model.ChatMessage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OtherPlayerBalanceDetector implements EventDetector {
	
	protected static final String findString = "your bettable balance is:";

	@Override
	public BattleGroundEvent detect(ChatMessage message) {
		OtherPlayerBalanceEvent event = null;
		if(StringUtils.equals(StringUtils.lowerCase(message.getUsername()), StringUtils.lowerCase("FFTBattleground"))) {
			List<BalanceEvent> balanceEvents = new ArrayList<BalanceEvent>();
			String[] splitStrings = StringUtils.split(message.getMessage(), ";");
			for(String splitString: splitStrings) {
				BalanceEvent newEvent = this.detectBalanceEventFromSplitString(splitString);
				if(newEvent != null) {
					balanceEvents.add(newEvent);
				}
			}
			
			if(balanceEvents.size() > 0) {
				event = new OtherPlayerBalanceEvent(BattleGroundEventType.OTHER_PLAYER_BALANCE, balanceEvents);
			}
		}
		return event;
	}
	
	protected BalanceEvent detectBalanceEventFromSplitString(String splitString) {
		BalanceEvent event = null;
		if(StringUtils.contains(splitString, findString)) {
			String username = StringUtils.trim(StringUtils.substringBefore(splitString, ","));
			
			int index = StringUtils.indexOf(splitString, findString) + findString.length();
			String startStringAtRelevantPortion = StringUtils.substring(splitString, index);
			String removeEndPortion = StringUtils.substringBefore(startStringAtRelevantPortion, ", with");
			
			Integer amount = Integer.valueOf(StringUtils.replace(StringUtils.replace(StringUtils.trim(StringUtils.substringBefore(removeEndPortion, "G (")), ",", ""), "G", ""));
			
			String removeSpendable = StringUtils.substringAfter(startStringAtRelevantPortion, "Spendable: ");
			Integer spendable = Integer.valueOf(StringUtils.replace(StringUtils.trim(StringUtils.substringBefore(removeSpendable, "G).")), ",", ""));
			
			event = new BalanceEvent(BattleGroundEventType.OTHER_PLAYER_BALANCE, username, amount, spendable);
		}
		return event;
	}

}
