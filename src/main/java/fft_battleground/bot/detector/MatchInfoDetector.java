package fft_battleground.bot.detector;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import fft_battleground.bot.model.event.BattleGroundEvent;
import fft_battleground.bot.model.event.MatchInfoEvent;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.model.ChatMessage;

public class MatchInfoDetector implements EventDetector {

	private static final String BEGIN_STRING = "Current match: ";
	private static final String END_STRING = " seconds longer until a Time Over";
	
	@Override
	public BattleGroundEvent detect(ChatMessage message) {
		MatchInfoEvent event = null;
		if(StringUtils.contains(message.getMessage(), BEGIN_STRING)) {
			String relevantString = StringUtils.substringBetween(message.getMessage(), BEGIN_STRING, END_STRING);
			
			//parse team
			String teamString = StringUtils.substringBefore(relevantString, " on");
			Pair<BattleGroundTeam, BattleGroundTeam> teamPair = this.parseTeams(teamString);
			
			//parse map
			String mapString = StringUtils.substringBetween(relevantString, "on ", ".");
			Pair<Integer, String> mapData = this.parseMap(mapString);
			
			event = new MatchInfoEvent(teamPair, mapData);
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
