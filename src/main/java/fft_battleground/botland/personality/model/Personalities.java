package fft_battleground.botland.personality.model;

import org.apache.commons.lang3.StringUtils;

import fft_battleground.botland.personality.FactsPersonality;
import fft_battleground.botland.personality.FollowerPersonality;
import fft_battleground.botland.personality.InversePercentilePersonality;
import fft_battleground.botland.personality.InversePersonality;
import fft_battleground.botland.personality.NerdPersonality;
import fft_battleground.botland.personality.PersonalityModule;
import fft_battleground.botland.personality.SidePersonality;
import fft_battleground.botland.personality.YoloPersonality;

public enum Personalities {
	FACTS("facts", new FactsPersonality()),
	YOLO("yolo", new YoloPersonality()),
	FOLLOWER("follower", new FollowerPersonality()),
	MONEY_FOLLOWER("money", new FollowerPersonality(FollowerPersonality.MONEY)),
	NERD("nerd", new NerdPersonality()),
	LEFT("left", new SidePersonality("left")),
	RIGHT("right", new SidePersonality("right")),
	INVERSE("inverse", new InversePersonality()),
	INVERSE_PERCENTILE("inverse_percentile", new InversePercentilePersonality());
	
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
