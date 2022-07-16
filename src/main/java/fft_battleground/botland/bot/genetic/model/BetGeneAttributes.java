package fft_battleground.botland.bot.genetic.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper=true)
public class BetGeneAttributes 
extends GeneAttributes
implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5502967701083946211L;
	
	@JsonIgnore
	private transient Map<Integer, Double> betGeneMap;
	
	@Override
	public void init() {
		this.betGeneMap = this.geneAttributes.getAttributes().stream()
				.collect(Collectors.toMap(this::parseKey, GeneAttributePair::getValue));
	}
	
	protected Integer parseKey(GeneAttributePair geneAttributePair) {
		String[] keySplit = StringUtils.split(geneAttributePair.getKey(), "-");
		int key = Integer.valueOf(keySplit[0]);
		return key;
	}
	
	public Integer getValue(int roundedRatio) {
		double geneValue = this.betGeneMap.get(roundedRatio);
		
		int roundedResult = BigDecimal.valueOf(geneValue).setScale(0, RoundingMode.HALF_UP).intValue();
		return roundedResult;
	}
}
