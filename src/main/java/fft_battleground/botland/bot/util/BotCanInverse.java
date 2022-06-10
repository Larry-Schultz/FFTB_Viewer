package fft_battleground.botland.bot.util;

public interface BotCanInverse {
	public static final String INVERSE_PARAM = "inverse";
	public default String getInverseParam() {
		return INVERSE_PARAM;
	}
	
	public default Boolean readInverseParameter(BotParameterReader reader) {
		boolean result = reader.readOptionalBooleanParam(this.getInverseParam());
		return result;
	}
	
	public boolean isInverse();
}
