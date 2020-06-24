package fft_battleground.util;

import org.apache.commons.lang3.StringUtils;

import fft_battleground.event.model.BetEvent;
import fft_battleground.repo.model.PlayerRecord;

public class GambleUtil {
	
	public static final Integer MINIMUM_BET = 100;
	public static final Short DEFAULT_REMAINING_EXP = 100;
	public static final Integer MAX_BET = 1000;
	
	public static Float bettingOdds(Integer thisTeamAmount, Integer otherTeamAmount) {
		Float odds = 1f;
		try {
			odds = (float) otherTeamAmount / thisTeamAmount;  
		} catch(ArithmeticException e) {
			odds = 1f;
		}
		
		return odds;
	}
	
	public static Float bettingPercentage(Integer thisTeamAmount, Integer otherTeamAmount) {
		Float odds = 1f;
		try {
			odds = ((float) thisTeamAmount)/((float) thisTeamAmount + (float) otherTeamAmount);
		} catch(ArithmeticException e) {
			odds = 1f;
		}
		
		return odds;
	}
	
	public static Integer getAmountUpdateFromBet(Integer betAmount, Float bettingOdds, boolean win) {
		Integer newAmount = 0;
		if(win) {
			newAmount = (int) (bettingOdds * betAmount);
		} else {
			newAmount = (-1) * betAmount;
		}
		
		return newAmount;
	}
	
	public static Integer getBetAmountFromBetString(PlayerRecord player, BetEvent event) {
		Integer value = 0;
		switch(event.getBetType()) {
		case VALUE:
			value = Integer.valueOf(event.getBetAmount());
			break;
		case PERCENTAGE:
			String filteredValue = StringUtils.remove(event.getBetAmount(), '%');
			Integer lastKnownAmount = player.getLastKnownAmount();
			if(lastKnownAmount != null && StringUtils.isNumeric(filteredValue)) {
				Integer percentage = Integer.valueOf(filteredValue);
				value = new Integer( (int) (lastKnownAmount.floatValue() * percentage.floatValue() * 0.01f));
			}
			break;
		case ALLIN:
			if(player.getLastKnownAmount() != null) {
				value = player.getLastKnownAmount();
			} else if(player.getLastKnownLevel() != null) {
				value = getMinimumBetForLevel(player.getLastKnownLevel());
			} else {
				value = MINIMUM_BET;
			}
			break;
		case HALF:
			if(player.getLastKnownAmount() != null) {
				value = player.getLastKnownAmount()/2;
			} else if(player.getLastKnownLevel() != null) {
				value = getMinimumBetForLevel(player.getLastKnownLevel());
			} else {
				value = MINIMUM_BET;
			}
			break;
		default:
			break;
		}
		
		return value;
	}
	
	public static Integer getMinimumBetForLevel(Short level) {
		Integer minimumBet = null;
		if(level != null && level <= 100) {
			minimumBet = (4 * level) + MINIMUM_BET;
		} else if(level != null && level > 100) {
			minimumBet = (4 * 100) + MINIMUM_BET;
		} else {
			minimumBet = MINIMUM_BET;
		}
		return minimumBet;
	}
	
	public static String cleanString(String str) {
		String result = str;
		result = StringUtils.lowerCase(result);
		result = StringUtils.replace(result, ",", "");
		result = StringUtils.trim(result);
		return result;
	}
	
}
