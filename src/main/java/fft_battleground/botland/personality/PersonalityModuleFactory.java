package fft_battleground.botland.personality;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import fft_battleground.botland.bot.exception.BotConfigException;
import fft_battleground.botland.personality.model.Personalities;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PersonalityModuleFactory {
	
	@Getter
	private Map<String, String> lastBotPersonalityResponses;
	
	public PersonalityModuleFactory() {
		this.lastBotPersonalityResponses = new HashMap<>();
	}
	
	public PersonalityModule getPersonalityModuleByName(String name) throws BotConfigException {
		Personalities personality = Personalities.matchPersonalityByName(name);
		PersonalityModule module = null;
		if(personality != null) {
			module = personality.getModule();
		} else {
			String errorMessage = "cannot find personality module for " + name;
			log.error(errorMessage);
			throw new BotConfigException(errorMessage);
		}
		return module;
	}
	
	public void addBotResponse(String botName, String response) {
		this.lastBotPersonalityResponses.put(botName, response);
	}
}
