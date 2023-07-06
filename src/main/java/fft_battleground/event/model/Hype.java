package fft_battleground.event.model;

import org.apache.commons.lang3.StringUtils;

public enum Hype {
	catJAM("catJAM"),
	ratJAM("ratJAM"),
	pepeJAM("pepeJAM"),                                        
	MageRage("MageRage"),
	moogleH("moogleH"),
	elisSpin("elisSpin"),
	SourPls("SourPls"),
	PianoTime("PianoTime"),
	beeBobble("beeBobble"),
	BirbRave("BirbRave"),
	peepoDJ("peepoDJ"),
	gooseRave("gooseRave"),
	gachiBASS("gachiBASS"),
	hoSway("hoSway"),
	blobDance("blobDance"),
	xar2EDM("xar2EDM"),
	Vibes("Vibes"),
	stripDance("stripDance"),
	FluteTime("FluteTime"),
	ViolinTime("ViolinTime"),
	rooRave("rooRave"),
	rooBobble("rooBobble"),
	TrumpetTime("TrumpetTime"),
	zeurelDance("zeurelDance"),
	BoneZone("BoneZone"),
	deebitRock("deebitRock"),
	deebitGroove("deebitGroove"),
	kirstJAM("kirstJAM"),
	dadiDance("dadiDance");
	
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
