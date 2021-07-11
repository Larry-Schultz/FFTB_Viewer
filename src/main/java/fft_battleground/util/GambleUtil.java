package fft_battleground.util;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import fft_battleground.event.detector.model.BetEvent;
import fft_battleground.repo.model.PlayerRecord;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GambleUtil {
	
	public static final Integer MINIMUM_BET = 100;
	public static final Integer SUBSCRIBER_MINIMUM_BET = 200;
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
			try {
			value = Integer.valueOf(event.getBetAmount());
			} catch(NumberFormatException e) {
				value = 0;
			}
			if(event.isAllinbutFlag()) {
				value = player.getLastKnownAmount() - value;
			}
			break;
		case PERCENTAGE:
			String filteredValue = StringUtils.remove(event.getBetAmount(), '%');
			Integer lastKnownAmount = player.getLastKnownAmount();
			if(lastKnownAmount != null && StringUtils.isNumeric(filteredValue)) {
				Integer percentage = Integer.valueOf(filteredValue);
				value = new Integer( (int) (lastKnownAmount.floatValue() * percentage.floatValue() * 0.01f));
				if(event.isAllinbutFlag()) {
					value = lastKnownAmount - value;
				}
			}
			break;
		case ALLIN:
			if(player.getLastKnownAmount() != null) {
				value = player.getLastKnownAmount();
			} else if(player.getLastKnownLevel() != null) {
				value = getMinimumBetForLevel(player.getLastKnownLevel(), player.prestigeLevel(), event.getIsSubscriber());
			} else {
				value = getMinimumBetForLevel((short)0, player.prestigeLevel(), event.getIsSubscriber());
			}
			break;
		case HALF:
			if(player.getLastKnownAmount() != null) {
				value = player.getLastKnownAmount()/2;
			} else if(player.getLastKnownLevel() != null) {
				value = getMinimumBetForLevel(player.getLastKnownLevel(), player.prestigeLevel(), event.getIsSubscriber());
			} else {
				value = getMinimumBetForLevel((short)0, player.prestigeLevel(), event.getIsSubscriber());
			}
			//no need to implement allinbut code here, since allinbut half is the same as bet half.
			break;
		case FLOOR:
			if(player.getLastKnownLevel() != null) {
				value = getMinimumBetForLevel(player.getLastKnownLevel(), player.prestigeLevel(), event.getIsSubscriber());
			} else {
				value = getMinimumBetForLevel((short)0, player.prestigeLevel(), event.getIsSubscriber());
			}
			if(event.isAllinbutFlag()) {
				value = player.getLastKnownAmount() - value;
			}
			break;
		default:
			break;
		}
		
		if(value < 0) {
			log.warn("player had bet value below zero {}", player.getPlayer());
		}
		
		return value;
	}
	
	public static Integer getMinimumBetForLevel(Short level, boolean isSubscriber) {
		Integer result = getMinimumBetForLevel(level, 0, isSubscriber);
		return result;
	}
	
	public static Integer getMinimumBetForLevel(Short level, Integer prestigeLevel, boolean isSubscriber) {
		Integer minimumBet = null;
		if(level != null && level <= 100) {
			minimumBet = (100 * prestigeLevel) + (4 * level) + getMinimumBetForBettor(isSubscriber);
		} else if(level != null && level > 100) {
			minimumBet = (100 * prestigeLevel) + (4 * 100) + getMinimumBetForBettor(isSubscriber);
		} else {
			minimumBet = (100 * prestigeLevel) + getMinimumBetForBettor(isSubscriber);
		}
		return minimumBet;
	}
	

	
	public static Integer getMinimumBetForBettor(boolean isSubscriber) {
		Integer result = isSubscriber ? SUBSCRIBER_MINIMUM_BET : MINIMUM_BET;
		return result;
	}
	
	public static String cleanString(String str) {
		String result = str;
		result = StringUtils.lowerCase(result);
		result = StringUtils.replace(result, ",", "");
		result = StringUtils.trim(result);
		return result;
	}
	
	public static int calculatePercentile(Float leftScore, Float rightScore, Map<Integer, Integer> percentiles) {
		if(percentiles == null || percentiles.isEmpty()) {
			return 50;
		}
		int scoreDifference = (int) Math.abs(leftScore - rightScore);
		int percentile = 0;
		for(int i = 1; i < 100 && percentiles.get(i) < scoreDifference; i++) {
			percentile = i;
		}
		return percentile;
	}
	
}
