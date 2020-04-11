package fft_battleground.bot.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import fft_battleground.bot.model.event.TeamInfoEvent;
import fft_battleground.bot.model.event.UnitInfoEvent;
import lombok.Data;

@Data
public class TeamData {
	private TeamInfoEvent leftTeamData;
	private TeamInfoEvent rightTeamData;
	private Map<UnitDataCommands, UnitInfoEvent> unitDataCommandUnitStringMap;
	
	private UnitDataCommands lastCommandRecomended;
	
	protected Function<UnitDataCommands,UnitInfoEvent> commandToEventLambda = dataCommand -> this.unitDataCommandUnitStringMap.get(dataCommand);
	
	public TeamData() {
		this.unitDataCommandUnitStringMap = this.generateDefaultUnitDataCommandUnitStringMap();
	}
	
	public TeamData(TeamInfoEvent leftTeamData, TeamInfoEvent rightTeamData) {
		this.leftTeamData = leftTeamData;
		this.rightTeamData = rightTeamData;
		this.unitDataCommandUnitStringMap = this.generateDefaultUnitDataCommandUnitStringMap();
	}
	
	protected Map<UnitDataCommands, UnitInfoEvent> generateDefaultUnitDataCommandUnitStringMap() {
		Map<UnitDataCommands, UnitInfoEvent> defaultMap = new HashMap<>();
		for(UnitDataCommands command : UnitDataCommands.values()) {
			defaultMap.put(command, new UnitInfoEvent());
		}
		
		return defaultMap;
	}
	
	
	/**
	 * Returns true if the last event was derived from the most recently recommended unit command
	 * 
	 * @param event
	 * @return
	 */
	public boolean addUnitInfo(UnitInfoEvent event) {
		boolean isRecommendedCommand = false;
		boolean matchFound = false;
		if(this.leftTeamData != null) {
			for(int i = 0; i< this.leftTeamData.getPlayerUnitPairs().size() && !matchFound; i++) {
				if(StringUtils.equals(StringUtils.lowerCase(event.getPlayer()), StringUtils.lowerCase(leftTeamData.getPlayerUnitPairs().get(i).getLeft()))) {
					String unitString = event.getUnitInfoString();
					UnitDataCommands currentCommand = null;
					switch(i) {
						case 0:
							currentCommand = UnitDataCommands.LEFT_UNIT_1;
							break;
						case 1:
							currentCommand = UnitDataCommands.LEFT_UNIT_2;
							break;
						case 2:
							currentCommand = UnitDataCommands.LEFT_UNIT_3;
							break;
						case 3:
							currentCommand = UnitDataCommands.LEFT_UNIT_4;
							break;
					}
					if(currentCommand != null) {
						this.unitDataCommandUnitStringMap.put(currentCommand, event);
						matchFound = true;
						isRecommendedCommand = currentCommand == this.lastCommandRecomended;
					}
				}
			}
		}
		
		if(this.rightTeamData != null) {
			for(int i = 0; i<this.rightTeamData.getPlayerUnitPairs().size() && !matchFound; i++) {
				if(StringUtils.equals(StringUtils.lowerCase(event.getPlayer()), StringUtils.lowerCase(rightTeamData.getPlayerUnitPairs().get(i).getLeft()))) {
					String unitString = event.getUnitInfoString();
					UnitDataCommands currentCommand = null;
					switch(i) {
						case 0:
							currentCommand = UnitDataCommands.RIGHT_UNIT_1;
							break;
						case 1:
							currentCommand = UnitDataCommands.RIGHT_UNIT_2;
							break;
						case 2:
							currentCommand = UnitDataCommands.RIGHT_UNIT_3;
							break;
						case 3:
							currentCommand = UnitDataCommands.RIGHT_UNIT_4;
							break;
					}
					if(currentCommand != null) {
						this.unitDataCommandUnitStringMap.put(currentCommand, event);
						matchFound = true;
						isRecommendedCommand = currentCommand == this.lastCommandRecomended;
					}
				}
			}
		}
		
		return isRecommendedCommand;
	}
	
