package fft_battleground.botland.personality.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import fft_battleground.model.BattleGroundTeam;

public enum PersonalityDisplayFields {
	WINNING_TEAM_PROPER,
	WINNING_TEAM_INFORMAL,
	LOSING_TEAM_PROPER,
	LOSING_TEAM_INFORMAL,
	LEFT_TEAM_PROPER,
	RIGHT_TEAM_PROPER,
	WINNING_SCORE,
	LOSING_SCORE,
	DIFFERENCE,
	PERCENTILE;
	
	PersonalityDisplayFields() {}
	
	public static Map<PersonalityDisplayFields, String> generateFieldData(Float leftScore, BattleGroundTeam leftTeam, Float rightScore,
			BattleGroundTeam rightTeam, Map<Integer, Integer> percentiles, Integer percentile, List<PersonalityDisplayFields> requiredFields) {
		BattleGroundTeam winningSide = PersonalityDisplayFields.findWinningSide(leftScore, rightScore);
		Map<PersonalityDisplayFields, String> fieldData = new HashMap<>();
		Integer leftScoreRounded = (int) leftScore.floatValue();
		Integer rightScoreRounded = (int) rightScore.floatValue();
		
		if(requiredFields.contains(PersonalityDisplayFields.DIFFERENCE)) {
			Integer scoreDifference = (int) Math.abs(leftScore - rightScore);
			fieldData.put(PersonalityDisplayFields.DIFFERENCE, scoreDifference.toString());
		}
		if(requiredFields.contains(PersonalityDisplayFields.PERCENTILE)) {
			String percentileString = percentile != null ? percentile.toString() : "__";
			fieldData.put(PersonalityDisplayFields.PERCENTILE, percentileString);
		}
		if(requiredFields.contains(PersonalityDisplayFields.LOSING_SCORE)) {
			if(winningSide == BattleGroundTeam.LEFT) {
				fieldData.put(PersonalityDisplayFields.LOSING_SCORE, rightScoreRounded.toString());
			} else {
				fieldData.put(PersonalityDisplayFields.LOSING_SCORE, leftScoreRounded.toString());
			}
		}
		if(requiredFields.contains(PersonalityDisplayFields.WINNING_SCORE)) {
			if(winningSide == BattleGroundTeam.LEFT) {
				fieldData.put(PersonalityDisplayFields.WINNING_SCORE, leftScoreRounded.toString());
			} else {
				fieldData.put(PersonalityDisplayFields.WINNING_SCORE, rightScoreRounded.toString());
			}
		}
		if(requiredFields.contains(PersonalityDisplayFields.LOSING_TEAM_PROPER)) {
			if(winningSide == BattleGroundTeam.LEFT) {
				fieldData.put(PersonalityDisplayFields.LOSING_TEAM_PROPER, StringUtils.capitalize(rightTeam.getProperName()));
			} else {
				fieldData.put(PersonalityDisplayFields.LOSING_TEAM_PROPER, StringUtils.capitalize(leftTeam.getProperName()));
			}
		}
		if(requiredFields.contains(PersonalityDisplayFields.LOSING_TEAM_INFORMAL)) {
			if(winningSide == BattleGroundTeam.LEFT) {
				fieldData.put(PersonalityDisplayFields.LOSING_TEAM_INFORMAL, StringUtils.capitalize(rightTeam.getInformalName()));
			} else {
				fieldData.put(PersonalityDisplayFields.LOSING_TEAM_INFORMAL, StringUtils.capitalize(leftTeam.getInformalName()));
			}
		}
		
		if(requiredFields.contains(PersonalityDisplayFields.WINNING_TEAM_PROPER)) {
			if(winningSide == BattleGroundTeam.LEFT) {
				fieldData.put(PersonalityDisplayFields.WINNING_TEAM_PROPER, StringUtils.capitalize(leftTeam.getProperName()));
			} else {
				fieldData.put(PersonalityDisplayFields.WINNING_TEAM_PROPER, StringUtils.capitalize(rightTeam.getProperName()));
			}
		}
		if(requiredFields.contains(PersonalityDisplayFields.WINNING_TEAM_INFORMAL)) {
			if(winningSide == BattleGroundTeam.LEFT) {
				fieldData.put(PersonalityDisplayFields.WINNING_TEAM_INFORMAL, StringUtils.capitalize(leftTeam.getInformalName()));
			} else {
				fieldData.put(PersonalityDisplayFields.WINNING_TEAM_INFORMAL, StringUtils.capitalize(rightTeam.getInformalName()));
			}	
		}
		
		if(requiredFields.contains(PersonalityDisplayFields.LEFT_TEAM_PROPER)) {
			fieldData.put(PersonalityDisplayFields.LEFT_TEAM_PROPER, StringUtils.capitalize(leftTeam.getProperName()));
		}
		if(requiredFields.contains(PersonalityDisplayFields.RIGHT_TEAM_PROPER)) {
			fieldData.put(PersonalityDisplayFields.RIGHT_TEAM_PROPER, StringUtils.capitalize(rightTeam.getProperName()));
		}
		
		return fieldData;
	}
	
	protected static BattleGroundTeam findWinningSide(Float leftScore, Float rightScore) {
		if(leftScore >= rightScore) {
			return BattleGroundTeam.LEFT;
		} else {
			return BattleGroundTeam.RIGHT;
		}
	}
}