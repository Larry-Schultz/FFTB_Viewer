package fft_battleground.botland.bot.genetic.model;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeneAttributeContainer implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6855272283492577538L;
	
	private List<GeneAttributePair> attributes;
}
