package fft_battleground.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

public enum BattleGroundTeam {
	RED(new String[] {"red"}, 0),
	BLUE(new String[] {"blue"}, 1),
	GREEN(new String[] {"green"}, 2),
	YELLOW(new String[] {"yellow"}, 3),
	WHITE(new String[] {"white"}, 4),
	BLACK(new String[] {"black"}, 5),
	PURPLE(new String[] {"purple"}, 6),
	BROWN(new String[] {"brown"}, 7),
	CHAMPION(new String[] {"champ", "champion", "champs", "orange"}, 8),
	LEFT(new String[] {"left", "p1", "t1"}, null),
	RIGHT(new String[] {"right", "p2", "t2"}, null),
	RANDOM(new String[] {"random"}, null), 
	NONE(new String[] {"None"}, -1);
	
	private Set<String> teamNames;
	private Integer teamCode;
	
	BattleGroundTeam(String[] teamNames, Integer teamCode) {
		List<String> teamNamesList = Arrays.asList(teamNames);
		this.teamNames = new HashSet<String>();
		this.teamNames.addAll(teamNamesList);
		
		this.teamCode = teamCode;
	}
	
	public static BattleGroundTeam parse(String teamName) {
		String cleanedTeamName = StringUtils.lowerCase(teamName);
		if(cleanedTeamName != null) {
			for(BattleGroundTeam team: BattleGroundTeam.values()) {
				if(team.teamNames.contains(cleanedTeamName)) {
					return team;
				}
			}
		}
		return null;
		
	}
	
	public static BattleGroundTeam parse(Integer teamCode) {
		for(BattleGroundTeam team : BattleGroundTeam.values()) {
			if(team.getTeamCode() != null && team.getTeamCode() == teamCode) {
				return team;
			}
		}
		
		return null;
	}
	
	public static boolean isBattleGroundTeamname(String teamName) {
		boolean result = false;
		if(teamName != null) {
			for(BattleGroundTeam team: BattleGroundTeam.values()) {
				if(team.teamNames.contains(teamName)) {
					result = true;
				}
			}
		}
		
		return result;
	}
	
	public static String getTeamName(BattleGroundTeam team) {
		if(team != null) {
			return team.getTeamName();
		} else {
			return null;
		}
	}
	
	public Set<String> getTeamNames() {
		return this.teamNames;
	}
	
	public Integer getTeamCode() {
		return this.teamCode;
	}
	
	protected String getTeamName() {
		return (String) this.teamNames.toArray()[0];
	}
	
}
