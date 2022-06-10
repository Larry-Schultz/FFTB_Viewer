package fft_battleground.tournament.classifier;

import java.util.LinkedList;
import java.util.List;

import fft_battleground.tournament.model.Unit;

public abstract class AbstractUnitAttributeClassifier implements UnitAttributeClassifier {

	@Override
	public List<String> getUnitGeneAbilityElements(Unit unit) {
		List<String> elements = new LinkedList<>();
		elements.add(unit.getClassName());
		elements.add(unit.getActionSkill());
		elements.add(unit.getReactionSkill());
		elements.add(unit.getMoveSkill());
		
		String mainHand = this.addItemSuffixIfNecessary(unit.getMainhand(), unit);
		elements.add(mainHand);
		
		String offHand = this.addItemSuffixIfNecessary(unit.getOffhand(), unit);
		elements.add(offHand);

		String head = this.addItemSuffixIfNecessary(unit.getHead(), unit);
		elements.add(head);
		
		String armor = this.addItemSuffixIfNecessary(unit.getArmor(), unit);
		elements.add(armor);
		
		String accessory = this.addItemSuffixIfNecessary(unit.getAccessory(), unit);
		elements.add(accessory);
		
		if(unit.getClassSkills() != null) {
			elements.addAll(unit.getClassSkills());
		}
		if(unit.getExtraSkills() != null) {
			elements.addAll(unit.getExtraSkills());
		}
		
		return elements;
	}
	
	protected abstract String addItemSuffixIfNecessary(final String item, Unit unit);

}
