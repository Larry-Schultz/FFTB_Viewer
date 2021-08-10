package fft_battleground.botland.model;

import org.apache.commons.lang3.StringUtils;

public enum BotNames {
	BETCOUNT("betcountbot"),
	DATA("databetbot"),
	ODDS("oddsbot"),
	ARBITRARY("arbitrarybot"),
	GENE("genebot"),
	BRAVEFAITH("bravefaithbot"),
	TEAMVALUE("teamvalue"),
	UNIT("unitawarebot");
	
	private String botname;
	
	private BotNames(String botname) {
		this.botname = botname;
	}
	
	public String getName() {
		return this.botname;
	}
	
	public static BotNames parseBotname(String name) {
		BotNames result = null;
		for(BotNames bots: BotNames.values()) {
			if(StringUtils.equalsIgnoreCase(bots.getName(), name)) {
				result = bots;
				break;
			}
		}
		
		return result;
	}
}
