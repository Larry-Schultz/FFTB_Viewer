package fft_battleground.botland.bot.genetic.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper=true)
public class PlayerDataGeneAttributes extends GeneAttributes {
	public static final String BET_WIN_RATIO = "BetWinRatio";
	public static final String FIGHT_WIN_RATIO = "FightWinRatio";
	public static final String MISSING_FIGHT_WIN_RATIO = "MissingFightWinRatio";
	public static final String CURRENT_BALANCE_TO_BET_RATIO = "CurrentBalanceToBetRatio";
	public static final String HUMAN = "Human";
	public static final String ROBOT = "Robot";
	public static final String SUBSCRIBER = "Subscriber";
	
	public Double betWinRatio() {
		return this.attributeNameMap.get(BET_WIN_RATIO);
	}
	
	public Double fightWinRatio() {
		return this.attributeNameMap.get(FIGHT_WIN_RATIO);
	}
	
	public Double missingFightWinRatio() {
		return this.attributeNameMap.get(MISSING_FIGHT_WIN_RATIO);
	}
	
	public Double currentBalanceToBetRatio() {
		return this.attributeNameMap.get(CURRENT_BALANCE_TO_BET_RATIO);
	}
	
	public Double human() {
		return this.attributeNameMap.get(HUMAN);
	}
	
	public Double robot() {
		return this.attributeNameMap.get(ROBOT);
	}
	
	public Double subscriber() {
		return this.attributeNameMap.get(SUBSCRIBER);
	}
}
