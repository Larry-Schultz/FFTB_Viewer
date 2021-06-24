package fft_battleground.event.detector;

import org.apache.commons.lang3.StringUtils;

import fft_battleground.event.EventDetector;
import fft_battleground.event.detector.model.AllegianceEvent;
import fft_battleground.event.detector.model.BattleGroundEvent;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.model.ChatMessage;

public class AllegianceDetector implements EventDetector<AllegianceEvent> {

	private static final String SEARCH_STRING = " Starting next tournament, if you fail to get in with !fight you will still earn 3 EXP if this team wins.";
	private static final String SEARCH_STRING_2 = ", you are currently allied with the ";
	
	@Override
	public AllegianceEvent detect(ChatMessage message) {
		AllegianceEvent event = null;
		if(StringUtils.contains(message.getUsername(), "fftbattleground") && (
				StringUtils.contains(message.getMessage(), SEARCH_STRING) || StringUtils.contains(message.getMessage(), SEARCH_STRING_2))) {
			for(String str: StringUtils.split(message.getMessage(), ";")) {
				if(StringUtils.contains(str, SEARCH_STRING)) {
					String player = StringUtils.substringBefore(str, " welcome to the");
					String teamName = StringUtils.substringBetween(str, " welcome to the ", " Team! Starting next tournament, if you fail to get in with !fight you will still earn 3 EXP if this team wins.");
					BattleGroundTeam team = BattleGroundTeam.parse(teamName);
					event = new AllegianceEvent(player, team);
					break;
				} else if(StringUtils.contains(str, SEARCH_STRING_2)) {
					String player = StringUtils.substringBefore(str, ", you are currently allied with the ");
					String teamName = StringUtils.substringBetween(str, ", you are currently allied with the ", " team.");
					BattleGroundTeam team = BattleGroundTeam.parse(teamName);
					event = new AllegianceEvent(player, team);
					break;
				}
			}
			
		}
		
		return event;
	}

}
