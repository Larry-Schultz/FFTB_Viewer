package fft_battleground.event.detector;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import fft_battleground.event.EventDetector;
import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.event.model.MatchInfoEvent;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.model.ChatMessage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MatchInfoDetector implements EventDetector<MatchInfoEvent> {

	private static final String BEGIN_STRING = "Current match: ";
	private static final String END_STRING = " seconds longer until a Time Over";
	
	@Override
	public MatchInfoEvent detect(ChatMessage message) {
		MatchInfoEvent event = null;
		if(StringUtils.contains(message.getMessage(), BEGIN_STRING)) {
			String teamString = null;
			try {
				String relevantString = StringUtils.substringBetween(message.getMessage(), BEGIN_STRING, END_STRING);
				
				//parse team
				teamString = StringUtils.substringBefore(relevantString, " on");
				Pair<BattleGroundTeam, BattleGroundTeam> teamPair = this.parseTeams(teamString);
				
				//parse map
				String mapString = StringUtils.substringBetween(relevantString, "on ", ".");
				Pair<Integer, String> mapData = this.parseMap(mapString);
				
				event = new MatchInfoEvent(teamPair, mapData);
			} catch(NullPointerException e) {
				event = null;
				log.error("Exception found in MatchInfoDetector, the ChatMessage was: {}", message, e);
				log.error("team string was: {}", teamString);
			}
		}
		return event;
	}
	
	public Pair<BattleGroundTeam, BattleGroundTeam> parseTeams(String teamsString) {
		Pair<BattleGroundTeam, BattleGroundTeam> teamPair = null;
		String[] split = StringUtils.split(teamsString, " vs ");
		BattleGroundTeam team1 = BattleGroundTeam.parse(split[0]);
		BattleGroundTeam team2 = BattleGroundTeam.parse(split[1]);
		
		teamPair = new ImmutablePair<>(team1, team2);
		
		return teamPair;
	}
	
	public Pair<Integer, String> parseMap(String mapString) {
		Pair<Integer, String> mapData = null;
		String[] split = StringUtils.split(mapString, "-");
		
		String mapNumberString = StringUtils.substringAfter(split[0], "Map ");
		Integer mapNumber = Integer.valueOf(StringUtils.trim(mapNumberString));
		
		String mapName = StringUtils.trim(split[1]);
		mapData = new ImmutablePair<>(mapNumber, mapName);
		
		return mapData; 
	}

}
