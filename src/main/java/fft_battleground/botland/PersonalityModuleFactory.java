package fft_battleground.botland;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import fft_battleground.botland.personality.Personalities;
import fft_battleground.botland.personality.PersonalityModule;
import lombok.Getter;

@Service
public class PersonalityModuleFactory {
	
	@Getter
	private Map<String, String> lastBotPersonalityResponses;
	
	public PersonalityModuleFactory() {
		this.lastBotPersonalityResponses = new HashMap<>();
	}
	
	public PersonalityModule getPersonalityModuleByName(String name) {
		Personalities personality = Personalities.matchPersonalityByName(name);
		PersonalityModule module = personality.getModule();
		
		return module;
	}
	
	public void addBotResponse(String botName, String response) {
		this.lastBotPersonalityResponses.put(botName, response);
	}
}
