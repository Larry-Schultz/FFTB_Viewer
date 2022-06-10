package fft_battleground.botland.bot.util;

import fft_battleground.util.GambleUtil;

public interface BotCanBetBelowMinimum {
	public static final String CAN_BET_BELOW_MINIMUM = "bet_below_minimum";
	public static final int NEW_MINIMUM = 1;
	
	public default String getCanBetBelowMinimumParameter() {
		return CAN_BET_BELOW_MINIMUM;
	}
	
	public default boolean readCanBetBelowMinimumParameter(BotParameterReader reader) {
		boolean result = reader.readOptionalBooleanParam(getCanBetBelowMinimumParameter());
		return result;
	}
	
	public default int handleMinimumBet(boolean isSubscriber) {
		if(this.isCanBetBelowMinimum()) {
			return NEW_MINIMUM;
		} else {
			return GambleUtil.getMinimumBetForBettor(isSubscriber);
		}
	}
	
	public boolean isCanBetBelowMinimum();
}
