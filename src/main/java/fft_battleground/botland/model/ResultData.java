package fft_battleground.botland.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import fft_battleground.util.GenericPairing;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ResultData {
	private boolean abilityEnabled;
	private boolean itemEnabled;
	private boolean userSkillsEnabled;
	private boolean classEnabled;
	private boolean mapsEnabled;
	private boolean braveFaithEnabled;
	private boolean sideEnabled;
	private int numberOfAnalyzedTournaments;
	private int currentResultSuccessPercentage;
	private double threshold;
	private int population;
	private int maxGene;
	private int minGene;
	private int maxBraveFaithGene;
	private int minBraveFaithGene;
	private List<GenericPairing<String, Integer>> geneticAttributes;
	
	public ResultData(List<Integer> geneAttributes, List<String> attributeNames, int currentResultSuccessPercentage, boolean ABILITY_ENABLED, boolean ITEM_ENABLED,
	boolean USER_SKILLS_ENABLED, boolean CLASS_ENABLED, boolean MAPS_ENABLED, boolean BRAVE_FAITH_ENABLED,
	boolean SIDE_ENABLED, int numberOfAnalyzedTournaments, double threshold, int population, int maxGene, int minGene, 
	int maxBraveFaithGene, int minBraveFaithGene) {
		this.geneticAttributes = new ArrayList<>();
		for(int i = 0; i < geneAttributes.size() && i < attributeNames.size(); i++) {
			GenericPairing<String, Integer> genericPairing = new GenericPairing<>(attributeNames.get(i), geneAttributes.get(i));
			this.geneticAttributes.add(genericPairing);
		}
		
		this.currentResultSuccessPercentage = currentResultSuccessPercentage;
		this.abilityEnabled = ABILITY_ENABLED;
		this.itemEnabled = ITEM_ENABLED;
		this.userSkillsEnabled = USER_SKILLS_ENABLED;
		this.classEnabled = CLASS_ENABLED;
		this.mapsEnabled = MAPS_ENABLED;
		this.braveFaithEnabled = BRAVE_FAITH_ENABLED;
		this.sideEnabled = SIDE_ENABLED;
		this.numberOfAnalyzedTournaments = numberOfAnalyzedTournaments;
		this.threshold = threshold;
		this.population = population;
		this.maxGene = maxGene;
		this.minGene = minGene;
		this.maxBraveFaithGene = maxBraveFaithGene;
		this.minBraveFaithGene = minBraveFaithGene;
	}
	
	public void sortGeneticAttributesByAbsoluteValue() {
		Comparator<GenericPairing<String, Integer>> comparator = new Comparator<GenericPairing<String, Integer>>() {
			@Override
			public int compare(GenericPairing<String, Integer> o1, GenericPairing<String, Integer> o2) {
				Integer o1AbsoluteValue = Math.abs(o1.getValue());
				Integer o2AbsoluteValue = Math.abs(o2.getValue());
				return o1AbsoluteValue.compareTo(o2AbsoluteValue);
			}
			
		};
		Collections.sort(this.geneticAttributes, comparator.reversed());
	}
}
