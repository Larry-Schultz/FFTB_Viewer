package fft_battleground.event.model;

import org.apache.commons.lang3.StringUtils;

public enum Hype {
	CATJAM("catJAM"),
	RATJAM("ratJAM"),
	PEPEJAM("pepeJAM"),                                        
	MAGERAGE("MageRage"),
	MOOGLEH("moogleH"),
	ELISSPIN("elisSpin"),
	SOURPLS("SourPls"),
	PIANOTIME("PianoTime"),
	BEEBOBBLE("beeBobble"),
	BIRBRAVE("BirbRave"),
	PEEPODJ("peepoDJ"),
	GOOSERAVE("gooseRave"),
	GACHIBASS("gachiBASS");
	
	private String emote;
	
	Hype(String emote) {
		this.emote = emote;
	}
	
	public static Hype getHypeByString(String emoteString) {
		for(Hype hype: Hype.values()) {
			if(StringUtils.equals(hype.getEmote(), emoteString)) {
				return hype;
			}
		}
		
		return null;
	}
	
	public String getEmote() {
		return this.emote;
	}
	
}
