package fft_battleground.event.model;

public enum Hype {
	CATJAM("catJAM");
	
	private String emote;
	
	Hype(String emote) {
		this.emote = emote;
	}
	
	public String getEmote() {
		return this.emote;
	}
}
