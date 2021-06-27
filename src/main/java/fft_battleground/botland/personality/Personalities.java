package fft_battleground.botland.personality;

import org.apache.commons.lang3.StringUtils;

public enum Personalities {
	FACTS("facts", new FactsPersonality()),
	YOLO("yolo", new YoloPersonality()),
	FOLLOWER("follower", new FollowerPersonality()),
	NERD("nerd", new NerdPersonality());
	
	Personalities(String name, PersonalityModule module) {
		this.name = name;
		this.module = module;
	}
	
	private String name;
	private PersonalityModule module;
	
	public static Personalities matchPersonalityByName(String name) {
		Personalities result = null;
		for(Personalities personalities : Personalities.values()) {
			if(StringUtils.equalsIgnoreCase(name, personalities.getName())) {
				result = personalities;
				break;
			}
		}
		
		return result;
	}
	
	public String getName() {
		return this.name;
	}
	
	public PersonalityModule getModule() {
		return this.module;
	}
	
}
