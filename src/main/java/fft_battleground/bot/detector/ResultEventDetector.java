package fft_battleground.bot.detector;

import org.apache.commons.lang3.StringUtils;

import fft_battleground.bot.model.event.BattleGroundEvent;
import fft_battleground.bot.model.event.ResultEvent;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.model.ChatMessage;

public class ResultEventDetector implements EventDetector {

	private static final String SEARCH_STRING = " team was victorious! Next match starting soon...";
	
	@Override
	public BattleGroundEvent detect(ChatMessage message) {
		ResultEvent event = null;
		if(StringUtils.equals(message.getUsername(), "fftbattleground") && StringUtils.contains(message.getMessage(), SEARCH_STRING)) {
			for(String split : StringUtils.split(message.getMessage(), ";")) {
				if(StringUtils.contains(split, SEARCH_STRING)) {
					String teamString = StringUtils.substringBetween(split, "The ", SEARCH_STRING);
					BattleGroundTeam team = BattleGroundTeam.parse(teamString);
					event = new ResultEvent(team);
					break;
				}
			}
		}
		
		return event;
	}

}
