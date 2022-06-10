package fft_battleground.botland.personality;

import java.util.Map;

import fft_battleground.botland.personality.model.PersonalityDisplayFields;
import fft_battleground.botland.personality.model.PersonalityMatrixEntry;
import fft_battleground.model.BattleGroundTeam;

public class BirbBrainPersonality extends PersonalityModule {

	protected static PersonalityMatrixEntry kwehPersonality = new PersonalityMatrixEntry(map -> new StringBuilder("KWEH my bet is !bet ").append(map.get(PersonalityDisplayFields.WINNING_TEAM_PROPER))
			.append(" ").append(map.get(PersonalityDisplayFields.WINNING_SCORE)).toString(),
		false,
		new PersonalityDisplayFields[] {PersonalityDisplayFields.LOSING_TEAM_PROPER, PersonalityDisplayFields.WINNING_TEAM_PROPER, PersonalityDisplayFields.LOSING_SCORE,
				PersonalityDisplayFields.WINNING_SCORE,});
	
	protected static PersonalityMatrixEntry inverseKwehPersonality = new PersonalityMatrixEntry(map -> new StringBuilder("My bet is always opposite of birbbrainsbot. my current bet is !bet ")
			.append(map.get(PersonalityDisplayFields.WINNING_TEAM_PROPER)).append(" ").append(map.get(PersonalityDisplayFields.WINNING_SCORE)).toString(),
		false,
		new PersonalityDisplayFields[] {PersonalityDisplayFields.WINNING_TEAM_PROPER, PersonalityDisplayFields.LOSING_SCORE,});
	
	private PersonalityMatrixEntry entry;
	
	public BirbBrainPersonality() {
		this.entry = kwehPersonality;
	}
	
	public BirbBrainPersonality(boolean inverse) {
		if(inverse) {
			entry = inverseKwehPersonality;
		} else {
			entry = kwehPersonality;
		}
	}
	
	@Override
	protected String personalityString(String botName, Float leftScore, BattleGroundTeam leftTeam, Float rightScore,
			BattleGroundTeam rightTeam, Map<Integer, Integer> percentiles, Integer percentile) {
		Map<PersonalityDisplayFields, String> map = PersonalityDisplayFields.generateFieldData(leftScore, leftTeam, rightScore, rightTeam, percentiles, percentile, this.entry.getRequiredFields());
		String message = this.entry.getMessage(map);
		return message;
	}

}
