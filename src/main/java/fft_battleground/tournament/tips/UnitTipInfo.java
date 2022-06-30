package fft_battleground.tournament.tips;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import fft_battleground.exception.MissingTipException;
import fft_battleground.model.Gender;
import fft_battleground.tournament.model.Unit;
import fft_battleground.util.GenericPairing;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@NoArgsConstructor
@Slf4j
public class UnitTipInfo {
	private String classTip;
	private String signTip;
	private String actionSkillTip;
	private String reactionSkillTip;
	private String moveSkillTip;
	private String mainhandTip;
	private String offhandTip;
	private String headTip;
	private String armorTip;
	private String accessoryTip;
	private List<GenericPairing<String, String>> skillTips;
	
	public UnitTipInfo(Unit unit, Tips tips) throws MissingTipException {
		if(StringUtils.isNotEmpty(unit.getClassName())) {
			if(unit.getGender() == Gender.MALE || unit.getGender() == Gender.FEMALE) {
				String className = StringUtils.replace(unit.getClassName(), " ", "");
				this.classTip = this.readTipMap(tips.getClassMap(), className + " " + unit.getGender().toCapitalizedString());
			} else {
				String searchString = StringUtils.replace(unit.getClassName(), " ", "");
				this.classTip = this.readTipMap(tips.getClassMap(), searchString);
			}
		}
		if(StringUtils.isNotBlank(unit.getSign())) {
			this.signTip = this.readTipMap(tips.getZodiac(), unit.getSign()); 
		}
		if(StringUtils.isNotBlank(unit.getActionSkill())) {
			this.actionSkillTip = this.readTipMap(tips.getAbility(), unit.getActionSkill());
		}
		if(StringUtils.isNotBlank(unit.getReactionSkill())) {
			this.reactionSkillTip = this.readTipMap(tips.getAbility(),unit.getReactionSkill());
		}
		if(StringUtils.isNotBlank(unit.getMoveSkill())) {
			this.moveSkillTip = this.readTipMap(tips.getAbility(),unit.getMoveSkill());
		}
		if(StringUtils.isNotBlank(unit.getMainhand())) {
			this.mainhandTip = this.readTipMap(tips.getItem(),unit.getMainhand());
		}
		if(StringUtils.isNotBlank(unit.getOffhand())) {
			this.offhandTip = this.readTipMap(tips.getItem(),unit.getOffhand());
		}
		if(StringUtils.isNotBlank(unit.getHead())) {
			this.headTip = this.readTipMap(tips.getItem(),unit.getHead());
		}
		if(StringUtils.isNotBlank(unit.getArmor())) {
			this.armorTip = this.readTipMap(tips.getItem(),unit.getArmor());
		}
		if(StringUtils.isNotBlank(unit.getAccessory())) {
			this.accessoryTip = this.readTipMap(tips.getItem(),unit.getAccessory());
		}
		
		Map<String, String> skillTips = new HashMap<>();
		Set<String> skillsSet = Stream.of(unit.getClassSkills(), unit.getExtraSkills()).flatMap(List<String>::stream)
				.collect(Collectors.toSet());
		for(String skill: skillsSet) {
			if(StringUtils.isNotBlank(skill)) {
				try {
					skillTips.put(skill, this.readTipMap(tips.getAbility(), skill));
				} catch(MissingTipException e) {
					log.error("Missing tip data for ability");
				}
			}
		}
		
		this.skillTips = GenericPairing.convertMapToGenericPairList(skillTips);
		
	}
	
	private String readTipMap(Map<String, String> map, String searchString) throws MissingTipException {
		String result = map.get(searchString);
		if(StringUtils.isEmpty(result)) {
			throw new MissingTipException("Missing tip data for " + searchString);
		}
		
		return result;
	}
}
