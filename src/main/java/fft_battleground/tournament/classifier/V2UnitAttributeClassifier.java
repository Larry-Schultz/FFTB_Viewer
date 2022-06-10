package fft_battleground.tournament.classifier;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import fft_battleground.tournament.model.Unit;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class V2UnitAttributeClassifier extends AbstractUnitAttributeClassifier {
	public static final List<String> itemsToPrefix = Arrays.asList(new String[] {"Bracer", "Elixir", "Kiyomori", "Soft", "X-Potion", "Remedy", "Muramasa",
			"Maiden's Kiss", "Masamune", "Murasame", "Eye Drop", "Shuriken", "Antidote", "Hi-Potion", "Heaven's Cloud", "Bizen Boat",
			"Hi-Ether", "Chirijiraden", "Kikuichimoji", "Potion", "Holy Water", "Spear", "Echo Grass", "Phoenix Down", "Ether"});
	public static final String itemSuffix = "-Item";
	
	@Override
	public List<String> getUnitGeneAbilityElements(Unit unit) {
		List<String> unitAttributes = super.getUnitGeneAbilityElements(unit);
		List<String> filteredUnitAttributes = unitAttributes.stream().filter(StringUtils::isNotBlank).collect(Collectors.toList());
		return filteredUnitAttributes;
	}
	
	protected String addItemSuffixIfNecessary(final String item, Unit unit) {
		String result = item;
		if(itemsToPrefix.contains(item)) {
			result = item + itemSuffix; //unfortunate bug with the V1 bot.  This is what the bot was trained with
		}
		
		return result;
	}

}
