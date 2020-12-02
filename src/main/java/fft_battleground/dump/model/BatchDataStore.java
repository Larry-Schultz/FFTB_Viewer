package fft_battleground.dump.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchDataStore {
	private BatchDataEntry userSkillData;
	private BatchDataEntry allegianceData;
	private BatchDataEntry portraitData;
}
