package fft_battleground.botland.bot.model;

import org.apache.commons.lang3.StringUtils;

public enum BetType {
	VALUE("value"),
	PERCENTAGE("percentage"),
	HALF("half"),
	ALLIN("allin"),
	FLOOR("floor"),
	RANDOM("random")
	;
	
	private String str;
	
	private BetType(String str) {
		this.str = str;
	}
	
	public String getString() {
		return this.str;
	}
	
	public static BetType getBetType(String parameter) {
		BetType result = null;
		for(BetType type : BetType.values()) {
			if(StringUtils.equalsIgnoreCase(type.getString(), parameter)) {
				result = type;
				break;
			}
		}
		
		return result;
	}
}
