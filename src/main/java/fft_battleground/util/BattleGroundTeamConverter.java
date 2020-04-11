package fft_battleground.util;

import javax.persistence.AttributeConverter;

import org.apache.commons.lang3.StringUtils;

import fft_battleground.model.BattleGroundTeam;

public class BattleGroundTeamConverter implements AttributeConverter<BattleGroundTeam, String> {

	@Override
	public String convertToDatabaseColumn(BattleGroundTeam attribute) {
		String name =  BattleGroundTeam.getTeamName(attribute);
		return name;
	}

	@Override
	public BattleGroundTeam convertToEntityAttribute(String dbData) {
		BattleGroundTeam team = BattleGroundTeam.parse(StringUtils.lowerCase(dbData));
		return team;
	}

}
