package fft_battleground.botland.bot.genetic;

import java.util.Date;

import fft_battleground.botland.bot.genetic.model.ResultData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeneFile<T> implements Comparable<GeneFile<T>> {
	private String filename;
	private Date fileDate;
	private T data;
	
	@Override
	public int compareTo(GeneFile o) {
		return this.getFileDate().compareTo(o.getFileDate());
	}
}