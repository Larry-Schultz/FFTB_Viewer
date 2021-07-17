package fft_battleground.botland.personality;

import java.util.Map;

import fft_battleground.botland.personality.model.PersonalityDisplayFields;
import fft_battleground.botland.personality.model.PersonalityMatrixEntry;
import fft_battleground.model.BattleGroundTeam;

public class InversePercentilePersonality extends PersonalityModule {

	public static PersonalityMatrixEntry matrixEntry = new PersonalityMatrixEntry(map -> new StringBuilder("inverse: Picking ")
			.append(map.get(PersonalityDisplayFields.LOSING_TEAM_PROPER)).append(" over ").append(map.get(PersonalityDisplayFields.WINNING_TEAM_PROPER))
			.append(" as they have the lowest score, ").append(map.get(PersonalityDisplayFields.LOSING_SCORE)).append(" vs ").append(map.get(PersonalityDisplayFields.WINNING_SCORE))
			.append(". Percentile: ").append(map.get(PersonalityDisplayFields.PERCENTILE)).append("%.")
			.toString(),
		false,
		new PersonalityDisplayFields[] {PersonalityDisplayFields.LOSING_TEAM_PROPER, PersonalityDisplayFields.WINNING_TEAM_PROPER, PersonalityDisplayFields.LOSING_SCORE,
				PersonalityDisplayFields.WINNING_SCORE, PersonalityDisplayFields.PERCENTILE});
	
	@Override
	protected String personalityString(String botName, Float leftScore, BattleGroundTeam leftTeam, Float rightScore,
			BattleGroundTeam rightTeam, Map<Integer, Integer> percentiles, Integer percentile) {
		Map<PersonalityDisplayFields, String> map = PersonalityDisplayFields.generateFieldData(leftScore, leftTeam, rightScore, rightTeam, percentiles, percentile, matrixEntry.getRequiredFields());
		String response = matrixEntry.getMessage(map);
		
		return response;
	}

}
