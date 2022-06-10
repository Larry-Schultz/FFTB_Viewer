package fft_battleground.botland.bot.genetic.model;

import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper=true)
public class MissingGeneAttributes extends GeneAttributes {
	
	@Override
	public Double getGene(String attributeName) {
		return super.getGene(StringUtils.lowerCase(attributeName));
	}
	
	public boolean containsGene(String attributeName) {
		return super.attributeNameMap.containsKey(StringUtils.lowerCase(attributeName));
	}
	
	@Override
	protected Map<String, Double> buildAttributeNameMap() {
		return this.geneAttributes.getAttributes().stream()
				.collect(Collectors.toMap(this::createLowercaseKey, GeneAttributePair::getValue));
	}
	
	protected String createLowercaseKey(GeneAttributePair geneAttributePair) {
		return StringUtils.lowerCase(geneAttributePair.getKey());
	}
}
