package fft_battleground.tournament.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import fft_battleground.event.model.BattleGroundEvent;
import fft_battleground.event.model.MatchInfoEvent;
import fft_battleground.event.model.SkillDropEvent;
import fft_battleground.event.model.TeamInfoEvent;
import fft_battleground.event.model.UnitInfoEvent;
import fft_battleground.model.BattleGroundTeam;
import fft_battleground.util.GambleUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@JsonIgnoreProperties(ignoreUnknown = true)
public class Tournament {
	@JsonProperty("Type")
	private String Type;
	@JsonProperty("ID")
	private Long ID;
	@JsonProperty("LastMod")
	private String LastMod;
	@JsonProperty("Teams")
	private Teams Teams;
	@JsonProperty("Maps")
	private List<String> Maps;
	@JsonProperty("Winners")
	private List<String> Winners;
	@JsonProperty("SkillDrop")
	private String SkillDrop;
	@JsonProperty("Entrants")
	private List<String> Entrants;
	@JsonProperty("Snubs")
	private List<String> Snubs;
	
	private Integer winnersCount;
	private List<String> raidbosses;
	
	public Tournament() {}
	
	public List<BattleGroundEvent> getEventsFromTournament(BattleGroundTeam team1, BattleGroundTeam team2) {
		List<BattleGroundEvent> events = new ArrayList<>();
		
		SkillDropEvent skilldrop = new SkillDropEvent(this.SkillDrop);
		events.add(skilldrop);
		TeamInfoEvent team1Info = this.Teams.getTeamInfoEventByBattleGroundTeam(team1);
		events.add(team1Info);
		TeamInfoEvent team2Info = this.Teams.getTeamInfoEventByBattleGroundTeam(team2);
		events.add(team2Info);
		
		List<UnitInfoEvent> unitInfoEventsForTeam1 = this.Teams.getUnitInfoEventByBattleGroundTeam(team1);
		this.attachRaidbossDataToUnitInfoEventList(unitInfoEventsForTeam1);
		events.addAll(unitInfoEventsForTeam1);
		List<UnitInfoEvent> unitInfoEventsForTeam2 = this.Teams.getUnitInfoEventByBattleGroundTeam(team2);
		this.attachRaidbossDataToUnitInfoEventList(unitInfoEventsForTeam2);
		events.addAll(unitInfoEventsForTeam2);
		
		MatchInfoEvent matchInfoEvent = this.getMatchInfo(team1, team2);
		if(matchInfoEvent != null) {
			events.add(matchInfoEvent);
		}
		
		return events;
	}
	
	public MatchInfoEvent getMatchInfo(BattleGroundTeam team1, BattleGroundTeam team2) {
		MatchInfoEvent event = null;
		if(this.winnersCount == null) {
			this.winnersCount = Winners.size();
		}
		
		if(this.winnersCount <=7) { //greater than 7 seems to imply we are in fight mode right now.
			String map = this.Maps.get(winnersCount);
			String mapNumber = StringUtils.substringBefore(map, ")");
			Integer mapNumberInt = Integer.valueOf(mapNumber);
			String mapName = StringUtils.substringAfter(map, ") ");
			Pair<BattleGroundTeam, BattleGroundTeam> teamPair = new ImmutablePair<BattleGroundTeam, BattleGroundTeam>(team1, team2);
			Pair<Integer, String> mapData = new ImmutablePair<Integer, String>(mapNumberInt, mapName);
			event = new MatchInfoEvent(teamPair, mapData);
		} else {
			log.error("The winnersCount was greater than 7, somehow");
		}
		return event;
	}
	
	public void attachRaidbossDataToUnitInfoEventList(List<UnitInfoEvent> unitInfoEvents) {
		for(UnitInfoEvent event : unitInfoEvents) {
			if(this.raidbosses.contains(GambleUtil.cleanString(event.getPlayer()))) {
				event.setIsRaidBoss(true);
			}
		}
	}
}
