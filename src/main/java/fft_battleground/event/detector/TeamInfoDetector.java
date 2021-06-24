package fft_battleground.event.detector;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import fft_battleground.event.EventDetector;
import fft_battleground.event.detector.model.TeamInfoEvent;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.model.ChatMessage;

public class TeamInfoDetector implements EventDetector<TeamInfoEvent> {

	private String SEARCH_STRING_POSTFIX = " Team:";
	private String SEARCH_STRING_POSTFIX_2 = " Team (";
	private String TEAM_VS_UNIT_SPLIT_STRING = " the ";
	
	private List<String> prefixes;
	
	public TeamInfoDetector() {
		this.prefixes = this.generatePrefixes();
	}
	
	private List<String> generatePrefixes() {
		List<String> prefixes = new ArrayList<String>();
		BattleGroundTeam[] teams = BattleGroundTeam.values();
		for(BattleGroundTeam team: teams) {
			if(team != BattleGroundTeam.CHAMPION) {
				for(String teamName: team.getTeamNames()) {
						String capitalizedTeamName = StringUtils.capitalize(teamName);
						StringBuilder builder = new StringBuilder(capitalizedTeamName).append(SEARCH_STRING_POSTFIX);
						prefixes.add(builder.toString());
					}
				} else {
					//champion has its own prefixes
					for(BattleGroundTeam team2: teams) {
						for(String teamName : team2.getTeamNames()) {
							String championPrefix = String.format("Champion Team (%1$s): ", StringUtils.capitalize(teamName));
							prefixes.add(championPrefix);
						}
					}
				}
		}
		
		return prefixes;
	}
	
	@Override
	public TeamInfoEvent detect(ChatMessage message) {
		TeamInfoEvent event = null;
		if(StringUtils.contains(message.getUsername(), "fftbattleground") && this.eventDetected(message.getMessage())) {
			String[] splitStrings = StringUtils.split(message.getMessage(), ";");
			for(String str : splitStrings) {
				if(this.eventDetected(message.getMessage())) {
					String teamString = StringUtils.substringBefore(message.getMessage(), "Team");
					BattleGroundTeam team = BattleGroundTeam.parse(StringUtils.trim(teamString));
					
					String playerUnitString = "";
					if(team != BattleGroundTeam.CHAMPION) {
						playerUnitString = StringUtils.substringAfter(message.getMessage(), SEARCH_STRING_POSTFIX);
					} else {
						playerUnitString = StringUtils.substringAfter(message.getMessage(), "): ");
					}
					String[] playerUnitSplitString = StringUtils.split(playerUnitString, ",");
					List<Pair<String, String>> unitData = this.getUnitData(playerUnitSplitString);
					
					event = new TeamInfoEvent(team, unitData);
					break;
				}
			}
		}
		
		return event;
	}
	
	private boolean eventDetected(String message) {
		boolean found = false;
		if(StringUtils.contains(message, SEARCH_STRING_POSTFIX) || StringUtils.contains(message, SEARCH_STRING_POSTFIX_2)) {
			for(String prefix: this.prefixes) {
				if(StringUtils.contains(message, prefix)) {
					found = true;
					break;
				}
			}
		}
		return found;
	}
	
	private List<Pair<String, String>> getUnitData(String[] playerUnitSplitString) {
		List<Pair<String, String>> unitData = new ArrayList<>();
		for(String playerUnitString: playerUnitSplitString) {
			String player = StringUtils.substringBefore(playerUnitString, TEAM_VS_UNIT_SPLIT_STRING);
			String unit = StringUtils.substringAfter(playerUnitString, TEAM_VS_UNIT_SPLIT_STRING);
			Pair<String, String> playerUnitPair = new ImmutablePair<>(StringUtils.trim(player), StringUtils.trim(unit));
			unitData.add(playerUnitPair);
		}
		
		return unitData;
	}

}
