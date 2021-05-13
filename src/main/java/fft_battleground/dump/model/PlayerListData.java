package fft_battleground.dump.model;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlayerListData implements Comparable<PlayerListData> {
	private String playerName;
	private Date lastUpdated;
	
	@Override
	public int compareTo(PlayerListData o) {
		return this.lastUpdated.compareTo(o.getLastUpdated());
	}
}