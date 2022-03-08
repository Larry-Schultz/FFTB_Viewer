package fft_battleground.botland.personality;

import java.util.Map;

import fft_battleground.botland.personality.model.PersonalityDisplayFields;
import fft_battleground.botland.personality.model.PersonalityMatrixEntry;
import fft_battleground.model.BattleGroundTeam;

public class PokemonMasterPersonality extends PersonalityModule {

	protected static PersonalityMatrixEntry pokemonMasterPersonality = new PersonalityMatrixEntry(map -> new StringBuilder("I choose you ").append(map.get(PersonalityDisplayFields.WINNING_TEAM_PROPER))
			.append(" Team! Score: ").append(map.get(PersonalityDisplayFields.WINNING_TEAM_PROPER)).append(" ").append(map.get(PersonalityDisplayFields.WINNING_SCORE))
			.append(" vs ").append(map.get(PersonalityDisplayFields.LOSING_TEAM_PROPER)).append(" ").append(map.get(PersonalityDisplayFields.LOSING_SCORE)).append(".")
			.toString(),
		false,
		new PersonalityDisplayFields[] {PersonalityDisplayFields.LOSING_TEAM_PROPER, PersonalityDisplayFields.WINNING_TEAM_PROPER, PersonalityDisplayFields.LOSING_SCORE,
				PersonalityDisplayFields.WINNING_SCORE,});
	
	@Override
	protected String personalityString(String botName, Float leftScore, BattleGroundTeam leftTeam, Float rightScore,
			BattleGroundTeam rightTeam, Map<Integer, Integer> percentiles, Integer percentile) {
		Map<PersonalityDisplayFields, String> map = PersonalityDisplayFields.generateFieldData(leftScore, leftTeam, rightScore, rightTeam, percentiles, percentile, pokemonMasterPersonality.getRequiredFields());
		String message = pokemonMasterPersonality.getMessage(map);
		return message;
	}

}
