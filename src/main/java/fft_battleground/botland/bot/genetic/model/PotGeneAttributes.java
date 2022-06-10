package fft_battleground.botland.bot.genetic.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper=true)
public class PotGeneAttributes extends GeneAttributes {
	public static final String BET_COUNT_KEY = "betCount";
	public static final String ODDS_KEY = "odds";
	public static final String POT_AMOUNT_KEY = "potAmount";
	
	public Double betCount() {
		return this.attributeNameMap.get(BET_COUNT_KEY);
	}
	
	public Double odds() {
		return this.attributeNameMap.get(ODDS_KEY);
	}
	
	public Double potAmount() {
		return this.attributeNameMap.get(POT_AMOUNT_KEY);
	}
}
