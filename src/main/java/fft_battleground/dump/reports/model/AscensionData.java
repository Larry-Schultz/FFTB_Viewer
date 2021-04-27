package fft_battleground.dump.reports.model;

import java.util.List;

import fft_battleground.dump.model.PrestigeTableEntry;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AscensionData {
	private List<PrestigeTableEntry> prestigeData;
	private String generationDateString;
	
	public AscensionData(List<PrestigeTableEntry> prestigeData) {
		this.prestigeData = prestigeData;
	}
}
