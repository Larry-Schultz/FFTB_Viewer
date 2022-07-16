package fft_battleground.botland.bot.genetic.model;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import fft_battleground.exception.BotConfigException;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BotGenome implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8669765184330928045L;
	
	private MapGeneAttributes mapGeneAttributes;
	private BraveFaithAttributes braveFaithAttributes;
	private PotGeneAttributes potGeneAttributes;
	private PlayerDataGeneAttributes playerDataGeneAttributes;
	private SideGeneAttributes sideGeneAttributes;
	private BetGeneAttributes betGeneAttributes;
	private MissingGeneAttributes missingGeneAttributes;
	
	public void init() throws BotConfigException {
		List<GeneAttributes> geneAttributes = List.of(this.mapGeneAttributes, this.braveFaithAttributes, 
			this.potGeneAttributes, this.playerDataGeneAttributes, this.sideGeneAttributes, this.betGeneAttributes, 
			this.missingGeneAttributes);
		for(GeneAttributes geneAttribute : geneAttributes) {
			geneAttribute.init();
		}
		
	}
}
