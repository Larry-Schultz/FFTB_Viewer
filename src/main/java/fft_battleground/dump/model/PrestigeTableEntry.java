package fft_battleground.dump.model;

import lombok.Data;

@Data
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
		if(this.prestigeLevel.compareTo(arg0.getPrestigeLevel()) == 0) {
			return this.name.compareTo(arg0.getName());
		} else {
			//flip the ordering
			if(this.prestigeLevel.compareTo(arg0.getPrestigeLevel()) < 0) {
				return 1;
			} else {
				return -1;
			}
		}
	}
}
