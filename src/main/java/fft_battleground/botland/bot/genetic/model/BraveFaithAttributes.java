package fft_battleground.botland.bot.genetic.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper=true)
public class BraveFaithAttributes extends GeneAttributes {
	public static final String BRAVE_KEY = "Brave";
	public static final String FAITH_KEY = "Faith";
	public static final String BRAVEFAITH_KEY = "BraveFaith";
	public static final String RAIDBOSS_KEY = "RaidBoss";
	
	public Double brave() {
		return this.attributeNameMap.get(BRAVE_KEY);
	}
	
	public Double faith() {
		return this.attributeNameMap.get(FAITH_KEY);
	}
	
	public Double braveFaith() {
		return this.attributeNameMap.get(BRAVEFAITH_KEY);
	}
	
	public Double raidBoss() {
		return this.attributeNameMap.get(RAIDBOSS_KEY);
	}
}
