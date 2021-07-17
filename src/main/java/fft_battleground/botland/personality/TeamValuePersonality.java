package fft_battleground.botland.personality;

import java.util.Map;

import fft_battleground.botland.personality.model.PersonalityDisplayFields;
import fft_battleground.botland.personality.model.PersonalityMatrixEntry;
import fft_battleground.model.BattleGroundTeam;

public class TeamValuePersonality extends PersonalityModule {

	protected static PersonalityMatrixEntry teamValueEntry = new PersonalityMatrixEntry(map -> new StringBuilder("Picking ").append(map.get(PersonalityDisplayFields.WINNING_TEAM_PROPER))
			.append(" over ").append(map.get(PersonalityDisplayFields.LOSING_TEAM_PROPER)).append(" based on team value: ").append(map.get(PersonalityDisplayFields.WINNING_SCORE)).append("G vs ")
			.append(map.get(PersonalityDisplayFields.LOSING_SCORE)).append("G.")
			.toString(),
		false,
		new PersonalityDisplayFields[] {PersonalityDisplayFields.LOSING_TEAM_PROPER, PersonalityDisplayFields.WINNING_TEAM_PROPER, PersonalityDisplayFields.LOSING_SCORE,
				PersonalityDisplayFields.WINNING_SCORE,});
	
	protected static PersonalityMatrixEntry inverseTeamValueEntry = new PersonalityMatrixEntry(map -> new StringBuilder("Inverse: picking ").append(map.get(PersonalityDisplayFields.LOSING_TEAM_PROPER))
			.append(" over ").append(map.get(PersonalityDisplayFields.WINNING_TEAM_PROPER)).append(" based on lower team value: ").append(map.get(PersonalityDisplayFields.LOSING_SCORE)).append("G vs ")
			.append(map.get(PersonalityDisplayFields.WINNING_SCORE)).append("G.")
			.toString(),
		false,
		new PersonalityDisplayFields[] {PersonalityDisplayFields.LOSING_TEAM_PROPER, PersonalityDisplayFields.WINNING_TEAM_PROPER, PersonalityDisplayFields.LOSING_SCORE,
				PersonalityDisplayFields.WINNING_SCORE,});
	
	private boolean inverse;
	private PersonalityMatrixEntry relevantEntry;
	
	public TeamValuePersonality() {
		this.inverse = false;
		this.relevantEntry = this.determineRelevantField(this.inverse);
	}
	
	public TeamValuePersonality(boolean inverse) {
		this.inverse = inverse;
		this.relevantEntry = this.determineRelevantField(this.inverse);
	}
	
	@Override
	protected String personalityString(String botName, Float leftScore, BattleGroundTeam leftTeam, Float rightScore,
			BattleGroundTeam rightTeam, Map<Integer, Integer> percentiles, Integer percentile) {
		Map<PersonalityDisplayFields, String> displayFieldMap = PersonalityDisplayFields.generateFieldData(leftScore, leftTeam, rightScore, rightTeam, percentiles, percentile, this.relevantEntry.getRequiredFields());
		String response = this.relevantEntry.getMessage(displayFieldMap);
		return response;
	}
	
	protected PersonalityMatrixEntry determineRelevantField(boolean inverse) {
		PersonalityMatrixEntry entry = !inverse ? teamValueEntry : inverseTeamValueEntry;
		return entry;
	}

}