	public String nextCommandToRun() {
		List<UnitDataCommands> orderedCommands = UnitDataCommands.UnitDataCommandsOrderedByPriority();
		for(UnitDataCommands command : orderedCommands) {
			if(StringUtils.isBlank(this.unitDataCommandUnitStringMap.get(command).getPlayer())) {
				this.lastCommandRecomended = command;
				return command.getCommand();
			}
		}
		
		return null;
	}
	
	public Map<String, String> getLeftTeamPlayerUnitStringMap() {
		Map<String, String> leftTeamUnitStringMap = new HashMap<>();
		for(Pair<String, String> leftTeamPair: this.leftTeamData.getPlayerUnitPairs()) {
			for(UnitDataCommands command: UnitDataCommands.leftUnitDataCommands()) {
				if(StringUtils.equals(StringUtils.lowerCase(leftTeamPair.getLeft()), StringUtils.lowerCase(this.unitDataCommandUnitStringMap.get(command).getPlayer()))) {
					leftTeamUnitStringMap.put(leftTeamPair.getLeft(), this.unitDataCommandUnitStringMap.get(command).getUnitInfoString());
				}
			}
		}
		
		return leftTeamUnitStringMap;
	}
	
	
	
	public List<UnitInfoEvent> getLeftUnitInfoEvents() {
		List<UnitInfoEvent> events = UnitDataCommands.leftUnitDataCommands().stream().map(commandToEventLambda).collect(Collectors.toList());
		return events;
	}
	
	public List<UnitInfoEvent> getRightUnitInfoEvents() {
		List<UnitInfoEvent> events = UnitDataCommands.rightUnitDataCommands().stream().map(commandToEventLambda).collect(Collectors.toList());
		return events;
	}
	
	public Map<String, String> getRightTeamPlayerUnitStringMap() {
		Map<String, String> rightTeamUnitStringMap = new HashMap<>();
		for(Pair<String, String> rightTeamPair: this.rightTeamData.getPlayerUnitPairs()) {
			for(UnitDataCommands command: UnitDataCommands.rightUnitDataCommands()) {
				if(StringUtils.equals(StringUtils.lowerCase(rightTeamPair.getLeft()), StringUtils.lowerCase(this.unitDataCommandUnitStringMap.get(command).getPlayer()))) {
					rightTeamUnitStringMap.put(rightTeamPair.getLeft(), this.unitDataCommandUnitStringMap.get(command).getUnitInfoString());
				}
			}
		}
		
		return rightTeamUnitStringMap;
	}
}

enum UnitDataCommands {
	LEFT_UNIT_1("!unit left 1", 8),
	LEFT_UNIT_2("!unit left 2", 6),
	LEFT_UNIT_3("!unit left 3", 4),
	LEFT_UNIT_4("!unit left 4", 2),
	RIGHT_UNIT_1("!unit right 1", 7),
	RIGHT_UNIT_2("!unit right 2", 5),
	RIGHT_UNIT_3("!unit right 3", 3),
	RIGHT_UNIT_4("!unit right 4", 1);
	
	private String command;
	private Integer order;
	
	private UnitDataCommands(String command, Integer order) {
		this.command = command;
		this.order = order;
	}
	
	public String getCommand() {
		return this.command;
	}
	
	public Integer getOrder() {
		return this.order;
	}
	
	public static List<UnitDataCommands> UnitDataCommandsOrderedByPriority() {
		List<UnitDataCommands> orderedCommands = Arrays.asList(UnitDataCommands.values());
		Collections.sort(orderedCommands, new Comparator<UnitDataCommands>() {

			@Override
			public int compare(UnitDataCommands arg0, UnitDataCommands arg1) {
				return arg0.getOrder().compareTo(arg1.getOrder());
			}
			
		});
		
		return orderedCommands;
	}
	
	public static List<UnitDataCommands> leftUnitDataCommands() {
		List<UnitDataCommands> leftCommands = Arrays.asList(new UnitDataCommands[] {LEFT_UNIT_1, LEFT_UNIT_2, LEFT_UNIT_3, LEFT_UNIT_4});
		return leftCommands;
	}
	
	public static List<UnitDataCommands> rightUnitDataCommands() {
		List<UnitDataCommands> rightCommands = Arrays.asList(new UnitDataCommands[] {RIGHT_UNIT_1, RIGHT_UNIT_2, RIGHT_UNIT_3, RIGHT_UNIT_4});
		return rightCommands;
	}
}