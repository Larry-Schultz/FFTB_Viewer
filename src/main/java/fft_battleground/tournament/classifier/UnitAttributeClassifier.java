package fft_battleground.tournament.classifier;

import java.util.List;

import fft_battleground.tournament.model.Unit;

public interface UnitAttributeClassifier {
	public List<String> getUnitGeneAbilityElements(Unit unit);
}
