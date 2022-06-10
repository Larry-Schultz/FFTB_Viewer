package fft_battleground.botland.bot.util;

import java.util.Optional;

public interface BotCanUseBetExpressions {
	public static final String BET_AMOUNT_EXPRESSION_PARAMETER = "betExpression";
	
	public default String getBetAmountExpressionParameter() {
		return BET_AMOUNT_EXPRESSION_PARAMETER;
	}
	
	public default Optional<String> readBetAmountExpression(BotParameterReader reader) {
		Optional<String> result = reader.readStringParam(getBetAmountExpressionParameter());
		return result;
	}
}
