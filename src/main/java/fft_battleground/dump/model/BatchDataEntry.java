package fft_battleground.dump.model;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchDataEntry {
	private Date lastUpdated;
	private Integer numberOfPlayersAnalyzed;
	private Integer numberOfPlayersUpdated;
}
