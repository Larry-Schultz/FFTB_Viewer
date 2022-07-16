package fft_battleground.botland.bot.genetic.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeneAttributePair implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7042664288388975962L;
	
	private String key;
	private double value;
}
