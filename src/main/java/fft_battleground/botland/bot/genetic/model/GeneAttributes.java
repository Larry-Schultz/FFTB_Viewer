package fft_battleground.botland.bot.genetic.model;

import java.io.Serializable;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;

import fft_battleground.exception.BotConfigException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeneAttributes implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1539905136661247962L;

	protected GeneAttributeContainer geneAttributes;
	
	@JsonIgnore
	protected transient Map<String, Double> attributeNameMap;
	
	public void init() throws BotConfigException {
		this.attributeNameMap = this.buildAttributeNameMap();
	}
	
	public Double getGene(String attributeName) {
		return this.attributeNameMap.get(attributeName);
	}
	
	protected Map<String, Double> buildAttributeNameMap() {
		return this.geneAttributes.getAttributes().stream()
				.collect(Collectors.toMap(GeneAttributePair::getKey, GeneAttributePair::getValue));
	}
}
