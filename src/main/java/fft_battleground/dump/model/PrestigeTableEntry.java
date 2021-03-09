package fft_battleground.dump.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class PrestigeTableEntry implements Comparable<PrestigeTableEntry> {
	public String name;
	public Integer prestigeLevel;
	public String lastActive;
	
	public PrestigeTableEntry() {}
	
	public PrestigeTableEntry(String player, int size) {
		this.name = player;
		this.prestigeLevel = size;
	}

	@Override
	public int compareTo(PrestigeTableEntry arg0) {
		int result = 0;
		
		if(this.prestigeLevel.compareTo(arg0.getPrestigeLevel()) == 0) {
			//if the levels are tied, next compare by lastActiveDate
			Integer compareLastActiveResult = this.compareLastActive(arg0);
			if(compareLastActiveResult == null || compareLastActiveResult == 0) { 
				result = this.comparePlayerName(arg0);
			} else {
				//if lastActive comparison fails or they are tied, next sort by name
				result = this.reverseResult(compareLastActiveResult.intValue());
			}
		} else {
			result = this.reverseResult(this.prestigeLevel.compareTo(arg0.getPrestigeLevel()));
		}
		
		return result;
	}
	
	private Integer compareLastActive(PrestigeTableEntry arg0) {
		Integer result = null;
		SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy");
		Date thisLastActive = null;
		Date otherTableEntryLastActive = null;
		try {
			thisLastActive = this.lastActive != null ? format.parse(this.lastActive) : null;
			otherTableEntryLastActive = arg0.getLastActive() != null ? format.parse(arg0.getLastActive()) : null;
		} catch (ParseException e) {
			log.error("Error parsing strings for prestige table entry sort", e);
			//if an exception happens, we log it and the logic that catches nulls should continue the passable behavior
		}
		
		if(thisLastActive == null || otherTableEntryLastActive == null) {
			result = null;
		} else {
			result = thisLastActive.compareTo(otherTableEntryLastActive);
		}
		
		return result;
	}
	
	
	private int comparePlayerName(PrestigeTableEntry arg0) {
		int result = this.name.compareTo(arg0.getName());
		return result;
	}
	
	private int reverseResult(final int value) {
		int newResult = 0;
		if(value == 0) {
			newResult = 0;
		} else if(value == 1) {
			newResult = -1;
		} else if(value == -1) {
			newResult = 1;
		} else {
			newResult = value;
		}
		
		return newResult;
	}
}
