package fft_battleground.dump.model;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FolderListData implements Comparable<FolderListData> {
	private String entityName;
	private Date lastUpdated;
	
	@Override
	public int compareTo(FolderListData o) {
		return this.lastUpdated.compareTo(o.getLastUpdated());
	}
}