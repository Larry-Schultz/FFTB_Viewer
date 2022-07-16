package fft_battleground.botland.bot.genetic.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Percentile implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4871130752558654751L;
	
	private int key;
	private double value;
	
	public int getRoundedPercentileValue() {
		int roundedValue = BigDecimal.valueOf(value).setScale(0, RoundingMode.HALF_UP).intValue();
		return roundedValue;
	}
}
