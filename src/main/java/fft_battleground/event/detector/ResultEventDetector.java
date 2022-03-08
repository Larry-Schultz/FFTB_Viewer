package fft_battleground.event.detector;

import org.apache.commons.lang3.StringUtils;

import fft_battleground.event.EventDetector;
import fft_battleground.event.detector.model.ResultEvent;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.model.ChatMessage;

public class ResultEventDetector implements EventDetector<ResultEvent> {

	private static final String SEARCH_STRING = " team was victorious!";
	
	@Override
	public ResultEvent detect(ChatMessage message) {
		ResultEvent event = null;
		String cleanedMessage = StringUtils.lowerCase(message.getMessage());
		if(StringUtils.equals(message.getUsername(), "fftbattleground") && StringUtils.contains(cleanedMessage, SEARCH_STRING)) {
			for(String split : StringUtils.split(cleanedMessage, ";")) {
				if(StringUtils.contains(split, SEARCH_STRING)) {
					String teamString = StringUtils.substringBetween(split, "the ", SEARCH_STRING);
					BattleGroundTeam team = BattleGroundTeam.parse(teamString);
					event = new ResultEvent(team);
					break;
				}
			}
		}
		
		return event;
	}

}
