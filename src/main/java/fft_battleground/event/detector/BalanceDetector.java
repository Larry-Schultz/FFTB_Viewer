package fft_battleground.event.detector;

import org.apache.commons.lang3.StringUtils;

import fft_battleground.botland.model.BattleGroundEventType;
import fft_battleground.event.model.BalanceEvent;
import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.model.ChatMessage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BalanceDetector implements EventDetector {

	protected static final String findStringFormat = "%1$s, your bettable balance is:";
	
	private String username;
	
	private Integer previousAmount = null;
	
	public BalanceDetector(String username) { 
		this.username = username;
	}
	
	@Override
	public BattleGroundEvent detect(ChatMessage message) {
		
		String findString = String.format(findStringFormat, this.username);
		if(StringUtils.equals(StringUtils.lowerCase(message.getUsername()), StringUtils.lowerCase("FFTBattleground"))
				&& StringUtils.contains(message.getMessage(), findString)) {
			int index = StringUtils.indexOf(message.getMessage(), findString) + findString.length();
			String startStringAtRelevantPortion = StringUtils.substring(message.getMessage(), index);
			String removeEndPortion = StringUtils.substringBefore(startStringAtRelevantPortion, ", with");
			
			Integer amount = Integer.valueOf(StringUtils.replace(StringUtils.replace(StringUtils.trim(StringUtils.substringBefore(removeEndPortion, "G (")), ",", ""), "G", ""));
			
			String removeSpendable = StringUtils.substringAfter(startStringAtRelevantPortion, "Spendable: ");
			Integer spendable = Integer.valueOf(StringUtils.replace(StringUtils.trim(StringUtils.substringBefore(removeSpendable, "G).")), ",", ""));
			
			
			Integer previousAmountBuffer = this.previousAmount;
			BalanceEvent event = null;
			event = new BalanceEvent(BattleGroundEventType.BALANCE, amount, spendable);
		
			
			log.info("The previous amount was {}, the new amount is {} and the eventType is {}", 
					previousAmountBuffer, amount, event != null ? event.getEventType() : null);
			
			return event;
		}
		return null;
	}
	
}
